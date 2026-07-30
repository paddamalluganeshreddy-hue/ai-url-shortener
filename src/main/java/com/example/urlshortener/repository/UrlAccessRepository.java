package com.example.urlshortener.repository;

import com.example.urlshortener.entity.UrlAccess;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlAccessRepository extends JpaRepository<UrlAccess, Long> {
    long countByShortCode(String shortCode);
}
