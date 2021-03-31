package ru.obukhov.trader.market.interfaces;

import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface PortfolioService {

    Collection<PortfolioPosition> getPositions();

    PortfolioPosition getPosition(String ticker);

    BigDecimal getAvailableBalance(Currency currency);

    List<CurrencyPosition> getCurrencies();

}