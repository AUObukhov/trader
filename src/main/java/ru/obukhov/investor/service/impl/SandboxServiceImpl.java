package ru.obukhov.investor.service.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.service.interfaces.ConnectionService;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.service.interfaces.SandboxService;
import ru.tinkoff.invest.openapi.SandboxContext;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.sandbox.CurrencyBalance;
import ru.tinkoff.invest.openapi.models.sandbox.PositionBalance;

import java.math.BigDecimal;

@Service
@ConditionalOnProperty(value = "trading.sandbox", havingValue = "true")
public class SandboxServiceImpl implements SandboxService {

    private final MarketService marketService;
    private final SandboxContext sandboxContext;

    public SandboxServiceImpl(ConnectionService connectionService, MarketService marketService) {
        this.marketService = marketService;
        this.sandboxContext = connectionService.getSandboxContext();
    }

    @Override
    public void setCurrencyBalance(@NotNull Currency currency,
                                   @NotNull BigDecimal balance,
                                   @Nullable String brokerAccountId) {

        CurrencyBalance currencyBalance = new CurrencyBalance(currency, balance);
        sandboxContext.setCurrencyBalance(currencyBalance, brokerAccountId).join();

    }

    @Override
    public void setPositionBalance(@NotNull String ticker,
                                   @NotNull BigDecimal balance,
                                   @Nullable String brokerAccountId) {

        String figi = marketService.getFigi(ticker);
        PositionBalance positionBalance = new PositionBalance(figi, balance);
        sandboxContext.setPositionBalance(positionBalance, brokerAccountId).join();

    }

    @Override
    public void clearAll(@Nullable String brokerAccountId) {
        sandboxContext.clearAll(brokerAccountId).join();
    }

}