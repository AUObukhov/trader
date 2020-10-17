package ru.obukhov.investor.bot.interfaces;

import ru.obukhov.investor.web.model.SimulateResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface Simulator {

    SimulateResponse simulate(String ticker, BigDecimal balance, OffsetDateTime from, OffsetDateTime to);

}
