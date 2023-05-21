package ru.obukhov.trader.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties(prefix = "trading")
@Validated
@AllArgsConstructor
public class TradingProperties {

    @NotBlank(message = "token is mandatory")
    private final String token;

}