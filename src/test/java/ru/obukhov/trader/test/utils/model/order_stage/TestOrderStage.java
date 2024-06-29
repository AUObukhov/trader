package ru.obukhov.trader.test.utils.model.order_stage;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.OrderStage;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;

public record TestOrderStage(OrderStage orderStage, ru.tinkoff.piapi.contract.v1.OrderStage tinkoffOrderStage) {

    TestOrderStage(final OrderStage orderStage) {
        this(orderStage, buildTinkoffOrderStage(orderStage));
    }

    private static ru.tinkoff.piapi.contract.v1.OrderStage buildTinkoffOrderStage(final OrderStage orderStage) {
        final MoneyValueMapper moneyValueMapper = Mappers.getMapper(MoneyValueMapper.class);

        return ru.tinkoff.piapi.contract.v1.OrderStage.newBuilder()
                .setPrice(moneyValueMapper.map(orderStage.price()))
                .setQuantity(orderStage.quantity())
                .setTradeId(orderStage.tradeId())
                .build();
    }

}