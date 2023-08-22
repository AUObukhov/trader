package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.etf.TestEtf1;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationState;
import ru.tinkoff.piapi.contract.v1.PortfolioPosition;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.models.Portfolio;
import ru.tinkoff.piapi.core.models.Position;

import java.time.OffsetDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class RealExtOperationsServiceUnitTest {

    @Mock
    private OperationsService operationsService;

    @InjectMocks
    private RealExtOperationsService extOperationsService;

    @Test
    void getOperations() {
        final String accountId = TestData.ACCOUNT_ID1;

        final String figi = TestShare1.FIGI;

        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 8, 10, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 8, 10, 19);

        final List<Operation> operations = List.of(
                TestData.createOperation(OperationState.OPERATION_STATE_EXECUTED),
                TestData.createOperation(OperationState.OPERATION_STATE_UNSPECIFIED)
        );

        Mockito.when(operationsService.getAllOperationsSync(accountId, from.toInstant(), to.toInstant(), figi))
                .thenReturn(operations);

        final List<Operation> result = extOperationsService.getOperations(accountId, Interval.of(from, to), figi);

        Assertions.assertEquals(result, operations);
    }

    @Test
    void getPositions() {
        // arrange

        final String accountId = TestData.ACCOUNT_ID1;

        final String figi1 = TestShare1.FIGI;
        final InstrumentType instrumentType1 = InstrumentType.INSTRUMENT_TYPE_SHARE;
        final int quantity1 = 10;
        final int averagePositionPrice1 = 15;
        final int expectedYield1 = 50;
        final int currentPrice1 = 20;
        final int quantityLots1 = 1;
        final String currency1 = TestShare1.CURRENCY;

        final String figi2 = TestShare2.FIGI;
        final InstrumentType instrumentType2 = InstrumentType.INSTRUMENT_TYPE_SHARE;
        final int quantity2 = 20;
        final int averagePositionPrice2 = 1;
        final int expectedYield2 = 60;
        final int currentPrice2 = 4;
        final int quantityLots2 = 2;
        final String currency2 = TestShare2.CURRENCY;

        final String figi3 = TestEtf1.FIGI;
        final InstrumentType instrumentType3 = InstrumentType.INSTRUMENT_TYPE_ETF;
        final int quantity3 = 5;
        final int averagePositionPrice3 = 15;
        final int expectedYield3 = -25;
        final int currentPrice3 = 10;
        final int quantityLots3 = 5;
        final String currency3 = TestEtf1.CURRENCY;

        final PortfolioPosition portfolioPosition1 = TestData.createPortfolioPosition(
                figi1,
                instrumentType1,
                quantity1,
                averagePositionPrice1,
                expectedYield1,
                currentPrice1,
                quantityLots1,
                currency1
        );
        final PortfolioPosition portfolioPosition2 = TestData.createPortfolioPosition(
                figi2,
                instrumentType2,
                quantity2,
                averagePositionPrice2,
                expectedYield2,
                currentPrice2,
                quantityLots2,
                currency2
        );
        final PortfolioPosition portfolioPosition3 = TestData.createPortfolioPosition(
                figi3,
                instrumentType3,
                quantity3,
                averagePositionPrice3,
                expectedYield3,
                currentPrice3,
                quantityLots3,
                currency3
        );

        final Portfolio portfolio = TestData.createPortfolio(portfolioPosition1, portfolioPosition2, portfolioPosition3);
        Mockito.when(operationsService.getPortfolioSync(accountId)).thenReturn(portfolio);

        // action

        final List<Position> positions = extOperationsService.getPositions(accountId);

        // assert

        final Position expectedPosition1 = new PositionBuilder()
                .setCurrency(currency1)
                .setFigi(figi1)
                .setInstrumentType(instrumentType1)
                .setQuantity(quantity1)
                .setAveragePositionPrice(averagePositionPrice1)
                .setExpectedYield(expectedYield1)
                .setCurrentNkd(0)
                .setCurrentPrice(currentPrice1)
                .setAveragePositionPriceFifo(0)
                .setQuantityLots(quantityLots1)
                .build();
        final Position expectedPosition2 = new PositionBuilder()
                .setCurrency(currency2)
                .setFigi(figi2)
                .setInstrumentType(instrumentType2)
                .setQuantity(quantity2)
                .setAveragePositionPrice(averagePositionPrice2)
                .setExpectedYield(expectedYield2)
                .setCurrentNkd(0)
                .setCurrentPrice(currentPrice2)
                .setAveragePositionPriceFifo(0)
                .setQuantityLots(quantityLots2)
                .build();
        final Position expectedPosition3 = new PositionBuilder()
                .setCurrency(currency3)
                .setFigi(figi3)
                .setInstrumentType(instrumentType3)
                .setQuantity(quantity3)
                .setAveragePositionPrice(averagePositionPrice3)
                .setExpectedYield(expectedYield3)
                .setCurrentNkd(0)
                .setCurrentPrice(currentPrice3)
                .setAveragePositionPriceFifo(0)
                .setQuantityLots(quantityLots3)
                .build();

        final List<Position> expectedPositions = List.of(expectedPosition1, expectedPosition2, expectedPosition3);

        AssertUtils.assertEquals(expectedPositions, positions);
    }

}