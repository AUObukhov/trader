package ru.obukhov.investor.model.transform;

import org.junit.Test;
import org.mapstruct.factory.Mappers;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.MoneyAmount;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class MoneyAmountMapperTest {

    private final MoneyAmountMapper mapper = Mappers.getMapper(MoneyAmountMapper.class);

    @Test
    public void mapsTinkoffToCustom() {
        Currency currency = Currency.RUB;
        BigDecimal value = BigDecimal.valueOf(100);

        MoneyAmount source = new MoneyAmount(currency, value);

        ru.obukhov.investor.model.MoneyAmount target = mapper.map(source);

        assertEquals(value, target.getValue());
        assertEquals(currency, target.getCurrency());
    }

    @Test
    public void mapsCustomToTinkoff() {
        Currency currency = Currency.RUB;
        BigDecimal value = BigDecimal.valueOf(100);

        ru.obukhov.investor.model.MoneyAmount source = new ru.obukhov.investor.model.MoneyAmount(currency, value);

        MoneyAmount target = mapper.map(source);

        assertEquals(value, target.value);
        assertEquals(currency, target.currency);
    }

}