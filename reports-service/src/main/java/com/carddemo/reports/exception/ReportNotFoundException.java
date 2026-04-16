package com.carddemo.reports.exception;

public class ReportNotFoundException extends RuntimeException {

    public ReportNotFoundException(String id) {
        super("Report not found with id: " + id);
    }
}
