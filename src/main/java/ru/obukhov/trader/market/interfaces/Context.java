package ru.obukhov.trader.market.interfaces;

import com.google.protobuf.Timestamp;

public interface Context {

    Timestamp getCurrentTimestamp();

}