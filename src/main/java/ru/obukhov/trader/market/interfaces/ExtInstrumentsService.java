package ru.obukhov.trader.market.interfaces;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.CurrencyInstrument;
import ru.tinkoff.piapi.contract.v1.Bond;
import ru.tinkoff.piapi.contract.v1.Etf;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.contract.v1.TradingDay;
import ru.tinkoff.piapi.contract.v1.TradingSchedule;

import java.util.List;

public interface ExtInstrumentsService {

    String getTickerByFigi(final String figi);

    String getExchange(final String ticker);

    Instrument getInstrument(final String figi);

    Share getShare(final String figi);

    Etf getEtf(final String figi);

    Bond getBond(final String figi);

    CurrencyInstrument getCurrency(final String figi);

    TradingDay getTradingDay(final String figi, final Timestamp timestamp);

    List<TradingDay> getTradingSchedule(final String exchange, final Interval interval);

    List<TradingDay> getTradingScheduleByFigi(final String figi, final Interval interval);

    List<TradingSchedule> getTradingSchedules(final Interval interval);

}