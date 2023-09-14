package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.market.interfaces.Context;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealContext implements Context {

    @Override
    public OffsetDateTime getCurrentDateTime() {
        return OffsetDateTime.now();
    }

}