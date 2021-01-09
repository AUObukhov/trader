package ru.obukhov.investor.bot.model;

import lombok.Data;
import ru.obukhov.investor.model.Candle;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.operations.Operation;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DecisionData {

    private BigDecimal balance;
    private Portfolio.PortfolioPosition position;
    private List<Candle> currentCandles;
    private List<Operation> lastOperations;
    private Instrument instrument;

}