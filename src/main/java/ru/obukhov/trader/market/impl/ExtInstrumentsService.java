package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.Assert;
import ru.tinkoff.piapi.contract.v1.AssetInstrument;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.InstrumentsService;

@RequiredArgsConstructor
public class ExtInstrumentsService {

    private final InstrumentsService instrumentsService;

    @Cacheable(value = "figiByTicker", sync = true)
    public String getFigiByTicker(final String ticker) {
        return instrumentsService.getAssetsSync().stream()
                .flatMap(asset -> asset.getInstrumentsList().stream())
                .filter(assetInstrument -> assetInstrument.getTicker().equals(ticker))
                .findFirst()
                .map(AssetInstrument::getFigi)
                .orElseThrow(() -> new IllegalArgumentException("Not found instrument for ticker '" + ticker + "'"));
    }

    @Cacheable(value = "tickerByFigi", sync = true)
    public String getTickerByFigi(final String figi) {
        final Instrument instrument = instrumentsService.getInstrumentByFigiSync(figi);
        Assert.notNull(instrument, "Not found instrument for figi '" + figi + "'");
        return instrument.getTicker();
    }

    public Share getShare(final String ticker) {
        return instrumentsService.getAllSharesSync().stream()
                .filter(share -> ticker.equals(share.getTicker()))
                .findFirst()
                .orElse(null);
    }

}