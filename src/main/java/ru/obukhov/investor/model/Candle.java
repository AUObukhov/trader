package ru.obukhov.investor.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import ru.obukhov.investor.model.transform.MoneySerializer;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class Candle {

    @JsonSerialize(using = MoneySerializer.class)
    public BigDecimal openPrice;
    @JsonSerialize(using = MoneySerializer.class)
    public BigDecimal closePrice;
    @JsonSerialize(using = MoneySerializer.class)
    public BigDecimal saldo;
    @JsonSerialize(using = MoneySerializer.class)
    public BigDecimal highestPrice;
    @JsonSerialize(using = MoneySerializer.class)
    public BigDecimal lowestPrice;

    public OffsetDateTime time;

}