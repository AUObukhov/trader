package ru.obukhov.trader.market.impl;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.SandboxRegisterRequest;
import ru.obukhov.trader.market.model.SandboxSetCurrencyBalanceRequest;
import ru.obukhov.trader.market.model.SandboxSetPositionBalanceRequest;
import ru.obukhov.trader.web.client.service.interfaces.SandboxClient;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Service to manage sandbox
 */
@AllArgsConstructor
public class SandboxService {

    private final TinkoffService tinkoffService;
    private final SandboxClient sandboxClient;

    public void register() throws IOException {
        sandboxClient.performRegistration(new SandboxRegisterRequest(null));
    }

    /**
     * Sets given value ({@code balance}) of balance of given {@code currency} at given {@code brokerAccountId}.
     * If {@code brokerAccountId} null, works with default broker account
     */
    public void setCurrencyBalance(@Nullable final String brokerAccountId, @NotNull final Currency currency, @NotNull final BigDecimal balance)
            throws IOException {
        final SandboxSetCurrencyBalanceRequest setCurrencyBalanceRequest = new SandboxSetCurrencyBalanceRequest(currency, balance);

        sandboxClient.setCurrencyBalance(setCurrencyBalanceRequest, brokerAccountId);
    }

    /**
     * Sets given value ({@code balance}) of balance of position with given {@code ticker} at given {@code brokerAccountId}.
     * If {@code brokerAccountId} null, works with default broker account
     */
    public void setPositionBalance(@Nullable final String brokerAccountId, @NotNull final String ticker, @NotNull final BigDecimal balance)
            throws IOException {
        final String figi = tinkoffService.getFigiByTicker(ticker);
        final SandboxSetPositionBalanceRequest setPositionBalanceRequest = new SandboxSetPositionBalanceRequest(figi, balance);

        sandboxClient.setPositionBalance(setPositionBalanceRequest, brokerAccountId);
    }

    /**
     * Clears all balances at given {@code brokerAccountId}.
     * If {@code brokerAccountId} null, works with default broker account
     */
    public void clearAll(@Nullable final String brokerAccountId) throws IOException {
        sandboxClient.clearAll(brokerAccountId);
    }

}