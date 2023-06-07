package ru.obukhov.trader.market.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.CurrencyInstrument;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;

import java.time.OffsetDateTime;
import java.util.List;

public interface ExtInstrumentsService {

    String getSingleFigiByTicker(final String ticker);

    List<String> getFigiesByTicker(final String ticker);

    String getTickerByFigi(final String figi);

    String getExchange(final String ticker);

    List<Share> getShares(final String ticker);

    Share getSingleShare(final String ticker);

    List<Etf> getEtfs(final String ticker);

    Etf getSingleEtf(final String ticker);

    List<Bond> getBonds(final String ticker);

    Bond getSingleBond(final String ticker);

    List<CurrencyInstrument> getCurrencies(final String ticker);

    CurrencyInstrument getSingleCurrency(final String ticker);

    TradingDay getTradingDay(final String ticker, final OffsetDateTime dateTime);

    List<TradingDay> getTradingSchedule(final String exchange, final Interval interval);

    List<TradingDay> getTradingScheduleByTicker(final String ticker, final Interval interval);

    List<TradingSchedule> getTradingSchedules(final Interval interval);

}