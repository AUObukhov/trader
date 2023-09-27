package ru.obukhov.trader.web.model;

import jakarta.validation.constraints.NotNull;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.math.BigDecimal;
import java.util.Map;

public record BotConfig(
        @NotNull(message = "accountId is mandatory") String accountId,
        @NotNull(message = "figi is mandatory") String figi,
        @NotNull(message = "candleInterval is mandatory") CandleInterval candleInterval,
        @NotNull(message = "commission is mandatory") BigDecimal commission,
        @NotNull(message = "strategyType is mandatory") StrategyType strategyType,
        Map<String, Object> strategyParams
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