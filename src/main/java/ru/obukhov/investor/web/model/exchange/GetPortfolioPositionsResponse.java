package ru.obukhov.investor.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.investor.market.model.PortfolioPosition;

import java.util.Collection;

@Data
@AllArgsConstructor
public class GetPortfolioPositionsResponse {

    private Collection<PortfolioPosition> positions;

}