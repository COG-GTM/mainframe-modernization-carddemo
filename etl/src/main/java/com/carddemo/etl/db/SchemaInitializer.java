package com.carddemo.etl.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Creates the {@code accounts} table (if absent) from the bundled {@code schema.sql} resource. */
public final class SchemaInitializer {

    private static final String SCHEMA_RESOURCE = "/schema.sql";

    public void initialize(Connection connection) throws SQLException {
        String ddl = loadSchema();
        try (Statement statement = connection.createStatement()) {
            statement.execute(ddl);
        }
    }

    private String loadSchema() {
        try (InputStream in = SchemaInitializer.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource " + SCHEMA_RESOURCE);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed reading " + SCHEMA_RESOURCE, e);
        }
    }
}
