package ru.obukhov.trader.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Set;

@Data
@ConstructorBinding
@ConfigurationProperties(prefix = "trend-reversal-decider")
public class TrendReversalDeciderProperties {

    private final Set<DeciderConfig> configs;

    @Data
    public static class DeciderConfig {
        private final Integer lastPricesCount;
        private final Integer extremumPriceIndex;
    }

}