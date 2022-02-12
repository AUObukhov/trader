package ru.tinkoff.invest.openapi.okhttp;

import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.market.model.UserAccounts;

import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс работы с OpenAPI в части, касающейся получения информации о клиенте.
 */
public interface UserContext extends Context {

    /**
     * Асинхронное получение списка брокерских счетов.
     *
     * @return Список счетов.
     */
    @NotNull
    CompletableFuture<UserAccounts> getAccounts();

}
