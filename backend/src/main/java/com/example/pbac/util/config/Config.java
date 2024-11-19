package com.example.pbac.util.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {
    @Value("${jwt.secret_key}")
    public String secret_key;

    @Value("${jwt.expiration_millis}")
    public long expiration_millis;
}
