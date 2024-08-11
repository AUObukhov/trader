package ru.obukhov.trader.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeightedShare {
    private String figi;
    private String ticker;
    private String name;
    private BigDecimal priceRub;
    private BigDecimal capitalizationWeight;
    private int lot;
    private BigDecimal lotPriceRub;
    private int portfolioSharesQuantity;
    private BigDecimal totalPriceRub;
    private BigDecimal portfolioWeight;
    private BigDecimal needToBuy;
}