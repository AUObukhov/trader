package ru.obukhov.trader.config;

import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

@Configuration
@RequiredArgsConstructor
public class ClientConfiguration {

    final OkHttpClient okHttpClient;

    @PreDestroy
    public void onExit() {
        okHttpClient.dispatcher().executorService().shutdown();
    }

}