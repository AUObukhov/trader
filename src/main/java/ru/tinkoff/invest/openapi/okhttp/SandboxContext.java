package ru.tinkoff.invest.openapi.okhttp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.model.SandboxAccount;
import ru.obukhov.trader.market.model.SandboxRegisterRequest;
import ru.obukhov.trader.market.model.SandboxSetCurrencyBalanceRequest;
import ru.obukhov.trader.market.model.SandboxSetPositionBalanceRequest;

import java.io.IOException;

public interface SandboxContext {

    SandboxAccount performRegistration(@NotNull SandboxRegisterRequest registerRequest) throws IOException;

    void setCurrencyBalance(@NotNull SandboxSetCurrencyBalanceRequest balanceRequest, @Nullable String brokerAccountId) throws IOException;

    void setPositionBalance(@NotNull SandboxSetPositionBalanceRequest balanceRequest, @Nullable String brokerAccountId) throws IOException;

    void removeAccount(@Nullable String brokerAccountId) throws IOException;

    void clearAll(@Nullable String brokerAccountId) throws IOException;
}
