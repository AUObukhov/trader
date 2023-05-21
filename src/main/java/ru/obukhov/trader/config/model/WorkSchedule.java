package ru.obukhov.trader.config.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.OffsetTime;
import java.time.temporal.ChronoUnit;

@Data
@Validated
public class WorkSchedule {

    @NotNull(message = "startTime is mandatory")
    private final OffsetTime startTime;

    @NotNull(message = "duration is mandatory")
    @DurationMin(message = "duration must be positive in minutes", minutes = 1)
    @DurationMax(message = "duration must be less than 1 day", days = 1)
    private final Duration duration;

    @ConstructorBinding
    public WorkSchedule(final OffsetTime startTime, final @DurationUnit(ChronoUnit.MINUTES) Duration duration) {
        this.startTime = startTime;
        this.duration = duration;
    }

    public OffsetTime getEndTime() {
        return startTime.plus(duration);
    }

}