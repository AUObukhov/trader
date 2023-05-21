package ru.obukhov.trader.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@AllArgsConstructor
@ConfigurationProperties(prefix = "back-test")
@Validated
public class BackTestProperties {

    @Getter
    @NotNull(message = "threadCount is mandatory")
    @Min(value = 1, message = "threadCount must be positive")
    private final Integer threadCount;

}