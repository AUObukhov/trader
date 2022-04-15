package ru.obukhov.trader.market.model;

import java.math.BigDecimal;
import java.util.List;

public record Orderbook(
        String figi,
        Integer depth,
        List<OrderResponse> bids,
        List<OrderResponse> asks,
        TradeStatus tradeStatus,
        BigDecimal minPriceIncrement,
        BigDecimal faceValue,
        BigDecimal lastPrice,
        BigDecimal closePrice,
        BigDecimal limitUp,
        BigDecimal limitDown
) {
}