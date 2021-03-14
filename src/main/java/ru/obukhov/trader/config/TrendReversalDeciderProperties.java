package ru.obukhov.trader.config;

import com.google.common.collect.ImmutableSet;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Set;

@Data
@ConstructorBinding
@ConfigurationProperties(prefix = "trend-reversal-decider")
public class TrendReversalDeciderProperties {

    @Getter
    private final Set<DeciderConfig> configs;

    public TrendReversalDeciderProperties(Set<DeciderConfig> configs) {
        this.configs = ImmutableSet.copyOf(configs);
    }

    @Data
    public static class DeciderConfig {
        private final Integer lastPricesCount;
        private final Integer extremumPriceIndex;
    }

}