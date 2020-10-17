package ru.obukhov.investor.bot.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.bot.interfaces.DataSupplier;
import ru.obukhov.investor.bot.interfaces.MarketMock;
import ru.obukhov.investor.bot.model.DecisionData;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.service.interfaces.OperationsService;
import ru.tinkoff.invest.openapi.models.operations.Operation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Class for getting mocked portfolio and market situation data for bot. Market data is actually real historical data
 */
@Service
@RequiredArgsConstructor
public class HistoricalDataSupplier implements DataSupplier {

    private final MarketService marketService;
    private final OperationsService operationsService;
    private final MarketMock marketMock;

    @Override
    public DecisionData getData(String ticker) {

        return DecisionData.builder()
                .balance(marketMock.getBalance())
                .position(marketMock.getPosition(ticker))
                .currentPrice(getCurrentPrice(ticker))
                .lastOperations(getLastWeekOperations(ticker))
                .instrument(marketService.getInstrument(ticker))
                .build();

    }

    private BigDecimal getCurrentPrice(String ticker) {
        return marketService.getLastCandle(ticker, marketMock.getCurrentDateTime()).getClosePrice();
    }

    private List<Operation> getLastWeekOperations(String ticker) {
        OffsetDateTime to = OffsetDateTime.now();
        OffsetDateTime from = to.minusWeeks(1);
        return operationsService.getOperations(from, to, ticker);
    }

}