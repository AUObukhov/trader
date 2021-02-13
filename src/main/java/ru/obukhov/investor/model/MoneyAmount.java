package ru.obukhov.investor.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.investor.model.transform.BigDecimalSerializer;
import ru.tinkoff.invest.openapi.models.Currency;

import java.math.BigDecimal;

@Data
public class MoneyAmount {

    @NotNull
    private final Currency currency;

    @NotNull
    @JsonSerialize(using = BigDecimalSerializer.class)
    private final BigDecimal value;

}