package ru.obukhov.investor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "trading")
public class TradingProperties {

    private boolean sandbox;
    private String token;

}