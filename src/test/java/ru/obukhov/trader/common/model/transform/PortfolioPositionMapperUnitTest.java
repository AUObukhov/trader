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

class PortfolioPositionMapperUnitTest {

    private final PortfolioPositionMapper mapper = Mappers.getMapper(PortfolioPositionMapper.class);

    @Test
    void mapsSinglePosition() {
        final Currency currency = Currency.RUB;

        ru.tinkoff.invest.openapi.model.rest.PortfolioPosition source =
                new ru.tinkoff.invest.openapi.model.rest.PortfolioPosition()
                        .figi("figi")
                        .ticker("ticker")
                        .isin("isin")
                        .instrumentType(InstrumentType.STOCK)
                        .balance(BigDecimal.valueOf(1000))
                        .blocked(BigDecimal.valueOf(500));

        MoneyAmount expectedYield = TestDataHelper.createMoneyAmount(currency, 200);
        source.setExpectedYield(expectedYield);

        source.setLots(10);

        MoneyAmount averagePositionPrice = TestDataHelper.createMoneyAmount(currency, 100);
        source.setAveragePositionPrice(averagePositionPrice);

        MoneyAmount averagePositionPriceNoNkd = TestDataHelper.createMoneyAmount(currency, 50);
        source.setAveragePositionPriceNoNkd(averagePositionPriceNoNkd);

        source.setName("name");

        PortfolioPosition target = mapper.map(source);

        Assertions.assertEquals(source.getTicker(), target.getTicker());
        Assertions.assertEquals(source.getBalance(), target.getBalance());
        Assertions.assertEquals(source.getBlocked(), target.getBlocked());
        AssertUtils.assertEquals(expectedYield.getValue(), target.getExpectedYield());
        Assertions.assertEquals(source.getLots(), target.getLotsCount());
        AssertUtils.assertEquals(averagePositionPrice.getValue(), target.getAveragePositionPrice());
        AssertUtils.assertEquals(averagePositionPriceNoNkd.getValue(), target.getAveragePositionPriceNoNkd());
        Assertions.assertEquals(source.getName(), target.getName());
    }

    @Test
    void mapCollection() {
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
                        .balance(BigDecimal.valueOf(1000))
                        .blocked(BigDecimal.valueOf(500))
                        .expectedYield(expectedYield1)
                        .lots(10)
                        .averagePositionPrice(averagePositionPrice1)
                        .averagePositionPriceNoNkd(averagePositionPriceNoNkd1)
                        .name("name1");

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
                        .balance(BigDecimal.valueOf(2000))
                        .blocked(BigDecimal.valueOf(1000))
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
        Assertions.assertEquals(source1.getBalance(), target1.getBalance());
        Assertions.assertEquals(source1.getBlocked(), target1.getBlocked());
        Assertions.assertEquals(expectedYield1.getValue(), target1.getExpectedYield());
        Assertions.assertEquals(source1.getLots(), target1.getLotsCount());
        Assertions.assertEquals(averagePositionPrice1.getValue(), target1.getAveragePositionPrice());
        Assertions.assertEquals(averagePositionPriceNoNkd1.getValue(), target1.getAveragePositionPriceNoNkd());
        Assertions.assertEquals(source1.getName(), target1.getName());

        PortfolioPosition target2 = iterator.next();
        Assertions.assertEquals(source2.getTicker(), target2.getTicker());
        Assertions.assertEquals(source2.getBalance(), target2.getBalance());
        Assertions.assertEquals(source2.getBlocked(), target2.getBlocked());
        Assertions.assertEquals(expectedYield2.getValue(), target2.getExpectedYield());
        Assertions.assertEquals(source2.getLots(), target2.getLotsCount());
        Assertions.assertEquals(averagePositionPrice2.getValue(), target2.getAveragePositionPrice());
        Assertions.assertEquals(averagePositionPriceNoNkd2.getValue(), target2.getAveragePositionPriceNoNkd());
        Assertions.assertEquals(source2.getName(), target2.getName());

        Assertions.assertFalse(iterator.hasNext());
    }

}