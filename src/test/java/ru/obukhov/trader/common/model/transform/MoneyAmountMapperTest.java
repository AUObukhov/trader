package ru.obukhov.trader.common.model.transform;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.transform.MoneyAmountMapper;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.MoneyAmount;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoneyAmountMapperTest {

    private final MoneyAmountMapper mapper = Mappers.getMapper(MoneyAmountMapper.class);

    @Test
    public void mapsTinkoffToCustom() {
        Currency currency = Currency.RUB;
        BigDecimal value = BigDecimal.valueOf(100);

        MoneyAmount source = new MoneyAmount(currency, value);

        ru.obukhov.trader.market.model.MoneyAmount target = mapper.map(source);

        assertEquals(value, target.getValue());
        assertEquals(currency, target.getCurrency());
    }

    @Test
    public void mapsCustomToTinkoff() {
        Currency currency = Currency.RUB;
        BigDecimal value = BigDecimal.valueOf(100);

        ru.obukhov.trader.market.model.MoneyAmount source = new ru.obukhov.trader.market.model.MoneyAmount(currency, value);

        MoneyAmount target = mapper.map(source);

        assertEquals(value, target.value);
        assertEquals(currency, target.currency);
    }

}