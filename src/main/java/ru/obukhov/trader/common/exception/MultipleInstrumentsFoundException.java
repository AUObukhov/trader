package ru.obukhov.trader.common.exception;

public class MultipleInstrumentsFoundException extends RuntimeException {

    public MultipleInstrumentsFoundException(final String instrumentId) {
        super("Multiple instruments found for id " + instrumentId);
    }

}