package ru.obukhov.trader.test.utils.model.orderstate;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.orderstage.TestOrderStage1;
import ru.obukhov.trader.test.utils.model.orderstage.TestOrderStage2;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus;
import ru.tinkoff.piapi.contract.v1.OrderStage;
import ru.tinkoff.piapi.contract.v1.OrderState;
import ru.tinkoff.piapi.contract.v1.OrderType;

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
    public static final Timestamp ORDER_DATE = TimestampUtils.newTimestamp(1691644827, 513541000);
    public static final String INSTRUMENT_UID = "46fef208-a525-4471-85e5-8fe4cee5f8ec";

    public static final OrderState ORDER_STATE = OrderState.newBuilder()
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
            .setOrderDate(ORDER_DATE)
            .setInstrumentUid(INSTRUMENT_UID)
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
            "\"stages\":[" +
            "{\"price\":{\"currency\":\"usd\",\"units\":30,\"nano\":0},\"quantity\":2,\"tradeId\":\"1234567890\"}," +
            "{\"price\":{\"currency\":\"usd\",\"units\":10,\"nano\":0},\"quantity\":1,\"tradeId\":\"abcdef\"}" +
            "]," +
            "\"serviceCommission\":{\"currency\":\"rub\",\"units\":0,\"nano\":0}," +
            "\"currency\":\"usd\"," +
            "\"orderType\":\"ORDER_TYPE_LIMIT\"," +
            "\"orderDate\":{\"seconds\":1691644827,\"nanos\":513541000}," +
            "\"instrumentUid\":\"46fef208-a525-4471-85e5-8fe4cee5f8ec\"}";

}