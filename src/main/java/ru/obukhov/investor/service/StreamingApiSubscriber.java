package ru.obukhov.investor.service;

import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.example.unicast.AsyncSubscriber;
import ru.tinkoff.invest.openapi.models.streaming.StreamingEvent;

import java.util.concurrent.Executor;

@Log
public class StreamingApiSubscriber extends AsyncSubscriber<StreamingEvent> {

    public StreamingApiSubscriber(@NotNull final Executor executor) {
        super(executor);
    }

    @Override
    protected boolean whenNext(final StreamingEvent event) {
        log.info("Пришло новое событие из Streaming API\n" + event);

        return true;
    }

}