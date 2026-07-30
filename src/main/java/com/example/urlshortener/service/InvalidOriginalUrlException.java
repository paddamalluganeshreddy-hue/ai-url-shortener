package com.example.urlshortener.service;

public class InvalidOriginalUrlException extends RuntimeException {
    public InvalidOriginalUrlException() { super("originalUrl must be an absolute HTTP or HTTPS URL"); }
}
