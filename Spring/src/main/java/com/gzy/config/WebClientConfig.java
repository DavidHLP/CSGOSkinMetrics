package com.gzy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .defaultHeader("Accept-Encoding", "gzip, deflate, br")
                .defaultHeader("Cache-Control", "no-cache")
                .defaultHeader("Pragma", "no-cache")
                .build();
    }
} 