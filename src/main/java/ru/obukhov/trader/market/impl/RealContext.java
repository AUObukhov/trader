package ru.obukhov.trader.market.impl;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.interfaces.Context;

@Slf4j
@RequiredArgsConstructor
public class RealContext implements Context {

    @Override
    public Timestamp getCurrentTimestamp() {
        return TimestampUtils.now();
    }

}