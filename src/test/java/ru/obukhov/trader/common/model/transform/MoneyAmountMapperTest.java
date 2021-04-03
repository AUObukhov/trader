package ru.obukhov.trader.common.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.MoneyAmount;
import ru.obukhov.trader.market.model.transform.MoneyAmountMapper;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.Currency;

import java.math.BigDecimal;

class MoneyAmountMapperTest {

    private final MoneyAmountMapper mapper = Mappers.getMapper(MoneyAmountMapper.class);

    @Test
    void mapsTinkoffToCustom() {
        ru.tinkoff.invest.openapi.model.rest.MoneyAmount source =
                TestDataHelper.createMoneyAmount(Currency.RUB, 100);

        MoneyAmount target = mapper.map(source);

        Assertions.assertEquals(source.getCurrency(), target.getCurrency());
        AssertUtils.assertEquals(source.getValue(), target.getValue());
    }

    @Test
    void mapsCustomToTinkoff() {
        Currency currency = Currency.RUB;
        BigDecimal value = BigDecimal.valueOf(100);

        MoneyAmount source = new MoneyAmount(currency, value);

        ru.tinkoff.invest.openapi.model.rest.MoneyAmount target = mapper.map(source);

        Assertions.assertEquals(currency, target.getCurrency());
        AssertUtils.assertEquals(value, target.getValue());
    }

}