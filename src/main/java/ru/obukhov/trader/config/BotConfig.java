package ru.obukhov.trader.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class BotConfig {

    @NotNull(message = "candleResolution is mandatory")
    private CandleResolution candleResolution;

    @NotNull(message = "strategyType is mandatory")
    private StrategyType strategyType;

    private Map<String, Object> strategyParams;

    public BotConfig(final CandleResolution candleResolution, final StrategyType strategyType) {
        this.candleResolution = candleResolution;
        this.strategyType = strategyType;
        this.strategyParams = Map.of();
    }

}
