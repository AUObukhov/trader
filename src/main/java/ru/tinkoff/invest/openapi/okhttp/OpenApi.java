package ru.tinkoff.invest.openapi.okhttp;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.properties.ApiProperties;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.market.model.SandboxRegisterRequest;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

@Service
public class OpenApi implements Closeable {

    private final ApiProperties apiProperties;
    private final TradingProperties tradingProperties;
    private final String authToken;

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

    public OpenApi(final ApiProperties apiProperties, final TradingProperties tradingProperties, final List<Interceptor> interceptors)
            throws IOException {

        this.apiProperties = apiProperties;
        this.tradingProperties = tradingProperties;

        this.authToken = "Bearer " + tradingProperties.getToken();

        this.executor = ForkJoinPool.commonPool();
        this.client = createClient(interceptors);

        if (tradingProperties.isSandbox()) {
            this.apiUrl = this.apiProperties.sandboxHost();
            getSandboxContext().performRegistration(new SandboxRegisterRequest());
        } else {
            this.apiUrl = this.apiProperties.host();
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
        if (tradingProperties.isSandbox()) {
            if (this.sandboxContext == null) {
                this.sandboxContext = new SandboxContextImpl(client, apiUrl, authToken);
            }
            return this.sandboxContext;
        } else {
            throw new IllegalStateException("Attempt to use sandbox context from not sandbox mode");
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
                    this.apiProperties.streamingUrl(),
                    authToken,
                    this.apiProperties.streamingParallelism(),
                    executor
            );
        }
        return this.streamingContext;
    }

}
