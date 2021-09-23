package ru.obukhov.trader.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import ru.obukhov.trader.web.model.BotConfig;

import javax.validation.constraints.NotNull;

@Validated
@Getter
@Setter
@ConfigurationProperties(prefix = "scheduled-bot")
public class ScheduledBotProperties {

    private boolean enabled;

    @NotNull(message = "botConfig is mandatory")
    private BotConfig botConfig;

}