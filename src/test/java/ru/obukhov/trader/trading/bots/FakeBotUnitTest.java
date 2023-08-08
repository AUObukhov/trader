package ru.obukhov.trader.trading.bots;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.FakeContext;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.contract.v1.TradingDay;

import java.math.BigDecimal;
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
        final String figi = TestShare1.FIGI;
        final Share expectedShare = Share.newBuilder().setFigi(figi).setLot(10).build();
        Mockito.when(extInstrumentsService.getShare(figi)).thenReturn(expectedShare);

        final Share share = fakeBot.getShare(figi);

        Assertions.assertEquals(expectedShare, share);
    }

    @Test
    void getCurrentTimestamp() {
        final Timestamp expectedCurrentTimestamp = TimestampUtils.now();
        Mockito.when(fakeContext.getCurrentTimestamp()).thenReturn(expectedCurrentTimestamp);

        final Timestamp currentTimestamp = fakeBot.getCurrentTimestamp();

        Assertions.assertEquals(expectedCurrentTimestamp, currentTimestamp);
    }

    @Test
    void nextScheduleMinute() {
        final Timestamp expectedNextMinute = TimestampUtils.now();
        final List<TradingDay> tradingSchedule = TestData.createTradingSchedule(
                TimestampUtils.newTimestamp(2023, 7, 21, 7),
                DateTimeTestData.createTime(19, 0, 0),
                5
        );
        Mockito.when(fakeContext.nextScheduleMinute(tradingSchedule)).thenReturn(expectedNextMinute);

        final Timestamp nextMinute = fakeBot.nextScheduleMinute(tradingSchedule);

        Assertions.assertEquals(expectedNextMinute, nextMinute);
    }

    @Test
    void getInvestments() {
        final String accountId = TestData.ACCOUNT_ID1;
        final String currency = Currencies.RUB;
        final SortedMap<Timestamp, BigDecimal> expectedInvestments = new TreeMap<>(TimestampUtils::compare);
        expectedInvestments.put(TimestampUtils.now(), DecimalUtils.setDefaultScale(10));
        Mockito.when(fakeContext.getInvestments(accountId, currency)).thenReturn(expectedInvestments);

        final SortedMap<Timestamp, BigDecimal> investments = fakeBot.getInvestments(accountId, currency);

        AssertUtils.assertMapsAreEqual(expectedInvestments, investments);
    }

    @Test
    void getBalance() {
        final String accountId = TestData.ACCOUNT_ID1;
        final String currency = Currencies.RUB;
        final BigDecimal expectedBalance = DecimalUtils.setDefaultScale(10);
        Mockito.when(fakeContext.getBalance(accountId, currency)).thenReturn(expectedBalance);

        final BigDecimal balance = fakeBot.getCurrentBalance(accountId, currency);

        AssertUtils.assertEquals(expectedBalance, balance);
    }

    @Test
    void getOperations() {
        final String accountId = TestData.ACCOUNT_ID1;
        final Interval interval = Interval.of(TimestampUtils.now(), TimestampUtils.now());
        final String figi = TestShare1.FIGI;
        final List<Operation> expectedOperations = new ArrayList<>();
        Mockito.when(extOperationsService.getOperations(accountId, interval, figi)).thenReturn(expectedOperations);

        final List<Operation> operations = fakeBot.getOperations(accountId, interval, figi);

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
        final String figi = TestShare1.FIGI;
        final Timestamp currentTimestamp = TimestampUtils.now();
        final BigDecimal expectedCurrentPrice = DecimalUtils.setDefaultScale(10);

        Mockito.when(fakeContext.getCurrentTimestamp()).thenReturn(currentTimestamp);

        Mockito.when(extMarketDataService.getLastPrice(figi, currentTimestamp)).thenReturn(expectedCurrentPrice);

        final BigDecimal currentPrice = fakeBot.getCurrentPrice(figi);

        AssertUtils.assertEquals(expectedCurrentPrice, currentPrice);
    }

}