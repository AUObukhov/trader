package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.etf.TestEtf1;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationState;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.models.Portfolio;

import java.time.OffsetDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class RealExtOperationsServiceUnitTest {

    @Mock
    private ExtInstrumentsService extInstrumentsService;
    @Mock
    private OperationsService operationsService;


    @InjectMocks
    private RealExtOperationsService extOperationsService;

    @Test
    void getOperations() {
        final String accountId = TestData.ACCOUNT_ID1;

        final String figi = TestShare1.FIGI;
        final String ticker = TestShare1.TICKER;

        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 8, 10, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 8, 10, 19);

        final List<Operation> operations = List.of(
                TestData.createOperation(OperationState.OPERATION_STATE_EXECUTED),
                TestData.createOperation(OperationState.OPERATION_STATE_UNSPECIFIED)
        );

        Mockito.when(extInstrumentsService.getFigiByTicker(ticker)).thenReturn(figi);
        Mockito.when(operationsService.getAllOperationsSync(accountId, from.toInstant(), to.toInstant(), figi))
                .thenReturn(operations);

        final List<Operation> result = extOperationsService.getOperations(accountId, Interval.of(from, to), ticker);

        Assertions.assertEquals(result, operations);
    }

    @Test
    void getPositions() {
        // arrange

        final String accountId = TestData.ACCOUNT_ID1;

        final String ticker1 = TestShare1.TICKER;
        final String figi1 = TestShare1.FIGI;
        final InstrumentType instrumentType1 = InstrumentType.STOCK;
        final int quantity1 = 10;
        final int averagePositionPrice1 = 15;
        final int expectedYield1 = 50;
        final int currentPrice1 = 20;
        final int quantityLots1 = 1;
        final Currency currency1 = TestShare1.CURRENCY;

        final String ticker2 = TestShare2.TICKER;
        final String figi2 = TestShare2.FIGI;
        final InstrumentType instrumentType2 = InstrumentType.STOCK;
        final int quantity2 = 20;
        final int averagePositionPrice2 = 1;
        final int expectedYield2 = 60;
        final int currentPrice2 = 4;
        final int quantityLots2 = 2;
        final Currency currency2 = TestShare2.CURRENCY;

        final String ticker3 = TestEtf1.TICKER;
        final String figi3 = TestEtf1.FIGI;
        final InstrumentType instrumentType3 = InstrumentType.ETF;
        final int quantity3 = 5;
        final int averagePositionPrice3 = 15;
        final int expectedYield3 = -25;
        final int currentPrice3 = 10;
        final int quantityLots3 = 5;
        final Currency currency3 = TestEtf1.CURRENCY;

        final ru.tinkoff.piapi.contract.v1.PortfolioPosition tinkoffPosition1 = TestData.createTinkoffPortfolioPosition(
                figi1,
                instrumentType1,
                quantity1,
                averagePositionPrice1,
                expectedYield1,
                currentPrice1,
                quantityLots1,
                currency1
        );
        final ru.tinkoff.piapi.contract.v1.PortfolioPosition tinkoffPosition2 = TestData.createTinkoffPortfolioPosition(
                figi2,
                instrumentType2,
                quantity2,
                averagePositionPrice2,
                expectedYield2,
                currentPrice2,
                quantityLots2,
                currency2
        );
        final ru.tinkoff.piapi.contract.v1.PortfolioPosition tinkoffPosition3 = TestData.createTinkoffPortfolioPosition(
                figi3,
                instrumentType3,
                quantity3,
                averagePositionPrice3,
                expectedYield3,
                currentPrice3,
                quantityLots3,
                currency3
        );

        final Portfolio portfolio = TestData.createPortfolio(tinkoffPosition1, tinkoffPosition2, tinkoffPosition3);
        Mockito.when(operationsService.getPortfolioSync(accountId)).thenReturn(portfolio);

        Mockito.when(extInstrumentsService.getTickerByFigi(figi1)).thenReturn(ticker1);
        Mockito.when(extInstrumentsService.getTickerByFigi(figi2)).thenReturn(ticker2);
        Mockito.when(extInstrumentsService.getTickerByFigi(figi3)).thenReturn(ticker3);

        // action

        final List<PortfolioPosition> positions = extOperationsService.getPositions(accountId);

        // assert

        final PortfolioPosition expectedPosition1 = TestData.createPortfolioPosition(
                ticker1,
                instrumentType1,
                quantity1,
                averagePositionPrice1,
                expectedYield1,
                currentPrice1,
                quantityLots1,
                currency1
        );
        final PortfolioPosition expectedPosition2 = TestData.createPortfolioPosition(
                ticker2,
                instrumentType2,
                quantity2,
                averagePositionPrice2,
                expectedYield2,
                currentPrice2,
                quantityLots2,
                currency2
        );
        final PortfolioPosition expectedPosition3 = TestData.createPortfolioPosition(
                ticker3,
                instrumentType3,
                quantity3,
                averagePositionPrice3,
                expectedYield3,
                currentPrice3,
                quantityLots3,
                currency3
        );

        final List<PortfolioPosition> expectedPositions = List.of(expectedPosition1, expectedPosition2, expectedPosition3);

        AssertUtils.assertEquals(expectedPositions, positions);
    }

}