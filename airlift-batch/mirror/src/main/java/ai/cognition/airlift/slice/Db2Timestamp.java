package ai.cognition.airlift.slice;

/**
 * The 26-character timestamp both programs build in Z-GET-DB2-FORMAT-TIMESTAMP
 * (CBTRN02C:692-705, CBACT04C:613-626) from FUNCTION CURRENT-DATE.
 *
 * <p>The COBOL harness receives that clock value through the AIRLIFT_CLOCK
 * environment variable so the run is reproducible; the mirror is handed the same
 * 21-character string and formats it the same way. Note the last four characters:
 * the COBOL moves the literal '0000' into DB2-REST rather than a real microsecond
 * fraction.
 */
public final class Db2Timestamp {

  private final String value;

  public Db2Timestamp(String clock) {
    if (clock == null || clock.length() < 16) {
      throw new IllegalArgumentException(
          "clock must be a FUNCTION CURRENT-DATE value (YYYYMMDDHHMMSShh...), got: " + clock);
    }
    String year = clock.substring(0, 4);
    String month = clock.substring(4, 6);
    String day = clock.substring(6, 8);
    String hour = clock.substring(8, 10);
    String minute = clock.substring(10, 12);
    String second = clock.substring(12, 14);
    String hundredths = clock.substring(14, 16);
    this.value =
        year + "-" + month + "-" + day + "-" + hour + "." + minute + "." + second + "."
            + hundredths + "0000";
  }

  public String value() {
    return value;
  }
}
