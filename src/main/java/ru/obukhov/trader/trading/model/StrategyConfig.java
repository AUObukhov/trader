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

    @NotNull(message = "type in StrategyConfig is mandatory")
    private StrategyType type;

    @NotNull(message = "candleResolution in StrategyConfig is mandatory")
    private CandleResolution candleResolution;

    private Map<String, Object> params;

    public StrategyConfig(final StrategyType strategyType, CandleResolution candleResolution) {
        this.type = strategyType;
        this.candleResolution = candleResolution;
    }

}
