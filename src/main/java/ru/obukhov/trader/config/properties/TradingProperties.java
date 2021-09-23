package ru.obukhov.trader.config.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@ConstructorBinding
@ConfigurationProperties(prefix = "trading")
@Validated
@AllArgsConstructor
public class TradingProperties {

    private final boolean sandbox;

    @NotBlank(message = "token is mandatory")
    private final String token;

}