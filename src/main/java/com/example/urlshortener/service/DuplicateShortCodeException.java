package com.example.urlshortener.service;

public class DuplicateShortCodeException extends RuntimeException {
    public DuplicateShortCodeException(String shortCode) {
        super("Short code is already in use: " + shortCode);
    }
}
