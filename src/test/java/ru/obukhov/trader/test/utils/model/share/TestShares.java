package ru.obukhov.trader.test.utils.model.share;

import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.ResourceUtils;

public class TestShares {

    public static final TestShare APPLE = readShare("aapl.json");
    public static final TestShare SBER = readShare("sber.json");
    public static final TestShare YANDEX = readShare("yndx.json");
    public static final TestShare DIOD = readShare("diod.json");

    private static TestShare readShare(final String fileName) {
        final Share share = ResourceUtils.getResourceAsObject("shares/" + fileName, Share.class);
        return new TestShare(share);
    }

}