package ru.obukhov.investor.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@AllArgsConstructor
public class GetSaldosResponse {
    private Map<?, BigDecimal> saldos;

}
