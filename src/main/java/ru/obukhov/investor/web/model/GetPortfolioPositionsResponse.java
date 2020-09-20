package ru.obukhov.investor.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.util.List;

@Data
@AllArgsConstructor
public class GetPortfolioPositionsResponse {

    private List<Portfolio.PortfolioPosition> positions;

}