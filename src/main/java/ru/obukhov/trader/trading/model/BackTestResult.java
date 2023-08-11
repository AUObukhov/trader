package ru.obukhov.trader.trading.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.Operation;

import java.util.List;

public record BackTestResult(
        BotConfig botConfig, // config of bot for which back test was ran
        Interval interval, // back test interval
        Balances balances,
        Profits profits,
        @JsonIgnore List<BackTestPosition> positions, // positions after back test
        @JsonIgnore List<Operation> operations, // operations made during back test
        @JsonIgnore List<Candle> candles, // all candles in back test interval
        String error
) {
}