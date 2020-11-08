package ru.obukhov.investor.bot.model;

import org.junit.Assert;
import org.junit.Test;
import ru.obukhov.investor.util.DateUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class PricesHolderTest {

    private static final String TICKER = "ticker";

    // region dataExists tests

    @Test
    public void dataExists_returnsFalse_whenNoData() {
        PricesHolder pricesHolder = new PricesHolder();
        OffsetDateTime writeDateTime = DateUtils.getDateTime(2020, 10, 10, 10, 0, 0);
        BigDecimal writePrice = BigDecimal.valueOf(100);
        pricesHolder.addPrice(TICKER, writeDateTime, writePrice);

        OffsetDateTime readDateTime = DateUtils.getDateTime(2020, 10, 11, 10, 0, 0);
        boolean result = pricesHolder.dataExists(TICKER, readDateTime);

        Assert.assertFalse(result);
    }

    @Test
    public void dataExists_returnTrue_whenDataExists() {
        PricesHolder pricesHolder = new PricesHolder();

        OffsetDateTime writeDateTime = DateUtils.getDateTime(2020, 10, 10, 10, 5, 0);
        BigDecimal writePrice = BigDecimal.valueOf(100);
        pricesHolder.addPrice(TICKER, writeDateTime, writePrice);

        OffsetDateTime readDateTime = DateUtils.getDateTime(2020, 10, 10, 9, 10, 15);
        boolean result = pricesHolder.dataExists(TICKER, readDateTime);

        Assert.assertTrue(result);
    }

    @Test
    public void dataExists_returnTrue_whenDataExists_andPriceIsNull() {
        PricesHolder pricesHolder = new PricesHolder();

        OffsetDateTime writeDateTime = DateUtils.getDateTime(2020, 10, 10, 10, 5, 0);
        BigDecimal writePrice = null;
        pricesHolder.addPrice(TICKER, writeDateTime, writePrice);

        OffsetDateTime readDateTime = DateUtils.getDateTime(2020, 10, 10, 9, 10, 15);
        boolean result = pricesHolder.dataExists(TICKER, readDateTime);

        Assert.assertTrue(result);
    }

    // endregion

    // region getPrice tests

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

        OffsetDateTime writeDateTime2 = DateUtils.getDateTime(2020, 10, 10, 11, 10, 0);
        BigDecimal writePrice2 = BigDecimal.valueOf(100);
        pricesHolder.addPrice(TICKER, writeDateTime2, writePrice2);

        OffsetDateTime readDateTime = DateUtils.getDateTime(2020, 10, 10, 10, 10, 0);
        BigDecimal readPrice = pricesHolder.getPrice(TICKER, readDateTime);

        Assert.assertEquals(writePrice1, readPrice);
    }

    @Test
    public void getPrice_returnsPreviousDayPrice_whenExistPriceOnSameDateButLaterThanTime() {
        PricesHolder pricesHolder = new PricesHolder();

        OffsetDateTime writeDateTime1 = DateUtils.getDateTime(2020, 10, 9, 15, 0, 0);
        BigDecimal writePrice1 = BigDecimal.valueOf(100);
        pricesHolder.addPrice(TICKER, writeDateTime1, writePrice1);

        OffsetDateTime writeDateTime2 = DateUtils.getDateTime(2020, 10, 10, 11, 00, 0);
        BigDecimal writePrice2 = BigDecimal.valueOf(100);
        pricesHolder.addPrice(TICKER, writeDateTime2, writePrice2);

        OffsetDateTime readDateTime = DateUtils.getDateTime(2020, 10, 10, 10, 0, 0);
        BigDecimal readPrice = pricesHolder.getPrice(TICKER, readDateTime);

        Assert.assertEquals(writePrice1, readPrice);
    }

    // endregion

}