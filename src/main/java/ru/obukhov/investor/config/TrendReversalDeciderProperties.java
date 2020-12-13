package ru.obukhov.investor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "trend-reversal-decider")
public class TrendReversalDeciderProperties {

    private Integer lastPricesCount;

    private Integer extremumPriceIndex;

}