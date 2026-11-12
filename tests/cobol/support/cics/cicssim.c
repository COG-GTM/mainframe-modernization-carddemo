/*
 * cicsrun: a tiny CICS stand-in for running CardDemo online programs under
 * GnuCOBOL. Translated programs CALL 'CICSSIM' USING '<descriptor>' args...
 * (see harness/cics.py). One process = one CICS task:
 *
 *   CICSSIM_PROGRAM  program to start            CICSSIM_AID   EIBAID as hex
 *   CICSSIM_FILES    <NAME>.dat/.meta datasets   CICSSIM_TASK  task directory
 *   CICSSIM_DATE/TIME  FORMATTIME values
 *
 * Task directory: in/commarea, in/map.<MAP> (RECEIVE MAP input), faults
 * ("CMD DATASET RESP [RESP2]" lines, '*' wildcards), out/events (one tab
 * separated line per command), out/NNN.* (SEND/WRITEQ payloads), out/commarea.
 */
#include <stdarg.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <libcob.h>

#define NORMAL 0
#define FILENOTFOUND 12
#define NOTFND 13
#define DUPREC 14
#define DUPKEY 15
#define INVREQ 16
#define ENDFILE 20
#define MAPFAIL 36

typedef struct {
    char name[9];
    int reclen, keyoff, keylen, n, cap;
    unsigned char *data;
    int br_pos, br_last, br_active;
    unsigned char upd_key[256];
    int upd_valid;
} dataset;

static dataset sets[32];
static int nsets;
static char task_dir[1024], files_dir[1024];
static int seq;
static FILE *events;

/* --------------------------------------------------------------- helpers */
static void trim_copy(char *dst, const unsigned char *src, int len, int max)
{
    int n = len < max ? len : max;
    memcpy(dst, src, n);
    dst[n] = 0;
    while (n > 0 && (dst[n - 1] == ' ' || dst[n - 1] == 0)) dst[--n] = 0;
}

static void event(const char *fmt, ...)
{
    va_list ap;
    if (!events) {
        char p[1100];
        snprintf(p, sizeof p, "%s/out/events", task_dir);
        events = fopen(p, "a");
    }
    va_start(ap, fmt);
    vfprintf(events, fmt, ap);
    va_end(ap);
    fputc('\n', events);
    fflush(events);
}

static const char *payload(const char *kind, const char *name, const void *data, int len)
{
    static char rel[256];
    char p[1400];
    FILE *f;
    snprintf(rel, sizeof rel, "%03d.%s.%s", ++seq, kind, name);
    snprintf(p, sizeof p, "%s/out/%s", task_dir, rel);
    f = fopen(p, "wb");
    fwrite(data, 1, len, f);
    fclose(f);
    return rel;
}

static void save_commarea(const void *data, int len)
{
    char p[1100];
    FILE *f;
    snprintf(p, sizeof p, "%s/out/commarea", task_dir);
    f = fopen(p, "wb");
    fwrite(data, 1, len, f);
    fclose(f);
}

/* -------------------------------------------------------------- datasets */
static dataset *find_set(const char *name)
{
    char p[1100];
    FILE *f;
    long size;
    dataset *d;
    for (int i = 0; i < nsets; i++)
        if (!strcmp(sets[i].name, name)) return &sets[i];
    snprintf(p, sizeof p, "%s/%s.meta", files_dir, name);
    f = fopen(p, "r");
    if (!f) return NULL;
    d = &sets[nsets++];
    memset(d, 0, sizeof *d);
    strncpy(d->name, name, 8);
    if (fscanf(f, "%d %d %d", &d->reclen, &d->keyoff, &d->keylen) != 3) exit(97);
    fclose(f);
    snprintf(p, sizeof p, "%s/%s.dat", files_dir, name);
    f = fopen(p, "rb");
    size = 0;
    if (f) { fseek(f, 0, SEEK_END); size = ftell(f); fseek(f, 0, SEEK_SET); }
    d->n = (int)(size / d->reclen);
    d->cap = d->n + 64;
    d->data = calloc(d->cap, d->reclen);
    if (f) { if (fread(d->data, d->reclen, d->n, f) != (size_t)d->n) exit(97); fclose(f); }
    return d;
}

static void save_set(dataset *d)
{
    char p[1100];
    FILE *f;
    snprintf(p, sizeof p, "%s/%s.dat", files_dir, d->name);
    f = fopen(p, "wb");
    fwrite(d->data, d->reclen, d->n, f);
    fclose(f);
}

static unsigned char *rec(dataset *d, int i) { return d->data + (size_t)i * d->reclen; }

static int keycmp(dataset *d, int i, const unsigned char *key)
{
    return memcmp(rec(d, i) + d->keyoff, key, d->keylen);
}

/* first index with record key >= key */
static int lower_bound(dataset *d, const unsigned char *key)
{
    int lo = 0, hi = d->n;
    while (lo < hi) {
        int mid = (lo + hi) / 2;
        if (keycmp(d, mid, key) < 0) lo = mid + 1; else hi = mid;
    }
    return lo;
}

static void key_from(dataset *d, unsigned char *key, const unsigned char *src, int len)
{
    memset(key, ' ', d->keylen);
    memcpy(key, src, len < d->keylen ? len : d->keylen);
}

static void copy_out(unsigned char *dst, int dstlen, const unsigned char *src, int srclen)
{
    memcpy(dst, src, srclen < dstlen ? srclen : dstlen);
}

/* -------------------------------------------------------------- dispatch */
typedef struct {
    char cmd[16];
    char opt[24][3];
    int argn[24];  /* parameter number, 0 for flags */
    int nopt;
} request;

static int opt(request *r, const char *code)
{
    for (int i = 0; i < r->nopt; i++)
        if (!strcmp(r->opt[i], code)) return r->argn[i] ? r->argn[i] : -1;
    return 0;
}

static void str_arg(request *r, const char *code, char *out, int max)
{
    int n = opt(r, code);
    out[0] = 0;
    if (n > 0) trim_copy(out, cob_get_param_data(n), cob_get_param_size(n), max);
}

static int fault(const char *cmd, const char *ds, int *resp2)
{
    char p[1100], line[256], c[32], d[32];
    int rc, r2;
    FILE *f;
    snprintf(p, sizeof p, "%s/faults", task_dir);
    if (!(f = fopen(p, "r"))) return -1;
    while (fgets(line, sizeof line, f)) {
        r2 = 0;
        if (sscanf(line, "%31s %31s %d %d", c, d, &rc, &r2) < 3) continue;
        if ((!strcmp(c, "*") || !strcmp(c, cmd)) && (!strcmp(d, "*") || !strcmp(d, ds))) {
            fclose(f);
            *resp2 = r2;
            return rc;
        }
    }
    fclose(f);
    return -1;
}

static void finish_resp(request *r, int resp, int resp2, const char *what)
{
    int n = opt(r, "RS"), n2 = opt(r, "R2");
    if (n > 0) cob_put_s64_param(n, resp);
    if (n2 > 0) cob_put_s64_param(n2, resp2);
    if (resp != NORMAL && n <= 0 && !opt(r, "NH")) {
        event("ABEND\tAEI%d\t%s", resp, what);
        cob_stop_run(0);
    }
}

static int file_cmd(request *r, const char *ds_name)
{
    dataset *d = find_set(ds_name);
    int n, i, len;
    unsigned char key[256];
    unsigned char *data;
    if (!d) return FILENOTFOUND;
    if (!strcmp(r->cmd, "READ") || !strcmp(r->cmd, "READNEXT") || !strcmp(r->cmd, "READPREV")
        || !strcmp(r->cmd, "STARTBR")) {
        n = opt(r, "RI");
        key_from(d, key, cob_get_param_data(n), cob_get_param_size(n));
    }
    if (!strcmp(r->cmd, "READ")) {
        i = lower_bound(d, key);
        if (i >= d->n || keycmp(d, i, key)) return NOTFND;
        n = opt(r, "IN");
        copy_out(cob_get_param_data(n), cob_get_param_size(n), rec(d, i), d->reclen);
        if (opt(r, "UP")) { memcpy(d->upd_key, key, d->keylen); d->upd_valid = 1; }
        return (i + 1 < d->n && !keycmp(d, i + 1, key)) ? DUPKEY : NORMAL;
    }
    if (!strcmp(r->cmd, "STARTBR")) {
        int high = 1;
        for (i = 0; i < d->keylen; i++) if (key[i] != 0xFF) high = 0;
        d->br_pos = lower_bound(d, key);
        d->br_last = -2;
        d->br_active = 1;
        if (opt(r, "EQ") && (d->br_pos >= d->n || keycmp(d, d->br_pos, key))) return NOTFND;
        if (d->br_pos >= d->n && !high) return NOTFND;
        return NORMAL;
    }
    if (!strcmp(r->cmd, "READNEXT") || !strcmp(r->cmd, "READPREV")) {
        if (!d->br_active) return INVREQ;
        if (!strcmp(r->cmd, "READNEXT"))
            i = d->br_last == -2 ? d->br_pos : d->br_last + 1;
        else if (d->br_last == -2)
            i = d->br_pos < d->n ? d->br_pos : d->n - 1;
        else
            i = d->br_last - 1;
        if (i < 0 || i >= d->n) return ENDFILE;
        d->br_last = i;
        n = opt(r, "IN");
        copy_out(cob_get_param_data(n), cob_get_param_size(n), rec(d, i), d->reclen);
        n = opt(r, "RI");
        copy_out(cob_get_param_data(n), cob_get_param_size(n), rec(d, i) + d->keyoff, d->keylen);
        return (i + 1 < d->n && !keycmp(d, i + 1, rec(d, i) + d->keyoff)) ? DUPKEY : NORMAL;
    }
    if (!strcmp(r->cmd, "ENDBR")) { d->br_active = 0; return NORMAL; }
    if (!strcmp(r->cmd, "WRITE") || !strcmp(r->cmd, "REWRITE")) {
        n = opt(r, "FR");
        data = cob_get_param_data(n);
        len = cob_get_param_size(n);
        key_from(d, key, data + d->keyoff, len - d->keyoff);
        i = lower_bound(d, key);
        if (!strcmp(r->cmd, "REWRITE")) {
            if (!d->upd_valid) return INVREQ;
            if (i >= d->n || keycmp(d, i, key)) return NOTFND;
            d->upd_valid = 0;
        } else {
            if (i < d->n && !keycmp(d, i, key)) return DUPREC;
            if (d->n == d->cap) {
                d->cap *= 2;
                d->data = realloc(d->data, (size_t)d->cap * d->reclen);
            }
            memmove(rec(d, i + 1), rec(d, i), (size_t)(d->n - i) * d->reclen);
            d->n++;
        }
        memset(rec(d, i), ' ', d->reclen);
        copy_out(rec(d, i), d->reclen, data, len);
        save_set(d);
        return NORMAL;
    }
    if (!strcmp(r->cmd, "DELETE")) {
        if ((n = opt(r, "RI")) > 0) key_from(d, key, cob_get_param_data(n), cob_get_param_size(n));
        else if (d->upd_valid) memcpy(key, d->upd_key, d->keylen);
        else return INVREQ;
        i = lower_bound(d, key);
        if (i >= d->n || keycmp(d, i, key)) return NOTFND;
        memmove(rec(d, i), rec(d, i + 1), (size_t)(d->n - i - 1) * d->reclen);
        d->n--;
        d->upd_valid = 0;
        save_set(d);
        return NORMAL;
    }
    return INVREQ;
}

static void put_text(request *r, const char *code, const char *text)
{
    int n = opt(r, code);
    if (n > 0) {
        int size = cob_get_param_size(n), len = (int)strlen(text);
        unsigned char *p = cob_get_param_data(n);
        memset(p, ' ', size);
        memcpy(p, text, len < size ? len : size);
    }
}

int CICSSIM(char *desc_arg)
{
    request r;
    char desc[256], *tok, name[64], name2[64], flags[128] = "";
    int nparams = cob_get_num_params(), next = 2, resp = NORMAL, resp2 = 0, forced, n;
    (void)desc_arg;
    trim_copy(desc, cob_get_param_data(1), cob_get_param_size(1), 255);
    memset(&r, 0, sizeof r);
    tok = strtok(desc, " ");
    strncpy(r.cmd, tok, 15);
    while ((tok = strtok(NULL, " ")) && r.nopt < 24) {
        static const char *flag_codes[] = {"ER", "CU", "FK", "UP", "GE", "EQ", "NH", "TD",
                                           "RB", "TX", NULL};
        int is_flag = 0;
        for (int i = 0; flag_codes[i]; i++) if (!strcmp(tok, flag_codes[i])) is_flag = 1;
        strncpy(r.opt[r.nopt], tok, 2);
        r.argn[r.nopt++] = is_flag ? 0 : next++;
        if (is_flag) { strcat(flags, flags[0] ? "," : ""); strcat(flags, tok); }
    }
    if (next - 1 != nparams) {
        event("ERROR\tparameter count mismatch for %s", r.cmd);
        cob_stop_run(98);
    }

    if (!strcmp(r.cmd, "READ") || !strcmp(r.cmd, "WRITE") || !strcmp(r.cmd, "REWRITE")
        || !strcmp(r.cmd, "DELETE") || !strcmp(r.cmd, "STARTBR") || !strcmp(r.cmd, "READNEXT")
        || !strcmp(r.cmd, "READPREV") || !strcmp(r.cmd, "ENDBR")) {
        str_arg(&r, "DS", name, 63);
        forced = fault(r.cmd, name, &resp2);
        resp = forced >= 0 ? forced : file_cmd(&r, name);
        event("%s\t%s\t%d\t%d", r.cmd, name, resp, resp2);
        finish_resp(&r, resp, resp2, name);
        return 0;
    }
    if (!strcmp(r.cmd, "RECEIVE")) {
        char p[1200];
        FILE *f;
        str_arg(&r, "MP", name, 63);
        forced = fault(r.cmd, name, &resp2);
        snprintf(p, sizeof p, "%s/in/map.%s", task_dir, name);
        if (forced >= 0) resp = forced;
        else if (!(f = fopen(p, "rb"))) resp = MAPFAIL;
        else {
            n = opt(&r, "IN");
            size_t got = fread(cob_get_param_data(n), 1, cob_get_param_size(n), f);
            (void)got;
            fclose(f);
        }
        event("RECEIVE\t%s\t%d", name, resp);
        finish_resp(&r, resp, resp2, name);
        return 0;
    }
    if (!strcmp(r.cmd, "SEND")) {
        n = opt(&r, "FR");
        if (opt(&r, "MP")) {
            str_arg(&r, "MP", name, 63);
            str_arg(&r, "MS", name2, 63);
            event("SEND\tMAP\t%s\t%s\t%s\t%s", name, name2, flags,
                  payload("map", name, cob_get_param_data(n), cob_get_param_size(n)));
        } else {
            event("SEND\tTEXT\t%s\t%s", flags,
                  payload("text", "txt", cob_get_param_data(n), cob_get_param_size(n)));
        }
        finish_resp(&r, NORMAL, 0, "SEND");
        return 0;
    }
    if (!strcmp(r.cmd, "RETURN") || !strcmp(r.cmd, "XCTL")) {
        str_arg(&r, !strcmp(r.cmd, "XCTL") ? "PG" : "TR", name, 63);
        if ((n = opt(&r, "CA")) > 0) save_commarea(cob_get_param_data(n), cob_get_param_size(n));
        event("%s\t%s", r.cmd, name);
        cob_stop_run(0);
    }
    if (!strcmp(r.cmd, "ABEND")) {
        str_arg(&r, "AB", name, 63);
        event("ABEND\t%s", name);
        cob_stop_run(0);
    }
    if (!strcmp(r.cmd, "ASSIGN")) {
        put_text(&r, "AP", "CARDDEMO");
        put_text(&r, "SY", "CICS");
        return 0;
    }
    if (!strcmp(r.cmd, "ASKTIME")) {
        if ((n = opt(&r, "AT")) > 0) cob_put_s64_param(n, 0);
        return 0;
    }
    if (!strcmp(r.cmd, "FORMATTIME")) {
        const char *date = getenv("CICSSIM_DATE"), *time = getenv("CICSSIM_TIME");
        char buf[32];
        int j = 0;
        char sep[8] = "";
        str_arg(&r, "DP", sep, 7);
        for (const char *c = date; *c; c++)
            if (isdigit((unsigned char)*c)) buf[j++] = *c;
            else if (sep[0]) buf[j++] = sep[0] == '\'' ? sep[1] : sep[0];
        buf[j] = 0;
        put_text(&r, "YD", buf);
        put_text(&r, "TM", time);
        return 0;
    }
    if (!strcmp(r.cmd, "WRITEQ")) {
        str_arg(&r, "QU", name, 63);
        n = opt(&r, "FR");
        forced = fault(r.cmd, name, &resp2);
        resp = forced >= 0 ? forced : NORMAL;
        if (resp == NORMAL)
            event("WRITEQ\tTD\t%s\t%s", name,
                  payload("td", name, cob_get_param_data(n), cob_get_param_size(n)));
        else
            event("WRITEQ\tFAILED\t%s\t%d", name, resp);
        finish_resp(&r, resp, resp2, name);
        return 0;
    }
    if (!strcmp(r.cmd, "SYNCPOINT")) {
        event("SYNCPOINT\t%s", flags);
        return 0;
    }
    event("ERROR\tunsupported command %s", r.cmd);
    cob_stop_run(98);
}

int main(void)
{
    const char *prog = getenv("CICSSIM_PROGRAM"), *aid = getenv("CICSSIM_AID");
    static unsigned char eib[128], commarea[32768];
    int (*fn)(void *, void *);
    char p[1100];
    FILE *f;
    long len = 0;
    strncpy(task_dir, getenv("CICSSIM_TASK"), sizeof task_dir - 1);
    strncpy(files_dir, getenv("CICSSIM_FILES"), sizeof files_dir - 1);
    snprintf(p, sizeof p, "%s/in/commarea", task_dir);
    if ((f = fopen(p, "rb"))) { len = (long)fread(commarea, 1, sizeof commarea, f); fclose(f); }
    memset(eib, 0, sizeof eib);
    memcpy(eib + 8, "CC00", 4);
    memcpy(eib + 16, "T001", 4);
    eib[24] = (unsigned char)(len >> 8);
    eib[25] = (unsigned char)len;
    eib[26] = (unsigned char)strtol(aid ? aid : "7D", NULL, 16);
    cob_init(0, NULL);
    fn = (int (*)(void *, void *))cob_resolve(prog);
    if (!fn) { fprintf(stderr, "cannot load %s\n", prog); return 96; }
    fn(eib, commarea);
    event("GOBACK");
    cob_stop_run(0);
}
