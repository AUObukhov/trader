package ru.obukhov.trader.market.impl;

import org.apache.commons.collections4.ListUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.exception.InstrumentNotFoundException;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.Asserter;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.model.WorkSchedule;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Dividend;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Instrument;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;
import ru.obukhov.trader.market.model.transform.BondMapper;
import ru.obukhov.trader.market.model.transform.CurrencyMapper;
import ru.obukhov.trader.market.model.transform.DividendMapper;
import ru.obukhov.trader.market.model.transform.EtfMapper;
import ru.obukhov.trader.market.model.transform.InstrumentMapper;
import ru.obukhov.trader.market.model.transform.ShareMapper;
import ru.obukhov.trader.market.model.transform.TradingDayMapper;
import ru.obukhov.trader.market.model.transform.TradingScheduleMapper;
import ru.tinkoff.piapi.core.InstrumentsService;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExtInstrumentsService {

    private static final TradingDayMapper TRADING_DAY_MAPPER = Mappers.getMapper(TradingDayMapper.class);
    private static final TradingScheduleMapper TRADING_SCHEDULE_MAPPER = Mappers.getMapper(TradingScheduleMapper.class);
    private static final InstrumentMapper INSTRUMENT_MAPPER = Mappers.getMapper(InstrumentMapper.class);
    private static final ShareMapper SHARE_MAPPER = Mappers.getMapper(ShareMapper.class);
    private static final BondMapper BOND_MAPPER = Mappers.getMapper(BondMapper.class);
    private static final EtfMapper ETF_MAPPER = Mappers.getMapper(EtfMapper.class);
    private static final CurrencyMapper CURRENCY_MAPPER = Mappers.getMapper(CurrencyMapper.class);
    private static final DividendMapper DIVIDEND_MAPPER = Mappers.getMapper(DividendMapper.class);

    private final WorkSchedule workSchedule;
    private final InstrumentsService instrumentsService;
    private final ExtInstrumentsService self;

    public ExtInstrumentsService(
            final MarketProperties marketProperties,
            final InstrumentsService instrumentsService,
            @Lazy final ExtInstrumentsService self
    ) {
        this.workSchedule = marketProperties.getWorkSchedule();
        this.instrumentsService = instrumentsService;
        this.self = self;
    }

    /**
     * @return ticker corresponding to given {@code figi}
     */
    @Cacheable(value = "tickerByFigi", sync = true)
    public String getTickerByFigi(final String figi) {
        final ru.tinkoff.piapi.contract.v1.Instrument instrument = instrumentsService.getInstrumentByFigiSync(figi);
        Asserter.notNull(instrument, () -> new InstrumentNotFoundException(figi));
        return instrument.getTicker();
    }

    /**
     * @return exchange of instrument for given {@code figi}
     */
    @Cacheable(value = "exchange", sync = true)
    public String getExchange(final String figi) {
        ru.tinkoff.piapi.contract.v1.Instrument instrument = instrumentsService.getInstrumentByFigiSync(figi);
        Asserter.notNull(instrument, () -> new InstrumentNotFoundException(figi));
        return instrument.getExchange();
    }

    /**
     * @return exchange of instrument for given {@code figi}
     */
    public List<String> getExchanges(final List<String> figies) {
        return figies.stream().map(self::getExchange).distinct().toList();
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
    @Cacheable(value = "share", sync = true)
    public Share getShare(final String figi) {
        return SHARE_MAPPER.map(instrumentsService.getShareByFigiSync(figi));
    }

    /**
     * @return List of {@link Share} corresponding to given {@code figies}
     * Keep same order as in given {@code figies}
     */
    public List<Share> getShares(final List<String> figies) {
        final Comparator<Share> comparator = Comparator.comparing(share -> figies.indexOf(share.figi()));
        return self.getAllTinkoffShares()
                .stream()
                .filter(share -> figies.contains(share.getFigi()))
                .map(SHARE_MAPPER::map)
                .sorted(comparator)
                .toList();
    }

    /**
     * @return list of all shares
     */
    public List<Share> getAllShares() {
        return self.getAllTinkoffShares()
                .stream()
                .map(SHARE_MAPPER::map)
                .toList();
    }

    @Cacheable(value = "allShares", sync = true)
    List<ru.tinkoff.piapi.contract.v1.Share> getAllTinkoffShares() {
        return instrumentsService.getAllSharesSync();
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
    public Currency getCurrencyByFigi(final String figi) {
        return CURRENCY_MAPPER.map(instrumentsService.getCurrencyByFigiSync(figi));
    }

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
    public TradingDay getTradingDay(final String figi, final OffsetDateTime dateTime) {
        final Interval interval = Interval.of(dateTime, dateTime);
        return getTradingScheduleByFigi(figi, interval).get(0);
    }

    /**
     * @return list of {@link TradingDay} with given {@code interval} corresponding to given {@code exchange}
     */
    public List<TradingDay> getTradingSchedule(final String exchange, final Interval interval) {
        if (interval.getFrom().isBefore(DateUtils.now())) {
            return interval.toTradingDays(workSchedule);
        } else {
            return instrumentsService.getTradingScheduleSync(exchange, interval.getFrom().toInstant(), interval.getTo().toInstant())
                    .getDaysList()
                    .stream()
                    .map(TRADING_DAY_MAPPER::map)
                    .toList();
        }
    }

    /**
     * @return list of {@link TradingDay} with given {@code interval} corresponding to given {@code figi}
     */
    public List<TradingDay> getTradingScheduleByFigi(final String figi, final Interval interval) {
        final String exchange = self.getExchange(figi);
        return getTradingSchedule(exchange, interval);
    }

    /**
     * @return intersection of trading schedules with given {@code interval} corresponding to given {@code figies}
     * @throws InstrumentNotFoundException when instrument not found for any of given {@code figies}
     * @throws IllegalArgumentException    when instruments with given {@code figies} are from different exchanges
     */
    public List<TradingDay> getTradingScheduleByFigies(final List<String> figies, final Interval interval) {
        Assert.isTrue(!figies.isEmpty(), "figies must not be empty");

        final List<String> exchanges = getExchanges(figies);
        final Collection<List<TradingDay>> tradingDaysGroups = exchanges.stream()
                .map(exchange -> getTradingSchedule(exchange, interval))
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(TradingDay::date, Collections::singletonList, ListUtils::union))
                .values();

        final List<TradingDay> result = new ArrayList<>();
        for (final List<TradingDay> tradingDaysGroup : tradingDaysGroups) {
            if (tradingDaysGroup.size() == figies.size()) {
                final TradingDay tradingDay = tradingDaysGroup.stream()
                        .reduce(TradingDay::intersect)
                        .orElseThrow();
                result.add(tradingDay);
            }
        }
        return result.stream()
                .sorted(Comparator.comparing(TradingDay::date))
                .toList();
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

    /**
     * @return list of dividends with not "Canceled" type
     */
    public List<Dividend> getDividends(final String figi, final OffsetDateTime from, final OffsetDateTime to) {
        return instrumentsService.getDividendsSync(figi, from.toInstant(), to.toInstant())
                .stream()
                .filter(dividend -> !"Cancelled".equals(dividend.getDividendType()))
                .map(DIVIDEND_MAPPER::map)
                .toList();
    }

}