package ru.obukhov.trader.trading.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.web.model.BotConfig;

import java.util.List;

@Data
@Builder
public class BackTestResult {

    /**
     * config of bot for which back test was ran
     */
    private BotConfig botConfig;

    /**
     * back test interval
     */
    private Interval interval;

    private Balances balances;

    private Profits profits;

    /**
     * positions after back test
     */
    @JsonIgnore
    private List<BackTestPosition> positions;

    /**
     * operations made during back test
     */
    @JsonIgnore
    private List<BackTestOperation> operations;

    /**
     * all candles in back test {@code interval}
     */
    @JsonIgnore
    private List<Candle> candles;

    private String error;
}