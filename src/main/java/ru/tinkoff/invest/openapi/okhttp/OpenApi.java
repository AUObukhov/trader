package ru.tinkoff.invest.openapi.okhttp;

import lombok.Getter;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.config.properties.ApiProperties;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.market.model.SandboxRegisterRequest;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Service
public class OpenApi implements Closeable {

    private final TradingProperties tradingProperties;
    private final String authToken;
    private final OkHttpClient client;
    private final String apiUrl;

    private SandboxContext sandboxContext;
    @Getter
    private final OrdersContext ordersContext;
    @Getter
    private final PortfolioContext portfolioContext;
    @Getter
    private final MarketContext marketContext;
    @Getter
    private final OperationsContext operationsContext;
    @Getter
    private final UserContext userContext;

    public OpenApi(final ApiProperties apiProperties, final TradingProperties tradingProperties, final List<Interceptor> interceptors)
            throws IOException {

        this.tradingProperties = tradingProperties;
        this.authToken = "Bearer " + tradingProperties.getToken();
        this.client = createClient(interceptors);

        if (tradingProperties.isSandbox()) {
            this.apiUrl = apiProperties.sandboxHost();
            getSandboxContext().performRegistration(new SandboxRegisterRequest());
        } else {
            this.apiUrl = apiProperties.host();
        }

        this.ordersContext = new OrdersContextImpl(client, apiUrl, authToken);
        this.portfolioContext = new PortfolioContextImpl(client, apiUrl, authToken);
        this.marketContext = new MarketContextImpl(client, apiUrl, authToken);
        this.operationsContext = new OperationsContextImpl(client, apiUrl, authToken);
        this.userContext = new UserContextImpl(client, apiUrl, authToken);
    }

    private OkHttpClient createClient(final List<Interceptor> interceptors) {
        final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().pingInterval(Duration.ofSeconds(5));
        clientBuilder.interceptors().addAll(interceptors);
        return clientBuilder.build();
    }

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

    @Override
    public void close() {
        this.client.dispatcher().executorService().shutdown();
    }

}