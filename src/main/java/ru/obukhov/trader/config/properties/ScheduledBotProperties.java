package ru.obukhov.trader.config.properties;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.obukhov.trader.web.model.TradingConfig;

import java.util.Collection;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "scheduled-bot")
public class ScheduledBotProperties extends TradingConfig {

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