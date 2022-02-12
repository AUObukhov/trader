package ru.tinkoff.invest.openapi.okhttp;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import ru.obukhov.trader.market.model.StreamingEvent;
import ru.obukhov.trader.market.model.StreamingRequest;

@SuppressWarnings("ReactiveStreamsPublisherImplementation")
public interface StreamingContext extends Publisher<StreamingEvent> {
    void sendRequest(@NotNull StreamingRequest request);
}