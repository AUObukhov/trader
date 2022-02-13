package ru.obukhov.trader.web.client.service;

import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.model.CurrencyPosition;
import ru.obukhov.trader.market.model.PortfolioPosition;

import java.io.IOException;
import java.util.List;

public interface PortfolioClient {

    List<PortfolioPosition> getPortfolio(@Nullable final String brokerAccountId) throws IOException;

    List<CurrencyPosition> getPortfolioCurrencies(@Nullable final String brokerAccountId) throws IOException;

}
