package ru.obukhov.investor.web.model;

import lombok.Builder;
import lombok.Data;
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

    private List<SimulatedPosition> positions;

    private List<SimulatedOperation> operations;

}