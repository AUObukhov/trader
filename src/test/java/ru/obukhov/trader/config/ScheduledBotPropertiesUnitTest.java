package ru.obukhov.trader.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ScheduledBotPropertiesUnitTest {

    @Test
    void setTickersAndGetTickers_copiesCollection() {
        final ScheduledBotProperties config = new ScheduledBotProperties();
        final Collection<String> initialTickers = Set.of("ticker1", "ticker2");
        final Collection<String> tickers = new HashSet<>(initialTickers);

        config.setTickers(tickers);
        tickers.add("ticker3");

        final Set<String> receivedTickers = config.getTickers();

        Assertions.assertEquals(initialTickers, receivedTickers);
        Assertions.assertNotSame(initialTickers, receivedTickers);
    }

    @Test
    void setTickersAndGetTickersAreThreadSafe() throws InterruptedException {
        final ScheduledBotProperties config = new ScheduledBotProperties();
        AtomicReference<Exception> error = new AtomicReference<>();

        final Runnable target = () -> {
            try {
                for (int i = 0; i < 20; i++) {
                    final Set<String> tickers = new HashSet<>(6, 1);
                    tickers.add("ticker1");
                    tickers.add("ticker2");
                    tickers.add("ticker3");

                    config.setTickers(tickers);

                    tickers.add("ticker4");

                    TimeUnit.MILLISECONDS.sleep(1);

                    Set<String> receivedTickers = config.getTickers();
                    Assertions.assertEquals(3, receivedTickers.size());
                }
            } catch (Exception exception) {
                error.set(exception);
            }
        };

        final List<Thread> threads = Stream.generate(() -> new Thread(target)).limit(50).collect(Collectors.toList());

        threads.forEach(Thread::start);

        for (Thread thread : threads) {
            thread.join(10000);
            Assertions.assertEquals(Thread.State.TERMINATED, thread.getState(), "probably deadlock");
        }

        Assertions.assertNull(error.get());
    }

}