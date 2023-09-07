package ru.obukhov.trader.test.utils.model.orderstate;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.OrderStage;
import ru.obukhov.trader.market.model.OrderState;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;
import ru.obukhov.trader.market.model.transform.OrderStageMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.orderstage.TestOrderStages;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus;
import ru.tinkoff.piapi.contract.v1.OrderType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class TestOrderState1 {

    private static final OrderStageMapper ORDER_STAGE_MAPPER = Mappers.getMapper(OrderStageMapper.class);
    private static final MoneyValueMapper MONEY_VALUE_MAPPER = Mappers.getMapper(MoneyValueMapper.class);

    public static final String ORDER_ID = "582899921200";
    public static final OrderExecutionReportStatus EXECUTION_REPORT_STATUS = OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_NEW;
    public static final long LOTS_REQUESTED = 1L;
    public static final long LOTS_EXECUTED = 0L;
    public static final BigDecimal INITIAL_ORDER_PRICE = DecimalUtils.setDefaultScale(30);
    public static final BigDecimal EXECUTED_ORDER_PRICE = DecimalUtils.ZERO;
    public static final BigDecimal TOTAL_ORDER_AMOUNT = DecimalUtils.setDefaultScale(30);
    public static final BigDecimal AVERAGE_POSITION_PRICE = DecimalUtils.ZERO;
    public static final BigDecimal INITIAL_COMMISSION = DecimalUtils.setDefaultScale(0.09);
    public static final BigDecimal EXECUTED_COMMISSION = DecimalUtils.ZERO;
    public static final String FIGI = "BBG00K7T3037";
    public static final OrderDirection DIRECTION = OrderDirection.ORDER_DIRECTION_SELL;
    public static final BigDecimal INITIAL_SECURITY_PRICE = DecimalUtils.setDefaultScale(30);
    public static final List<OrderStage> STAGES = List.of(TestOrderStages.ORDER_STAGE1.orderStage(), TestOrderStages.ORDER_STAGE2.orderStage());
    public static final BigDecimal SERVICE_COMMISSION = DecimalUtils.ZERO;
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
            .setInitialOrderPrice(MONEY_VALUE_MAPPER.map(INITIAL_ORDER_PRICE, CURRENCY))
            .setExecutedOrderPrice(MONEY_VALUE_MAPPER.map(EXECUTED_ORDER_PRICE, CURRENCY))
            .setTotalOrderAmount(MONEY_VALUE_MAPPER.map(TOTAL_ORDER_AMOUNT, CURRENCY))
            .setAveragePositionPrice(MONEY_VALUE_MAPPER.map(AVERAGE_POSITION_PRICE, CURRENCY))
            .setInitialCommission(MONEY_VALUE_MAPPER.map(INITIAL_COMMISSION, CURRENCY))
            .setExecutedCommission(MONEY_VALUE_MAPPER.map(EXECUTED_COMMISSION, CURRENCY))
            .setFigi(FIGI)
            .setDirection(DIRECTION)
            .setInitialSecurityPrice(MONEY_VALUE_MAPPER.map(INITIAL_SECURITY_PRICE, CURRENCY))
            .addAllStages(STAGES.stream().map(ORDER_STAGE_MAPPER::map).toList())
            .setServiceCommission(MONEY_VALUE_MAPPER.map(SERVICE_COMMISSION, Currencies.RUB))
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
            "\"initialOrderPrice\":30.000000000," +
            "\"executedOrderPrice\":0.000000000," +
            "\"totalOrderAmount\":30.000000000," +
            "\"averagePositionPrice\":0.000000000," +
            "\"initialCommission\":0.090000000," +
            "\"executedCommission\":0.000000000," +
            "\"figi\":\"BBG00K7T3037\"," +
            "\"direction\":\"ORDER_DIRECTION_SELL\"," +
            "\"initialSecurityPrice\":30.000000000," +
            "\"stages\":[" + TestOrderStages.ORDER_STAGE1.jsonString() + "," + TestOrderStages.ORDER_STAGE2.jsonString() + "]," +
            "\"serviceCommission\":0.000000000," +
            "\"currency\":\"usd\"," +
            "\"orderType\":\"ORDER_TYPE_LIMIT\"," +
            "\"orderDate\":\"2023-08-10T08:20:27.000513541+03:00\"," +
            "\"instrumentUid\":\"46fef208-a525-4471-85e5-8fe4cee5f8ec\"," +
            "\"orderRequestId\":\"5627319091032181721\"}";

}