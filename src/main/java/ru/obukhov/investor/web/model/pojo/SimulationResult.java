package ru.obukhov.investor.web.model.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import ru.obukhov.investor.common.model.Interval;
import ru.obukhov.investor.market.model.Candle;

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

    private String error;
}