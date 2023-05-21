package ru.obukhov.trader.config.properties;

import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "ru.tinkoff.invest.openapi")
@Validated
public record ApiProperties(
        @NotNull(message = "host is mandatory") String host,
        @Nullable Integer port
) {
}