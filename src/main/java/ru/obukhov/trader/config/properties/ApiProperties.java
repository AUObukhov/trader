package ru.obukhov.trader.config.properties;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "ru.tinkoff.invest.openapi")
@Validated
public record ApiProperties(
        @NotNull(message = "host is mandatory") String host,
        @Nullable Integer port,
        Long throttlingInterval
) {

    @ConstructorBinding
    public ApiProperties(final String host, final Integer port, final Long throttlingInterval) {
        this.host = host;
        this.port = port;
        this.throttlingInterval = ObjectUtils.defaultIfNull(throttlingInterval, 60000L);
    }

}