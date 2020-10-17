package ru.obukhov.investor.bot.interfaces;

import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface MarketMock {

    OffsetDateTime getCurrentDateTime();

    void init(OffsetDateTime currentDateTime, BigDecimal balance);

    void setCurrentDateTime(OffsetDateTime currentDateTime);

    BigDecimal getBalance();

    void setBalance(BigDecimal balance);

    Portfolio.PortfolioPosition getPosition(String ticker);

    void nextMinute();

    void performOperation(@NotNull String ticker, int lots, @NotNull ru.tinkoff.invest.openapi.models.orders.Operation operation, BigDecimal price);
}