package ru.obukhov.trader.test.utils.model.orderstage;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.OrderStage;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;

import java.math.BigDecimal;

public class TestOrderStage1 {

    private static final MoneyValueMapper MONEY_VALUE_MAPPER = Mappers.getMapper(MoneyValueMapper.class);

    public static final BigDecimal PRICE = DecimalUtils.setDefaultScale(30);
    public static final long QUANTITY = 2L;
    public static final String TRADE_ID = "1234567890"; // todo real data

    public static final OrderStage ORDER_STAGE = OrderStage.builder()
            .price(PRICE)
            .quantity(QUANTITY)
            .tradeId(TRADE_ID)
            .build();

    public static final ru.tinkoff.piapi.contract.v1.OrderStage TINKOFF_ORDER_STAGE = ru.tinkoff.piapi.contract.v1.OrderStage.newBuilder()
            .setPrice(MONEY_VALUE_MAPPER.map(PRICE))
            .setQuantity(QUANTITY)
            .setTradeId(TRADE_ID)
            .build();

    public static final String JSON_STRING = "{" +
            "\"price\":30.000000000," +
            "\"quantity\":2," +
            "\"tradeId\":\"1234567890\"" +
            "}";
}