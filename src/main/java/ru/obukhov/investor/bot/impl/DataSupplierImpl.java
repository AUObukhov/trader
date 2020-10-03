package ru.obukhov.investor.bot.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.bot.interfaces.DataSupplier;
import ru.obukhov.investor.bot.model.DecisionData;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.service.interfaces.OperationsService;
import ru.obukhov.investor.service.interfaces.PortfolioService;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.operations.Operation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataSupplierImpl implements DataSupplier {

    private final MarketService marketService;
    private final PortfolioService portfolioService;
    private final OperationsService operationsService;

    @Override
    public DecisionData getData(String ticker) {
        return new DecisionData(portfolioService.getAvailableBalance(Currency.RUB),
                portfolioService.getPosition(ticker),
                getCurrentPrice(ticker),
                getLastWeekOperations(ticker),
                marketService.getInstrument(ticker));
    }

    private BigDecimal getCurrentPrice(String ticker) {
        return marketService.getLastCandle(ticker).getClosePrice();
    }

    private List<Operation> getLastWeekOperations(String ticker) {
        OffsetDateTime to = OffsetDateTime.now();
        OffsetDateTime from = to.minusWeeks(1);
        return operationsService.getOperations(from, to, ticker);
    }

}