package ru.obukhov.investor.bot.interfaces;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface Simulator {

    BigDecimal simulate(String ticker, BigDecimal balance, OffsetDateTime from, OffsetDateTime to);

}
