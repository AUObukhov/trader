package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.tinkoff.piapi.contract.v1.Share;

// todo: rename
@RequiredArgsConstructor
public class MarketInstrumentsService {

    private final TinkoffService tinkoffService;

    public Share getShare(final String ticker) {
        return tinkoffService.getAllShares().stream()
                .filter(share -> ticker.equals(share.getTicker()))
                .findFirst()
                .orElse(null);
    }

}