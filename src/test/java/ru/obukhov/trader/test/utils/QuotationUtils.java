package ru.obukhov.trader.test.utils;

import ru.tinkoff.piapi.contract.v1.Quotation;

public class QuotationUtils {
    static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

    public static Quotation parseQuotation(final String string) {
        String[] parts = string.split("\\.");
        long units = Long.parseLong(parts[0]);
        int nano = parts.length == 1 ? 0 : Integer.parseInt(parts[1]) * POWERS_OF_10[9 - parts[1].length()];
        return Quotation.newBuilder().setUnits(units).setNano(nano).build();
    }

}