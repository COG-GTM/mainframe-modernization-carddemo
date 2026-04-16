package com.carddemo.reports.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReportType {

    MONTHLY("01"),
    YEARLY("02"),
    CUSTOM("03");

    private final String code;

    ReportType(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static ReportType fromCode(String code) {
        for (ReportType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    public String displayName() {
        return switch (this) {
            case MONTHLY -> "Monthly";
            case YEARLY -> "Yearly";
            case CUSTOM -> "Custom";
        };
    }
}
