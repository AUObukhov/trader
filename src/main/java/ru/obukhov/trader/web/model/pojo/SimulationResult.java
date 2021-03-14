package ru.obukhov.trader.web.model.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Candle;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class SimulationResult {

    /**
     * name of bot for which simulation was ran
     */
    private String botName;

    /**
     * simulation interval
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
     * currency balance + costs of all position after simulation
     */
    private BigDecimal finalTotalBalance;

    /**
     * currency balance after simulation
     */
    private BigDecimal finalBalance;

    /**
     * weighted average value of all investments where weights are time of corresponding investment was last investment
     */
    private BigDecimal weightedAverageInvestment;

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
     * positions after simulation
     */
    @JsonIgnore
    private List<SimulatedPosition> positions;

    /**
     * operations made during simulation
     */
    @JsonIgnore
    private List<SimulatedOperation> operations;

    /**
     * all candles in simulation {@code interval}
     */
    @JsonIgnore
    private List<Candle> candles;

    private String error;
}