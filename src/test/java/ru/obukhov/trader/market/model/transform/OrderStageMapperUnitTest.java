package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.OrderStage;
import ru.obukhov.trader.test.utils.model.order_stage.TestOrderStage;
import ru.obukhov.trader.test.utils.model.order_stage.TestOrderStages;

class OrderStageMapperUnitTest {

    private final OrderStageMapper orderStageMapper = Mappers.getMapper(OrderStageMapper.class);

    @Test
    void map_tToCustom() {
        final TestOrderStage testOrderStage = TestOrderStages.ORDER_STAGE1;

        final OrderStage orderStage = orderStageMapper.map(testOrderStage.tOrderStage());

        Assertions.assertEquals(testOrderStage.orderStage(), orderStage);
    }

    @Test
    void map_tToCustom_whenValueIsNull() {
        final OrderStage orderStage = orderStageMapper.map((ru.tinkoff.piapi.contract.v1.OrderStage) null);

        Assertions.assertNull(orderStage);
    }

    @Test
    void map_customToT() {
        final TestOrderStage testOrderStage = TestOrderStages.ORDER_STAGE1;

        final ru.tinkoff.piapi.contract.v1.OrderStage orderStage = orderStageMapper.map(testOrderStage.orderStage());

        Assertions.assertEquals(testOrderStage.tOrderStage(), orderStage);
    }

    @Test
    void map_customToT_whenValueIsNull() {
        final ru.tinkoff.piapi.contract.v1.OrderStage orderStage = orderStageMapper.map((OrderStage) null);

        Assertions.assertNull(orderStage);
    }

}