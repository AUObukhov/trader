package ru.tinkoff.invest.openapi.okhttp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.Portfolio;

import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс работы с OpenAPI в части, касающейся получения информации о состоянии портфеля.
 */
public interface PortfolioContext extends Context {

    /**
     * Асинхронное получение информации по портфелю инструментов.
     *
     * @param brokerAccountId Идентификатор брокерского счёта.
     * @return Портфель инструментов.
     */
    @NotNull
    CompletableFuture<Portfolio> getPortfolio(@Nullable String brokerAccountId);

    /**
     * Асинхронное получение информации по валютным активам.
     *
     * @param brokerAccountId Идентификатор брокерского счёта.
     * @return Портфель валют.
     */
    @NotNull
    CompletableFuture<Currencies> getPortfolioCurrencies(@Nullable String brokerAccountId);

}
