package ru.obukhov.trader.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "scheduling")
public class SchedulingProperties {

    private boolean enabled;

}