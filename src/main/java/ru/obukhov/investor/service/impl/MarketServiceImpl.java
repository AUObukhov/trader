package ru.obukhov.investor.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.TickerType;
import ru.obukhov.investor.model.transform.CandleMapper;
import ru.obukhov.investor.service.ConnectionService;
import ru.obukhov.investor.service.MarketService;
import ru.obukhov.investor.util.DateUtils;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Log
@Service
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class MarketServiceImpl implements MarketService {

    private final ConnectionService connectionService;
    private final String token;
    private final CandleMapper candleMapper = Mappers.getMapper(CandleMapper.class);

    @Override
    public List<Candle> getCandles(String ticker, OffsetDateTime from, OffsetDateTime to, CandleInterval interval) {
        validateToken();

        OffsetDateTime currentFrom = from;
        OffsetDateTime currentTo = currentFrom.plusDays(1);

        Instrument instrument = getInstrument(ticker);

        List<Candle> candles = new ArrayList<>();
        while (currentFrom.isBefore(to)) {
            if (DateUtils.isWorkDay(currentFrom)) {
                List<Candle> currentCandles = getCandlesShort(instrument.figi, currentFrom, currentTo, interval);
                candles.addAll(currentCandles);

                log.info("Loaded " + currentCandles.size() + " candles in " +
                        "[" + currentFrom + "; " + currentTo + ") for '" + ticker + "'");
            }

            currentFrom = currentTo;
            currentTo = currentFrom.plusDays(1);
        }

        log.info("Loaded " + candles.size() + " candles for '" + ticker + "'");

        return candles;
    }

    private List<Candle> getCandlesShort(String figi, OffsetDateTime from, OffsetDateTime to, CandleInterval interval) {
        return getContext()
                .getMarketCandles(figi, from, to, interval).join()
                .map(c -> c.candles)
                .orElse(new ArrayList<>())
                .stream()
                .map(candleMapper::map)
                .collect(Collectors.toList());
    }

    @Override
    public List<Instrument> getInstruments(TickerType type) {
        validateToken();

        switch (type) {
            case ETF:
                return getContext().getMarketEtfs().join().instruments;
            case STOCK:
                return getContext().getMarketStocks().join().instruments;
            case BOND:
                return getContext().getMarketBonds().join().instruments;
            case CURRENCY:
                return getContext().getMarketCurrencies().join().instruments;
            default:
                throw new IllegalArgumentException("Unknown ticker type " + type);
        }
    }

    private Instrument getInstrument(String ticker) {
        validateToken();

        List<Instrument> instruments = getContext().searchMarketInstrumentsByTicker(ticker).join().instruments;
        Assert.isTrue(instruments.size() == 1, "Expected one instrument by ticker " + ticker);

        return instruments.get(0);
    }

    @NotNull
    private MarketContext getContext() {
        return connectionService.getApi(this.token).getMarketContext();
    }

    @Override
    public void closeConnection() {
        validateToken();

        connectionService.closeConnection(token);
    }

    private void validateToken() {
        if (this.token == null) {
            throw new IllegalStateException("Token expected to be initialized");
        }
    }

}