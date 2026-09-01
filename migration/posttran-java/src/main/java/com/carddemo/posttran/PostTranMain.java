package com.carddemo.posttran;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Step harness for POSTTRAN STEP15 (app/jcl/POSTTRAN.jcl:23). It reproduces the DD wiring of the
 * JCL through environment variables; the step passes no PARM (app/jcl/POSTTRAN.jcl:23), so none is
 * accepted here.
 *
 * <p>Keyed DDs are supplied as flat, key-ordered unloads of the KSDS clusters (the format an
 * IDCAMS REPRO produces and that parity/tools/VSAMUNLD.cbl emits for the GnuCOBOL baseline).
 */
public final class PostTranMain {

    private PostTranMain() {
    }

    public static void main(String[] args) throws IOException {
        Path dalytran = ddPath("DD_DALYTRAN");
        Path xreffile = ddPath("DD_XREFFILE");
        Path acctfile = ddPath("DD_ACCTFILE");
        Path tcatbalf = ddPath("DD_TCATBALF");
        Path outDir = ddPath("OUT_DIR");
        java.nio.file.Files.createDirectories(outDir);

        StringWriter sysout = new StringWriter();
        PostTranJob.Result result;
        try (Files.SeqInput in = new Files.SeqInput(dalytran, Layouts.DalyTran.LEN);
             Files.SeqOutput rejects = new Files.SeqOutput(outDir.resolve("DALYREJS.dat"))) {

            Files.KeyedFile xref = Files.KeyedFile.open(xreffile, Layouts.Xref.LEN, Layouts.Xref.KEY_LEN);
            Files.KeyedFile acct = Files.KeyedFile.open(acctfile, Layouts.Account.LEN, Layouts.Account.KEY_LEN);
            Files.KeyedFile tcat = Files.KeyedFile.open(tcatbalf, Layouts.TranCatBal.LEN, Layouts.TranCatBal.KEY_LEN);
            // OPEN OUTPUT TRANSACT-FILE (app/cbl/CBTRN02C.cbl:256) — the cluster starts empty.
            Files.KeyedFile tran = Files.KeyedFile.empty(Layouts.Tran.LEN, Layouts.Xref.KEY_LEN);

            PostTranJob job = new PostTranJob(in, tran, xref, rejects, acct, tcat,
                    LocalDateTime::now, sysout);
            result = job.run();

            tran.unload(outDir.resolve("TRANSACT.after"));
            acct.unload(outDir.resolve("ACCTDATA.after"));
            tcat.unload(outDir.resolve("TCATBALF.after"));
        }

        try (PrintWriter w = new PrintWriter(
                java.nio.file.Files.newBufferedWriter(outDir.resolve("SYSOUT.txt"), StandardCharsets.ISO_8859_1))) {
            w.print(sysout);
            w.printf("CBTRN02C RC=%d%n", result.returnCode);
        }
        System.out.print(sysout);
        System.out.printf("CBTRN02C RC=%d%n", result.returnCode);
        System.exit(result.returnCode);
    }

    private static Path ddPath(String name) {
        String v = System.getenv(name);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("missing environment variable " + name);
        }
        return Path.of(v);
    }
}
