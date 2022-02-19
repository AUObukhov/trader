package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.transform.BigDecimalSerializer;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class PortfolioPosition {

    @Nullable
    private String ticker;

    @NotNull
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal balance;

    @Nullable
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal blocked;

    @NotNull
    private MoneyAmount expectedYield;

    /**
     * Count of securities, not lots
     */
    private int count;

    @Nullable
    private MoneyAmount averagePositionPrice;

    @Nullable
    private MoneyAmount averagePositionPriceNoNkd; // useful for bonds only

    @NotNull
    private String name;

}