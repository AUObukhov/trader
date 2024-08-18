package ru.obukhov.trader.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.core.InvestApi;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class InvestApiDisposer implements DisposableBean {
    private final InvestApi investApi;

    public void destroy() {
        investApi.destroy(60);
    }

}