package ai.cognition.airlift.store;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The mirror's stand-in for a VSAM KSDS cluster: an embedded H2 table keyed by the
 * cluster's RECORD KEY, holding the record image, optionally with a secondary
 * index standing in for an alternate-index PATH.
 *
 * <p>Records are stored as their byte image rather than as exploded columns. The
 * COBOL programs READ INTO a record area, change a few fields and REWRITE the
 * whole area, so bytes outside the slice - FILLER, fields no program in the chain
 * looks at - have to survive a round trip untouched for the outputs to be
 * comparable.
 */
public final class KeyedStore {

  private final JdbcTemplate jdbc;
  private final String table;
  private final boolean alternateKey;

  public KeyedStore(JdbcTemplate jdbc, String table, boolean alternateKey) {
    this.jdbc = jdbc;
    this.table = table;
    this.alternateKey = alternateKey;
  }

  /** DEFINE CLUSTER: drop and recreate, so a rerun starts from the fixtures. */
  public void define() {
    jdbc.execute("DROP TABLE IF EXISTS " + table);
    jdbc.execute(
        "CREATE TABLE "
            + table
            + " (record_key VARCHAR(64) PRIMARY KEY, alternate_key VARCHAR(64), image VARBINARY(4096))");
    if (alternateKey) {
      jdbc.execute("CREATE INDEX idx_" + table + "_alt ON " + table + "(alternate_key)");
    }
  }

  public void write(String key, byte[] image) {
    write(key, null, image);
  }

  public void write(String key, String alternate, byte[] image) {
    jdbc.update(
        "INSERT INTO " + table + " (record_key, alternate_key, image) VALUES (?, ?, ?)",
        key,
        alternate,
        image);
  }

  public Optional<byte[]> read(String key) {
    return jdbc
        .query("SELECT image FROM " + table + " WHERE record_key = ?", RowMappers.IMAGE, key)
        .stream()
        .findFirst();
  }

  /** READ ... KEY IS <alternate>: first record in key order under that alternate key. */
  public Optional<byte[]> readByAlternate(String alternate) {
    return jdbc
        .query(
            "SELECT image FROM " + table + " WHERE alternate_key = ? ORDER BY record_key",
            RowMappers.IMAGE,
            alternate)
        .stream()
        .findFirst();
  }

  public void rewrite(String key, byte[] image) {
    int updated = jdbc.update("UPDATE " + table + " SET image = ? WHERE record_key = ?", image, key);
    if (updated != 1) {
      throw new IllegalStateException("REWRITE of " + table + " key '" + key + "' found no record");
    }
  }

  /** Sequential read of the cluster in RECORD KEY order. */
  public List<byte[]> inKeyOrder() {
    return jdbc.query("SELECT image FROM " + table + " ORDER BY record_key", RowMappers.IMAGE);
  }

  public int count() {
    Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    return count == null ? 0 : count;
  }
}
