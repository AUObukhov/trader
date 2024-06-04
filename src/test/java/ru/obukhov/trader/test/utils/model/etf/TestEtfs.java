package ru.obukhov.trader.test.utils.model.etf;

import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.test.utils.ResourceUtils;

public class TestEtfs {

    public static final TestEtf EZA = readEtf("eza.json");
    public static final TestEtf FXIT = readEtf("fxit.json");
    public static final TestEtf FXUS = readEtf("fxus.json");

    private static TestEtf readEtf(final String fileName) {
        final Etf etf = ResourceUtils.getResourceAsObject("etfs/" + fileName, Etf.class);
        return new TestEtf(etf);
    }

}