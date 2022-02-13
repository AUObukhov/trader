package ru.obukhov.trader.web.client.service;

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

    private SandboxClient sandboxClient;
    @Getter
    private final OrdersClient ordersClient;
    @Getter
    private final PortfolioClient portfolioClient;
    @Getter
    private final MarketClient marketClient;
    @Getter
    private final OperationsClient operationsClient;
    @Getter
    private final UserClient userClient;

    public OpenApi(final ApiProperties apiProperties, final TradingProperties tradingProperties, final List<Interceptor> interceptors)
            throws IOException {

        this.tradingProperties = tradingProperties;
        this.authToken = "Bearer " + tradingProperties.getToken();
        this.client = createClient(interceptors);

        if (tradingProperties.isSandbox()) {
            this.apiUrl = apiProperties.sandboxHost();
            getSandboxClient().performRegistration(new SandboxRegisterRequest());
        } else {
            this.apiUrl = apiProperties.host();
        }

        this.ordersClient = new OrdersClientImpl(client, apiUrl, authToken);
        this.portfolioClient = new PortfolioClientImpl(client, apiUrl, authToken);
        this.marketClient = new MarketClientImpl(client, apiUrl, authToken);
        this.operationsClient = new OperationsClientImpl(client, apiUrl, authToken);
        this.userClient = new UserClientImpl(client, apiUrl, authToken);
    }

    private OkHttpClient createClient(final List<Interceptor> interceptors) {
        final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().pingInterval(Duration.ofSeconds(5));
        clientBuilder.interceptors().addAll(interceptors);
        return clientBuilder.build();
    }

    public SandboxClient getSandboxClient() {
        if (tradingProperties.isSandbox()) {
            if (this.sandboxClient == null) {
                this.sandboxClient = new SandboxClientImpl(client, apiUrl, authToken);
            }
            return this.sandboxClient;
        } else {
            throw new IllegalStateException("Attempt to use sandbox context from not sandbox mode");
        }
    }

    @Override
    public void close() {
        this.client.dispatcher().executorService().shutdown();
    }

}