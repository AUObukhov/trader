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
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.models.Position;

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
    @SuppressWarnings("unused")
    private TradingStrategy strategy;
    @Mock
    private FakeContext fakeContext;

    @InjectMocks
    private FakeBot fakeBot;

    @Test
    void getShare() {
        final String figi = TestShares.APPLE.share().figi();
        final Share expectedShare = Share.builder().figi(figi).lot(10).build();
        Mockito.when(extInstrumentsService.getShare(figi)).thenReturn(expectedShare);

        final Share share = fakeBot.getShare(figi);

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
    void nextScheduleMinute() {
        final OffsetDateTime expectedNextMinute = OffsetDateTime.now();
        final List<TradingDay> tradingSchedule = TestData.newTradingSchedule(
                DateTimeTestData.newDateTime(2023, 7, 21, 7),
                DateTimeTestData.newTime(19, 0, 0),
                5
        );
        Mockito.when(fakeContext.nextScheduleMinute(tradingSchedule)).thenReturn(expectedNextMinute);

        final OffsetDateTime nextMinute = fakeBot.nextScheduleMinute(tradingSchedule);

        Assertions.assertEquals(expectedNextMinute, nextMinute);
    }

    @Test
    void getInvestments() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        final String currency = Currencies.RUB;
        final SortedMap<OffsetDateTime, BigDecimal> expectedInvestments = new TreeMap<>();
        expectedInvestments.put(OffsetDateTime.now(), DecimalUtils.setDefaultScale(10));
        Mockito.when(fakeContext.getInvestments(accountId, currency)).thenReturn(expectedInvestments);

        final SortedMap<OffsetDateTime, BigDecimal> investments = fakeBot.getInvestments(accountId, currency);

        AssertUtils.assertMapsAreEqual(expectedInvestments, investments);
    }

    @Test
    void getBalance() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        final String currency = Currencies.RUB;
        final BigDecimal expectedBalance = DecimalUtils.setDefaultScale(10);
        Mockito.when(fakeContext.getBalance(accountId, currency)).thenReturn(expectedBalance);

        final BigDecimal balance = fakeBot.getCurrentBalance(accountId, currency);

        AssertUtils.assertEquals(expectedBalance, balance);
    }

    @Test
    void getOperations() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        final Interval interval = Interval.of(OffsetDateTime.now(), OffsetDateTime.now());
        final String figi = TestShares.APPLE.share().figi();
        final List<Operation> expectedOperations = new ArrayList<>();
        Mockito.when(extOperationsService.getOperations(accountId, interval, figi)).thenReturn(expectedOperations);

        final List<Operation> operations = fakeBot.getOperations(accountId, interval, figi);

        AssertUtils.assertEquals(expectedOperations, operations);
    }

    @Test
    void getPortfolioPositions() {
        final String accountId = TestAccounts.TINKOFF.account().id();
        final List<Position> expectedPositions = new ArrayList<>();
        Mockito.when(extOperationsService.getPositions(accountId)).thenReturn(expectedPositions);

        final List<Position> positions = fakeBot.getPortfolioPositions(accountId);

        AssertUtils.assertEquals(expectedPositions, positions);
    }

    @Test
    void getCurrentPrice_usesCurrentDateTime_whenCurrentDateTimeIsNotNull() {
        final String figi = TestShares.APPLE.share().figi();
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final BigDecimal expectedCurrentPrice = DecimalUtils.setDefaultScale(10);

        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(currentDateTime);

        Mockito.when(extMarketDataService.getLastPrice(figi, currentDateTime)).thenReturn(expectedCurrentPrice);

        final BigDecimal currentPrice = fakeBot.getCurrentPrice(figi, null);

        AssertUtils.assertEquals(expectedCurrentPrice, currentPrice);
    }

    @Test
    void getCurrentPrice_usesTo_whenCurrentDateTimeIsNull() {
        final String figi = TestShares.APPLE.share().figi();
        final OffsetDateTime to = OffsetDateTime.now();
        final BigDecimal expectedCurrentPrice = DecimalUtils.setDefaultScale(10);

        Mockito.when(fakeContext.getCurrentDateTime()).thenReturn(null);

        Mockito.when(extMarketDataService.getLastPrice(figi, to)).thenReturn(expectedCurrentPrice);

        final BigDecimal currentPrice = fakeBot.getCurrentPrice(figi, to);

        AssertUtils.assertEquals(expectedCurrentPrice, currentPrice);
    }

}