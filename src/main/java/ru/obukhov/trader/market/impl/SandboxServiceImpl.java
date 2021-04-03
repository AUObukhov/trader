package ru.obukhov.trader.market.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.SandboxService;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.SandboxContext;
import ru.tinkoff.invest.openapi.model.rest.SandboxCurrency;
import ru.tinkoff.invest.openapi.model.rest.SandboxSetCurrencyBalanceRequest;
import ru.tinkoff.invest.openapi.model.rest.SandboxSetPositionBalanceRequest;

import java.math.BigDecimal;

public class SandboxServiceImpl implements SandboxService {

    private final MarketService marketService;
    private final SandboxContext sandboxContext;

    public SandboxServiceImpl(OpenApi opeApi, MarketService marketService) {
        this.marketService = marketService;
        this.sandboxContext = opeApi.getSandboxContext();
    }

    @Override
    public void setCurrencyBalance(
            @NotNull SandboxCurrency currency,
            @NotNull BigDecimal balance,
            @Nullable String brokerAccountId
    ) {

        SandboxSetCurrencyBalanceRequest setCurrencyBalanceRequest = new SandboxSetCurrencyBalanceRequest()
                .currency(currency)
                .balance(balance);

        sandboxContext.setCurrencyBalance(setCurrencyBalanceRequest, brokerAccountId).join();

    }

    @Override
    public void setPositionBalance(
            @NotNull String ticker,
            @NotNull BigDecimal balance,
            @Nullable String brokerAccountId
    ) {

        SandboxSetPositionBalanceRequest setPositionBalanceRequest = new SandboxSetPositionBalanceRequest()
                .figi(marketService.getFigi(ticker))
                .balance(balance);

        sandboxContext.setPositionBalance(setPositionBalanceRequest, brokerAccountId).join();

    }

    @Override
    public void clearAll(@Nullable String brokerAccountId) {
        sandboxContext.clearAll(brokerAccountId).join();
    }

}