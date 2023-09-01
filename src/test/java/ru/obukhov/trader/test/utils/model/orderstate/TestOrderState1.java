package ru.obukhov.trader.test.utils.model.orderstate;

import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.orderstage.TestOrderStage1;
import ru.obukhov.trader.test.utils.model.orderstage.TestOrderStage2;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus;
import ru.tinkoff.piapi.contract.v1.OrderStage;
import ru.tinkoff.piapi.contract.v1.OrderType;

import java.time.OffsetDateTime;
import java.util.List;

public class TestOrderState1 {

    public static final String ORDER_ID = "582899921200";
    public static final OrderExecutionReportStatus EXECUTION_REPORT_STATUS = OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_NEW;
    public static final long LOTS_REQUESTED = 1L;
    public static final long LOTS_EXECUTED = 0L;
    public static final MoneyValue INITIAL_ORDER_PRICE = TestData.createMoneyValue(30.0, "usd");
    public static final MoneyValue EXECUTED_ORDER_PRICE = TestData.createMoneyValue(0.0, "usd");
    public static final MoneyValue TOTAL_ORDER_AMOUNT = TestData.createMoneyValue(30.0, "usd");
    public static final MoneyValue AVERAGE_POSITION_PRICE = TestData.createMoneyValue(0.0, "usd");
    public static final MoneyValue INITIAL_COMMISSION = TestData.createMoneyValue(0, 90000000, "usd");
    public static final MoneyValue EXECUTED_COMMISSION = TestData.createMoneyValue(0.0, "usd");
    public static final String FIGI = "BBG00K7T3037";
    public static final OrderDirection DIRECTION = OrderDirection.ORDER_DIRECTION_SELL;
    public static final MoneyValue INITIAL_SECURITY_PRICE = TestData.createMoneyValue(30.0, "usd");
    public static final List<OrderStage> STAGES = List.of(TestOrderStage1.ORDER_STAGE, TestOrderStage2.ORDER_STAGE);
    public static final MoneyValue SERVICE_COMMISSION = TestData.createMoneyValue(0.0, "rub");
    public static final String CURRENCY = "usd";
    public static final OrderType ORDER_TYPE = OrderType.ORDER_TYPE_LIMIT;
    public static final OffsetDateTime ORDER_DATE = DateTimeTestData.createDateTime(2023, 8, 10, 8, 20, 27, 513541);
    public static final String INSTRUMENT_UID = "46fef208-a525-4471-85e5-8fe4cee5f8ec";
    public static final String ORDER_REQUEST_ID = "5627319091032181721";

    public static final OrderState ORDER_STATE = OrderState.builder()
            .orderId(ORDER_ID)
            .executionReportStatus(EXECUTION_REPORT_STATUS)
            .lotsRequested(LOTS_REQUESTED)
            .lotsExecuted(LOTS_EXECUTED)
            .initialOrderPrice(INITIAL_ORDER_PRICE)
            .executedOrderPrice(EXECUTED_ORDER_PRICE)
            .totalOrderAmount(TOTAL_ORDER_AMOUNT)
            .averagePositionPrice(AVERAGE_POSITION_PRICE)
            .initialCommission(INITIAL_COMMISSION)
            .executedCommission(EXECUTED_COMMISSION)
            .figi(FIGI)
            .direction(DIRECTION)
            .initialSecurityPrice(INITIAL_SECURITY_PRICE)
            .stages(STAGES)
            .serviceCommission(SERVICE_COMMISSION)
            .currency(CURRENCY)
            .orderType(ORDER_TYPE)
            .orderDate(ORDER_DATE)
            .instrumentUid(INSTRUMENT_UID)
            .orderRequestId(ORDER_REQUEST_ID)
            .build();

    public static final ru.tinkoff.piapi.contract.v1.OrderState TINKOFF_ORDER_STATE = ru.tinkoff.piapi.contract.v1.OrderState.newBuilder()
            .setOrderId(ORDER_ID)
            .setExecutionReportStatus(EXECUTION_REPORT_STATUS)
            .setLotsRequested(LOTS_REQUESTED)
            .setLotsExecuted(LOTS_EXECUTED)
            .setInitialOrderPrice(INITIAL_ORDER_PRICE)
            .setExecutedOrderPrice(EXECUTED_ORDER_PRICE)
            .setTotalOrderAmount(TOTAL_ORDER_AMOUNT)
            .setAveragePositionPrice(AVERAGE_POSITION_PRICE)
            .setInitialCommission(INITIAL_COMMISSION)
            .setExecutedCommission(EXECUTED_COMMISSION)
            .setFigi(FIGI)
            .setDirection(DIRECTION)
            .setInitialSecurityPrice(INITIAL_SECURITY_PRICE)
            .addAllStages(STAGES)
            .setServiceCommission(SERVICE_COMMISSION)
            .setCurrency(CURRENCY)
            .setOrderType(ORDER_TYPE)
            .setOrderDate(TimestampUtils.newTimestamp(ORDER_DATE))
            .setInstrumentUid(INSTRUMENT_UID)
            .setOrderRequestId(ORDER_REQUEST_ID)
            .build();

    public static final String JSON_STRING = "{\"orderId\":\"582899921200\"," +
            "\"executionReportStatus\":\"EXECUTION_REPORT_STATUS_NEW\"," +
            "\"lotsRequested\":1," +
            "\"lotsExecuted\":0," +
            "\"initialOrderPrice\":{\"currency\":\"usd\",\"units\":30,\"nano\":0}," +
            "\"executedOrderPrice\":{\"currency\":\"usd\",\"units\":0,\"nano\":0}," +
            "\"totalOrderAmount\":{\"currency\":\"usd\",\"units\":30,\"nano\":0}," +
            "\"averagePositionPrice\":{\"currency\":\"usd\",\"units\":0,\"nano\":0}," +
            "\"initialCommission\":{\"currency\":\"usd\",\"units\":0,\"nano\":90000000}," +
            "\"executedCommission\":{\"currency\":\"usd\",\"units\":0,\"nano\":0}," +
            "\"figi\":\"BBG00K7T3037\"," +
            "\"direction\":\"ORDER_DIRECTION_SELL\"," +
            "\"initialSecurityPrice\":{\"currency\":\"usd\",\"units\":30,\"nano\":0}," +
            "\"stages\":[" + TestOrderStage1.JSON_STRING + "," + TestOrderStage2.JSON_STRING + "]," +
            "\"serviceCommission\":{\"currency\":\"rub\",\"units\":0,\"nano\":0}," +
            "\"currency\":\"usd\"," +
            "\"orderType\":\"ORDER_TYPE_LIMIT\"," +
            "\"orderDate\":\"2023-08-10T08:20:27.000513541+03:00\"," +
            "\"instrumentUid\":\"46fef208-a525-4471-85e5-8fe4cee5f8ec\"," +
            "\"orderRequestId\":\"5627319091032181721\"}";

}