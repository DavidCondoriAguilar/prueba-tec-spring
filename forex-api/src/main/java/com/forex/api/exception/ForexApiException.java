package com.forex.api.exception;

public class ForexApiException extends RuntimeException {
    
    private final int statusCode;
    
    public ForexApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public ForexApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}
