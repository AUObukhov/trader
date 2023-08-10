package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.orderstage.TestOrderStage1;
import ru.tinkoff.piapi.contract.v1.OrderStage;

import java.io.IOException;

class OrderStageSerializerUnitTest extends SerializerAbstractUnitTest<OrderStage> {

    private final OrderStageSerializer orderStageSerializer = new OrderStageSerializer();

    @Test
    void test() throws IOException {
        test(orderStageSerializer, TestOrderStage1.ORDER_STAGE, TestOrderStage1.STRING, new MoneyValueSerializer());
    }

}