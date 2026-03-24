package com.businessagent.channel.exception;

public class WebhookValidationException extends RuntimeException {
    public WebhookValidationException(String message) {
        super(message);
    }

    public WebhookValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
