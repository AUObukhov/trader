package ru.obukhov.trader.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collection;
import java.util.Set;

@ConfigurationProperties(prefix = "scheduled-bot")
public class ScheduledBotConfig extends BotConfig {

    @Getter
    @Setter
    private boolean enabled;

    @SuppressWarnings("java:S3077") // Non-primitive fields should not be "volatile"
    private volatile Set<String> tickers; // set is immutable, so sonar is wrong

    public Set<String> getTickers() {
        return Set.copyOf(tickers);
    }

    public void setTickers(final Collection<String> tickers) {
        this.tickers = Set.copyOf(tickers);
    }

}