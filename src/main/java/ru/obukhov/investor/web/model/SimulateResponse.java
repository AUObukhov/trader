package ru.obukhov.investor.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SimulateResponse {

    private BigDecimal balance;

}