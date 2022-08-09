package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.InstrumentsService;

// todo: rename
@RequiredArgsConstructor
public class MarketInstrumentsService {

    private final InstrumentsService instrumentsService;

    public Share getShare(final String ticker) {
        return instrumentsService.getAllSharesSync().stream()
                .filter(share -> ticker.equals(share.getTicker()))
                .findFirst()
                .orElse(null);
    }

}