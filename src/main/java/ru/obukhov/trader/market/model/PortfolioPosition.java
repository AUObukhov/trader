package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.transform.BigDecimalSerializer;

import java.math.BigDecimal;

public record PortfolioPosition(
        @Nullable String ticker,
        @NotNull @JsonSerialize(using = BigDecimalSerializer.class) BigDecimal balance,
        @Nullable @JsonSerialize(using = BigDecimalSerializer.class) BigDecimal blocked,
        @NotNull MoneyAmount expectedYield,
        @Nullable Long count, // Count of securities, not lots
        @Nullable MoneyAmount averagePositionPrice,
        @Nullable MoneyAmount averagePositionPriceNoNkd, // useful for bonds only
        @NotNull String name
) {
}