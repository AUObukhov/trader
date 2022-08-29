package ru.obukhov.trader.market.interfaces;

import java.time.OffsetDateTime;

/**
 * Proxy for multiple Tinkoff contexts
 */
public interface TinkoffService {

    OffsetDateTime getCurrentDateTime();

}