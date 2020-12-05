package ru.obukhov.investor.model.transform;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.investor.web.model.SimulatedPosition;
import ru.tinkoff.invest.openapi.models.portfolio.InstrumentType;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.math.BigDecimal;

public class PositionMapperTest {

    private final PositionMapper positionMapper = Mappers.getMapper(PositionMapper.class);

    @Test
    public void mapsTickerAndPriceAndQuantity() {
        String ticker = "ticker";
        BigDecimal balance = BigDecimal.TEN;
        int lots = 5;
        Portfolio.PortfolioPosition source = createPosition(ticker, balance, lots);

        SimulatedPosition target = positionMapper.map(source);

        Assert.assertEquals(ticker, target.getTicker());
        Assert.assertEquals(balance, target.getPrice());
        Assert.assertEquals(lots, target.getQuantity());
    }

    private Portfolio.PortfolioPosition createPosition(String ticker, BigDecimal balance, int lots) {
        return new Portfolio.PortfolioPosition(StringUtils.EMPTY,
                ticker,
                null,
                InstrumentType.Stock,
                balance,
                null,
                null,
                lots,
                null,
                null,
                StringUtils.EMPTY);
    }

}