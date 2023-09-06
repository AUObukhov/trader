package ru.obukhov.trader.common.exception;

public class InstrumentNotFoundException extends RuntimeException {

    public InstrumentNotFoundException(final String instrumentId) {
        super("Instrument " + instrumentId + " not found");
    }

}