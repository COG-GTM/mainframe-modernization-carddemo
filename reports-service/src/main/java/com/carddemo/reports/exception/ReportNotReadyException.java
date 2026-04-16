package com.carddemo.reports.exception;

import com.carddemo.reports.model.ReportStatus;

public class ReportNotReadyException extends RuntimeException {

    public ReportNotReadyException(String id, ReportStatus status) {
        super("Report " + id + " is not ready for download. Current status: " + status);
    }
}
