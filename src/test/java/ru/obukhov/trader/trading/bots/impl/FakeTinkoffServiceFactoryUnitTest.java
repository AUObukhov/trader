package ru.obukhov.trader.trading.bots.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FakeTinkoffServiceFactoryUnitTest {

    @InjectMocks
    private FakeTinkoffServiceFactory factory;

    @Test
    void createBot_returnsNotNull() {
        Assertions.assertNotNull(factory.createService(0.0));
    }

}