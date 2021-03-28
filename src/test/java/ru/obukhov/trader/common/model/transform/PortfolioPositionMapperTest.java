package ru.obukhov.trader.common.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.transform.PortfolioPositionMapper;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.MoneyAmount;
import ru.tinkoff.invest.openapi.models.portfolio.InstrumentType;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

class PortfolioPositionMapperTest {

    private final PortfolioPositionMapper mapper = Mappers.getMapper(PortfolioPositionMapper.class);

    @Test
    void mapsSinglePosition() {
        final String ticker = "ticker";
        final BigDecimal balance = BigDecimal.valueOf(1000);
        final BigDecimal blocked = BigDecimal.valueOf(500);
        final Currency currency = Currency.RUB;
        final BigDecimal expectedYield = BigDecimal.valueOf(200);
        final int lots = 10;
        final BigDecimal averagePositionPrice = BigDecimal.valueOf(100);
        final BigDecimal averagePositionPriceNoNkd = BigDecimal.valueOf(50);
        final String name = "name";
        Portfolio.PortfolioPosition source = new Portfolio.PortfolioPosition("figi",
                ticker,
                "isin",
                InstrumentType.Stock,
                balance,
                blocked,
                new MoneyAmount(currency, expectedYield),
                lots,
                new MoneyAmount(currency, averagePositionPrice),
                new MoneyAmount(currency, averagePositionPriceNoNkd),
                name);

        PortfolioPosition target = mapper.map(source);

        Assertions.assertEquals(ticker, target.getTicker());
        Assertions.assertEquals(balance, target.getBalance());
        Assertions.assertEquals(blocked, target.getBlocked());
        Assertions.assertEquals(expectedYield, target.getExpectedYield());
        Assertions.assertEquals(lots, target.getLotsCount());
        Assertions.assertEquals(averagePositionPrice, target.getAveragePositionPrice());
        Assertions.assertEquals(averagePositionPriceNoNkd, target.getAveragePositionPriceNoNkd());
        Assertions.assertEquals(name, target.getName());
    }

    @Test
    void mapCollection() {
        final String ticker1 = "ticker1";
        final BigDecimal balance1 = BigDecimal.valueOf(1000);
        final BigDecimal blocked1 = BigDecimal.valueOf(500);
        final Currency currency1 = Currency.RUB;
        final BigDecimal expectedYield1 = BigDecimal.valueOf(200);
        final int lots1 = 10;
        final BigDecimal averagePositionPrice1 = BigDecimal.valueOf(100);
        final BigDecimal averagePositionPriceNoNkd1 = BigDecimal.valueOf(50);
        final String name1 = "name1";
        Portfolio.PortfolioPosition source1 = new Portfolio.PortfolioPosition("figi1",
                ticker1,
                "isin1",
                InstrumentType.Stock,
                balance1,
                blocked1,
                new MoneyAmount(currency1, expectedYield1),
                lots1,
                new MoneyAmount(currency1, averagePositionPrice1),
                new MoneyAmount(currency1, averagePositionPriceNoNkd1),
                name1);

        final String ticker2 = "ticker2";
        final BigDecimal balance2 = BigDecimal.valueOf(2000);
        final BigDecimal blocked2 = BigDecimal.valueOf(1000);
        final Currency currency2 = Currency.USD;
        final BigDecimal expectedYield2 = BigDecimal.valueOf(400);
        final int lots2 = 5;
        final BigDecimal averagePositionPrice2 = BigDecimal.valueOf(200);
        final BigDecimal averagePositionPriceNoNkd2 = BigDecimal.valueOf(100);
        final String name2 = "name2";
        Portfolio.PortfolioPosition source2 = new Portfolio.PortfolioPosition("figi2",
                ticker2,
                "isin2",
                InstrumentType.Bond,
                balance2,
                blocked2,
                new MoneyAmount(currency2, expectedYield2),
                lots2,
                new MoneyAmount(currency2, averagePositionPrice2),
                new MoneyAmount(currency2, averagePositionPriceNoNkd2),
                name2);

        Collection<Portfolio.PortfolioPosition> source = Arrays.asList(source1, source2);

        Collection<PortfolioPosition> target = mapper.map(source);

        Iterator<PortfolioPosition> iterator = target.iterator();
        PortfolioPosition target1 = iterator.next();
        Assertions.assertEquals(ticker1, target1.getTicker());
        Assertions.assertEquals(balance1, target1.getBalance());
        Assertions.assertEquals(blocked1, target1.getBlocked());
        Assertions.assertEquals(expectedYield1, target1.getExpectedYield());
        Assertions.assertEquals(lots1, target1.getLotsCount());
        Assertions.assertEquals(averagePositionPrice1, target1.getAveragePositionPrice());
        Assertions.assertEquals(averagePositionPriceNoNkd1, target1.getAveragePositionPriceNoNkd());
        Assertions.assertEquals(name1, target1.getName());

        PortfolioPosition target2 = iterator.next();
        Assertions.assertEquals(ticker2, target2.getTicker());
        Assertions.assertEquals(balance2, target2.getBalance());
        Assertions.assertEquals(blocked2, target2.getBlocked());
        Assertions.assertEquals(expectedYield2, target2.getExpectedYield());
        Assertions.assertEquals(lots2, target2.getLotsCount());
        Assertions.assertEquals(averagePositionPrice2, target2.getAveragePositionPrice());
        Assertions.assertEquals(averagePositionPriceNoNkd2, target2.getAveragePositionPriceNoNkd());
        Assertions.assertEquals(name2, target2.getName());

        Assertions.assertFalse(iterator.hasNext());
    }

}