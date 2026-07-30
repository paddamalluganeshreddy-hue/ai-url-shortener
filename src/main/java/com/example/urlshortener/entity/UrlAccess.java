package com.example.urlshortener.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "url_accesses", indexes = @Index(name = "idx_access_code_time", columnList = "shortCode,accessedAt"))
public class UrlAccess {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String shortCode;
    private Instant accessedAt;

    protected UrlAccess() { }
    public UrlAccess(String shortCode) { this.shortCode = shortCode; this.accessedAt = Instant.now(); }
    public Long getId() { return id; }
    public String getShortCode() { return shortCode; }
    public Instant getAccessedAt() { return accessedAt; }
}
