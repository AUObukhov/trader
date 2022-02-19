package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.common.model.transform.BigDecimalSerializer;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoneyAmount {

    @NotNull
    private Currency currency;

    @NotNull
    @JsonSerialize(using = BigDecimalSerializer.class)
    private BigDecimal value;

}