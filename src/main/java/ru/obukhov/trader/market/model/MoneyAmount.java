package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.common.model.transform.BigDecimalSerializer;

import java.math.BigDecimal;

public record MoneyAmount(
        @NotNull Currency currency,
        @NotNull @JsonSerialize(using = BigDecimalSerializer.class) BigDecimal value
) {
}