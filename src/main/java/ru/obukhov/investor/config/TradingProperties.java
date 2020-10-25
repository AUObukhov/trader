package ru.obukhov.investor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.OffsetTime;
import java.time.temporal.ChronoUnit;

@Data
@Component
@ConfigurationProperties(prefix = "trading")
public class TradingProperties {

    @NotBlank
    private String token;

    private double commission;

    @NotNull
    private OffsetTime workStartTime;

    @NotNull
    @DurationUnit(ChronoUnit.MINUTES)
    private Duration workDuration;

}