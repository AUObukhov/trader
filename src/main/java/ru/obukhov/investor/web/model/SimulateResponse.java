package ru.obukhov.investor.web.model;

import lombok.Builder;
import lombok.Data;
import ru.tinkoff.invest.openapi.models.operations.Operation;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class SimulateResponse {

    private BigDecimal balance;

    private BigDecimal fullBalance;

    private List<Portfolio.PortfolioPosition> positions;

    private List<Operation> operations;

}