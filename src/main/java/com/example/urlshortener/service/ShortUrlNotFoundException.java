package com.example.urlshortener.service;

public class ShortUrlNotFoundException extends RuntimeException {
    public ShortUrlNotFoundException(String shortCode) {
        super("No short URL exists for code: " + shortCode);
    }
}
