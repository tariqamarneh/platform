package com.businessagent.channel.exception;

import lombok.Getter;

@Getter
public class ProviderApiException extends RuntimeException {
    private final int statusCode;

    public ProviderApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }
}
