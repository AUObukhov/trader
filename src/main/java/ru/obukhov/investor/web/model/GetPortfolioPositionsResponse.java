package ru.obukhov.investor.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.investor.model.PortfolioPosition;

import java.util.Collection;

@Data
@AllArgsConstructor
public class GetPortfolioPositionsResponse {

    private Collection<PortfolioPosition> positions;

}