package ru.obukhov.trader.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.ChronoUnit;

@Data
@ConstructorBinding
@ConfigurationProperties(prefix = "market")
@Validated
public class MarketProperties {

    @NotNull(message = "workStartTime is mandatory")
    private final OffsetTime workStartTime;

    @NotNull(message = "workDuration is mandatory")
    private final Duration workDuration;

    @NotNull(message = "consecutiveEmptyDaysLimit is mandatory")
    private final Integer consecutiveEmptyDaysLimit;

    @NotNull(message = "startDate is mandatory")
    private final OffsetDateTime startDate;

    public MarketProperties(
            final OffsetTime workStartTime,
            final @DurationUnit(ChronoUnit.MINUTES) Duration workDuration,
            final Integer consecutiveEmptyDaysLimit,
            final OffsetDateTime startDate
    ) {
        this.workStartTime = workStartTime;
        this.workDuration = workDuration;
        this.consecutiveEmptyDaysLimit = consecutiveEmptyDaysLimit;
        this.startDate = startDate;
    }

}