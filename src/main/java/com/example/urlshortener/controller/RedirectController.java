package com.example.urlshortener.controller;

import com.example.urlshortener.entity.ShortUrl;
import com.example.urlshortener.service.ShortUrlService;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Redirects", description = "Public URL redirection")
public class RedirectController {
    private final ShortUrlService service;
    public RedirectController(ShortUrlService service) { this.service = service; }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirect a short URL and record an access event")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        ShortUrl url = service.resolveAndRecordAccess(shortCode);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url.getOriginalUrl())).build();
    }
}
