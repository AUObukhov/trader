package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.Assert;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.transform.EtfMapper;
import ru.obukhov.trader.market.model.transform.ShareMapper;
import ru.tinkoff.piapi.contract.v1.AssetInstrument;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.core.InstrumentsService;

import java.util.List;

@RequiredArgsConstructor
public class ExtInstrumentsService {

    private static final ShareMapper SHARE_MAPPER = Mappers.getMapper(ShareMapper.class);
    private static final EtfMapper ETF_MAPPER = Mappers.getMapper(EtfMapper.class);

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

    public List<Share> getShares(final String ticker) {
        return instrumentsService.getAllSharesSync().stream()
                .filter(share -> ticker.equalsIgnoreCase(share.getTicker()))
                .map(SHARE_MAPPER::map)
                .toList();
    }

    public Share getSingleShare(final String ticker) {
        final List<Share> shares = getShares(ticker);
        Assert.isTrue(shares.size() == 1, () -> "Expected single share for ticker " + ticker + ". Found " + shares.size());
        return shares.get(0);
    }

    public List<Etf> getEtfs(final String ticker) {
        return instrumentsService.getAllEtfsSync().stream()
                .filter(etf -> ticker.equalsIgnoreCase(etf.getTicker()))
                .map(ETF_MAPPER::map)
                .toList();
    }

    public Etf getSingleEtf(final String ticker) {
        final List<Etf> etfs = getEtfs(ticker);
        Assert.isTrue(etfs.size() == 1, () -> "Expected single etf for ticker " + ticker + ". Found " + etfs.size());
        return etfs.get(0);
    }

}