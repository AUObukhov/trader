package ru.obukhov.investor.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.investor.web.model.SimulatedPosition;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

/**
 * Maps {@link Portfolio.PortfolioPosition} to {@link SimulatedPosition}
 */
@Mapper
public interface PositionMapper {

    @Mapping(target = "price", source = "balance")
    @Mapping(target = "quantity", source = "lots")
    SimulatedPosition map(Portfolio.PortfolioPosition source);

}