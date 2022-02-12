package ru.obukhov.trader.market.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.SandboxSetCurrencyBalanceRequest;
import ru.obukhov.trader.market.model.SandboxSetPositionBalanceRequest;
import ru.tinkoff.invest.openapi.okhttp.OpenApi;
import ru.tinkoff.invest.openapi.okhttp.SandboxContext;

import java.math.BigDecimal;

/**
 * Service to manage sandbox
 */
public class SandboxService {

    private final MarketService marketService;
    private final SandboxContext sandboxContext;

    public SandboxService(final OpenApi opeApi, final MarketService marketService) {
        this.marketService = marketService;
        this.sandboxContext = opeApi.getSandboxContext();
    }

    /**
     * Sets given value ({@code balance}) of balance of given {@code currency} at given {@code brokerAccountId}.
     * If {@code brokerAccountId} null, works with default broker account
     */
    public void setCurrencyBalance(
            @NotNull final Currency currency,
            @NotNull final BigDecimal balance,
            @Nullable final String brokerAccountId
    ) {
        final SandboxSetCurrencyBalanceRequest setCurrencyBalanceRequest = new SandboxSetCurrencyBalanceRequest()
                .currency(currency)
                .balance(balance);

        sandboxContext.setCurrencyBalance(setCurrencyBalanceRequest, brokerAccountId).join();
    }

    /**
     * Sets given value ({@code balance}) of balance of position with given {@code ticker} at given {@code brokerAccountId}.
     * If {@code brokerAccountId} null, works with default broker account
     */
    public void setPositionBalance(@NotNull final String ticker, @NotNull final BigDecimal balance, @Nullable final String brokerAccountId) {
        final SandboxSetPositionBalanceRequest setPositionBalanceRequest = new SandboxSetPositionBalanceRequest()
                .figi(marketService.getFigi(ticker))
                .balance(balance);

        sandboxContext.setPositionBalance(setPositionBalanceRequest, brokerAccountId).join();
    }

    /**
     * Clears all balances at given {@code brokerAccountId}.
     * If {@code brokerAccountId} null, works with default broker account
     */
    public void clearAll(@Nullable final String brokerAccountId) {
        sandboxContext.clearAll(brokerAccountId).join();
    }

}