package com.example.urlshortener.service;

import com.example.urlshortener.entity.ShortUrl;
import com.example.urlshortener.repository.ShortUrlRepository;
import com.example.urlshortener.entity.UrlAccess;
import com.example.urlshortener.repository.UrlAccessRepository;
import java.util.List;
import java.net.URI;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ShortUrlService {
    private final ShortUrlRepository repository;
    private final UrlAccessRepository accessRepository;

    public ShortUrlService(ShortUrlRepository repository, UrlAccessRepository accessRepository) {
        this.repository = repository;
        this.accessRepository = accessRepository;
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
        validateOriginalUrl(originalUrl);
        if (repository.findByShortCode(shortCode).isPresent()) {
            throw new DuplicateShortCodeException(shortCode);
        }
        return repository.save(new ShortUrl(shortCode, originalUrl));
    }

    @Transactional
    public ShortUrl resolveAndRecordAccess(String shortCode) {
        ShortUrl shortUrl = findByCode(shortCode);
        accessRepository.save(new UrlAccess(shortCode));
        return shortUrl;
    }

    public long accessCount(String shortCode) {
        findByCode(shortCode);
        return accessRepository.countByShortCode(shortCode);
    }

    private void validateOriginalUrl(String originalUrl) {
        try {
            URI uri = URI.create(originalUrl);
            if (!uri.isAbsolute() || uri.getHost() == null
                    || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
                throw new InvalidOriginalUrlException();
            }
        } catch (IllegalArgumentException exception) {
            throw new InvalidOriginalUrlException();
        }
    }
}
