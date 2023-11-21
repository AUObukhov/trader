package ru.obukhov.trader.trading.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.models.Position;

import java.util.List;
import java.util.Map;

public record BackTestResult(
        BotConfig botConfig, // config of bot for which back test was ran
        Interval interval, // back test interval
        Map<String, Balances> balances,
        Map<String, Profits> profits,
        @JsonIgnore List<Position> positions, // positions after back test
        @JsonIgnore List<Operation> operations, // operations made during back test
        @JsonIgnore List<Candle> candles, // all candles in back test interval
        String error
) {
}