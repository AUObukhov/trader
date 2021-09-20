package ru.obukhov.trader.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
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

    @Nullable
    @ApiModelProperty(value = "Account id", position = 1, example = "2000124699")
    private String brokerAccountId;

    @NotNull(message = "ticker is mandatory")
    @ApiModelProperty(value = "Ticker", position = 2, example = "FXIT")
    private String ticker;

    @NotNull(message = "candleResolution is mandatory")
    @ApiModelProperty(value = "Candle interval", required = true, position = 3, example = "1min")
    private CandleResolution candleResolution;

    @NotNull(message = "commission is mandatory")
    @ApiModelProperty(value = "Operating commission", required = true, position = 4, example = "0.003")
    private Double commission = 0.0;

    @NotNull(message = "strategyType is mandatory")
    @ApiModelProperty(value = "Trading strategy type", required = true, position = 5, example = "cross")
    private StrategyType strategyType;

    @ApiModelProperty(
            value = "Map of trading strategy params. Required keys name and values types depend on strategyType",
            dataType = "java.util.Map",
            position = 6,
            example = "{\"minimumProfit\": 0.01, \"movingAverageType\": \"LWMA\", \"order\": 1, \"indexCoefficient\": 0.3, \"greedy\": false, " +
                    "\"smallWindow\": 100, \"bigWindow\": 200}"
    )
    private Map<String, Object> strategyParams;

    public TradingConfig(
            @Nullable final String brokerAccountId,
            final String ticker,
            final CandleResolution candleResolution,
            final Double commission,
            final StrategyType strategyType
    ) {
        this.ticker = ticker;
        this.brokerAccountId = brokerAccountId;
        this.candleResolution = candleResolution;
        this.commission = commission;
        this.strategyType = strategyType;
        this.strategyParams = Map.of();
    }

    @Override
    public String toString() {
        return "[" +
                "brokerAccountId=" + brokerAccountId +
                ", ticker=" + ticker +
                ", candleResolution=" + candleResolution +
                ", commission=" + commission +
                ", strategyType=" + strategyType +
                ", strategyParams=" + strategyParams +
                ']';
    }
}
