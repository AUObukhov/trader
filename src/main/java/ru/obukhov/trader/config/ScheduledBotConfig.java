package ru.obukhov.trader.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "scheduled-bot")
public class ScheduledBotConfig {

    @Getter
    @Setter
    private boolean enabled;

    private Set<String> tickers;

    public synchronized Set<String> getTickers() {
        return new HashSet<>(tickers);
    }

    public synchronized void setTickers(final Collection<String> tickers) {
        this.tickers = new HashSet<>(tickers);
    }

}