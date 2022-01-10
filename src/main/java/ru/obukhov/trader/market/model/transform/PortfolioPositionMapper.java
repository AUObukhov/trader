package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.trader.market.model.PortfolioPosition;

import java.util.Collection;
import java.util.List;

/**
 * Maps {@link PortfolioPosition} to {@link PortfolioPosition}
 */
@Mapper(uses = MoneyAmountMapper.class)
public interface PortfolioPositionMapper {

    @Mapping(source = "expectedYield.currency", target = "currency")
    @Mapping(source = "expectedYield.value", target = "expectedYield")
    @Mapping(source = "lots", target = "count")
    @Mapping(source = "averagePositionPrice.value", target = "averagePositionPrice")
    @Mapping(source = "averagePositionPriceNoNkd.value", target = "averagePositionPriceNoNkd")
    PortfolioPosition map(final ru.tinkoff.invest.openapi.model.rest.PortfolioPosition source);

    List<PortfolioPosition> map(final Collection<ru.tinkoff.invest.openapi.model.rest.PortfolioPosition> source);

}