package ru.obukhov.trader.test.utils.model.orderstage;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.OrderStage;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;

public record TestOrderStage(OrderStage orderStage, ru.tinkoff.piapi.contract.v1.OrderStage tinkoffOrderStage, String jsonString) {

    TestOrderStage(final OrderStage orderStage) {
        this(orderStage, buildTinkoffOrderStage(orderStage), buildJsonString(orderStage));
    }

    private static ru.tinkoff.piapi.contract.v1.OrderStage buildTinkoffOrderStage(final OrderStage orderStage) {
        final MoneyValueMapper moneyValueMapper = Mappers.getMapper(MoneyValueMapper.class);

        return ru.tinkoff.piapi.contract.v1.OrderStage.newBuilder()
                .setPrice(moneyValueMapper.map(orderStage.price()))
                .setQuantity(orderStage.quantity())
                .setTradeId(orderStage.tradeId())
                .build();
    }

    private static String buildJsonString(final OrderStage orderStage) {
        return "{\"price\":" + orderStage.price() + "," +
                "\"quantity\":" + orderStage.quantity() + "," +
                "\"tradeId\":\"" + orderStage.tradeId() + "\"}";
    }

}