package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.tinkoff.piapi.core.models.Position;

/**
 * Maps {@link ru.tinkoff.piapi.core.models.Position} to {@link ru.obukhov.trader.market.model.PortfolioPosition}
 */
@Mapper
public interface PositionMapper {

    PortfolioPosition map(final String figi, final Position source);

}