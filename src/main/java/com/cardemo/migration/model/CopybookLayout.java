package com.cardemo.migration.model;

import java.util.Collections;
import java.util.List;

/**
 * Defines the complete copybook layout for a VSAM record type.
 * Contains all fields with their byte offsets and types.
 */
public record CopybookLayout(
        String name,
        int recordLength,
        List<CopybookField> fields
) {

    public CopybookLayout {
        fields = Collections.unmodifiableList(fields);
    }

    /**
     * Returns only the data fields (excludes FILLER).
     */
    public List<CopybookField> dataFields() {
        return fields.stream()
                .filter(f -> f.type() != CopybookField.FieldType.FILLER)
                .toList();
    }

    /**
     * Finds a field by its copybook name.
     */
    public CopybookField findField(String fieldName) {
        return fields.stream()
                .filter(f -> f.name().equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Field not found in layout " + name + ": " + fieldName));
    }
}
