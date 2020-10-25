package ru.obukhov.investor.web.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class SimulateResponse {

    private BigDecimal totalBalance;

    private BigDecimal currencyBalance;

    private List<SimulatedPosition> positions;

    private List<SimulatedOperation> operations;

}