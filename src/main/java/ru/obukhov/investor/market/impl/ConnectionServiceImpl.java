package ru.obukhov.investor.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.common.service.impl.StreamingApiSubscriber;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.market.interfaces.ConnectionService;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionServiceImpl implements ConnectionService {

    private final TradingProperties tradingProperties;
    private final OkHttpOpenApiFactory okHttpOpenApiFactory;
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
        if (!tradingProperties.isSandbox()) {
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
        if (tradingProperties.isSandbox()) {
            this.api = okHttpOpenApiFactory.createSandboxOpenApiClient(Executors.newSingleThreadExecutor());
        } else {
            this.api = okHttpOpenApiFactory.createOpenApiClient(Executors.newSingleThreadExecutor());
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
     * Close opened api if it exists
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
            log.error("Exception while closing connection", e);
        }
    }

}