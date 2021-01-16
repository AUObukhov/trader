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

    private BigDecimal highestPrice;

    private BigDecimal lowestPrice;

    private OffsetDateTime time;

}