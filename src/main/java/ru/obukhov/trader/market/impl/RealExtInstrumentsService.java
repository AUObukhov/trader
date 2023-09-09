package ru.obukhov.trader.market.impl;

import org.mapstruct.factory.Mappers;
import org.springframework.cache.annotation.Cacheable;
import ru.obukhov.trader.common.exception.InstrumentNotFoundException;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.Asserter;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.model.WorkSchedule;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Instrument;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;
import ru.obukhov.trader.market.model.transform.BondMapper;
import ru.obukhov.trader.market.model.transform.CurrencyMapper;
import ru.obukhov.trader.market.model.transform.EtfMapper;
import ru.obukhov.trader.market.model.transform.InstrumentMapper;
import ru.obukhov.trader.market.model.transform.ShareMapper;
import ru.obukhov.trader.market.model.transform.TradingDayMapper;
import ru.obukhov.trader.market.model.transform.TradingScheduleMapper;
import ru.tinkoff.piapi.core.InstrumentsService;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class RealExtInstrumentsService implements ExtInstrumentsService {

    private static final TradingDayMapper TRADING_DAY_MAPPER = Mappers.getMapper(TradingDayMapper.class);
    private static final TradingScheduleMapper TRADING_SCHEDULE_MAPPER = Mappers.getMapper(TradingScheduleMapper.class);
    private static final InstrumentMapper INSTRUMENT_MAPPER = Mappers.getMapper(InstrumentMapper.class);
    private static final ShareMapper SHARE_MAPPER = Mappers.getMapper(ShareMapper.class);
    private static final BondMapper BOND_MAPPER = Mappers.getMapper(BondMapper.class);
    private static final EtfMapper ETF_MAPPER = Mappers.getMapper(EtfMapper.class);
    private static final CurrencyMapper CURRENCY_MAPPER = Mappers.getMapper(CurrencyMapper.class);

    private final WorkSchedule workSchedule;
    private final InstrumentsService instrumentsService;
    private final RealExtInstrumentsService self;

    public RealExtInstrumentsService(
            final MarketProperties marketProperties,
            final InstrumentsService instrumentsService,
            final RealExtInstrumentsService realExtInstrumentsService
    ) {
        this.workSchedule = marketProperties.getWorkSchedule();
        this.instrumentsService = instrumentsService;
        this.self = realExtInstrumentsService;
    }

    /**
     * @return ticker corresponding to given {@code figi}
     */
    @Override
    @Cacheable(value = "tickerByFigi", sync = true)
    public String getTickerByFigi(final String figi) {
        final ru.tinkoff.piapi.contract.v1.Instrument instrument = instrumentsService.getInstrumentByFigiSync(figi);
        Asserter.notNull(instrument, () -> new InstrumentNotFoundException(figi));
        return instrument.getTicker();
    }

    /**
     * @return exchange of instrument for given {@code figi}
     */
    @Override
    public String getExchange(final String figi) {
        ru.tinkoff.piapi.contract.v1.Instrument instrument = instrumentsService.getInstrumentByFigiSync(figi);
        Asserter.notNull(instrument, () -> new InstrumentNotFoundException(figi));
        return instrument.getExchange();
    }

    /**
     * @return {@link Instrument} corresponding to given {@code figi}
     * @throws IllegalArgumentException when given {@code figi} has no corresponding share or has more than one corresponding share
     */
    @Override
    public Instrument getInstrument(final String figi) {
        return INSTRUMENT_MAPPER.map(instrumentsService.getInstrumentByFigiSync(figi));
    }

    /**
     * @return {@link Share} corresponding to given {@code figi}
     */
    @Override
    public Share getShare(final String figi) {
        return SHARE_MAPPER.map(instrumentsService.getShareByFigiSync(figi));
    }

    /**
     * @return List of {@link Share} corresponding to given {@code figies}
     * Keep same order as in given {@code figies}
     */
    public List<Share> getShares(final List<String> figies) {
        final Comparator<Share> comparator = Comparator.comparing(share -> figies.indexOf(share.figi()));
        return instrumentsService.getAllSharesSync()
                .stream()
                .filter(share -> figies.contains(share.getFigi()))
                .map(SHARE_MAPPER::map)
                .sorted(comparator)
                .toList();
    }

    /**
     * @return {@link Etf} corresponding to given {@code figi}
     */
    @Override
    public Etf getEtf(final String figi) {
        return ETF_MAPPER.map(instrumentsService.getEtfByFigiSync(figi));
    }

    /**
     * @return {@link Bond} corresponding to given {@code figi}
     */
    @Override
    public Bond getBond(final String figi) {
        return BOND_MAPPER.map(instrumentsService.getBondByFigiSync(figi));
    }

    /**
     * @return {@link Currency} corresponding to given {@code figi}
     */
    @Override
    public Currency getCurrencyByFigi(final String figi) {
        return CURRENCY_MAPPER.map(instrumentsService.getCurrencyByFigiSync(figi));
    }

    @Override
    @Cacheable(value = "allCurrencies", sync = true)
    public List<Currency> getAllCurrencies() {
        return instrumentsService.getAllCurrenciesSync()
                .stream()
                .map(CURRENCY_MAPPER::map)
                .toList();
    }

    /**
     * @return {@link Currency} corresponding to given {@code currenciesIsoNames}
     * @throws IllegalArgumentException if currency not found
     */
    @Override
    public List<Currency> getCurrenciesByIsoNames(final String... currenciesIsoNames) {
        List<String> isoNamesList = Arrays.stream(currenciesIsoNames).distinct().toList();
        return self.getAllCurrencies()
                .stream()
                .filter(currency -> isoNamesList.contains(currency.isoCurrencyName()))
                .toList();
    }

    /**
     * @return {@link TradingDay} with given {@code dateTime} corresponding to given {@code figi}
     */
    @Override
    public TradingDay getTradingDay(final String figi, final OffsetDateTime dateTime) {
        final Interval interval = Interval.of(dateTime, dateTime);
        return getTradingScheduleByFigi(figi, interval).get(0);
    }

    /**
     * @return list of {@link TradingDay} with given {@code interval} corresponding to given {@code exchange}
     */
    @Override
    public List<TradingDay> getTradingSchedule(final String exchange, final Interval interval) {
        final Instant fromInstant = DateUtils.toSameDayInstant(interval.getFrom());
        final Instant toInstant = DateUtils.toSameDayInstant(interval.getTo());
        if (fromInstant.isBefore(Instant.now())) {
            return interval.toTradingDays(workSchedule);
        } else {
            return instrumentsService.getTradingScheduleSync(exchange, fromInstant, toInstant)
                    .getDaysList()
                    .stream()
                    .map(TRADING_DAY_MAPPER::map)
                    .toList();
        }
    }

    /**
     * @return list of {@link TradingDay} with given {@code interval} corresponding to given {@code figi}
     */
    @Override
    public List<TradingDay> getTradingScheduleByFigi(final String figi, final Interval interval) {
        final String exchange = getExchange(figi);
        return getTradingSchedule(exchange, interval);
    }

    /**
     * @return list of {@link TradingSchedule} with given {@code interval}. Each schedule corresponds to some exchange
     */
    @Override
    public List<TradingSchedule> getTradingSchedules(final Interval interval) {
        final Instant from = interval.getFrom().toInstant();
        final Instant to = interval.getTo().toInstant();
        return instrumentsService.getTradingSchedulesSync(from, to)
                .stream()
                .map(TRADING_SCHEDULE_MAPPER::map)
                .toList();
    }

}