package ru.obukhov.trader.market.model;

import java.math.BigDecimal;

public record SandboxSetCurrencyBalanceRequest(Currency currency, BigDecimal balance) {
}