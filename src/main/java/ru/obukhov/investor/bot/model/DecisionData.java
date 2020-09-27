package ru.obukhov.investor.bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.tinkoff.invest.openapi.models.operations.Operation;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class DecisionData {

    private BigDecimal balance;
    private Portfolio.PortfolioPosition position;
    private BigDecimal currentPrice;
    private List<Operation> lastOperations;

}