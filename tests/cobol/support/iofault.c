/*
 * LD_PRELOAD shim that forces a FILE STATUS on libcob file operations, so batch
 * tests can reach the I/O error and abend paths of the programs under test.
 *
 * COB_IO_FAULTS="OP SELECT-NAME STATUS[@N];..."
 *   OP      OPEN, CLOSE, READ (READ and READ NEXT), WRITE, REWRITE, START, DELETE
 *   SELECT  the file's SELECT name, e.g. TCATBAL-FILE
 *   STATUS  two-character status returned instead of performing the operation;
 *           a successful status (0x) performs it first, as does CLOSE
 *   @N      only the N-th such call (1-based); every call when omitted
 */
#define _GNU_SOURCE
#include <dlfcn.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <libcob.h>

#define MAX_FAULTS 32

struct fault { char op[8]; char file[32]; char status[3]; int nth; int calls; };
static struct fault faults[MAX_FAULTS];
static int nfaults = -1;

static void load_faults(void)
{
    const char *spec = getenv("COB_IO_FAULTS");
    nfaults = 0;
    if (!spec) return;
    char *copy = strdup(spec), *save = NULL;
    for (char *tok = strtok_r(copy, ";", &save); tok && nfaults < MAX_FAULTS;
         tok = strtok_r(NULL, ";", &save)) {
        struct fault *f = &faults[nfaults];
        memset(f, 0, sizeof *f);
        char status[8] = "";
        if (sscanf(tok, " %7s %31s %7s", f->op, f->file, status) != 3) continue;
        char *at = strchr(status, '@');
        if (at) { f->nth = atoi(at + 1); *at = '\0'; }
        memcpy(f->status, status, 2);
        nfaults++;
    }
    free(copy);
}

static const char *forced(const char *op, cob_file *file)
{
    if (nfaults < 0) load_faults();
    for (int i = 0; i < nfaults; i++) {
        struct fault *f = &faults[i];
        if (strcasecmp(f->op, op) || strcasecmp(f->file, file->select_name)) continue;
        f->calls++;
        if (f->nth == 0 || f->nth == f->calls) return f->status;
    }
    return NULL;
}

static void set_status(cob_file *file, cob_field *fnstatus, const char *status)
{
    if (file->file_status) memcpy(file->file_status, status, 2);
    if (fnstatus && fnstatus->size >= 2) memcpy(fnstatus->data, status, 2);
}

#define REAL(name) static __typeof__(name) *real_##name; \
    if (!real_##name) real_##name = (__typeof__(name) *)dlsym(RTLD_NEXT, #name)

void cob_open(cob_file *f, const int mode, const int sharing, cob_field *st)
{
    REAL(cob_open);
    const char *s = forced("OPEN", f);
    if (!s || s[0] == '0') real_cob_open(f, mode, sharing, st);
    if (s) set_status(f, st, s);
}

void cob_close(cob_file *f, cob_field *st, const int opt, const int remfil)
{
    REAL(cob_close);
    const char *s = forced("CLOSE", f);
    real_cob_close(f, st, opt, remfil);
    if (s) set_status(f, st, s);
}

void cob_read(cob_file *f, cob_field *key, cob_field *st, const int opts)
{
    REAL(cob_read);
    const char *s = forced("READ", f);
    if (!s || s[0] == '0') real_cob_read(f, key, st, opts);
    if (s) set_status(f, st, s);
}

void cob_read_next(cob_file *f, cob_field *st, const int opts)
{
    REAL(cob_read_next);
    const char *s = forced("READ", f);
    if (!s || s[0] == '0') real_cob_read_next(f, st, opts);
    if (s) set_status(f, st, s);
}

void cob_write(cob_file *f, cob_field *rec, const int opt, cob_field *st, const unsigned int eop)
{
    REAL(cob_write);
    const char *s = forced("WRITE", f);
    if (!s || s[0] == '0') real_cob_write(f, rec, opt, st, eop);
    if (s) set_status(f, st, s);
}

void cob_rewrite(cob_file *f, cob_field *rec, const int opt, cob_field *st)
{
    REAL(cob_rewrite);
    const char *s = forced("REWRITE", f);
    if (!s || s[0] == '0') real_cob_rewrite(f, rec, opt, st);
    if (s) set_status(f, st, s);
}

void cob_start(cob_file *f, const int cond, cob_field *key, cob_field *keysize, cob_field *st)
{
    REAL(cob_start);
    const char *s = forced("START", f);
    if (!s || s[0] == '0') real_cob_start(f, cond, key, keysize, st);
    if (s) set_status(f, st, s);
}

void cob_delete(cob_file *f, cob_field *st)
{
    REAL(cob_delete);
    const char *s = forced("DELETE", f);
    if (!s || s[0] == '0') real_cob_delete(f, st);
    if (s) set_status(f, st, s);
}
