package ru.obukhov.trader.market.model;

import java.math.BigDecimal;

public record SandboxSetPositionBalanceRequest(String figi, BigDecimal balance) {
}