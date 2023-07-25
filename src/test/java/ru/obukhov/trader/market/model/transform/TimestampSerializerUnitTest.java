package ru.obukhov.trader.market.model.transform;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class TimestampSerializerUnitTest extends SerializerAbstractUnitTest<Timestamp> {

    private final TimestampSerializer moneyValueSerializer = new TimestampSerializer();

    @Test
    void test() throws IOException {
        final Timestamp moneyValue = Timestamp.newBuilder()
                .setSeconds(1753747200)
                .setNanos(500000000)
                .build();
        final String expectedResult = "{\"seconds\":1753747200,\"nanos\":500000000}";
        test(moneyValueSerializer, moneyValue, expectedResult);
    }

}