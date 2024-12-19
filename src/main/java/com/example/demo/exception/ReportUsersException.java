package com.example.demo.exception;

public class ReportUsersException extends RuntimeException {
    public ReportUsersException(String message) {
        super(message);
    }

    public ReportUsersException(String message, Throwable cause) {
        super(message, cause);
    }
}
