package ru.obukhov.investor.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.Interval;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class SimulationResult {

    private String botName;

    private Interval interval;

    private BigDecimal initialBalance;

    private BigDecimal totalBalance;

    private BigDecimal currencyBalance;

    private BigDecimal absoluteProfit;

    private Double relativeProfit;

    private Double relativeYearProfit;

    @JsonIgnore
    private List<SimulatedPosition> positions;

    @JsonIgnore
    private List<SimulatedOperation> operations;

    @JsonIgnore
    private List<Candle> candles;
}