package ru.obukhov.investor.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulatedPosition {

    private String ticker;

    private BigDecimal price;

    private int quantity;

}