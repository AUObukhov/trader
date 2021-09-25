package ru.obukhov.trader.trading.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.web.model.BotConfig;

import java.math.BigDecimal;
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

    /**
     * initial investment
     */
    private BigDecimal initialBalance;

    /**
     * sum of all investments
     */
    private BigDecimal totalInvestment;

    /**
     * weighted average value of all investments where weight is time of corresponding investment being last investment
     */
    private BigDecimal weightedAverageInvestment;

    /**
     * currency balance + costs of all position after back test
     */
    private BigDecimal finalTotalBalance;

    /**
     * currency balance after back test
     */
    private BigDecimal finalBalance;

    /**
     * {@code finalBalance} - {@code totalInvestment}
     */
    private BigDecimal absoluteProfit;

    /**
     * relation of {@code absoluteProfit} to {@code weightedAverageInvestment}
     */
    private Double relativeProfit;

    /**
     * average profit per annum
     */
    private Double relativeYearProfit;

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