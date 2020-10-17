package ru.obukhov.investor.bot.model;

import org.junit.Assert;
import org.junit.Test;
import ru.obukhov.investor.util.DateUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PricesHolderTest {

    private static final String TICKER = "ticker";

    @Test
    public void getPrice_returnsNull_whenNoPricesOnDate() {
        PricesHolder pricesHolder = new PricesHolder();
        OffsetDateTime writeDateTime = DateUtils.getDateTime(2020, 10, 10, 10, 0, 0);
        BigDecimal writePrice = BigDecimal.valueOf(100);
        pricesHolder.addPrice(TICKER, writeDateTime, writePrice);

        OffsetDateTime readDateTime = DateUtils.getDateTime(2020, 10, 11, 10, 0, 0);
        BigDecimal readPrice = pricesHolder.getPrice(TICKER, readDateTime);

        Assert.assertNull(readPrice);
    }

    @Test
    public void getPrice_returnsNull_whenExistPricesOnDateButOnlyAfterTime() {
        PricesHolder pricesHolder = new PricesHolder();

        OffsetDateTime writeDateTime1 = DateUtils.getDateTime(2020, 10, 10, 10, 5, 0);
        BigDecimal writePrice1 = BigDecimal.valueOf(100);
        pricesHolder.addPrice(TICKER, writeDateTime1, writePrice1);

        OffsetDateTime writeDateTime2 = DateUtils.getDateTime(2020, 10, 10, 11, 10, 15);
        BigDecimal writePrice2 = BigDecimal.valueOf(100);
        pricesHolder.addPrice(TICKER, writeDateTime2, writePrice2);

        OffsetDateTime readDateTime = DateUtils.getDateTime(2020, 10, 10, 9, 10, 15);
        BigDecimal readPrice = pricesHolder.getPrice(TICKER, readDateTime);

        Assert.assertNull(readPrice);
    }

    @Test
    public void getPrice_returnsKeptPrice() {
        PricesHolder pricesHolder = new PricesHolder();
        OffsetDateTime writeDateTime = DateUtils.getDateTime(2020, 10, 10, 10, 10, 15);
        BigDecimal writePrice = BigDecimal.valueOf(100);
        pricesHolder.addPrice(TICKER, writeDateTime, writePrice);

        OffsetDateTime readDateTime = DateUtils.getDateTime(2020, 10, 10, 10, 10, 15);
        BigDecimal readPrice = pricesHolder.getPrice(TICKER, readDateTime);

        Assert.assertEquals(writePrice, readPrice);
    }

    @Test
    public void getPrice_returnsFloorPrice_whenExistPriceOnSameDateButNotTime() {
        PricesHolder pricesHolder = new PricesHolder();

        OffsetDateTime writeDateTime1 = DateUtils.getDateTime(2020, 10, 10, 10, 5, 0);
        BigDecimal writePrice1 = BigDecimal.valueOf(100);
        pricesHolder.addPrice(TICKER, writeDateTime1, writePrice1);

        OffsetDateTime writeDateTime2 = DateUtils.getDateTime(2020, 10, 10, 11, 10, 15);
        BigDecimal writePrice2 = BigDecimal.valueOf(100);
        pricesHolder.addPrice(TICKER, writeDateTime2, writePrice2);

        OffsetDateTime readDateTime = DateUtils.getDateTime(2020, 10, 10, 10, 10, 15);
        BigDecimal readPrice = pricesHolder.getPrice(TICKER, readDateTime);

        Assert.assertEquals(writePrice1, readPrice);
    }

}