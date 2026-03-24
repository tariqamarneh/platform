package com.businessagent.inbox.exception;

public class ServiceCallException extends RuntimeException {

    private final int statusCode;

    public ServiceCallException(String message) {
        super(message);
        this.statusCode = 502;
    }

    public ServiceCallException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ServiceCallException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 502;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
