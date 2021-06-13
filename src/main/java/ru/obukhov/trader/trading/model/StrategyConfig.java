package ru.obukhov.trader.trading.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@ConfigurationProperties("strategy-config")
public class StrategyConfig {

    @NotNull(message = "type in StrategyConfig is mandatory")
    private StrategyType type;

    @Min(0)
    private float minimumProfit;

    private Map<String, Object> params;

    public StrategyConfig(final StrategyType strategyType, final Float minimumProfit) {
        this.type = strategyType;
        this.minimumProfit = minimumProfit;
    }

}
