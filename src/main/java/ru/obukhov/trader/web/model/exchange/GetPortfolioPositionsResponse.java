package ru.obukhov.trader.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.trader.market.model.PortfolioPosition;

import java.util.Collection;

@Data
@AllArgsConstructor
public class GetPortfolioPositionsResponse {

    private Collection<PortfolioPosition> positions;

}