package ru.obukhov.trader.market.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Instrument;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;

import java.time.OffsetDateTime;
import java.util.List;

public interface ExtInstrumentsService {

    String getTickerByFigi(final String figi);

    String getExchange(final String ticker);

    List<String> getExchanges(final List<String> figies);

    Instrument getInstrument(final String figi);

    Share getShare(final String figi);

    List<Share> getShares(final List<String> figies);

    Etf getEtf(final String figi);

    Bond getBond(final String figi);

    Currency getCurrencyByFigi(final String figi);

    List<Currency> getAllCurrencies();

    List<Currency> getCurrenciesByIsoNames(final String... isoNames);

    TradingDay getTradingDay(final String figi, final OffsetDateTime dateTime);

    List<TradingDay> getTradingSchedule(final String exchange, final Interval interval);

    List<TradingDay> getTradingScheduleByFigi(final String figi, final Interval interval);

    List<TradingDay> getTradingScheduleByFigies(final List<String> figies, final Interval interval);

    List<TradingSchedule> getTradingSchedules(final Interval interval);

}