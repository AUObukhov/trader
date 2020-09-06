package ru.obukhov.investor.service.impl;

import lombok.extern.java.Log;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.investor.config.TokenHolder;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.TickerType;
import ru.obukhov.investor.model.transform.CandleMapper;
import ru.obukhov.investor.service.ConnectionService;
import ru.obukhov.investor.service.MarketService;
import ru.obukhov.investor.util.DateUtils;
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.HistoricalCandles;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.InstrumentsList;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Log
@Service
public class MarketServiceImpl implements MarketService, DisposableBean {

    private final ConnectionService connectionService;
    private final CandleMapper candleMapper = Mappers.getMapper(CandleMapper.class);
    private MarketContext context;

    public MarketServiceImpl(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    /**
     * Load candles by conditions period by period.
     *
     * @return list of loaded candles
     */
    @Override
    public List<Candle> getCandles(String ticker,
                                   OffsetDateTime from,
                                   OffsetDateTime to,
                                   CandleInterval interval,
                                   TemporalUnit periodUnit) {

        OffsetDateTime currentFrom = from;
        OffsetDateTime currentTo = DateUtils.plusLimited(currentFrom, 1, periodUnit, to);

        Instrument instrument = getInstrument(ticker);

        List<CompletableFuture<Optional<HistoricalCandles>>> futures = new ArrayList<>();
        while (currentFrom.isBefore(to)) {
            CompletableFuture<Optional<HistoricalCandles>> currentCandles =
                    getContext().getMarketCandles(instrument.figi, currentFrom, currentTo, interval);
            futures.add(currentCandles);

            currentFrom = currentTo;
            currentTo = DateUtils.plusLimited(currentFrom, 1, periodUnit, to);
        }

        List<Candle> candles = futures.stream()
                .map(CompletableFuture::join)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(historicalCandles -> historicalCandles.candles.stream())
                .map(candleMapper::map)
                .collect(Collectors.toList());

        log.info("Loaded " + candles.size() + " candles for '" + ticker + "'");

        return candles;
    }

    /**
     * @return list of available instruments of given {@code type}
     */
    @Override
    public List<Instrument> getInstruments(TickerType type) {
        if (type == null) {
            return getAllInstruments();
        }

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

    private List<Instrument> getAllInstruments() {
        CompletableFuture<InstrumentsList> etfs = getContext().getMarketEtfs();
        CompletableFuture<InstrumentsList> stocks = getContext().getMarketStocks();
        CompletableFuture<InstrumentsList> bonds = getContext().getMarketBonds();
        CompletableFuture<InstrumentsList> currencies = getContext().getMarketCurrencies();

        List<Instrument> result = new ArrayList<>();
        result.addAll(etfs.join().instruments);
        result.addAll(stocks.join().instruments);
        result.addAll(bonds.join().instruments);
        result.addAll(currencies.join().instruments);

        return result;
    }

    private Instrument getInstrument(String ticker) {
        List<Instrument> instruments = getContext().searchMarketInstrumentsByTicker(ticker).join().instruments;
        Assert.isTrue(instruments.size() == 1, "Expected one instrument by ticker " + ticker);

        return instruments.get(0);
    }

    @NotNull
    private MarketContext getContext() {
        if (this.context == null) {
            this.context = connectionService.getApi(TokenHolder.getToken()).getMarketContext();
        }

        return this.context;
    }

    @Override
    public void destroy() {
        connectionService.closeConnection(TokenHolder.getToken());
    }
}