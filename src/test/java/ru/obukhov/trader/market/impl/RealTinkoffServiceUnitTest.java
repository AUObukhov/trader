package ru.obukhov.trader.market.impl;

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
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.AccessLevel;
import ru.tinkoff.piapi.contract.v1.Account;
import ru.tinkoff.piapi.contract.v1.AccountStatus;
import ru.tinkoff.piapi.contract.v1.AccountType;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus;
import ru.tinkoff.piapi.contract.v1.OrderStage;
import ru.tinkoff.piapi.contract.v1.OrderState;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.OrdersService;
import ru.tinkoff.piapi.core.UsersService;
import ru.tinkoff.piapi.core.models.Portfolio;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class RealTinkoffServiceUnitTest {

    @Mock
    private InstrumentsService instrumentsService;
    @Mock
    private OperationsService operationsService;
    @Mock
    private OrdersService ordersService;
    @Mock
    private UsersService usersService;
    @Mock
    private ExtInstrumentsService extInstrumentsService;

    @InjectMocks
    private RealTinkoffService realTinkoffService;

    // region OperationsContext methods tests

    @Test
    void getOperations_returnsOperations() throws IOException {
        final String accountId = "2000124699";
        final String ticker = "ticker";
        final String figi = "figi";
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 1, 2);

        Mockito.when(extInstrumentsService.getFigiByTicker(ticker)).thenReturn(figi);

        final Operation operation1 = TestData.createOperation();
        final Operation operation2 = TestData.createOperation();
        final List<Operation> operations = List.of(operation1, operation2);
        Mockito.when(operationsService.getAllOperationsSync(accountId, from.toInstant(), to.toInstant(), figi)).thenReturn(operations);

        final List<Operation> result = realTinkoffService.getOperations(accountId, Interval.of(from, to), ticker);

        Assertions.assertSame(operations, result);
    }

    // endregion

    // region OrdersContext methods tests

    @Test
    void getOrders() {

        // arrange

        final String accountId = "2000124699";

        // todo realistic data (copy from OrderMapperTest)
        final Currency currency1 = Currency.EUR;
        final String orderId1 = "orderId1";
        final OrderExecutionReportStatus executionReportStatus1 = OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_FILL;
        final int lotsRequested1 = 1;
        final int lotsExecuted1 = 2;
        final double initialOrderPrice1 = 3;
        final double totalOrderAmount1 = 4;
        final double averagePositionPrice1 = 5;
        final double initialCommission1 = 6;
        final double executedCommission1 = 7;
        final String figi1 = "figi1";
        final OrderDirection orderDirection1 = OrderDirection.ORDER_DIRECTION_BUY;
        final double initialSecurityPrice1 = 8;
        final List<OrderStage> stages1 = List.of(
                TestData.createOrderStage(currency1, 9, 10, "tradeId1"),
                TestData.createOrderStage(currency1, 11, 12, "tradeId1")
        );
        final double serviceCommission1 = 13;
        final ru.tinkoff.piapi.contract.v1.OrderType orderType1 = OrderType.ORDER_TYPE_MARKET;
        final OffsetDateTime orderDate1 = OffsetDateTime.now();

        final OrderState orderState1 = TestData.createOrderState(
                currency1,
                orderId1,
                executionReportStatus1,
                lotsRequested1,
                lotsExecuted1,
                initialOrderPrice1,
                totalOrderAmount1,
                averagePositionPrice1,
                initialCommission1,
                executedCommission1,
                figi1,
                orderDirection1,
                initialSecurityPrice1,
                stages1,
                serviceCommission1,
                orderType1,
                orderDate1
        );

        final Currency currency2 = Currency.USD;
        final String orderId2 = "orderId2";
        final OrderExecutionReportStatus executionReportStatus2 = OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_NEW;
        final int lotsRequested2 = 14;
        final int lotsExecuted2 = 15;
        final double initialOrderPrice2 = 16;
        final double totalOrderAmount2 = 17;
        final double averagePositionPrice2 = 18;
        final double initialCommission2 = 19;
        final double executedCommission2 = 20;
        final String figi2 = "figi2";
        final OrderDirection orderDirection2 = OrderDirection.ORDER_DIRECTION_SELL;
        final double initialSecurityPrice2 = 21;
        final List<OrderStage> stages2 = List.of(
                TestData.createOrderStage(currency2, 22, 23, "tradeId2"),
                TestData.createOrderStage(currency2, 24, 25, "tradeId2")
        );
        final double serviceCommission2 = 26;
        final ru.tinkoff.piapi.contract.v1.OrderType orderType2 = OrderType.ORDER_TYPE_LIMIT;
        final OffsetDateTime orderDate2 = OffsetDateTime.now();

        final OrderState orderState2 = TestData.createOrderState(
                currency2,
                orderId2,
                executionReportStatus2,
                lotsRequested2,
                lotsExecuted2,
                initialOrderPrice2,
                totalOrderAmount2,
                averagePositionPrice2,
                initialCommission2,
                executedCommission2,
                figi2,
                orderDirection2,
                initialSecurityPrice2,
                stages2,
                serviceCommission2,
                orderType2,
                orderDate2
        );

        final List<OrderState> orderStates = List.of(orderState1, orderState2);
        Mockito.when(ordersService.getOrdersSync(accountId)).thenReturn(orderStates);

        // action

        final List<Order> result = realTinkoffService.getOrders(accountId);

        // assert

        final Order order1 = TestData.createOrder(
                currency1,
                orderId1,
                executionReportStatus1,
                lotsExecuted1,
                initialOrderPrice1,
                totalOrderAmount1,
                averagePositionPrice1,
                executedCommission1,
                figi1,
                orderDirection1,
                initialSecurityPrice1,
                serviceCommission1,
                orderType1,
                orderDate1
        );
        final Order order2 = TestData.createOrder(
                currency2,
                orderId2,
                executionReportStatus2,
                lotsExecuted2,
                initialOrderPrice2,
                totalOrderAmount2,
                averagePositionPrice2,
                executedCommission2,
                figi2,
                orderDirection2,
                initialSecurityPrice2,
                serviceCommission2,
                orderType2,
                orderDate2
        );
        final List<Order> expectedResult = List.of(order1, order2);

        AssertUtils.assertEquals(expectedResult, result);
    }

    @Test
    void postOrder() {
        final String accountId = "2000124699";
        final String ticker = "ticker";
        final String figi = "figi";
        final long quantityLots = 10;
        final BigDecimal price = BigDecimal.valueOf(200);
        final OrderDirection direction = OrderDirection.ORDER_DIRECTION_BUY;
        final OrderType type = OrderType.ORDER_TYPE_MARKET;
        final String orderId = "orderId";

        Mockito.when(extInstrumentsService.getFigiByTicker(ticker)).thenReturn(figi);

        PostOrderResponse response = TestData.createPostOrderResponse(
                Currency.USD,
                2000,
                10,
                20,
                orderId,
                quantityLots,
                figi,
                direction,
                type
        );
        Mockito.when(ordersService.postOrderSync(figi, quantityLots, DecimalUtils.toQuotation(price), direction, accountId, type, orderId))
                .thenReturn(response);

        final PostOrderResponse result = realTinkoffService.postOrder(accountId, ticker, quantityLots, price, direction, type, orderId);

        Assertions.assertSame(response, result);
    }

    @Test
    void cancelOrder() {
        final String accountId = "2000124699";
        final String orderId = "orderId";

        realTinkoffService.cancelOrder(accountId, orderId);

        Mockito.verify(ordersService, Mockito.times(1)).cancelOrderSync(accountId, orderId);
    }

    // endregion

    // region PortfolioContext methods tests

    @Test
    void getPortfolioPositions_returnsAndMapsPositions() {
        final String accountId = "2000124699";

        final String figi1 = "figi1";
        final String ticker1 = "ticker1";
        final InstrumentType instrumentType1 = InstrumentType.STOCK;
        final int quantity1 = 1000;
        final int averagePositionPrice1 = 110;
        final int expectedYield1 = 10000;
        final int currentPrice1 = 120;
        final int quantityLots1 = 10;
        final Currency currency1 = Currency.RUB;

        final String figi2 = "figi2";
        final String ticker2 = "ticker2";
        final InstrumentType instrumentType2 = InstrumentType.ETF;
        final int quantity2 = 2000;
        final int averagePositionPrice2 = 440;
        final int expectedYield2 = -10000;
        final int currentPrice2 = 430;
        final int quantityLots2 = 5;
        final Currency currency2 = Currency.USD;

        Mocker.mockTickerByFigi(instrumentsService, ticker1, figi1);
        Mocker.mockTickerByFigi(instrumentsService, ticker2, figi2);

        final ru.tinkoff.piapi.contract.v1.PortfolioPosition tinkoffPortfolioPosition1 = TestData.createTinkoffPortfolioPosition(
                figi1,
                instrumentType1,
                quantity1,
                averagePositionPrice1,
                expectedYield1,
                currentPrice1,
                quantityLots1,
                currency1
        );

        final ru.tinkoff.piapi.contract.v1.PortfolioPosition tinkoffPortfolioPosition2 = TestData.createTinkoffPortfolioPosition(
                figi2,
                instrumentType2,
                quantity2,
                averagePositionPrice2,
                expectedYield2,
                currentPrice2,
                quantityLots2,
                currency2
        );

        final Portfolio portfolio = TestData.createPortfolio(tinkoffPortfolioPosition1, tinkoffPortfolioPosition2);
        Mockito.when(operationsService.getPortfolioSync(accountId)).thenReturn(portfolio);

        final List<PortfolioPosition> result = realTinkoffService.getPortfolioPositions(accountId);

        final PortfolioPosition expectedPosition1 = TestData.createPortfolioPosition(
                ticker1,
                instrumentType1,
                quantity1,
                averagePositionPrice1,
                expectedYield1,
                currentPrice1,
                quantityLots1,
                currency1
        );
        final PortfolioPosition expectedPosition2 = TestData.createPortfolioPosition(
                ticker2,
                instrumentType2,
                quantity2,
                averagePositionPrice2,
                expectedYield2,
                currentPrice2,
                quantityLots2,
                currency2
        );

        Assertions.assertEquals(2, result.size());
        AssertUtils.assertEquals(expectedPosition1, result.get(0));
        AssertUtils.assertEquals(expectedPosition2, result.get(1));
    }

    @Test
    void getPortfolioCurrencies() {
        final String accountId = "2000124699";

        final List<MoneyValue> moneys = List.of(
                TestData.createMoneyValue(10000, Currency.RUB),
                TestData.createMoneyValue(1000, Currency.USD)
        );
        final List<MoneyValue> blocked = List.of(TestData.createMoneyValue(1000, Currency.RUB));
        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys, blocked);
        Mockito.when(operationsService.getWithdrawLimitsSync(accountId)).thenReturn(withdrawLimits);

        final WithdrawLimits result = realTinkoffService.getWithdrawLimits(accountId);

        Assertions.assertSame(withdrawLimits, result);
    }

    // endregion

    // region UserContext methods tests

    @Test
    void getUserAccounts() {
        final String id1 = "2008941383";
        final AccountType accountType1 = AccountType.ACCOUNT_TYPE_TINKOFF_IIS;
        final String name1 = "ИИС";
        final AccountStatus accountStatus1 = AccountStatus.ACCOUNT_STATUS_OPEN;
        final Timestamp openedDate1 = DateTimeTestData.createTimestamp(1562889600);
        final OffsetDateTime openedDateTime1 = DateTimeTestData.createDateTime(2019, 7, 12, 3, ZoneOffset.ofHours(3));
        final Timestamp closedDate1 = DateTimeTestData.createTimestamp(-10800);
        final OffsetDateTime closedDateTime1 = DateTimeTestData.createDateTime(1970, 1, 1, ZoneOffset.ofHours(3));
        final AccessLevel accessLevel1 = AccessLevel.ACCOUNT_ACCESS_LEVEL_FULL_ACCESS;

        Account account1 = Account.newBuilder()
                .setId(id1)
                .setType(accountType1)
                .setName(name1)
                .setStatus(accountStatus1)
                .setOpenedDate(openedDate1)
                .setClosedDate(closedDate1)
                .setAccessLevel(accessLevel1)
                .build();

        final String id2 = "2000124699";
        final AccountType accountType2 = AccountType.ACCOUNT_TYPE_TINKOFF;
        final String name2 = "Брокерский счёт";
        final AccountStatus accountStatus2 = AccountStatus.ACCOUNT_STATUS_OPEN;
        final Timestamp openedDate2 = DateTimeTestData.createTimestamp(1527206400);
        final OffsetDateTime openedDateTime2 = DateTimeTestData.createDateTime(2018, 5, 25, 3, ZoneOffset.ofHours(3));
        final Timestamp closedDate2 = DateTimeTestData.createTimestamp(-10800);
        final OffsetDateTime closedDateTime2 = DateTimeTestData.createDateTime(1970, 1, 1, ZoneOffset.ofHours(3));
        final AccessLevel accessLevel2 = AccessLevel.ACCOUNT_ACCESS_LEVEL_FULL_ACCESS;

        Account account2 = Account.newBuilder()
                .setId(id2)
                .setType(accountType2)
                .setName(name2)
                .setStatus(accountStatus2)
                .setOpenedDate(openedDate2)
                .setClosedDate(closedDate2)
                .setAccessLevel(accessLevel2)
                .build();

        Mockito.when(usersService.getAccountsSync())
                .thenReturn(List.of(account1, account2));

        final UserAccount userAccount1 = new UserAccount(id1, accountType1, name1, accountStatus1, openedDateTime1, closedDateTime1, accessLevel1);
        final UserAccount userAccount2 = new UserAccount(id2, accountType2, name2, accountStatus2, openedDateTime2, closedDateTime2, accessLevel2);
        final List<UserAccount> expectedUserAccounts = List.of(userAccount1, userAccount2);

        Mockito.when(usersService.getAccountsSync()).thenReturn(List.of(account1, account2));

        final List<UserAccount> result = realTinkoffService.getAccounts();

        Assertions.assertEquals(expectedUserAccounts, result);
    }

    // endregion

    @Test
    void getCurrentDateTime_returnsCurrentDateTime() {
        final OffsetDateTime now = OffsetDateTime.now();

        final OffsetDateTime currentDateTime = realTinkoffService.getCurrentDateTime();

        final long delay = Duration.between(now, currentDateTime).toMillis();

        Assertions.assertTrue(delay >= 0);

        final int maxDelay = 5;
        Assertions.assertTrue(delay < maxDelay);
    }

}