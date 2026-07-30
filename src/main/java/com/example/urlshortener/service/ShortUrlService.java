package com.example.urlshortener.service;

import com.example.urlshortener.entity.ShortUrl;
import com.example.urlshortener.repository.ShortUrlRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ShortUrlService {
    private final ShortUrlRepository repository;

    public ShortUrlService(ShortUrlRepository repository) {
        this.repository = repository;
    }

    public List<ShortUrl> findAll() {
        return repository.findAll();
    }

    public ShortUrl findByCode(String shortCode) {
        return repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
    }

    @Transactional
    public ShortUrl create(String shortCode, String originalUrl) {
        if (repository.findByShortCode(shortCode).isPresent()) {
            throw new DuplicateShortCodeException(shortCode);
        }
        return repository.save(new ShortUrl(shortCode, originalUrl));
    }
}
