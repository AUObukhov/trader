package ru.obukhov.trader.market.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Instrument;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;
import ru.tinkoff.piapi.contract.v1.Bond;
import ru.tinkoff.piapi.contract.v1.Currency;
import ru.tinkoff.piapi.contract.v1.Etf;
import ru.tinkoff.piapi.contract.v1.Share;

import java.time.OffsetDateTime;
import java.util.List;

public interface ExtInstrumentsService {

    String getTickerByFigi(final String figi);

    String getExchange(final String ticker);

    Instrument getInstrument(final String figi);

    Share getShare(final String figi);

    Etf getEtf(final String figi);

    Bond getBond(final String figi);

    Currency getCurrency(final String figi);

    TradingDay getTradingDay(final String figi, final OffsetDateTime dateTime);

    List<TradingDay> getTradingSchedule(final String exchange, final Interval interval);

    List<TradingDay> getTradingScheduleByFigi(final String figi, final Interval interval);

    List<TradingSchedule> getTradingSchedules(final Interval interval);

}