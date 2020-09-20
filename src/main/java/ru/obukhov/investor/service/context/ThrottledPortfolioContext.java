package ru.obukhov.investor.service.context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import ru.tinkoff.invest.openapi.PortfolioContext;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.util.concurrent.CompletableFuture;

@Component
public class ThrottledPortfolioContext extends ContextProxy<PortfolioContext> implements PortfolioContext {

    @NotNull
    @Override
    public CompletableFuture<Portfolio> getPortfolio(@Nullable String brokerAccountId) {
        return innerContext.getPortfolio(brokerAccountId);
    }

    @NotNull
    @Override
    public CompletableFuture<PortfolioCurrencies> getPortfolioCurrencies(@Nullable String brokerAccountId) {
        return innerContext.getPortfolioCurrencies(brokerAccountId);
    }

}