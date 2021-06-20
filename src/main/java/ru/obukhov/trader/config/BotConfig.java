package ru.obukhov.trader.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Accessors(chain = true)
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
