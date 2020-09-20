package ru.obukhov.investor.service.context;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import ru.tinkoff.invest.openapi.Context;

public abstract class ContextProxy<T extends Context> implements Context {
    @Setter
    protected T innerContext;

    @NotNull
    @Override
    public String getPath() {
        return innerContext.getPath();
    }

}