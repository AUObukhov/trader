package ru.obukhov.trader.test.utils.model.orderstage;

import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.OrderStage;

public class TestOrderStages {

    private static final OrderStage ORDER_STAGE_OBJECT1 = OrderStage.builder()
            .price(DecimalUtils.setDefaultScale(30))
            .quantity(2L)
            .tradeId("1234567890") // todo real data
            .build();

    private static final OrderStage ORDER_STAGE_OBJECT2 = OrderStage.builder()
            .price(DecimalUtils.setDefaultScale(10))
            .quantity(1L)
            .tradeId("abcdef") // todo real data
            .build();

    public static final TestOrderStage ORDER_STAGE1 = new TestOrderStage(ORDER_STAGE_OBJECT1);
    public static final TestOrderStage ORDER_STAGE2 = new TestOrderStage(ORDER_STAGE_OBJECT2);

}