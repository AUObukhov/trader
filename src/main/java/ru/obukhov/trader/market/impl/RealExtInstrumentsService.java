package ru.obukhov.trader.market.impl;

import org.mapstruct.factory.Mappers;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Instrument;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;
import ru.obukhov.trader.market.model.transform.BondMapper;
import ru.obukhov.trader.market.model.transform.EtfMapper;
import ru.obukhov.trader.market.model.transform.InstrumentMapper;
import ru.obukhov.trader.market.model.transform.ShareMapper;
import ru.obukhov.trader.market.model.transform.TradingDayMapper;
import ru.obukhov.trader.market.model.transform.TradingScheduleMapper;
import ru.tinkoff.piapi.contract.v1.Currency;
import ru.tinkoff.piapi.core.InstrumentsService;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

public class RealExtInstrumentsService implements ExtInstrumentsService {

    private static final TradingDayMapper TRADING_DAY_MAPPER = Mappers.getMapper(TradingDayMapper.class);
    private static final TradingScheduleMapper TRADING_SCHEDULE_MAPPER = Mappers.getMapper(TradingScheduleMapper.class);
    private static final InstrumentMapper INSTRUMENT_MAPPER = Mappers.getMapper(InstrumentMapper.class);
    private static final ShareMapper SHARE_MAPPER = Mappers.getMapper(ShareMapper.class);
    private static final BondMapper BOND_MAPPER = Mappers.getMapper(BondMapper.class);
    private static final EtfMapper ETF_MAPPER = Mappers.getMapper(EtfMapper.class);

    private final InstrumentsService instrumentsService;

    public RealExtInstrumentsService(final InstrumentsService instrumentsService) {
        this.instrumentsService = instrumentsService;
    }

    /**
     * @return ticker corresponding to given {@code figi}
     */
    @Cacheable(value = "tickerByFigi", sync = true)
    public String getTickerByFigi(final String figi) {
        final ru.tinkoff.piapi.contract.v1.Instrument instrument = instrumentsService.getInstrumentByFigiSync(figi);
        Assert.notNull(instrument, "Not found instrument for FIGI '" + figi + "'");
        return instrument.getTicker();
    }

    /**
     * @return exchange of instrument for given {@code figi}
     */
    public String getExchange(final String figi) {
        ru.tinkoff.piapi.contract.v1.Instrument instrument = instrumentsService.getInstrumentByFigiSync(figi);
        Assert.notNull(instrument, "Not found instrument for FIGI '" + figi + "'");
        return instrument.getExchange();
    }

    /**
     * @return {@link Instrument} corresponding to given {@code figi}
     * @throws IllegalArgumentException when given {@code figi} has no corresponding share or has more than one corresponding share
     */
    public Instrument getInstrument(final String figi) {
        return INSTRUMENT_MAPPER.map(instrumentsService.getInstrumentByFigiSync(figi));
    }

    /**
     * @return {@link Share} corresponding to given {@code figi}
     */
    public Share getShare(final String figi) {
        return SHARE_MAPPER.map(instrumentsService.getShareByFigiSync(figi));
    }

    /**
     * @return {@link Etf} corresponding to given {@code figi}
     */
    public Etf getEtf(final String figi) {
        return ETF_MAPPER.map(instrumentsService.getEtfByFigiSync(figi));
    }

    /**
     * @return {@link Bond} corresponding to given {@code figi}
     */
    public Bond getBond(final String figi) {
        return BOND_MAPPER.map(instrumentsService.getBondByFigiSync(figi));
    }

    /**
     * @return {@link Currency} corresponding to given {@code figi}
     */
    public Currency getCurrency(final String figi) {
        return instrumentsService.getCurrencyByFigiSync(figi);

    }

    /**
     * @return {@link TradingDay} with given {@code dateTime} corresponding to given {@code figi}
     */
    public TradingDay getTradingDay(final String figi, final OffsetDateTime dateTime) {
        final Interval interval = Interval.of(dateTime, dateTime);
        return getTradingScheduleByFigi(figi, interval).get(0);
    }

    /**
     * @return list of {@link TradingDay} with given {@code interval} corresponding to given {@code exchange}
     */
    public List<TradingDay> getTradingSchedule(final String exchange, final Interval interval) {
        final Instant fromInstant = DateUtils.toSameDayInstant(interval.getFrom());
        final Instant toInstant = DateUtils.toSameDayInstant(interval.getTo());
        return instrumentsService.getTradingScheduleSync(exchange, fromInstant, toInstant)
                .getDaysList()
                .stream()
                .map(TRADING_DAY_MAPPER::map)
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
        final Instant from = interval.getFrom().toInstant();
        final Instant to = interval.getTo().toInstant();
        return instrumentsService.getTradingSchedulesSync(from, to)
                .stream()
                .map(TRADING_SCHEDULE_MAPPER::map)
                .toList();
    }

}