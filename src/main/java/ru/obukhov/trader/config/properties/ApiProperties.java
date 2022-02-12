package ru.obukhov.trader.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@ConstructorBinding
@ConfigurationProperties(prefix = "ru.tinkoff.invest.openapi")
@Validated
public record ApiProperties(
        @NotNull String host,
        @NotNull String sandboxHost,
        @NotNull String streamingUrl,
        @NotNull Integer streamingParallelism
) {
}
