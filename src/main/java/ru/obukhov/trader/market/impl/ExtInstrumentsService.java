package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Exchange;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;
import ru.obukhov.trader.market.model.transform.EtfMapper;
import ru.obukhov.trader.market.model.transform.ShareMapper;
import ru.obukhov.trader.market.model.transform.TradingDayMapper;
import ru.obukhov.trader.market.model.transform.TradingScheduleMapper;
import ru.tinkoff.piapi.contract.v1.AssetInstrument;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.core.InstrumentsService;

import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
public class ExtInstrumentsService implements ApplicationContextAware {

    private static final ShareMapper SHARE_MAPPER = Mappers.getMapper(ShareMapper.class);
    private static final EtfMapper ETF_MAPPER = Mappers.getMapper(EtfMapper.class);
    private static final TradingDayMapper TRADING_DAY_MAPPER = Mappers.getMapper(TradingDayMapper.class);
    private static final TradingScheduleMapper TRADING_SCHEDULE_MAPPER = Mappers.getMapper(TradingScheduleMapper.class);

    private final InstrumentsService instrumentsService;
    private ExtInstrumentsService self;

    /**
     * @return FIGI corresponding to given {@code ticker}
     * @throws IllegalArgumentException when given {@code ticker} has no corresponding FIGIes or has more than one corresponding FIGI
     */
    @Cacheable(value = "singleFigiByTicker", sync = true)
    public String getSingleFigiByTicker(final String ticker) {
        final List<String> figies = self.getFigiesByTicker(ticker);
        Assert.isTrue(figies.size() == 1, () -> getInstrumentsCountErrorMessage("instrument", ticker, figies.size()));
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
        Assert.isTrue(shares.size() == 1, () -> getInstrumentsCountErrorMessage(InstrumentType.SHARE.getValue(), ticker, shares.size()));
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
        Assert.isTrue(etfs.size() == 1, () -> getInstrumentsCountErrorMessage(InstrumentType.ETF.getValue(), ticker, etfs.size()));
        return etfs.get(0);
    }

    /**
     * @return list of {@link TradingDay} with given {@code interval} corresponding to given {@code exchange}
     */
    public List<TradingDay> getTradingSchedule(final Exchange exchange, final Interval interval) {
        final Instant fromInstant = interval.getFrom().toInstant();
        final Instant toInstant = interval.getTo().toInstant();
        return instrumentsService.getTradingScheduleSync(exchange.getValue(), fromInstant, toInstant)
                .getDaysList()
                .stream()
                .map(TRADING_DAY_MAPPER::map)
                .toList();
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

    private static String getInstrumentsCountErrorMessage(final String instrumentType, final String ticker, final int actualCount) {
        return "Expected single " + instrumentType + " for ticker '" + ticker + "'. Found " + actualCount;
    }

    @Override
    public void setApplicationContext(@NotNull final ApplicationContext applicationContext) {
        this.self = applicationContext.getBean(ExtInstrumentsService.class);
    }

}