package ru.obukhov.investor.bot.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.bot.interfaces.DataSupplier;
import ru.obukhov.investor.bot.interfaces.MarketMock;
import ru.obukhov.investor.bot.model.DecisionData;
import ru.obukhov.investor.bot.model.PricesHolder;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.util.DateUtils;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Class for getting mocked portfolio and market situation data for bot. Market data is actually real historical data
 */
@Service
@RequiredArgsConstructor
public class HistoricalDataSupplier implements DataSupplier {

    private final MarketService marketService;
    private final MarketMock marketMock;
    private final TradingProperties tradingProperties;

    private final PricesHolder pricesHolder = new PricesHolder();

    @Override
    public DecisionData getData(String ticker) {

        return DecisionData.builder()
                .balance(marketMock.getBalance())
                .position(marketMock.getPosition(ticker))
                .currentPrice(getCurrentPrice(ticker))
                .lastOperations(Collections.emptyList())
                .instrument(marketService.getInstrument(ticker))
                .build();

    }

    private BigDecimal getCurrentPrice(String ticker) {
        if (pricesHolder.dataExists(ticker, marketMock.getCurrentDateTime())) {
            return pricesHolder.getPrice(ticker, marketMock.getCurrentDateTime());
        }

        updatePrices(ticker);
        return pricesHolder.getPrice(ticker, marketMock.getCurrentDateTime());

    }

    private void updatePrices(String ticker) {
        OffsetDateTime from = DateUtils.setTime(marketMock.getCurrentDateTime(), tradingProperties.getWorkStartTime());
        OffsetDateTime to = from.plus(tradingProperties.getWorkDuration());
        List<Candle> candles = marketService.getCandles(ticker, from, to, CandleInterval.ONE_MIN);
        if (candles.isEmpty()) {
            pricesHolder.addPrice(ticker, from, null); // to prevent reloading of candles when no candles on date
        } else {
            for (Candle candle : candles) {
                pricesHolder.addPrice(ticker, candle.getTime(), candle.getClosePrice());
            }
        }
    }

}