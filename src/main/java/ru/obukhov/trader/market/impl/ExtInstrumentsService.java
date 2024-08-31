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
import ru.obukhov.trader.common.util.SingleItemCollector;
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

    @Cacheable(value = "tickerByFigi", sync = true)
    public String getTickerByFigi(final String figi) {
        final ru.tinkoff.piapi.contract.v1.Instrument instrument = instrumentsService.getInstrumentByFigiSync(figi);
        Asserter.notNull(instrument, () -> new InstrumentNotFoundException(figi));
        return instrument.getTicker();
    }

    @Cacheable(value = "exchange", sync = true)
    public String getExchange(final String figi) {
        ru.tinkoff.piapi.contract.v1.Instrument instrument = instrumentsService.getInstrumentByFigiSync(figi);
        Asserter.notNull(instrument, () -> new InstrumentNotFoundException(figi));
        return instrument.getExchange();
    }

    public List<String> getExchanges(final List<String> figies) {
        return figies.stream().map(self::getExchange).distinct().toList();
    }

    @Cacheable(value = "instrument", sync = true)
    public Instrument getInstrument(final String figi) {
        return INSTRUMENT_MAPPER.map(instrumentsService.getInstrumentByFigiSync(figi));
    }

    @Cacheable(value = "share", sync = true)
    public Share getShare(final String figi) {
        return SHARE_MAPPER.map(instrumentsService.getShareByFigiSync(figi));
    }

    public List<Share> getShares(final List<String> figies) {
        final Comparator<Share> comparator = Comparator.comparing(share -> figies.indexOf(share.figi()));
        return self.getAllTShares()
                .stream()
                .filter(share -> figies.contains(share.getFigi()))
                .map(SHARE_MAPPER::map)
                .sorted(comparator)
                .toList();
    }

    public List<Share> getAllShares() {
        return self.getAllTShares()
                .stream()
                .map(SHARE_MAPPER::map)
                .toList();
    }

    @Cacheable(value = "allShares", sync = true)
    List<ru.tinkoff.piapi.contract.v1.Share> getAllTShares() {
        return instrumentsService.getAllSharesSync();
    }

    public Etf getEtf(final String figi) {
        return ETF_MAPPER.map(instrumentsService.getEtfByFigiSync(figi));
    }

    public Bond getBond(final String figi) {
        return BOND_MAPPER.map(instrumentsService.getBondByFigiSync(figi));
    }

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

    public List<Currency> getCurrenciesByIsoNames(final String... currenciesIsoNames) {
        List<String> isoNamesList = Arrays.stream(currenciesIsoNames).distinct().toList();
        return self.getAllCurrencies()
                .stream()
                .filter(currency -> isoNamesList.contains(currency.isoCurrencyName()))
                .toList();
    }

    public Currency getTomCurrencyByIsoName(final String isoName) {
        return self.getAllCurrencies()
                .stream()
                .filter(currency -> isoName.equals(currency.isoCurrencyName()) && currency.ticker().endsWith("TOM"))
                .collect(new SingleItemCollector<>());
    }

    public TradingDay getTradingDay(final String figi, final OffsetDateTime dateTime) {
        final Interval interval = Interval.of(dateTime, dateTime);
        return getTradingScheduleByFigi(figi, interval).getFirst();
    }

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

    public List<TradingDay> getTradingScheduleByFigi(final String figi, final Interval interval) {
        final String exchange = self.getExchange(figi);
        return getTradingSchedule(exchange, interval);
    }

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

    public List<TradingSchedule> getTradingSchedules(final Interval interval) {
        final Instant from = interval.getFrom().toInstant();
        final Instant to = interval.getTo().toInstant();
        return instrumentsService.getTradingSchedulesSync(from, to)
                .stream()
                .map(TRADING_SCHEDULE_MAPPER::map)
                .toList();
    }

    public List<Dividend> getDividends(final String figi, final Interval interval) {
        return instrumentsService.getDividendsSync(figi, interval.getFrom().toInstant(), interval.getTo().toInstant())
                .stream()
                .filter(dividend -> !"Cancelled".equals(dividend.getDividendType()))
                .map(DIVIDEND_MAPPER::map)
                .toList();
    }

}