/*
 * CALL 'MVSPSA' USING PSAPTR: points PSAPTR at a fake z/OS PSA -> TCB -> TIOT chain,
 * so CBSTM03A's control-block walk runs off-mainframe.
 *
 *   MVS_JOB / MVS_STEP   TIOTNJOB / TIOTJSTP
 *   MVS_DDS              comma-separated DD names; "NAME:NULL" gives a null UCB
 *
 * CBSTM03A walks the TIOT by adding to BUMP-TIOT, a big-endian PIC S9(8) BINARY
 * that redefines the first 4 bytes of the 8-byte (little-endian) TIOT-INDEX pointer.
 * Adding N to it therefore adds N << 24 to the address, so the header and every
 * 20-byte entry live on their own page at TIOT_BASE + (offset << 24). Each entry
 * page also carries the first word of the next entry after its 20 bytes, which is
 * where CBSTM03A tests END-OF-TIOT, as it would in a contiguous TIOT.
 */
#define _GNU_SOURCE
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>

#define TIOT_BASE  0x100000000000UL
#define HEADER_LEN 24
#define ENTRY_LEN  20
#define MAX_DDS    9

static unsigned char *page_at(unsigned long offset)
{
    void *want = (void *)(TIOT_BASE + (offset << 24));
    void *got = mmap(want, 4096, PROT_READ | PROT_WRITE,
                     MAP_PRIVATE | MAP_ANONYMOUS | MAP_FIXED_NOREPLACE, -1, 0);
    return got == want ? got : NULL;
}

static void put(unsigned char *dst, const char *src, size_t len)
{
    size_t n = strlen(src);
    memset(dst, ' ', len);
    memcpy(dst, src, n < len ? n : len);
}

int MVSPSA(void **psaptr)
{
    static unsigned char psa[544], tcb[20];
    static unsigned char *tiot;
    if (!tiot) {
        if (!(tiot = page_at(0))) return 8;
        put(tiot, getenv("MVS_JOB") ? getenv("MVS_JOB") : "CREASTMT", 8);
        put(tiot + 8, getenv("MVS_STEP") ? getenv("MVS_STEP") : "STEP040", 8);
        put(tiot + 16, "", 8);
        char *dds = strdup(getenv("MVS_DDS") ? getenv("MVS_DDS") : ""), *save = NULL;
        unsigned long offset = HEADER_LEN;
        unsigned char *prev = NULL;
        int n = 0;
        for (char *dd = strtok_r(dds, ",", &save); dd && n < MAX_DDS;
             dd = strtok_r(NULL, ",", &save), offset += ENTRY_LEN, n++) {
            unsigned char *entry = page_at(offset);
            if (!entry) return 8;
            char *colon = strchr(dd, ':');
            if (colon) *colon = '\0';
            entry[0] = ENTRY_LEN;                           /* TIOELNGH */
            if (prev) memcpy(prev + ENTRY_LEN, entry, 4);
            prev = entry;
            put(entry + 4, dd, 8);                          /* TIOEDDNM */
            if (!colon) memcpy(entry + 17, "\x00\x1f\x40", 3); /* TIOEFSRT (UCB) */
        }
        free(dds);
        if (!page_at(offset)) return 8;                     /* zero entry ends the TIOT */
        void *tiot_ptr = tiot, *tcb_ptr = tcb;
        memcpy(tcb + 12, &tiot_ptr, sizeof tiot_ptr);       /* TCBTIO */
        memcpy(psa + 536, &tcb_ptr, sizeof tcb_ptr);        /* PSATOLD */
    }
    *psaptr = psa;
    return 0;
}
