package ru.obukhov.investor.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.service.StreamingApiSubscriber;
import ru.obukhov.investor.service.interfaces.ConnectionService;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.OperationsContext;
import ru.tinkoff.invest.openapi.OrdersContext;
import ru.tinkoff.invest.openapi.PortfolioContext;
import ru.tinkoff.invest.openapi.SandboxContext;
import ru.tinkoff.invest.openapi.SandboxOpenApi;
import ru.tinkoff.invest.openapi.StreamingContext;
import ru.tinkoff.invest.openapi.UserContext;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApiFactory;

import java.util.concurrent.Executors;
import java.util.logging.Level;

@Log
@Service
@RequiredArgsConstructor
public class ConnectionServiceImpl implements ConnectionService {

    private final TradingProperties tradingProperties;
    @Setter
    private String token;
    private OpenApi api;

    @Override
    public MarketContext getMarketContext() {
        softRefreshApi();

        return api.getMarketContext();
    }

    @Override
    public OperationsContext getOperationsContext() {
        softRefreshApi();

        return api.getOperationsContext();
    }

    @Override
    public OrdersContext getOrdersContext() {
        softRefreshApi();

        return api.getOrdersContext();
    }

    @Override
    public PortfolioContext getPortfolioContext() {
        softRefreshApi();

        return api.getPortfolioContext();
    }

    @Override
    public UserContext getUserContext() {
        softRefreshApi();

        return api.getUserContext();
    }

    @Override
    public StreamingContext getStreamingContext() {
        softRefreshApi();

        return api.getStreamingContext();
    }

    @Override
    public SandboxContext getSandboxContext() {
        if (!tradingProperties.getSandbox()) {
            throw new IllegalStateException("Not sandbox mode");
        }

        softRefreshApi();

        return ((SandboxOpenApi) api).getSandboxContext();
    }

    private void softRefreshApi() {
        if (this.api == null || this.api.hasClosed()) {
            refreshApi();
        }
    }

    /**
     * Creates and opens {@link OpenApi} by {@code token}
     * Subscribes listener to events of created api
     */
    private void refreshApi() {
        final OkHttpOpenApiFactory factory = new OkHttpOpenApiFactory(this.token, log);
        this.api = factory.createSandboxOpenApiClient(Executors.newSingleThreadExecutor());

        if (tradingProperties.getSandbox()) {
            ((SandboxOpenApi) api).getSandboxContext().performRegistration(null).join();
        }

        subscribeListener();
    }

    /**
     * Created and subscribes listener to events of {@code api}
     *
     * @return created listener
     */
    protected StreamingApiSubscriber subscribeListener() {
        final StreamingApiSubscriber listener = new StreamingApiSubscriber(Executors.newSingleThreadExecutor());

        this.api.getStreamingContext().getEventPublisher().subscribe(listener);

        return listener;
    }

    /**
     * Close api, opened by {@code token} if it exists
     */
    @Override
    public void closeConnection() {
        try {
            if (this.api == null) {
                log.info("Connection not found");
            } else if (this.api.hasClosed()) {
                log.info("Connection already closed");
            } else {
                log.info("Closing connection");
                this.api.close();
            }
        } catch (final Exception e) {
            log.log(Level.SEVERE, "Exception while closing connection", e);
        }
    }

}