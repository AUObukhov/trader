package ru.obukhov.trader.config.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "scheduling")
public class SchedulingProperties {

    private final Duration delay;

    @Setter
    private boolean enabled;

}