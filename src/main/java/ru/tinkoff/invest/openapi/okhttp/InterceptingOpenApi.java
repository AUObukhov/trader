package ru.tinkoff.invest.openapi.okhttp;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.OperationsContext;
import ru.tinkoff.invest.openapi.OrdersContext;
import ru.tinkoff.invest.openapi.PortfolioContext;
import ru.tinkoff.invest.openapi.SandboxContext;
import ru.tinkoff.invest.openapi.StreamingContext;
import ru.tinkoff.invest.openapi.UserContext;
import ru.tinkoff.invest.openapi.model.rest.SandboxRegisterRequest;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Almost same as {@link OkHttpOpenApi}, but with adding of interceptors to {@link OkHttpClient}.
 * Located in tinkoff package due to using of tinkoff package private classes
 */
@Component
public class InterceptingOpenApi extends OpenApi {

    private final Executor executor;
    private final OkHttpClient client;
    private final String apiUrl;

    private SandboxContext sandboxContext;
    private OrdersContext ordersContext;
    private PortfolioContext portfolioContext;
    private MarketContext marketContext;
    private OperationsContext operationsContext;
    private UserContext userContext;
    private StreamingContext streamingContext;

    public InterceptingOpenApi(TradingProperties tradingProperties, List<Interceptor> interceptors) {
        super(tradingProperties.getToken(), tradingProperties.isSandbox());
        this.executor = ForkJoinPool.commonPool();
        this.client = createClient(interceptors);

        if (tradingProperties.isSandbox()) {
            this.apiUrl = this.config.sandboxApiUrl;
            getSandboxContext().performRegistration(new SandboxRegisterRequest()).join();
        } else {
            this.apiUrl = this.config.marketApiUrl;
        }
    }

    private OkHttpClient createClient(List<Interceptor> interceptors) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().pingInterval(Duration.ofSeconds(5));
        clientBuilder.interceptors().addAll(interceptors);
        return clientBuilder.build();
    }

    @Override
    public void close() {
        this.client.dispatcher().executorService().shutdown();
    }

    @NotNull
    public SandboxContext getSandboxContext() {
        if (this.isSandboxMode) {
            if (this.sandboxContext == null) {
                this.sandboxContext = new SandboxContextImpl(client, apiUrl, authToken);
            }
            return this.sandboxContext;
        } else {
            throw new IllegalStateException(
                    "Попытка воспользоваться \"песочным\" контекстом API не в режиме \"песочницы\""
            );
        }
    }

    @NotNull
    public OrdersContext getOrdersContext() {
        if (this.ordersContext == null) {
            this.ordersContext = new OrdersContextImpl(client, apiUrl, authToken);
        }
        return this.ordersContext;
    }

    @NotNull
    public PortfolioContext getPortfolioContext() {
        if (Objects.isNull(this.portfolioContext)) {
            this.portfolioContext = new PortfolioContextImpl(client, apiUrl, authToken);
        }
        return this.portfolioContext;
    }

    @NotNull
    public MarketContext getMarketContext() {
        if (this.marketContext == null) {
            this.marketContext = new MarketContextImpl(client, apiUrl, authToken);
        }
        return this.marketContext;
    }

    @NotNull
    public OperationsContext getOperationsContext() {
        if (this.operationsContext == null) {
            this.operationsContext = new OperationsContextImpl(client, apiUrl, authToken);
        }
        return this.operationsContext;
    }

    @NotNull
    public UserContext getUserContext() {
        if (this.userContext == null) {
            this.userContext = new UserContextImpl(client, apiUrl, authToken);
        }
        return this.userContext;
    }

    @NotNull
    public StreamingContext getStreamingContext() {
        if (this.streamingContext == null) {
            this.streamingContext = new StreamingContextImpl(
                    client,
                    this.config.streamingUrl,
                    authToken,
                    this.config.streamingParallelism,
                    executor
            );
        }
        return this.streamingContext;
    }

}
