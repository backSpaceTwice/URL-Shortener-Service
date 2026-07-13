package com.quan.url_shorten_service.exception;

public class ExpiredShortUrlException extends RuntimeException {

    public ExpiredShortUrlException(String message) {
        super(message);
    }
}
