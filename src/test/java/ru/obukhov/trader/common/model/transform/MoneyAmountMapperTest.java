package ru.obukhov.trader.common.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.transform.MoneyAmountMapper;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.MoneyAmount;

import java.math.BigDecimal;

class MoneyAmountMapperTest {

    private final MoneyAmountMapper mapper = Mappers.getMapper(MoneyAmountMapper.class);

    @Test
    void mapsTinkoffToCustom() {
        Currency currency = Currency.RUB;
        BigDecimal value = BigDecimal.valueOf(100);

        MoneyAmount source = new MoneyAmount();
        source.setCurrency(currency);
        source.setValue(value);

        ru.obukhov.trader.market.model.MoneyAmount target = mapper.map(source);

        Assertions.assertEquals(value, target.getValue());
        Assertions.assertEquals(currency, target.getCurrency());
    }

    @Test
    void mapsCustomToTinkoff() {
        Currency currency = Currency.RUB;
        BigDecimal value = BigDecimal.valueOf(100);

        ru.obukhov.trader.market.model.MoneyAmount source = new ru.obukhov.trader.market.model.MoneyAmount(currency, value);

        MoneyAmount target = mapper.map(source);

        Assertions.assertEquals(currency, target.getCurrency());
        Assertions.assertEquals(value, target.getValue());
    }

}