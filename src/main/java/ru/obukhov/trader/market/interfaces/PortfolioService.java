package ru.obukhov.trader.market.interfaces;

import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;

import java.math.BigDecimal;
import java.util.List;

public interface PortfolioService {

    List<PortfolioPosition> getPositions(@Nullable final String brokerAccountId);

    PortfolioPosition getPosition(@Nullable final String brokerAccountId, final String ticker);

    BigDecimal getAvailableBalance(@Nullable final String brokerAccountId, final Currency currency);

    List<CurrencyPosition> getCurrencies(@Nullable final String brokerAccountId);

}