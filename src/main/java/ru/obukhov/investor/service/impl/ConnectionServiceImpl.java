package ru.obukhov.investor.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.service.ConnectionService;
import ru.obukhov.investor.service.StreamingApiSubscriber;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.SandboxOpenApi;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApiFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Level;

@Log
@Service
@RequiredArgsConstructor
public class ConnectionServiceImpl implements ConnectionService {

    private final TradingProperties tradingProperties;
    private final Map<String, OpenApi> apis = new HashMap<>();

    @Override
    public OpenApi getApi(String token) {
        OpenApi api = this.apis.get(token);
        if (api == null || api.hasClosed()) {
            refreshApi(token);
            api = this.apis.get(token);
        }

        return api;
    }

    protected void refreshApi(String token) {
        final OkHttpOpenApiFactory factory = new OkHttpOpenApiFactory(token, log);
        OpenApi api = factory.createSandboxOpenApiClient(Executors.newSingleThreadExecutor());

        if (tradingProperties.getSandbox()) {
            ((SandboxOpenApi) api).getSandboxContext().performRegistration(null).join();
        }

        subscribeListener(api);

        apis.put(token, api);
    }

    protected StreamingApiSubscriber subscribeListener(OpenApi api) {
        final StreamingApiSubscriber listener = new StreamingApiSubscriber(Executors.newSingleThreadExecutor());

        api.getStreamingContext().getEventPublisher().subscribe(listener);

        return listener;
    }

    @Override
    public void closeConnection(String token) {
        try {
            OpenApi api = this.apis.get(token);
            if (api == null) {
                log.info("Connection not found");
            } else if (api.hasClosed()) {
                log.info("Connection already closed");
            } else {
                log.info("Closing connection");
                api.close();
            }
        } catch (final Exception e) {
            log.log(Level.SEVERE, "Exception while closing connection", e);
        }
    }

}