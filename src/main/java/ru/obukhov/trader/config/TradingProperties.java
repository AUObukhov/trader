package ru.obukhov.trader.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.ChronoUnit;

@Data
@Component
@ConfigurationProperties(prefix = "trading")
@Validated
public class TradingProperties {

    private boolean sandbox;

    @NotBlank
    private String token;

    private double commission;

    @NotNull
    private OffsetTime workStartTime;

    @NotNull
    @DurationUnit(ChronoUnit.MINUTES)
    private Duration workDuration;

    @NotNull
    private Integer consecutiveEmptyDaysLimit;

    @NotNull
    private OffsetDateTime startDate;

}