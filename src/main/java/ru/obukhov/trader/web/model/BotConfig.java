package ru.obukhov.trader.web.model;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.math.BigDecimal;
import java.util.Map;

public record BotConfig(
        @NotNull(message = "accountId is mandatory")
        @ApiModelProperty(value = "Account id", position = 1, example = "2000124699")
        String accountId,

        @NotNull(message = "figi is mandatory")
        @ApiModelProperty(value = "Financial Instrument Global Identifier", position = 2, example = "BBG000B9XRY4")
        String figi,

        @NotNull(message = "candleInterval is mandatory")
        @ApiModelProperty(value = "Candle interval", required = true, position = 3, example = "CANDLE_INTERVAL_1_MIN")
        CandleInterval candleInterval,

        @NotNull(message = "commission is mandatory")
        @ApiModelProperty(value = "Operating commission", required = true, position = 4, example = "0.003")
        BigDecimal commission,

        @NotNull(message = "strategyType is mandatory")
        @ApiModelProperty(value = "Trading strategy type", required = true, position = 5, example = "cross")
        StrategyType strategyType,

        @ApiModelProperty(
                value = "Map of trading strategy params. Required keys name and values types depend on strategyType",
                dataType = "java.util.Map",
                position = 6,
                example = "{\"minimumProfit\": 0.01, \"movingAverageType\": \"LWMA\", \"order\": 1, \"indexCoefficient\": 0.3, \"greedy\": false, " +
                        "\"smallWindow\": 100, \"bigWindow\": 200}"
        ) Map<String, Object> strategyParams
) {
    @Override
    public String toString() {
        return "BotConfig{" +
                "accountId=" + accountId +
                ", figi=" + figi +
                ", candleInterval=" + candleInterval +
                ", commission=" + commission +
                ", strategyType=" + strategyType +
                ", strategyParams=" + strategyParams +
                '}';
    }

}