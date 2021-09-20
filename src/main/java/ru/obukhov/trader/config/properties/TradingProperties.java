package ru.obukhov.trader.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.ChronoUnit;

@Data
@ConstructorBinding
@ConfigurationProperties(prefix = "trading")
@Validated
public class TradingProperties {

    private final boolean sandbox;

    @NotBlank
    private final String token;

    @NotNull
    private final OffsetTime workStartTime;

    @NotNull
    private final Duration workDuration;

    @NotNull
    private final Integer consecutiveEmptyDaysLimit;

    @NotNull
    private final OffsetDateTime startDate;

    public TradingProperties(
            final boolean sandbox,
            final String token,
            final OffsetTime workStartTime,
            final @DurationUnit(ChronoUnit.MINUTES) Duration workDuration,
            final Integer consecutiveEmptyDaysLimit,
            final OffsetDateTime startDate
    ) {
        this.sandbox = sandbox;
        this.token = token;
        this.workStartTime = workStartTime;
        this.workDuration = workDuration;
        this.consecutiveEmptyDaysLimit = consecutiveEmptyDaysLimit;
        this.startDate = startDate;
    }

}