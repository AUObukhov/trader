package ru.obukhov.investor.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class Candle {

    public BigDecimal openPrice;
    public BigDecimal closePrice;
    public BigDecimal saldo;
    public BigDecimal highestPrice;
    public BigDecimal lowestPrice;
    public OffsetDateTime time;

}