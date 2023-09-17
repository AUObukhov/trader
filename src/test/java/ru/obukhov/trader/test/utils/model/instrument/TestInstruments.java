package ru.obukhov.trader.test.utils.model.instrument;

import ru.obukhov.trader.test.utils.model.share.TestShares;

public class TestInstruments {

    public static final TestInstrument APPLE = new TestInstrument(TestShares.APPLE.instrument());
    public static final TestInstrument SBER = new TestInstrument(TestShares.SBER.instrument());

}