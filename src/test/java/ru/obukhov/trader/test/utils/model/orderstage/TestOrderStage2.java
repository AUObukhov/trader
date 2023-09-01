package ru.obukhov.trader.test.utils.model.orderstage;

import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.OrderStage;

public class TestOrderStage2 {

    public static final MoneyValue PRICE = TestData.createMoneyValue(10.0, "usd");
    public static final long QUANTITY = 1L;
    public static final String TRADE_ID = "abcdef"; // todo real data

    public static final OrderStage ORDER_STAGE = OrderStage.newBuilder()
            .setPrice(PRICE)
            .setQuantity(QUANTITY)
            .setTradeId(TRADE_ID)
            .build();

    public static final String JSON_STRING = "{" +
            "\"price\":{\"currency\":\"usd\",\"units\":10,\"nano\":0}," +
            "\"quantity\":1," +
            "\"tradeId\":\"abcdef\"" +
            "}";
}