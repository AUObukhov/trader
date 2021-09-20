package ru.obukhov.trader.config.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@ConstructorBinding
@AllArgsConstructor
@ConfigurationProperties(prefix = "back-test")
@Validated
public class BackTestProperties {

    @Getter
    @NotNull(message = "threadCount is mandatory")
    @Min(value = 1, message = "threadCount must be positive")
    private final Integer threadCount;

}