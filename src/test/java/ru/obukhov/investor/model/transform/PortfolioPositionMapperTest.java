package ru.obukhov.investor.model.transform;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.investor.model.PortfolioPosition;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.MoneyAmount;
import ru.tinkoff.invest.openapi.models.portfolio.InstrumentType;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PortfolioPositionMapperTest {

    private final PortfolioPositionMapper mapper = Mappers.getMapper(PortfolioPositionMapper.class);

    @Test
    public void mapsSinglePosition() {
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

        assertEquals(ticker, target.getTicker());
        assertEquals(balance, target.getBalance());
        assertEquals(blocked, target.getBlocked());
        assertEquals(expectedYield, target.getExpectedYield());
        assertEquals(lots, target.getLotsCount());
        assertEquals(averagePositionPrice, target.getAveragePositionPrice());
        assertEquals(averagePositionPriceNoNkd, target.getAveragePositionPriceNoNkd());
        assertEquals(name, target.getName());
    }

    @Test
    public void mapCollection() {
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
        assertEquals(ticker1, target1.getTicker());
        assertEquals(balance1, target1.getBalance());
        assertEquals(blocked1, target1.getBlocked());
        assertEquals(expectedYield1, target1.getExpectedYield());
        assertEquals(lots1, target1.getLotsCount());
        assertEquals(averagePositionPrice1, target1.getAveragePositionPrice());
        assertEquals(averagePositionPriceNoNkd1, target1.getAveragePositionPriceNoNkd());
        assertEquals(name1, target1.getName());

        PortfolioPosition target2 = iterator.next();
        assertEquals(ticker2, target2.getTicker());
        assertEquals(balance2, target2.getBalance());
        assertEquals(blocked2, target2.getBlocked());
        assertEquals(expectedYield2, target2.getExpectedYield());
        assertEquals(lots2, target2.getLotsCount());
        assertEquals(averagePositionPrice2, target2.getAveragePositionPrice());
        assertEquals(averagePositionPriceNoNkd2, target2.getAveragePositionPriceNoNkd());
        assertEquals(name2, target2.getName());

        assertFalse(iterator.hasNext());
    }

}