package ru.obukhov.trader.trading.model;

import java.math.BigDecimal;

public record BackTestPosition(String figi, BigDecimal price, BigDecimal quantity) {

    public BigDecimal getTotalPrice() {
        return price.multiply(quantity);
    }

}