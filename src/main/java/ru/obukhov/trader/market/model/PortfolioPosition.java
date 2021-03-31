package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.transform.BigDecimalSerializer;
import ru.tinkoff.invest.openapi.model.rest.Currency;

import java.math.BigDecimal;

@Data
public class PortfolioPosition {

    @Nullable
    private final String ticker;

    @NotNull
    @JsonSerialize(using = BigDecimalSerializer.class)
    private final BigDecimal balance;

    @Nullable
    @JsonSerialize(using = BigDecimalSerializer.class)
    private final BigDecimal blocked;

    @NotNull
    private final Currency currency;

    @Nullable
    private final BigDecimal expectedYield;

    private final int lotsCount;

    @Nullable
    private final BigDecimal averagePositionPrice;

    @Nullable
    private final BigDecimal averagePositionPriceNoNkd; // useful for bonds only

    @NotNull
    private final String name;

}