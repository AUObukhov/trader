package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.core.models.Position;

class PositionMapperUnitTest {

    private final PositionMapper mapper = Mappers.getMapper(PositionMapper.class);

    @Test
    void map() {
        final String ticker = "ticker";
        final String figi = "figi";
        final InstrumentType instrumentType = InstrumentType.STOCK;
        final int quantity = 1000;
        final int averagePositionPrice = 110;
        final int expectedYield = 10000;
        final int currentPrice = 120;
        final int quantityLots = 10;
        final Currency currency = Currency.RUB;

        final ru.tinkoff.piapi.contract.v1.PortfolioPosition portfolioPosition = TestData.createTinkoffPortfolioPosition(
                figi,
                instrumentType,
                quantity,
                averagePositionPrice,
                expectedYield,
                currentPrice,
                quantityLots,
                currency
        );
        final Position source = Position.fromResponse(portfolioPosition);

        final PortfolioPosition position = mapper.map(ticker, source);

        final PortfolioPosition expectedPosition1 = TestData.createPortfolioPosition(
                ticker,
                instrumentType,
                quantity,
                averagePositionPrice,
                expectedYield,
                currentPrice,
                quantityLots,
                currency
        );

        AssertUtils.assertEquals(expectedPosition1, position);
    }

}