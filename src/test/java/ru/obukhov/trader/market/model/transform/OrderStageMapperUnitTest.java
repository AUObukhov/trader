package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.OrderStage;
import ru.obukhov.trader.test.utils.model.orderstage.TestOrderStage;
import ru.obukhov.trader.test.utils.model.orderstage.TestOrderStages;

class OrderStageMapperUnitTest {

    private final OrderStageMapper orderStageMapper = Mappers.getMapper(OrderStageMapper.class);

    @Test
    void map_tinkoffToCustom() {
        final TestOrderStage testOrderStage = TestOrderStages.ORDER_STAGE1;

        final OrderStage orderStage = orderStageMapper.map(testOrderStage.tinkoffOrderStage());

        Assertions.assertEquals(testOrderStage.orderStage(), orderStage);
    }

    @Test
    void map_tinkoffToCustom_whenValueIsNull() {
        final OrderStage orderStage = orderStageMapper.map((ru.tinkoff.piapi.contract.v1.OrderStage) null);

        Assertions.assertNull(orderStage);
    }

    @Test
    void map_customToTinkoff() {
        final TestOrderStage testOrderStage = TestOrderStages.ORDER_STAGE1;

        final ru.tinkoff.piapi.contract.v1.OrderStage orderStage = orderStageMapper.map(testOrderStage.orderStage());

        Assertions.assertEquals(testOrderStage.tinkoffOrderStage(), orderStage);
    }

    @Test
    void map_customToTinkoff_whenValueIsNull() {
        final ru.tinkoff.piapi.contract.v1.OrderStage orderStage = orderStageMapper.map((OrderStage) null);

        Assertions.assertNull(orderStage);
    }

}