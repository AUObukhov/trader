package ru.obukhov.trader.market.impl;

import com.google.protobuf.Timestamp;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.tinkoff.piapi.contract.v1.Bond;
import ru.tinkoff.piapi.contract.v1.Currency;
import ru.tinkoff.piapi.contract.v1.Etf;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.contract.v1.TradingDay;
import ru.tinkoff.piapi.contract.v1.TradingSchedule;
import ru.tinkoff.piapi.core.InstrumentsService;

import java.time.Instant;
import java.util.List;

public class RealExtInstrumentsService implements ExtInstrumentsService {

    private final InstrumentsService instrumentsService;

    public RealExtInstrumentsService(final InstrumentsService instrumentsService) {
        this.instrumentsService = instrumentsService;
    }

    /**
     * @return ticker corresponding to given {@code figi}
     */
    @Cacheable(value = "tickerByFigi", sync = true)
    public String getTickerByFigi(final String figi) {
        final Instrument instrument = instrumentsService.getInstrumentByFigiSync(figi);
        Assert.notNull(instrument, "Not found instrument for FIGI '" + figi + "'");
        return instrument.getTicker();
    }

    /**
     * @return exchange of instrument for given {@code figi}
     */
    public String getExchange(final String figi) {
        Instrument instrument = instrumentsService.getInstrumentByFigiSync(figi);
        Assert.notNull(instrument, "Not found instrument for FIGI '" + figi + "'");
        return instrument.getExchange();
    }

    /**
     * @return {@link Instrument} corresponding to given {@code figi}
     * @throws IllegalArgumentException when given {@code figi} has no corresponding share or has more than one corresponding share
     */
    public Instrument getInstrument(final String figi) {
        return instrumentsService.getInstrumentByFigiSync(figi);
    }

    /**
     * @return {@link Share} corresponding to given {@code figi}
     */
    public Share getShare(final String figi) {
        return instrumentsService.getShareByFigiSync(figi);
    }

    /**
     * @return {@link Etf} corresponding to given {@code figi}
     */
    public Etf getEtf(final String figi) {
        return instrumentsService.getEtfByFigiSync(figi);
    }

    /**
     * @return {@link Bond} corresponding to given {@code figi}
     */
    public Bond getBond(final String figi) {
        return instrumentsService.getBondByFigiSync(figi);
    }

    /**
     * @return {@link Currency} corresponding to given {@code figi}
     */
    public Currency getCurrency(final String figi) {
        return instrumentsService.getCurrencyByFigiSync(figi);

    }

    /**
     * @return {@link TradingDay} with given {@code timestamp} corresponding to given {@code figi}
     */
    public TradingDay getTradingDay(final String figi, final Timestamp timestamp) {
        final Interval interval = Interval.of(timestamp, timestamp);
        return getTradingScheduleByFigi(figi, interval).get(0);
    }

    /**
     * @return list of {@link TradingDay} with given {@code interval} corresponding to given {@code exchange}
     */
    public List<TradingDay> getTradingSchedule(final String exchange, final Interval interval) {
        final Instant fromInstant = TimestampUtils.toStartOfDayInstant(interval.getFrom());
        final Instant toInstant = TimestampUtils.toStartOfDayInstant(interval.getTo());
        return instrumentsService.getTradingScheduleSync(exchange, fromInstant, toInstant)
                .getDaysList()
                .stream()
                .toList();
    }

    /**
     * @return list of {@link TradingDay} with given {@code interval} corresponding to given {@code figi}
     */
    public List<TradingDay> getTradingScheduleByFigi(final String figi, final Interval interval) {
        final String exchange = getExchange(figi);
        return getTradingSchedule(exchange, interval);
    }

    /**
     * @return list of {@link TradingSchedule} with given {@code interval}. Each schedule corresponds to some exchange
     */
    public List<TradingSchedule> getTradingSchedules(final Interval interval) {
        final Instant from = TimestampUtils.toInstant(interval.getFrom());
        final Instant to = TimestampUtils.toInstant(interval.getTo());
        return instrumentsService.getTradingSchedulesSync(from, to);
    }

}