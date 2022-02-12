package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.transform.BigDecimalSerializer;

import java.math.BigDecimal;

@Data
@Builder
public class PortfolioPosition {

    @Nullable
    private final String ticker;

    @NotNull
    @JsonSerialize(using = BigDecimalSerializer.class)
    private final BigDecimal balance;

    @Nullable
    @JsonSerialize(using = BigDecimalSerializer.class)
    private final BigDecimal blocked;

    @Nullable
    private final Currency currency;

    @Nullable
    private final BigDecimal expectedYield;

    /**
     * Count of securities, not lots
     */
    private final int count;

    @Nullable
    private final BigDecimal averagePositionPrice;

    @Nullable
    private final BigDecimal averagePositionPriceNoNkd; // useful for bonds only

    @NotNull
    private final String name;

}