package com.spring.Live.Vehicle.Map.Delhi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    @Bean
    @Primary
    public WebClient webClient() {

        final int bufferSize = 16 * 1024 * 1024;

        log.info("Creating WebClient bean with a buffer size of {} MB", bufferSize / (1024 * 1024));

        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(bufferSize))
                .build();

        return WebClient.builder()
                .exchangeStrategies(strategies)
                .build();
    }
}

