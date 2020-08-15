package ru.obukhov.investor.service.impl;

import lombok.RequiredArgsConstructor;
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
import ru.tinkoff.invest.openapi.MarketContext;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class MarketServiceImpl implements MarketService {

    private final ConnectionService connectionService;
    private final String token;
    private final CandleMapper candleMapper = Mappers.getMapper(CandleMapper.class);

    @Override
    public List<Candle> getMarketCandles(@NotNull final String ticker,
                                         @NotNull final OffsetDateTime from,
                                         @NotNull final OffsetDateTime to,
                                         @NotNull final CandleInterval interval) {
        validateToken();

        Instrument instrument = getInstrument(ticker);

        return getContext()
                .getMarketCandles(instrument.figi, from, to, interval).join()
                .map(c -> c.candles)
                .orElse(new ArrayList<>())
                .stream()
                .map(candleMapper::mapCandle)
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