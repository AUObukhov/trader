package ru.obukhov.trader.market.model.transform;

import com.fasterxml.jackson.databind.JsonSerializer;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency1;
import ru.tinkoff.piapi.contract.v1.Currency;

import java.io.IOException;

class CurrencySerializerUnitTest extends SerializerAbstractUnitTest<Currency> {

    private final CurrencySerializer currencySerializer = new CurrencySerializer();

    @Test
    void test() throws IOException {
        final JsonSerializer<?>[] serializers = {new QuotationSerializer(), new MoneyValueSerializer(), new TimestampSerializer()};
        test(currencySerializer, TestCurrency1.CURRENCY, TestCurrency1.JSON_STRING, serializers);
    }

}