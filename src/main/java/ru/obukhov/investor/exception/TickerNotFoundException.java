package ru.obukhov.investor.exception;

public class TickerNotFoundException extends RuntimeException {

    public TickerNotFoundException(String ticker) {
        super("Ticker '" + ticker + "' not found");
    }

}