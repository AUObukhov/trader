package ru.obukhov.trader.test.utils.model.order_state;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;
import ru.obukhov.trader.market.model.transform.OrderStageMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

public record TestOrderState(OrderState orderState, ru.tinkoff.piapi.contract.v1.OrderState tOrderState) {

    TestOrderState(final OrderState orderState) {
        this(orderState, buildTOrderState(orderState));
    }

    private static ru.tinkoff.piapi.contract.v1.OrderState buildTOrderState(final OrderState orderState) {
        final OrderStageMapper orderStageMapper = Mappers.getMapper(OrderStageMapper.class);
        final MoneyValueMapper moneyValueMapper = Mappers.getMapper(MoneyValueMapper.class);
        return ru.tinkoff.piapi.contract.v1.OrderState.newBuilder()
                .setOrderId(orderState.orderId())
                .setExecutionReportStatus(orderState.executionReportStatus())
                .setLotsRequested(orderState.lotsRequested())
                .setLotsExecuted(orderState.lotsExecuted())
                .setInitialOrderPrice(moneyValueMapper.map(orderState.initialOrderPrice(), orderState.currency()))
                .setExecutedOrderPrice(moneyValueMapper.map(orderState.executedOrderPrice(), orderState.currency()))
                .setTotalOrderAmount(moneyValueMapper.map(orderState.totalOrderAmount(), orderState.currency()))
                .setAveragePositionPrice(moneyValueMapper.map(orderState.averagePositionPrice(), orderState.currency()))
                .setInitialCommission(moneyValueMapper.map(orderState.initialCommission(), orderState.currency()))
                .setExecutedCommission(moneyValueMapper.map(orderState.executedCommission(), orderState.currency()))
                .setFigi(orderState.figi())
                .setDirection(orderState.direction())
                .setInitialSecurityPrice(moneyValueMapper.map(orderState.initialSecurityPrice(), orderState.currency()))
                .addAllStages(orderState.stages().stream().map(orderStageMapper::map).toList())
                .setServiceCommission(moneyValueMapper.map(orderState.serviceCommission(), Currencies.RUB))
                .setCurrency(orderState.currency())
                .setOrderType(orderState.orderType())
                .setOrderDate(DateTimeTestData.newTimestamp(orderState.orderDate()))
                .setInstrumentUid(orderState.instrumentUid())
                .setOrderRequestId(orderState.orderRequestId())
                .build();
    }

    public String getOrderId() {
        return orderState.orderId();
    }

}