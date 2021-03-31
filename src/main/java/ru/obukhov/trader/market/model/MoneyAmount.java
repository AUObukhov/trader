package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.common.model.transform.BigDecimalSerializer;
import ru.tinkoff.invest.openapi.model.rest.Currency;

import java.math.BigDecimal;

@Data
public class MoneyAmount {

    @NotNull
    private final Currency currency;

    @NotNull
    @JsonSerialize(using = BigDecimalSerializer.class)
    private final BigDecimal value;

}