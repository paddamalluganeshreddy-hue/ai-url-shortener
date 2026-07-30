package com.example.urlshortener.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.urlshortener.entity.ShortUrl;
import com.example.urlshortener.repository.ShortUrlRepository;
import com.example.urlshortener.repository.UrlAccessRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShortUrlServiceTests {
    @Mock ShortUrlRepository urls;
    @Mock UrlAccessRepository accesses;
    @InjectMocks ShortUrlService service;

    @Test
    void resolvesUrlAndPersistsAccessEvent() {
        ShortUrl url = new ShortUrl("docs", "https://spring.io");
        when(urls.findByShortCode("docs")).thenReturn(Optional.of(url));
        when(accesses.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.resolveAndRecordAccess("docs")).isSameAs(url);

        verify(accesses).save(any());
    }
}
