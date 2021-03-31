package ru.obukhov.trader.common.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.transform.PortfolioPositionMapper;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.InstrumentType;
import ru.tinkoff.invest.openapi.model.rest.MoneyAmount;

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
        final BigDecimal expectedYieldValue = BigDecimal.valueOf(200);
        final int lots = 10;
        final BigDecimal averagePositionPriceValue = BigDecimal.valueOf(100);
        final BigDecimal averagePositionPriceNoNkdValue = BigDecimal.valueOf(50);
        final String name = "name";
        ru.tinkoff.invest.openapi.model.rest.PortfolioPosition source =
                new ru.tinkoff.invest.openapi.model.rest.PortfolioPosition();
        source.setFigi("figi");
        source.setTicker(ticker);
        source.setIsin("isin");
        source.setInstrumentType(InstrumentType.STOCK);
        source.setBalance(balance);
        source.setBlocked(blocked);

        MoneyAmount expectedYield = new MoneyAmount();
        expectedYield.setCurrency(currency);
        expectedYield.setValue(expectedYieldValue);
        source.setExpectedYield(expectedYield);

        source.setLots(lots);

        MoneyAmount averagePositionPrice = new MoneyAmount();
        averagePositionPrice.setCurrency(currency);
        averagePositionPrice.setValue(averagePositionPriceValue);
        source.setAveragePositionPrice(averagePositionPrice);

        MoneyAmount averagePositionPriceNoNkd = new MoneyAmount();
        averagePositionPriceNoNkd.setCurrency(currency);
        averagePositionPriceNoNkd.setValue(averagePositionPriceNoNkdValue);
        source.setAveragePositionPriceNoNkd(averagePositionPriceNoNkd);

        source.setName(name);

        PortfolioPosition target = mapper.map(source);

        Assertions.assertEquals(ticker, target.getTicker());
        Assertions.assertEquals(balance, target.getBalance());
        Assertions.assertEquals(blocked, target.getBlocked());
        AssertUtils.assertEquals(expectedYieldValue, target.getExpectedYield());
        Assertions.assertEquals(lots, target.getLotsCount());
        AssertUtils.assertEquals(averagePositionPriceValue, target.getAveragePositionPrice());
        AssertUtils.assertEquals(averagePositionPriceNoNkdValue, target.getAveragePositionPriceNoNkd());
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
        ru.tinkoff.invest.openapi.model.rest.PortfolioPosition source1 = TestDataHelper.createTinkoffPortfolioPosition(
                "figi1",
                ticker1,
                "isin1",
                InstrumentType.STOCK,
                balance1,
                blocked1,
                TestDataHelper.createMoneyAmount(currency1, expectedYield1),
                lots1,
                TestDataHelper.createMoneyAmount(currency1, averagePositionPrice1),
                TestDataHelper.createMoneyAmount(currency1, averagePositionPriceNoNkd1),
                name1
        );

        final String ticker2 = "ticker2";
        final BigDecimal balance2 = BigDecimal.valueOf(2000);
        final BigDecimal blocked2 = BigDecimal.valueOf(1000);
        final Currency currency2 = Currency.USD;
        final BigDecimal expectedYield2 = BigDecimal.valueOf(400);
        final int lots2 = 5;
        final BigDecimal averagePositionPrice2 = BigDecimal.valueOf(200);
        final BigDecimal averagePositionPriceNoNkd2 = BigDecimal.valueOf(100);
        final String name2 = "name2";
        ru.tinkoff.invest.openapi.model.rest.PortfolioPosition source2 = TestDataHelper.createTinkoffPortfolioPosition(
                "figi2",
                ticker2,
                "isin2",
                InstrumentType.BOND,
                balance2,
                blocked2,
                TestDataHelper.createMoneyAmount(currency2, expectedYield2),
                lots2,
                TestDataHelper.createMoneyAmount(currency2, averagePositionPrice2),
                TestDataHelper.createMoneyAmount(currency2, averagePositionPriceNoNkd2),
                name2
        );

        Collection<ru.tinkoff.invest.openapi.model.rest.PortfolioPosition> source = Arrays.asList(source1, source2);

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