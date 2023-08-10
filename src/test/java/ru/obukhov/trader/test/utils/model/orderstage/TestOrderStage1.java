package ru.obukhov.trader.test.utils.model.orderstage;

import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.OrderStage;

public class TestOrderStage1 {

    public static final MoneyValue PRICE = TestData.createMoneyValue(30.0, "usd");
    public static final long QUANTITY = 2L;
    public static final String TRADE_ID = "1234567890"; // todo real data

    public static final OrderStage ORDER_STAGE = OrderStage.newBuilder()
            .setPrice(PRICE)
            .setQuantity(QUANTITY)
            .setTradeId(TRADE_ID)
            .build();

    public static final String STRING = "{\"orderId\":{\"currency\":\"usd\",\"units\":30,\"nano\":0},\"executionReportStatus\":2,\"lotsRequested\":\"1234567890\"}";
}