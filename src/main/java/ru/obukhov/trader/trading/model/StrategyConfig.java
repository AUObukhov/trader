package ru.obukhov.trader.trading.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@ConfigurationProperties("strategy-config")
public class StrategyConfig {

    @NotNull(message = "candleResolution in StrategyConfig is mandatory")
    private CandleResolution candleResolution;

    @NotNull(message = "strategyType in StrategyConfig is mandatory")
    private StrategyType strategyType;

    private Map<String, Object> strategyParams;

    public StrategyConfig(final CandleResolution candleResolution, final StrategyType strategyType) {
        this.candleResolution = candleResolution;
        this.strategyType = strategyType;
    }

}
