package ru.obukhov.trader.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Set;

@Data
@ConstructorBinding
@ConfigurationProperties(prefix = "trend-reversal-strategy")
public class TrendReversalStrategyProperties {

    @Getter
    private final Set<StrategyConfig> configs;

    public TrendReversalStrategyProperties(Set<StrategyConfig> configs) {
        this.configs = Set.copyOf(configs);
    }

    @Data
    public static class StrategyConfig {
        private final Integer lastPricesCount;
        private final Integer extremumPriceIndex;
    }

}