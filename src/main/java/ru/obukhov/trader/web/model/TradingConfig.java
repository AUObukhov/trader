package ru.obukhov.trader.web.model;

import io.swagger.annotations.ApiModelProperty;
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
public class TradingConfig {

    @NotNull(message = "candleResolution is mandatory")
    @ApiModelProperty(
            value = "Candle interval",
            example = "1min",
            required = true,
            position = 1
    )
    private CandleResolution candleResolution;

    @NotNull(message = "strategyType is mandatory")
    @ApiModelProperty(
            value = "Trading strategy type",
            example = "goldenCross",
            required = true,
            position = 2
    )
    private StrategyType strategyType;

    @ApiModelProperty(
            value = "Map of trading strategy params. Required keys name and values types depend on strategyType",
            example = "{\"minimumProfit\": 0.01, \"movingAverageType\": \"LWMA\", \"order\": 1, \"indexCoefficient\": 0.3, \"greedy\": false, \"smallWindow\": 100, \"bigWindow\": 200}",
            dataType = "java.util.Map",
            position = 3
    )
    private Map<String, Object> strategyParams;

    public TradingConfig(final CandleResolution candleResolution, final StrategyType strategyType) {
        this.candleResolution = candleResolution;
        this.strategyType = strategyType;
        this.strategyParams = Map.of();
    }

    @Override
    public String toString() {
        return "[" +
                "candleResolution=" + candleResolution +
                ", strategyType=" + strategyType +
                ", strategyParams=" + strategyParams +
                ']';
    }
}
