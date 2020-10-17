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

/**
 * Class for getting real portfolio and current market situation data for bot
 */
@Service("realDataSupplier")
@RequiredArgsConstructor
public class RealDataSupplier implements DataSupplier {

    private final MarketService marketService;
    private final PortfolioService portfolioService;
    private final OperationsService operationsService;

    @Override
    public DecisionData getData(String ticker) {

        return DecisionData.builder()
                .balance(portfolioService.getAvailableBalance(Currency.RUB))
                .position(portfolioService.getPosition(ticker))
                .currentPrice(getCurrentPrice(ticker))
                .lastOperations(getLastWeekOperations(ticker))
                .instrument(marketService.getInstrument(ticker))
                .build();

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