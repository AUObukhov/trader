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
        final BigDecimal balance1 = BigDecimal.valueOf(1000);
        final BigDecimal blocked1 = BigDecimal.valueOf(500);
        final Currency currency1 = Currency.RUB;
        final MoneyAmount expectedYield1 = TestDataHelper.createMoneyAmount(currency1, 200);
        final MoneyAmount averagePositionPrice1 = TestDataHelper.createMoneyAmount(currency1, 100);
        final MoneyAmount averagePositionPriceNoNkd1 = TestDataHelper.createMoneyAmount(currency1, 50);

        ru.tinkoff.invest.openapi.model.rest.PortfolioPosition source1 =
                new ru.tinkoff.invest.openapi.model.rest.PortfolioPosition()
                        .figi("figi1")
                        .ticker("ticker1")
                        .isin("isin1")
                        .instrumentType(InstrumentType.STOCK)
                        .balance(balance1)
                        .blocked(blocked1)
                        .expectedYield(expectedYield1)
                        .lots(10)
                        .averagePositionPrice(averagePositionPrice1)
                        .averagePositionPriceNoNkd(averagePositionPriceNoNkd1)
                        .name("name1");

        final BigDecimal balance2 = BigDecimal.valueOf(2000);
        final BigDecimal blocked2 = BigDecimal.valueOf(1000);
        final Currency currency2 = Currency.USD;
        final MoneyAmount expectedYield2 = TestDataHelper.createMoneyAmount(currency2, 400);
        final MoneyAmount averagePositionPrice2 = TestDataHelper.createMoneyAmount(currency2, 200);
        final MoneyAmount averagePositionPriceNoNkd2 = TestDataHelper.createMoneyAmount(currency2, 100);

        ru.tinkoff.invest.openapi.model.rest.PortfolioPosition source2 =
                new ru.tinkoff.invest.openapi.model.rest.PortfolioPosition()
                        .figi("figi2")
                        .ticker("ticker2")
                        .isin("isin2")
                        .instrumentType(InstrumentType.BOND)
                        .balance(balance2)
                        .blocked(blocked2)
                        .expectedYield(expectedYield2)
                        .lots(5)
                        .averagePositionPrice(averagePositionPrice2)
                        .averagePositionPriceNoNkd(averagePositionPriceNoNkd2)
                        .name("name2");

        Collection<ru.tinkoff.invest.openapi.model.rest.PortfolioPosition> source = Arrays.asList(source1, source2);

        Collection<PortfolioPosition> target = mapper.map(source);

        Iterator<PortfolioPosition> iterator = target.iterator();
        PortfolioPosition target1 = iterator.next();
        Assertions.assertEquals(source1.getTicker(), target1.getTicker());
        Assertions.assertEquals(balance1, target1.getBalance());
        Assertions.assertEquals(blocked1, target1.getBlocked());
        Assertions.assertEquals(expectedYield1.getValue(), target1.getExpectedYield());
        Assertions.assertEquals(source1.getLots(), target1.getLotsCount());
        Assertions.assertEquals(averagePositionPrice1.getValue(), target1.getAveragePositionPrice());
        Assertions.assertEquals(averagePositionPriceNoNkd1.getValue(), target1.getAveragePositionPriceNoNkd());
        Assertions.assertEquals(source1.getName(), target1.getName());

        PortfolioPosition target2 = iterator.next();
        Assertions.assertEquals(source2.getTicker(), target2.getTicker());
        Assertions.assertEquals(balance2, target2.getBalance());
        Assertions.assertEquals(blocked2, target2.getBlocked());
        Assertions.assertEquals(expectedYield2.getValue(), target2.getExpectedYield());
        Assertions.assertEquals(source2.getLots(), target2.getLotsCount());
        Assertions.assertEquals(averagePositionPrice2.getValue(), target2.getAveragePositionPrice());
        Assertions.assertEquals(averagePositionPriceNoNkd2.getValue(), target2.getAveragePositionPriceNoNkd());
        Assertions.assertEquals(source2.getName(), target2.getName());

        Assertions.assertFalse(iterator.hasNext());
    }

}