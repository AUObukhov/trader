package ru.obukhov.trader.test.utils.model.orderstate;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.OrderStage;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;
import ru.obukhov.trader.market.model.transform.OrderStageMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

import java.util.stream.Collectors;

public record TestOrderState(OrderState orderState, ru.tinkoff.piapi.contract.v1.OrderState tinkoffOrderState, String jsonString) {

    TestOrderState(final OrderState orderState) {
        this(orderState, buildTinkoffOrderState(orderState), buildJsonString(orderState));
    }

    private static ru.tinkoff.piapi.contract.v1.OrderState buildTinkoffOrderState(final OrderState orderState) {
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

    private static String buildJsonString(final OrderState orderState) {
        return "{\"orderId\":\"" + orderState.orderId() + "\"," +
                "\"executionReportStatus\":\"" + orderState.executionReportStatus() + "\"," +
                "\"lotsRequested\":" + orderState.lotsRequested() + "," +
                "\"lotsExecuted\":" + orderState.lotsExecuted() + "," +
                "\"initialOrderPrice\":" + orderState.initialOrderPrice() + "," +
                "\"executedOrderPrice\":" + orderState.executedOrderPrice() + "," +
                "\"totalOrderAmount\":" + orderState.totalOrderAmount() + "," +
                "\"averagePositionPrice\":" + orderState.averagePositionPrice() + "," +
                "\"initialCommission\":" + orderState.initialCommission() + "," +
                "\"executedCommission\":" + orderState.executedCommission() + "," +
                "\"figi\":\"" + orderState.figi() + "\"," +
                "\"direction\":\"" + orderState.direction() + "\"," +
                "\"initialSecurityPrice\":" + orderState.initialSecurityPrice() + "," +
                "\"stages\":" + orderStagesToString(orderState) + "," +
                "\"serviceCommission\":" + orderState.serviceCommission() + "," +
                "\"currency\":\"" + orderState.currency() + "\"," +
                "\"orderType\":\"" + orderState.orderType() + "\"," +
                "\"orderDate\":\"" + orderState.orderDate() + "\"," +
                "\"instrumentUid\":\"" + orderState.instrumentUid() + "\"," +
                "\"orderRequestId\":\"" + orderState.orderRequestId() + "\"}";
    }

    private static String orderStagesToString(final OrderState orderState) {
        return "[" +
                orderState.stages().stream()
                        .map(TestOrderState::orderStageToString)
                        .collect(Collectors.joining(",")) +
                "]";
    }

    private static String orderStageToString(OrderStage orderStage) {
        return "{\"price\":" + orderStage.price() + "," +
                "\"quantity\":" + orderStage.quantity() + "," +
                "\"tradeId\":\"" + orderStage.tradeId() + "\"}";
    }

}