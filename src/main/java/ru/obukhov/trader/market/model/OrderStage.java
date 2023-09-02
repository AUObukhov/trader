package ru.obukhov.trader.market.model;

import lombok.Builder;
import ru.tinkoff.piapi.contract.v1.MoneyValue;

@Builder
public record OrderStage(
        MoneyValue price,
        long quantity,
        String tradeId
) {
}