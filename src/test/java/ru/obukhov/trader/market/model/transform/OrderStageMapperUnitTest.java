package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.OrderStage;
import ru.obukhov.trader.test.utils.model.orderstage.TestOrderStage1;
import ru.obukhov.trader.test.utils.model.orderstage.TestOrderStage2;

class OrderStageMapperUnitTest {

    private final OrderStageMapper orderStageMapper = Mappers.getMapper(OrderStageMapper.class);

    @Test
    void map_tinkoffToCustom() {
        final OrderStage orderStage = orderStageMapper.map(TestOrderStage1.TINKOFF_ORDER_STAGE);

        Assertions.assertEquals(TestOrderStage1.ORDER_STAGE, orderStage);
    }

    @Test
    void map_tinkoffToCustom_whenValueIsNull() {
        final OrderStage orderStage = orderStageMapper.map((ru.tinkoff.piapi.contract.v1.OrderStage) null);

        Assertions.assertNull(orderStage);
    }

    @Test
    void map_customToTinkoff() {
        final ru.tinkoff.piapi.contract.v1.OrderStage orderStage = orderStageMapper.map(TestOrderStage2.ORDER_STAGE);

        Assertions.assertEquals(TestOrderStage2.TINKOFF_ORDER_STAGE, orderStage);
    }

    @Test
    void map_customToTinkoff_whenValueIsNull() {
        final ru.tinkoff.piapi.contract.v1.OrderStage orderStage = orderStageMapper.map((OrderStage) null);

        Assertions.assertNull(orderStage);
    }

}