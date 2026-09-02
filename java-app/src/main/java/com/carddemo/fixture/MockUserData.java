package com.carddemo.fixture;

import com.carddemo.domain.User;

import java.util.List;

/**
 * Mock USRSEC records.
 *
 * <p>The repository ships no ASCII user security file, so these users mirror the in-stream data of
 * {@code app/jcl/DUSRSECJ.jcl}, which includes the README seed logins {@code ADMIN001/PASSWORD}
 * (admin) and {@code USER0001/PASSWORD} (regular).
 */
public final class MockUserData {

    private static final List<User> SEED_USERS = List.of(
            new User("ADMIN001", "MARGARET", "GOLD", "PASSWORD", User.TYPE_ADMIN),
            new User("ADMIN002", "RUSSELL", "RUSSELL", "PASSWORD", User.TYPE_ADMIN),
            new User("ADMIN003", "RAYMOND", "WHITMORE", "PASSWORD", User.TYPE_ADMIN),
            new User("ADMIN004", "EMMANUEL", "CASGRAIN", "PASSWORD", User.TYPE_ADMIN),
            new User("ADMIN005", "GRANVILLE", "LACHAPELLE", "PASSWORD", User.TYPE_ADMIN),
            new User("USER0001", "LAWRENCE", "THOMAS", "PASSWORD", User.TYPE_REGULAR),
            new User("USER0002", "AJITH", "KUMAR", "PASSWORD", User.TYPE_REGULAR),
            new User("USER0003", "LAURITZ", "ALME", "PASSWORD", User.TYPE_REGULAR),
            new User("USER0004", "AVERARDO", "MAZZI", "PASSWORD", User.TYPE_REGULAR),
            new User("USER0005", "LEE", "TING", "PASSWORD", User.TYPE_REGULAR));

    private MockUserData() {
    }

    /** Fresh mutable copies of the seed users, so callers can update or delete without side effects. */
    public static List<User> users() {
        return SEED_USERS.stream()
                .map(user -> new User(user.getUserId(), user.getFirstName(), user.getLastName(), user.getPassword(),
                        user.getUserType()))
                .toList();
    }
}
