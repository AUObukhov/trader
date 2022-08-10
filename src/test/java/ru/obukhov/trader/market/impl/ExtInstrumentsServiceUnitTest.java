package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.InstrumentsService;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ExtInstrumentsServiceUnitTest {

    @Mock
    private InstrumentsService instrumentsService;

    @InjectMocks
    private ExtInstrumentsService extInstrumentsService;

    // region MarketContext methods tests

    @Test
    void getShare_returnsShare_whenExists() {
        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final Share share1 = Share.newBuilder().setTicker(ticker1).build();
        final Share share2 = Share.newBuilder().setTicker(ticker2).build();

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(share1, share2));

        final Share result = extInstrumentsService.getShare(ticker2);

        Assertions.assertSame(share2, result);
    }

    @Test
    void getShare_returnsNull_whenShareNotExists() {
        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final String ticker3 = "ticker3";
        final Share share1 = Share.newBuilder().setTicker(ticker1).build();
        final Share share2 = Share.newBuilder().setTicker(ticker2).build();

        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(share1, share2));

        final Share result = extInstrumentsService.getShare(ticker3);

        Assertions.assertNull(result);
    }

}