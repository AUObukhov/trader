package ru.obukhov.investor.bot.model;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class PricesHolder {

    private final Table<String, OffsetDateTime, TreeMap<OffsetDateTime, BigDecimal>> data = HashBasedTable.create();

    public void addPrice(String ticker, OffsetDateTime dateTime, BigDecimal price) {
        OffsetDateTime dateKey = dateTime.truncatedTo(ChronoUnit.DAYS);
        TreeMap<OffsetDateTime, BigDecimal> datePrices = data.get(ticker, dateKey);
        if (datePrices == null) {
            datePrices = new TreeMap<>();
            data.put(ticker, dateKey, datePrices);
        }

        BigDecimal existedPrice = datePrices.put(dateTime, price);
        if (existedPrice != null) {
            log.warn("Prices for ticker = '" + ticker + "' and date = " + dateTime
                    + " already existed and were rewritten");
        }
    }

    public BigDecimal getPrice(String ticker, OffsetDateTime dateTime) {
        OffsetDateTime dateKey = dateTime.truncatedTo(ChronoUnit.DAYS);
        TreeMap<OffsetDateTime, BigDecimal> prices = data.get(ticker, dateKey);
        BigDecimal price = null;
        if (prices != null) {
            Map.Entry<OffsetDateTime, BigDecimal> entry = prices.floorEntry(dateTime);
            if (entry != null) {
                price = entry.getValue();
            }
        }

        return price;
    }

}