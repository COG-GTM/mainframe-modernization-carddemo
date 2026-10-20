package com.carddemo.batch.core.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.carddemo.batch.core.BatchConfig;
import com.carddemo.batch.core.CardDemoFile;
import com.carddemo.batch.core.record.CardXrefRecord;
import com.carddemo.batch.core.record.TranCatBalRecord;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IndexedFileStoreTest {

    private static IndexedFileStore<String, TranCatBalRecord> tcatbal() {
        return IndexedFileStore.load(BatchConfig.defaultInputDir().resolve(CardDemoFile.TCATBAL.fileName()),
                TranCatBalRecord.LENGTH, TranCatBalRecord::fromBytes, TranCatBalRecord::key);
    }

    private static TranCatBalRecord rec(long acct, int cat, String bal) {
        return new TranCatBalRecord(acct, "01", cat, new BigDecimal(bal));
    }

    @Test
    void randomReadReturnsOkOrNotFound() {
        IndexedFileStore<String, TranCatBalRecord> store = tcatbal();
        assertEquals(50, store.size());
        assertEquals(FileStatus.OK, store.read(TranCatBalRecord.key(1, "01", 1)).status());
        IoResult<TranCatBalRecord> missing = store.read(TranCatBalRecord.key(999, "01", 1));
        assertEquals(FileStatus.NOT_FOUND, missing.status());
        assertTrue(missing.isNotFound());
        assertTrue(missing.asOptional().isEmpty());
    }

    @Test
    void writeRewriteDeleteStatuses() {
        IndexedFileStore<String, TranCatBalRecord> store = new IndexedFileStore<>(TranCatBalRecord::key);
        assertEquals(FileStatus.NOT_FOUND, store.rewrite(rec(1, 1, "1")).status());
        assertEquals(FileStatus.OK, store.write(rec(1, 1, "1")).status());
        assertEquals(FileStatus.DUPLICATE_KEY, store.write(rec(1, 1, "2")).status());
        assertEquals(FileStatus.OK, store.rewrite(rec(1, 1, "3")).status());
        assertEquals(new BigDecimal("3.00"), store.read(TranCatBalRecord.key(1, "01", 1)).record().balance());
        assertEquals(FileStatus.OK, store.delete(TranCatBalRecord.key(1, "01", 1)).status());
        assertEquals(FileStatus.NOT_FOUND, store.delete(TranCatBalRecord.key(1, "01", 1)).status());
    }

    @Test
    void sequentialReadInKeyOrderEndsWithStatus10() {
        IndexedFileStore<String, TranCatBalRecord> store =
                IndexedFileStore.of(List.of(rec(3, 1, "3"), rec(1, 2, "1"), rec(2, 1, "2")), TranCatBalRecord::key);
        assertEquals(1, store.readNext().record().acctId());
        assertEquals(2, store.readNext().record().acctId());
        assertEquals(3, store.readNext().record().acctId());
        IoResult<TranCatBalRecord> eof = store.readNext();
        assertEquals(FileStatus.END_OF_FILE, eof.status());
        assertTrue(eof.isEndOfFile());
        assertEquals(FileStatus.END_OF_FILE, store.readNext().status());
        store.rewind();
        assertEquals(1, store.readNext().record().acctId());
    }

    @Test
    void startAndRandomReadPositionSequentialRead() {
        IndexedFileStore<String, TranCatBalRecord> store =
                IndexedFileStore.of(List.of(rec(1, 1, "1"), rec(2, 1, "2"), rec(4, 1, "4")), TranCatBalRecord::key);
        assertEquals(FileStatus.OK, store.start(TranCatBalRecord.key(3, "01", 1)));
        assertEquals(4, store.readNext().record().acctId());
        assertEquals(FileStatus.NOT_FOUND, store.start(TranCatBalRecord.key(5, "01", 1)));
        store.read(TranCatBalRecord.key(1, "01", 1));
        assertEquals(2, store.readNext().record().acctId());
    }

    @Test
    void recordsWrittenDuringSequentialScanAreSeenInKeyOrder() {
        IndexedFileStore<String, TranCatBalRecord> store =
                IndexedFileStore.of(List.of(rec(1, 1, "1"), rec(3, 1, "3")), TranCatBalRecord::key);
        assertEquals(1, store.readNext().record().acctId());
        store.write(rec(2, 1, "2"));
        assertEquals(2, store.readNext().record().acctId());
        assertEquals(3, store.readNext().record().acctId());
    }

    @Test
    void alternateKeyRead() {
        IndexedFileStore<String, CardXrefRecord> xref = IndexedFileStore.load(
                BatchConfig.defaultInputDir().resolve(CardDemoFile.CARDXREF.fileName()),
                CardXrefRecord.LENGTH, CardXrefRecord::fromBytes, CardXrefRecord::cardNum);
        IoResult<CardXrefRecord> byAcct = xref.readByAlternateKey(CardXrefRecord::acctId, 50L);
        assertEquals("0500024453765740", byAcct.record().cardNum());
        assertEquals(FileStatus.NOT_FOUND, xref.readByAlternateKey(CardXrefRecord::acctId, 99999L).status());
    }

    @Test
    void duplicateKeysRejectedOnLoad() {
        assertThrows(IllegalStateException.class,
                () -> IndexedFileStore.of(List.of(rec(1, 1, "1"), rec(1, 1, "2")), TranCatBalRecord::key));
    }

    @Test
    void saveUnloadsInKeyOrder(@TempDir Path dir) throws Exception {
        IndexedFileStore<String, TranCatBalRecord> store = tcatbal();
        Path out = dir.resolve("tcatbal.txt");
        store.save(out, TranCatBalRecord.LENGTH, TranCatBalRecord::toBytes);
        assertEquals(Files.readAllLines(BatchConfig.defaultInputDir().resolve("tcatbal.txt")), Files.readAllLines(out));
    }
}
