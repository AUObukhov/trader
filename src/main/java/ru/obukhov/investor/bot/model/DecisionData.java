package ru.obukhov.investor.bot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.operations.Operation;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class DecisionData {

    private BigDecimal balance;
    private Portfolio.PortfolioPosition position;
    private List<BigDecimal> currentPrices;
    private List<Operation> lastOperations;
    private Instrument instrument;

}