package ru.obukhov.investor.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.investor.model.PortfolioPosition;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.util.Collection;

/**
 * Maps {@link Portfolio.PortfolioPosition} to {@link PortfolioPosition}
 */
@Mapper(uses = MoneyAmountMapper.class)
public interface PortfolioPositionMapper {

    @Mapping(source = "expectedYield.currency", target = "currency")
    @Mapping(source = "expectedYield.value", target = "expectedYield")
    @Mapping(source = "lots", target = "lotsCount")
    @Mapping(source = "averagePositionPrice.value", target = "averagePositionPrice")
    @Mapping(source = "averagePositionPriceNoNkd.value", target = "averagePositionPriceNoNkd")
    PortfolioPosition map(Portfolio.PortfolioPosition source);

    Collection<PortfolioPosition> map(Collection<Portfolio.PortfolioPosition> source);

}