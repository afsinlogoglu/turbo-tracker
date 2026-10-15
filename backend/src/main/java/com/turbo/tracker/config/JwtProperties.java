package com.turbo.tracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "turbo.jwt")
public record JwtProperties(String secret, long ttlHours) {
}
