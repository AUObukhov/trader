package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.PortfolioPositionBuilder;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.tinkoff.piapi.core.models.Position;

class PositionMapperUnitTest {

    private final PositionMapper mapper = Mappers.getMapper(PositionMapper.class);

    @Test
    void map() {
        final String ticker = TestShare1.TICKER;
        final String figi = TestShare1.FIGI;
        final InstrumentType instrumentType = InstrumentType.SHARE;
        final int averagePositionPrice = 110;
        final int expectedYield = 10000;
        final int currentPrice = 120;
        final int quantityLots = 10;
        final int lotSize = 1000;
        final Currency currency = Currency.RUB;

        final ru.tinkoff.piapi.contract.v1.PortfolioPosition portfolioPosition = TestData.createTinkoffPortfolioPosition(
                figi,
                instrumentType,
                quantityLots * lotSize,
                averagePositionPrice,
                expectedYield,
                currentPrice,
                quantityLots,
                currency
        );
        final Position source = Position.fromResponse(portfolioPosition);

        final PortfolioPosition position = mapper.map(ticker, source);

        final PortfolioPosition expectedPosition = new PortfolioPositionBuilder()
                .setFigi(figi)
                .setTicker(ticker)
                .setInstrumentType(instrumentType)
                .setAveragePositionPrice(averagePositionPrice)
                .setExpectedYield(expectedYield)
                .setCurrentPrice(currentPrice)
                .setQuantityLots(quantityLots)
                .setCurrency(currency)
                .setLotSize(lotSize)
                .build();

        AssertUtils.assertEquals(expectedPosition, position);
    }

}