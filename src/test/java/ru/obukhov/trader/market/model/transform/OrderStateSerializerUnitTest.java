package ru.obukhov.trader.market.model.transform;

import com.fasterxml.jackson.databind.JsonSerializer;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.orderstate.TestOrderState1;
import ru.tinkoff.piapi.contract.v1.OrderState;

import java.io.IOException;

class OrderStateSerializerUnitTest extends SerializerAbstractUnitTest<OrderState> {

    private final OrderStateSerializer orderStateSerializer = new OrderStateSerializer();

    @Test
    void test() throws IOException {
        final JsonSerializer<?>[] jsonSerializers = {new OrderStageSerializer(), new MoneyValueSerializer(), new TimestampSerializer()};
        test(orderStateSerializer, TestOrderState1.ORDER_STATE, TestOrderState1.STRING, jsonSerializers);
    }

}