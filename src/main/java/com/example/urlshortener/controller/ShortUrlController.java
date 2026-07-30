package com.example.urlshortener.controller;

import com.example.urlshortener.entity.ShortUrl;
import com.example.urlshortener.service.DuplicateShortCodeException;
import com.example.urlshortener.service.ShortUrlNotFoundException;
import com.example.urlshortener.service.ShortUrlService;
import com.example.urlshortener.service.InvalidOriginalUrlException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/urls")
public class ShortUrlController {
    private final ShortUrlService service;
    public ShortUrlController(ShortUrlService service) { this.service = service; }

    @GetMapping
    public List<ShortUrlResponse> findAll() {
        return service.findAll().stream().map(ShortUrlResponse::from).toList();
    }

    @GetMapping("/{shortCode}")
    public ShortUrlResponse findByCode(@PathVariable String shortCode) {
        return ShortUrlResponse.from(service.findByCode(shortCode));
    }

    @GetMapping("/{shortCode}/analytics")
    public AnalyticsResponse analytics(@PathVariable String shortCode) {
        return new AnalyticsResponse(shortCode, service.accessCount(shortCode));
    }

    @PostMapping
    public ResponseEntity<ShortUrlResponse> create(@Valid @RequestBody CreateShortUrlRequest request) {
        ShortUrl created = service.create(request.shortCode(), request.originalUrl());
        return ResponseEntity.created(URI.create("/api/urls/" + created.getShortCode()))
                .body(ShortUrlResponse.from(created));
    }

    @ExceptionHandler(ShortUrlNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ShortUrlNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(exception.getMessage()));
    }

    @ExceptionHandler(DuplicateShortCodeException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateShortCodeException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(exception.getMessage()));
    }

    @ExceptionHandler(InvalidOriginalUrlException.class)
    public ResponseEntity<ApiError> handleInvalidUrl(InvalidOriginalUrlException exception) {
        return ResponseEntity.badRequest().body(new ApiError(exception.getMessage()));
    }

    public record CreateShortUrlRequest(
        @NotBlank @Size(max = 32) @Pattern(regexp = "[A-Za-z0-9_-]+") String shortCode,
        @NotBlank @Size(max = 2048) String originalUrl) { }
    public record ShortUrlResponse(Long id, String shortCode, String originalUrl) {
        static ShortUrlResponse from(ShortUrl item) { return new ShortUrlResponse(item.getId(), item.getShortCode(), item.getOriginalUrl()); }
    }
    public record ApiError(String message) { }
    public record AnalyticsResponse(String shortCode, long redirectCount) { }
}
