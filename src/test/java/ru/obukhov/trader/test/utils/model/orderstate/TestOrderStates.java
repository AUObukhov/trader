package ru.obukhov.trader.test.utils.model.orderstate;

import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.orderstage.TestOrderStages;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus;
import ru.tinkoff.piapi.contract.v1.OrderType;

import java.util.List;

public class TestOrderStates {

    private static final OrderState ORDER_STATE_OBJECT1 = OrderState.builder()
            .orderId("582899921200")
            .executionReportStatus(OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_NEW)
            .lotsRequested(1L)
            .lotsExecuted(0L)
            .initialOrderPrice(DecimalUtils.setDefaultScale(30))
            .executedOrderPrice(DecimalUtils.ZERO)
            .totalOrderAmount(DecimalUtils.setDefaultScale(30))
            .averagePositionPrice(DecimalUtils.ZERO)
            .initialCommission(DecimalUtils.setDefaultScale(0.09))
            .executedCommission(DecimalUtils.ZERO)
            .figi("BBG00K7T3037")
            .direction(OrderDirection.ORDER_DIRECTION_SELL)
            .initialSecurityPrice(DecimalUtils.setDefaultScale(30))
            .stages(List.of(TestOrderStages.ORDER_STAGE1.orderStage(), TestOrderStages.ORDER_STAGE2.orderStage()))
            .serviceCommission(DecimalUtils.ZERO)
            .currency("usd")
            .orderType(OrderType.ORDER_TYPE_LIMIT)
            .orderDate(DateTimeTestData.createDateTime(2023, 8, 10, 8, 20, 27, 513541))
            .instrumentUid("46fef208-a525-4471-85e5-8fe4cee5f8ec")
            .orderRequestId("5627319091032181721")
            .build();

    private static final OrderState ORDER_STATE_OBJECT2 = OrderState.builder()
            .orderId("582899921201")
            .executionReportStatus(OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_FILL)
            .lotsRequested(1L)
            .lotsExecuted(1L)
            .initialOrderPrice(DecimalUtils.setDefaultScale(30))
            .executedOrderPrice(DecimalUtils.setDefaultScale(30))
            .totalOrderAmount(DecimalUtils.setDefaultScale(30))
            .averagePositionPrice(DecimalUtils.setDefaultScale(30))
            .initialCommission(DecimalUtils.setDefaultScale(0.09))
            .executedCommission(DecimalUtils.setDefaultScale(0.09))
            .figi("BBG00K7T3037")
            .direction(OrderDirection.ORDER_DIRECTION_BUY)
            .initialSecurityPrice(DecimalUtils.setDefaultScale(30))
            .stages(List.of(TestOrderStages.ORDER_STAGE1.orderStage()))
            .serviceCommission(DecimalUtils.setDefaultScale(0.1))
            .currency("usd")
            .orderType(OrderType.ORDER_TYPE_LIMIT)
            .orderDate(DateTimeTestData.createDateTime(2023, 8, 10, 8, 20, 27, 513541))
            .instrumentUid("46fef208-a525-4471-85e5-8fe4cee5f8ec")
            .orderRequestId("5627319091032181721")
            .build();

    public static final TestOrderState ORDER_STATE1 = new TestOrderState(ORDER_STATE_OBJECT1);
    public static final TestOrderState ORDER_STATE2 = new TestOrderState(ORDER_STATE_OBJECT2);

}