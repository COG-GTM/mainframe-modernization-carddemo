package ai.cognition.airlift.store;

import ai.cognition.airlift.MirrorProperties;
import ai.cognition.airlift.codec.Layouts;
import ai.cognition.airlift.codec.RecordBuffer;
import ai.cognition.airlift.io.FixedRecordFile;
import java.nio.file.Path;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The clusters the slice uses, and the load/unload paths that stand in for the
 * IDCAMS REPRO steps the JCL runs around the programs.
 */
@Component
public class Datasets {

  public static final String ACCT_ID = "ACCT-ID";
  public static final String XREF_CARD_NUM = "XREF-CARD-NUM";
  public static final String XREF_ACCT_ID = "XREF-ACCT-ID";
  public static final String TRANCAT_ACCT_ID = "TRAN-CAT-KEY.TRANCAT-ACCT-ID";
  public static final String TRANCAT_TYPE_CD = "TRAN-CAT-KEY.TRANCAT-TYPE-CD";
  public static final String TRANCAT_CD = "TRAN-CAT-KEY.TRANCAT-CD";
  public static final String DIS_GROUP_ID = "DIS-GROUP-KEY.DIS-ACCT-GROUP-ID";
  public static final String DIS_TYPE_CD = "DIS-GROUP-KEY.DIS-TRAN-TYPE-CD";
  public static final String DIS_CAT_CD = "DIS-GROUP-KEY.DIS-TRAN-CAT-CD";
  public static final String TRAN_ID = "TRAN-ID";

  private final Layouts layouts;
  private final MirrorProperties properties;
  private final KeyedStore accounts;
  private final KeyedStore cardXref;
  private final KeyedStore disclosureGroups;
  private final KeyedStore tranCatBalances;
  private final KeyedStore transactions;

  public Datasets(Layouts layouts, MirrorProperties properties, JdbcTemplate jdbc) {
    this.layouts = layouts;
    this.properties = properties;
    this.accounts = new KeyedStore(jdbc, "acctfile", false);
    this.cardXref = new KeyedStore(jdbc, "xreffile", true);
    this.disclosureGroups = new KeyedStore(jdbc, "discgrp", false);
    this.tranCatBalances = new KeyedStore(jdbc, "tcatbalf", false);
    this.transactions = new KeyedStore(jdbc, "tranfile", false);
  }

  public Layouts layouts() {
    return layouts;
  }

  public KeyedStore accounts() {
    return accounts;
  }

  public KeyedStore cardXref() {
    return cardXref;
  }

  public KeyedStore disclosureGroups() {
    return disclosureGroups;
  }

  public KeyedStore tranCatBalances() {
    return tranCatBalances;
  }

  public KeyedStore transactions() {
    return transactions;
  }

  /** REPRO the sequential fixtures into the clusters the programs open. */
  public void loadAccounts() {
    accounts.define();
    for (byte[] image : FixedRecordFile.read(in("acctdata.dat"), layouts.account().length())) {
      RecordBuffer record = RecordBuffer.of(layouts.account(), image);
      accounts.write(record.text(ACCT_ID), image);
    }
  }

  public void loadSlice() {
    loadAccounts();
    cardXref.define();
    for (byte[] image : FixedRecordFile.read(in("cardxref.dat"), layouts.cardXref().length())) {
      RecordBuffer record = RecordBuffer.of(layouts.cardXref(), image);
      cardXref.write(record.text(XREF_CARD_NUM), record.text(XREF_ACCT_ID), image);
    }
    disclosureGroups.define();
    for (byte[] image :
        FixedRecordFile.read(in("discgrp.dat"), layouts.disclosureGroup().length())) {
      RecordBuffer record = RecordBuffer.of(layouts.disclosureGroup(), image);
      disclosureGroups.write(
          record.text(DIS_GROUP_ID) + record.text(DIS_TYPE_CD) + record.text(DIS_CAT_CD), image);
    }
    tranCatBalances.define();
    for (byte[] image : FixedRecordFile.read(in("tcatbal.dat"), layouts.tranCatBal().length())) {
      RecordBuffer record = RecordBuffer.of(layouts.tranCatBal(), image);
      tranCatBalances.write(tranCatKey(record), image);
    }
    transactions.define();
  }

  /** The key of a transaction-category balance record, as its RECORD KEY spans it. */
  public static String tranCatKey(RecordBuffer record) {
    return record.text(TRANCAT_ACCT_ID) + record.text(TRANCAT_TYPE_CD) + record.text(TRANCAT_CD);
  }

  public Path in(String dataset) {
    return properties.in().resolve(dataset);
  }

  public Path out(String dataset) {
    return properties.out().resolve(dataset);
  }

  /** The IDCAMS REPRO steps that unload the clusters for comparison. */
  public void unload() {
    write(out("acctdata.dat"), accounts.inKeyOrder());
    write(out("tcatbal.dat"), tranCatBalances.inKeyOrder());
    write(out("transact.dat"), transactions.inKeyOrder());
  }

  private static void write(Path path, List<byte[]> records) {
    try (FixedRecordFile.Writer writer = FixedRecordFile.writer(path)) {
      records.forEach(writer::write);
    }
  }
}
