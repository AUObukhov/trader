package ru.obukhov.trader.common.exception;

import java.util.Collection;

public class InstrumentNotFoundException extends RuntimeException {

    public InstrumentNotFoundException(final String instrumentId) {
        super("Instrument not found for id " + instrumentId);
    }

    public InstrumentNotFoundException(final Collection<String> instrumentIds) {
        super("Instruments not found for ids " + instrumentIds);
    }

}