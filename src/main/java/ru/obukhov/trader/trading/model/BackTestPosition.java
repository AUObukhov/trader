package ru.obukhov.trader.trading.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BackTestPosition {

    private String ticker;

    private BigDecimal price;

    private int quantity;

}