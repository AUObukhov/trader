package ru.obukhov.investor.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class Candle {

    private BigDecimal openPrice;

    private BigDecimal closePrice;

    /**
     * equals closePrice - openPrice
     */
    private BigDecimal saldo;

    private BigDecimal highestPrice;

    private BigDecimal lowestPrice;

    private OffsetDateTime time;

}