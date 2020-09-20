package ru.obukhov.investor.service.context;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.obukhov.investor.service.aop.Throttled;
import ru.tinkoff.invest.openapi.SandboxContext;
import ru.tinkoff.invest.openapi.models.sandbox.CurrencyBalance;
import ru.tinkoff.invest.openapi.models.sandbox.PositionBalance;
import ru.tinkoff.invest.openapi.models.user.BrokerAccountType;

import java.util.concurrent.CompletableFuture;

@Component
@ConditionalOnProperty(value = "trading.sandbox", havingValue = "true")
public class ThrottledSandboxContext implements SandboxContext {

    @Setter
    private SandboxContext innerContext;

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<Void> performRegistration(@Nullable BrokerAccountType brokerAccountType) {
        return innerContext.performRegistration(brokerAccountType);
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<Void> setCurrencyBalance(@NotNull CurrencyBalance data, @Nullable String brokerAccountId) {
        return innerContext.setCurrencyBalance(data, brokerAccountId);
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<Void> setPositionBalance(@NotNull PositionBalance data, @Nullable String brokerAccountId) {
        return innerContext.setPositionBalance(data, brokerAccountId);
    }

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<Void> clearAll(@Nullable String brokerAccountId) {
        return innerContext.clearAll(brokerAccountId);
    }

    @NotNull
    @Override
    public String getPath() {
        return innerContext.getPath();
    }

}