package ru.obukhov.trader.web.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.properties.ConstructorBinding;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Getter
@EqualsAndHashCode
@ConstructorBinding
@Builder
public class BotConfig {

    @Nullable
    @ApiModelProperty(value = "Account id", position = 1, example = "2000124699")
    private final String brokerAccountId;

    @NotNull(message = "ticker is mandatory")
    @ApiModelProperty(value = "Ticker", position = 2, example = "FXIT")
    private final String ticker;

    @NotNull(message = "candleResolution is mandatory")
    @ApiModelProperty(value = "Candle interval", required = true, position = 3, example = "1min")
    private final CandleResolution candleResolution;

    @NotNull(message = "commission is mandatory")
    @ApiModelProperty(value = "Operating commission", required = true, position = 4, example = "0.003")
    private final Double commission;

    @NotNull(message = "strategyType is mandatory")
    @ApiModelProperty(value = "Trading strategy type", required = true, position = 5, example = "cross")
    private final StrategyType strategyType;

    @ApiModelProperty(
            value = "Map of trading strategy params. Required keys name and values types depend on strategyType",
            dataType = "java.util.Map",
            position = 6,
            example = "{\"minimumProfit\": 0.01, \"movingAverageType\": \"LWMA\", \"order\": 1, \"indexCoefficient\": 0.3, \"greedy\": false, " +
                    "\"smallWindow\": 100, \"bigWindow\": 200}"
    )
    private final Map<String, Object> strategyParams;

    public BotConfig(
            @Nullable final String brokerAccountId,
            final String ticker,
            final CandleResolution candleResolution,
            final Double commission,
            final StrategyType strategyType,
            final Map<String, Object> strategyParams
    ) {
        this.brokerAccountId = brokerAccountId;
        this.ticker = ticker;
        this.candleResolution = candleResolution;
        this.commission = commission;
        this.strategyType = strategyType;
        this.strategyParams = strategyParams;
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