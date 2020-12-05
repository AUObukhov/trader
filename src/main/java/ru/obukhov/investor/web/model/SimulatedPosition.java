package ru.obukhov.investor.web.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class SimulatedPosition {

    private String ticker;

    private BigDecimal price;

    private int quantity;

}