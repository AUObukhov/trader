package ru.obukhov.trader.config.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@AllArgsConstructor
@ConfigurationProperties(prefix = "scheduling")
public class SchedulingProperties {

    private final int delay;

    @Setter
    private boolean enabled;

}