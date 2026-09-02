package com.carddemo.domain;

import com.carddemo.util.CobolCodec;
import com.carddemo.util.FieldCursor;

/**
 * SEC-USER-DATA, copybook CSUSR01Y, LRECL 80 (USRSEC).
 *
 * <p>No ASCII sample file ships with the repository; the seed users come from the in-stream data of
 * {@code app/jcl/DUSRSECJ.jcl}.
 */
public class User {

    public static final int RECORD_LENGTH = 80;

    public static final char TYPE_ADMIN = 'A';
    public static final char TYPE_REGULAR = 'U';

    private String userId;
    private String firstName;
    private String lastName;
    private String password;
    private char userType;

    public User() {
    }

    public User(String userId, String firstName, String lastName, String password, char userType) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.userType = userType;
    }

    public static User parse(String record) {
        FieldCursor cursor = new FieldCursor(record, RECORD_LENGTH);
        User user = new User();
        user.userId = cursor.text(8);
        user.firstName = cursor.text(20);
        user.lastName = cursor.text(20);
        user.password = cursor.text(8);
        user.userType = cursor.flag();
        return user;
    }

    public String format() {
        return CobolCodec.encodeText(userId, 8)
                + CobolCodec.encodeText(firstName, 20)
                + CobolCodec.encodeText(lastName, 20)
                + CobolCodec.encodeText(password, 8)
                + userType
                + " ".repeat(23);
    }

    public boolean isAdmin() {
        return userType == TYPE_ADMIN;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public char getUserType() {
        return userType;
    }

    public void setUserType(char userType) {
        this.userType = userType;
    }

    @Override
    public String toString() {
        return "User[" + userId + ", type=" + userType + "]";
    }
}
