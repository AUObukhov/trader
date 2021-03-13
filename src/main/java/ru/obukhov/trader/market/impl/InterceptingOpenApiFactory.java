package ru.obukhov.trader.market.impl;

import lombok.extern.java.Log;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import ru.obukhov.trader.config.TradingProperties;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.SandboxOpenApi;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApi;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApiFactory;
import ru.tinkoff.invest.openapi.okhttp.OkHttpSandboxOpenApi;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.Executor;

/**
 * Bean extension of OkHttpOpenApiFactory.
 * Adds all beans of type {@link Interceptor} to OkHttpClient.
 */
@Log
@Component
public class InterceptingOpenApiFactory extends OkHttpOpenApiFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public InterceptingOpenApiFactory(TradingProperties tradingProperties) {
        super(tradingProperties.getToken(), log);
    }

    @NotNull
    @Override
    public OpenApi createOpenApiClient(@NotNull final Executor executor) {
        return OkHttpOpenApi.create(
                createClient(),
                this.config.marketApiUrl,
                config.streamingUrl,
                config.streamingParallelism,
                authToken,
                executor,
                logger
        );
    }

    @NotNull
    @Override
    public SandboxOpenApi createSandboxOpenApiClient(@NotNull final Executor executor) {
        return OkHttpSandboxOpenApi.create(
                createClient(),
                this.config.marketApiUrl,
                config.streamingUrl,
                config.streamingParallelism,
                authToken,
                executor,
                logger
        );
    }

    private OkHttpClient createClient() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().pingInterval(Duration.ofSeconds(5));
        Collection<Interceptor> interceptors = applicationContext.getBeansOfType(Interceptor.class).values();
        clientBuilder.interceptors().addAll(interceptors);

        return clientBuilder.build();
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

}