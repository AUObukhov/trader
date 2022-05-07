package ru.obukhov.trader.market.model;

import ru.tinkoff.piapi.contract.v1.OperationType;

import java.math.BigDecimal;

public record LimitOrderRequest(Integer lots, OperationType operation, BigDecimal price) {
}