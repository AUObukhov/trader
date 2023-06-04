package ru.obukhov.trader.market.impl;

import org.mapstruct.factory.Mappers;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.CurrencyInstrument;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Exchange;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;
import ru.obukhov.trader.market.model.transform.BondMapper;
import ru.obukhov.trader.market.model.transform.CurrencyInstrumentMapper;
import ru.obukhov.trader.market.model.transform.EtfMapper;
import ru.obukhov.trader.market.model.transform.ShareMapper;
import ru.obukhov.trader.market.model.transform.TradingDayMapper;
import ru.obukhov.trader.market.model.transform.TradingScheduleMapper;
import ru.tinkoff.piapi.contract.v1.AssetInstrument;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.core.InstrumentsService;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Supplier;

public class RealExtInstrumentsService implements ExtInstrumentsService {

    private static final ShareMapper SHARE_MAPPER = Mappers.getMapper(ShareMapper.class);
    private static final EtfMapper ETF_MAPPER = Mappers.getMapper(EtfMapper.class);
    private static final BondMapper BOND_MAPPER = Mappers.getMapper(BondMapper.class);
    private static final CurrencyInstrumentMapper CURRENCY_INSTRUMENT_MAPPER = Mappers.getMapper(CurrencyInstrumentMapper.class);
    private static final TradingDayMapper TRADING_DAY_MAPPER = Mappers.getMapper(TradingDayMapper.class);
    private static final TradingScheduleMapper TRADING_SCHEDULE_MAPPER = Mappers.getMapper(TradingScheduleMapper.class);

    private final InstrumentsService instrumentsService;
    private final RealExtInstrumentsService self;

    public RealExtInstrumentsService(final InstrumentsService instrumentsService, @Lazy final RealExtInstrumentsService self) {
        this.instrumentsService = instrumentsService;
        this.self = self;
    }

    /**
     * @return FIGI corresponding to given {@code ticker}
     * @throws IllegalArgumentException when given {@code ticker} has no corresponding FIGIes or has more than one corresponding FIGI
     */
    @Cacheable(value = "singleFigiByTicker", sync = true)
    public String getSingleFigiByTicker(final String ticker) {
        final List<String> figies = self.getFigiesByTicker(ticker);
        Assert.isTrue(figies.size() == 1, () -> getSingleInstrumentCountErrorMessage("instrument", ticker, figies.size()));
        return figies.get(0);
    }

    /**
     * @return FIGIes corresponding to given {@code ticker}
     */
    @Cacheable(value = "figiesByTicker", sync = true)
    public List<String> getFigiesByTicker(final String ticker) {
        return instrumentsService.getAssetsSync().stream()
                .flatMap(asset -> asset.getInstrumentsList().stream())
                .filter(assetInstrument -> assetInstrument.getTicker().equals(ticker))
                .map(AssetInstrument::getFigi)
                .toList();
    }

    /**
     * @return ticker corresponding to given {@code figi}
     * @throws IllegalArgumentException when instrument with given {@code figi} does not exist
     */
    @Cacheable(value = "tickerByFigi", sync = true)
    public String getTickerByFigi(final String figi) {
        final Instrument instrument = instrumentsService.getInstrumentByFigiSync(figi);
        Assert.notNull(instrument, "Not found instrument for figi '" + figi + "'");
        return instrument.getTicker();
    }

    /**
     * @return exchange of instrument for given {@code ticker}
     * @throws IllegalArgumentException if instrument not found
     */
    public Exchange getExchange(final String ticker) {
        final List<Share> shares = getShares(ticker);
        if (!shares.isEmpty()) {
            final Supplier<String> messageSupplier =
                    () -> getNotMultipleInstrumentCountErrorMessage(InstrumentType.INSTRUMENT_TYPE_SHARE.toString(), ticker, shares.size());
            Assert.isTrue(shares.size() == 1, messageSupplier);
            return shares.get(0).exchange();
        }

        final List<CurrencyInstrument> currencies = getCurrencies(ticker);
        if (!currencies.isEmpty()) {
            final Supplier<String> messageSupplier =
                    () -> getNotMultipleInstrumentCountErrorMessage(InstrumentType.INSTRUMENT_TYPE_CURRENCY.toString(), ticker, currencies.size());
            Assert.isTrue(currencies.size() == 1, messageSupplier);
            return currencies.get(0).exchange();
        }

        final List<Etf> etfs = getEtfs(ticker);
        if (!etfs.isEmpty()) {
            final Supplier<String> messageSupplier =
                    () -> getNotMultipleInstrumentCountErrorMessage(InstrumentType.INSTRUMENT_TYPE_ETF.toString(), ticker, etfs.size());
            Assert.isTrue(etfs.size() == 1, messageSupplier);
            return etfs.get(0).exchange();
        }

        final List<Bond> bonds = getBonds(ticker);
        if (!bonds.isEmpty()) {
            final Supplier<String> messageSupplier =
                    () -> getNotMultipleInstrumentCountErrorMessage(InstrumentType.INSTRUMENT_TYPE_BOND.toString(), ticker, bonds.size());
            Assert.isTrue(bonds.size() == 1, messageSupplier);
            return bonds.get(0).exchange();
        }

        throw new IllegalArgumentException("Not found instrument for ticker '" + ticker + "'");
    }

    /**
     * @return list of {@link Share} corresponding to given {@code ticker}
     */
    public List<Share> getShares(final String ticker) {
        return instrumentsService.getAllSharesSync().stream()
                .filter(share -> ticker.equalsIgnoreCase(share.getTicker()))
                .map(SHARE_MAPPER::map)
                .toList();
    }

    /**
     * @return {@link Share} corresponding to given {@code ticker}
     * @throws IllegalArgumentException when given {@code ticker} has no corresponding share or has more than one corresponding share
     */
    public Share getSingleShare(final String ticker) {
        final List<Share> shares = getShares(ticker);
        final Supplier<String> messageSupplier =
                () -> getSingleInstrumentCountErrorMessage(InstrumentType.INSTRUMENT_TYPE_SHARE.toString(), ticker, shares.size());
        Assert.isTrue(shares.size() == 1, messageSupplier);
        return shares.get(0);
    }

    /**
     * @return list of {@link Etf} corresponding to given {@code ticker}
     */
    public List<Etf> getEtfs(final String ticker) {
        return instrumentsService.getAllEtfsSync().stream()
                .filter(etf -> ticker.equalsIgnoreCase(etf.getTicker()))
                .map(ETF_MAPPER::map)
                .toList();
    }

    /**
     * @return {@link Etf} corresponding to given {@code ticker}
     * @throws IllegalArgumentException when given {@code ticker} has no corresponding etf or has more than one corresponding etf
     */
    public Etf getSingleEtf(final String ticker) {
        final List<Etf> etfs = getEtfs(ticker);
        final Supplier<String> messageSupplier =
                () -> getSingleInstrumentCountErrorMessage(InstrumentType.INSTRUMENT_TYPE_ETF.toString(), ticker, etfs.size());
        Assert.isTrue(etfs.size() == 1, messageSupplier);
        return etfs.get(0);
    }

    /**
     * @return list of {@link Bond} corresponding to given {@code ticker}
     */
    public List<Bond> getBonds(final String ticker) {
        return instrumentsService.getAllBondsSync().stream()
                .filter(bond -> ticker.equalsIgnoreCase(bond.getTicker()))
                .map(BOND_MAPPER::map)
                .toList();
    }

    /**
     * @return {@link Bond} corresponding to given {@code ticker}
     * @throws IllegalArgumentException when given {@code ticker} has no corresponding bond or has more than one corresponding bond
     */
    public Bond getSingleBond(final String ticker) {
        final List<Bond> bonds = getBonds(ticker);
        final Supplier<String> messageSupplier =
                () -> getSingleInstrumentCountErrorMessage(InstrumentType.INSTRUMENT_TYPE_BOND.toString(), ticker, bonds.size());
        Assert.isTrue(bonds.size() == 1, messageSupplier);
        return bonds.get(0);
    }

    /**
     * @return list of {@link Bond} corresponding to given {@code ticker}
     */
    public List<CurrencyInstrument> getCurrencies(final String ticker) {
        return instrumentsService.getAllCurrenciesSync().stream()
                .filter(bond -> ticker.equalsIgnoreCase(bond.getTicker()))
                .map(CURRENCY_INSTRUMENT_MAPPER::map)
                .toList();
    }

    /**
     * @return {@link Bond} corresponding to given {@code ticker}
     * @throws IllegalArgumentException when given {@code ticker} has no corresponding bond or has more than one corresponding bond
     */
    public CurrencyInstrument getSingleCurrency(final String ticker) {
        final List<CurrencyInstrument> currencies = getCurrencies(ticker);
        final Supplier<String> messageSupplier =
                () -> getSingleInstrumentCountErrorMessage(InstrumentType.INSTRUMENT_TYPE_CURRENCY.toString(), ticker, currencies.size());
        Assert.isTrue(currencies.size() == 1, messageSupplier);
        return currencies.get(0);
    }

    /**
     * @return {@link TradingDay} with given {@code dateTime} corresponding to given {@code ticker}
     */
    public TradingDay getTradingDay(final String ticker, final OffsetDateTime dateTime) {
        final Interval interval = Interval.of(dateTime, dateTime);
        return getTradingSchedule(ticker, interval).get(0);
    }

    /**
     * @return list of {@link TradingDay} with given {@code interval} corresponding to given {@code exchange}
     */
    public List<TradingDay> getTradingSchedule(final Exchange exchange, final Interval interval) {
        final Instant fromInstant = DateUtils.toSameDayInstant(interval.getFrom());
        final Instant toInstant = DateUtils.toSameDayInstant(interval.getTo());
        return instrumentsService.getTradingScheduleSync(exchange.getValue(), fromInstant, toInstant)
                .getDaysList()
                .stream()
                .map(TRADING_DAY_MAPPER::map)
                .toList();
    }

    /**
     * @return list of {@link TradingDay} with given {@code interval} corresponding to given {@code ticker}
     */
    public List<TradingDay> getTradingSchedule(final String ticker, final Interval interval) {
        final Exchange exchange = getExchange(ticker);
        return getTradingSchedule(exchange, interval);
    }

    /**
     * @return list of {@link TradingSchedule} with given {@code interval}. Each schedule corresponds to some exchange
     */
    public List<TradingSchedule> getTradingSchedules(final Interval interval) {
        return instrumentsService.getTradingSchedulesSync(interval.getFrom().toInstant(), interval.getTo().toInstant())
                .stream()
                .map(TRADING_SCHEDULE_MAPPER::map)
                .toList();
    }

    private static String getSingleInstrumentCountErrorMessage(final String instrumentType, final String ticker, final int actualCount) {
        return "Expected single " + instrumentType + " for ticker '" + ticker + "'. Found " + actualCount;
    }

    private static String getNotMultipleInstrumentCountErrorMessage(final String instrumentType, final String ticker, final int actualCount) {
        return "Expected maximum of one " + instrumentType + " for ticker '" + ticker + "'. Found " + actualCount;
    }

}