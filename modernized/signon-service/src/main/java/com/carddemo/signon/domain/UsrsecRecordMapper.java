package com.carddemo.signon.domain;

/**
 * Fixed-width mapper for the 80-byte {@code USRSEC} record described by copybook
 * {@code CSUSR01Y}. It converts a raw mainframe record (as loaded by the
 * {@code DUSRSECJ}/{@code IEBGENER} job) to a {@link UserSecurity} and back,
 * preserving the exact field offsets and lengths.
 *
 * <p>This keeps the migration faithful to the copybook layout and provides a
 * deterministic way to seed the cloud datastore from the sample data files.
 */
public final class UsrsecRecordMapper {

    // Offsets/lengths are derived directly from CSUSR01Y (see UserSecurity).
    static final int ID_OFFSET = 0;
    static final int ID_LEN = 8;      // SEC-USR-ID    PIC X(08)
    static final int FNAME_OFFSET = 8;
    static final int FNAME_LEN = 20;  // SEC-USR-FNAME PIC X(20)
    static final int LNAME_OFFSET = 28;
    static final int LNAME_LEN = 20;  // SEC-USR-LNAME PIC X(20)
    static final int PWD_OFFSET = 48;
    static final int PWD_LEN = 8;     // SEC-USR-PWD   PIC X(08)
    static final int TYPE_OFFSET = 56;
    static final int TYPE_LEN = 1;    // SEC-USR-TYPE  PIC X(01)
    static final int FILLER_OFFSET = 57;
    static final int FILLER_LEN = 23; // SEC-USR-FILLER PIC X(23)

    /** Total record length: 8 + 20 + 20 + 8 + 1 + 23 = 80. */
    public static final int RECORD_LENGTH =
            ID_LEN + FNAME_LEN + LNAME_LEN + PWD_LEN + TYPE_LEN + FILLER_LEN;

    private UsrsecRecordMapper() {
    }

    /**
     * Parse an 80-character fixed-width {@code USRSEC} record. Trailing spaces in
     * each field are trimmed, matching the way COBOL {@code PIC X} fields are
     * space-padded on the mainframe.
     *
     * @throws IllegalArgumentException if the record is not exactly 80 chars.
     */
    public static UserSecurity parse(String record) {
        if (record == null || record.length() != RECORD_LENGTH) {
            throw new IllegalArgumentException(
                    "USRSEC record must be exactly " + RECORD_LENGTH
                            + " characters, got " + (record == null ? "null" : record.length()));
        }
        String id = field(record, ID_OFFSET, ID_LEN);
        String fname = field(record, FNAME_OFFSET, FNAME_LEN);
        String lname = field(record, LNAME_OFFSET, LNAME_LEN);
        String pwd = field(record, PWD_OFFSET, PWD_LEN);
        String type = field(record, TYPE_OFFSET, TYPE_LEN);
        return new UserSecurity(id, fname, lname, pwd, type);
    }

    /**
     * Render a {@link UserSecurity} back into an 80-character fixed-width record
     * with space-padded, right-truncated fields and a blank trailing filler.
     */
    public static String format(UserSecurity user) {
        StringBuilder sb = new StringBuilder(RECORD_LENGTH);
        sb.append(pad(user.getUserId(), ID_LEN));
        sb.append(pad(user.getFirstName(), FNAME_LEN));
        sb.append(pad(user.getLastName(), LNAME_LEN));
        sb.append(pad(user.getPassword(), PWD_LEN));
        sb.append(pad(user.getUserType(), TYPE_LEN));
        sb.append(pad("", FILLER_LEN));
        return sb.toString();
    }

    private static String field(String record, int offset, int length) {
        return stripTrailing(record.substring(offset, offset + length));
    }

    private static String stripTrailing(String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == ' ') {
            end--;
        }
        return value.substring(0, end);
    }

    private static String pad(String value, int length) {
        String v = value == null ? "" : value;
        if (v.length() > length) {
            return v.substring(0, length);
        }
        StringBuilder sb = new StringBuilder(length);
        sb.append(v);
        while (sb.length() < length) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
