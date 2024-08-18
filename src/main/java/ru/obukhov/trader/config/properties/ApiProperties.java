package ru.obukhov.trader.config.properties;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "ru.tinkoff.invest.openapi")
@Validated
public record ApiProperties(Long throttlingInterval) {

    @ConstructorBinding
    public ApiProperties(final Long throttlingInterval) {
        this.throttlingInterval = ObjectUtils.defaultIfNull(throttlingInterval, 60000L);
    }

}