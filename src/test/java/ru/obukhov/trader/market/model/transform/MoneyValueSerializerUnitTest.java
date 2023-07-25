package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Test;
import ru.tinkoff.piapi.contract.v1.MoneyValue;

import java.io.IOException;

class MoneyValueSerializerUnitTest extends SerializerAbstractUnitTest<MoneyValue> {

    private final MoneyValueSerializer moneyValueSerializer = new MoneyValueSerializer();

    @Test
    void test() throws IOException {
        final MoneyValue moneyValue = MoneyValue.newBuilder()
                .setCurrency("rub")
                .setUnits(100)
                .setNano(90000000)
                .build();
        final String expectedResult = "{\"currency\":\"rub\",\"units\":100,\"nano\":90000000}";
        test(moneyValueSerializer, moneyValue, expectedResult);
    }

}