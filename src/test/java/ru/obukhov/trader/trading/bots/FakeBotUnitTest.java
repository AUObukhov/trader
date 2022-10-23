package ru.obukhov.trader.trading.bots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.FakeContext;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.tinkoff.piapi.contract.v1.Operation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@ExtendWith(MockitoExtension.class)
class FakeBotUnitTest {

    @Mock
    private ExtMarketDataService extMarketDataService;
    @Mock
    private ExtInstrumentsService extInstrumentsService;
    @Mock
    private ExtOperationsService extOperationsService;
    @Mock
    private TradingStrategy strategy;
    @Mock
    private FakeContext fakeContext;

    @InjectMocks
    private FakeBot fakeBot;

    @Test
    void getShare() {
        final String ticker = TestShare1.TICKER;
        final Share expectedShare = Share.builder().ticker(ticker).lotSize(10).build();
        Mockito.when(extInstrumentsService.getSingleShare(ticker)).thenReturn(expectedShare);

        final Share share = fakeBot.getShare(ticker);

        Assertions.assertEquals(expectedShare, share);
    }

    @Test
    void getCurrentDateTime() {
        final OffsetDateTime expectedCurrentDateTime = OffsetDateTime.now();
        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(expectedCurrentDateTime);

        final OffsetDateTime currentDateTime = fakeBot.getCurrentDateTime();

        Assertions.assertEquals(expectedCurrentDateTime, currentDateTime);
    }

    @Test
    void nextMinute() {
        final OffsetDateTime expectedNextMinute = OffsetDateTime.now();
        Mockito.when(fakeContext.nextMinute()).thenReturn(expectedNextMinute);

        final OffsetDateTime nextMinute = fakeBot.nextMinute();

        Assertions.assertEquals(expectedNextMinute, nextMinute);
    }

    @Test
    void getInvestments() {
        final String accountId = TestData.ACCOUNT_ID1;
        final Currency currency = Currency.RUB;
        final SortedMap<OffsetDateTime, BigDecimal> expectedInvestments = new TreeMap<>();
        expectedInvestments.put(OffsetDateTime.now(), DecimalUtils.setDefaultScale(10));
        Mockito.when(fakeContext.getInvestments(accountId, currency)).thenReturn(expectedInvestments);

        final SortedMap<OffsetDateTime, BigDecimal> investments = fakeBot.getInvestments(accountId, currency);

        AssertUtils.assertMapsAreEqual(expectedInvestments, investments);
    }

    @Test
    void getBalance() {
        final String accountId = TestData.ACCOUNT_ID1;
        final Currency currency = Currency.RUB;
        final BigDecimal expectedBalance = DecimalUtils.setDefaultScale(10);
        Mockito.when(fakeContext.getBalance(accountId, currency)).thenReturn(expectedBalance);

        final BigDecimal balance = fakeBot.getCurrentBalance(accountId, currency);

        AssertUtils.assertEquals(expectedBalance, balance);
    }

    @Test
    void getOperations() {
        final String accountId = TestData.ACCOUNT_ID1;
        final Interval interval = Interval.of(OffsetDateTime.now(), OffsetDateTime.now());
        final String ticker = TestShare1.TICKER;
        final List<Operation> expectedOperations = new ArrayList<>();
        Mockito.when(extOperationsService.getOperations(accountId, interval, ticker)).thenReturn(expectedOperations);

        final List<Operation> operations = fakeBot.getOperations(accountId, interval, ticker);

        AssertUtils.assertEquals(expectedOperations, operations);
    }

    @Test
    void getPortfolioPositions() {
        final String accountId = TestData.ACCOUNT_ID1;
        final List<PortfolioPosition> expectedPositions = new ArrayList<>();
        Mockito.when(extOperationsService.getPositions(accountId)).thenReturn(expectedPositions);

        final List<PortfolioPosition> positions = fakeBot.getPortfolioPositions(accountId);

        AssertUtils.assertEquals(expectedPositions, positions);
    }

    @Test
    void getCurrentPrice() {
        final String ticker = TestShare1.TICKER;
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final BigDecimal expectedCurrentPrice = DecimalUtils.setDefaultScale(10);

        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(currentDateTime);

        Mockito.when(extMarketDataService.getLastPrice(ticker, currentDateTime)).thenReturn(expectedCurrentPrice);

        final BigDecimal currentPrice = fakeBot.getCurrentPrice(ticker);

        AssertUtils.assertEquals(expectedCurrentPrice, currentPrice);
    }

}