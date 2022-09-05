package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.web.model.exchange.GetAvailableBalancesResponse;
import ru.obukhov.trader.web.model.exchange.GetPortfolioPositionsResponse;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.Portfolio;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

class OperationsControllerIntegrationTest extends ControllerIntegrationTest {

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getPositions() throws Exception {
        final String accountId = "2000124699";

        final String ticker1 = "ticker1";
        final String figi1 = "figi1";
        final InstrumentType instrumentType1 = InstrumentType.STOCK;
        final int quantity1 = 10;
        final int averagePositionPrice1 = 15;
        final int expectedYield1 = 50;
        final int currentPrice1 = 20;
        final int quantityLots1 = 1;
        final Currency currency1 = Currency.EUR;

        final String ticker2 = "ticker2";
        final String figi2 = "figi2";
        final InstrumentType instrumentType2 = InstrumentType.STOCK;
        final int quantity2 = 20;
        final int averagePositionPrice2 = 1;
        final int expectedYield2 = 60;
        final int currentPrice2 = 4;
        final int quantityLots2 = 2;
        final Currency currency2 = Currency.USD;

        final String ticker3 = "ticker3";
        final String figi3 = "figi3";
        final InstrumentType instrumentType3 = InstrumentType.ETF;
        final int quantity3 = 5;
        final int averagePositionPrice3 = 15;
        final int expectedYield3 = -25;
        final int currentPrice3 = 10;
        final int quantityLots3 = 5;
        final Currency currency3 = Currency.USD;

        Mocker.mockTickerByFigi(instrumentsService, ticker1, figi1);
        Mocker.mockTickerByFigi(instrumentsService, ticker2, figi2);
        Mocker.mockTickerByFigi(instrumentsService, ticker3, figi3);

        final ru.tinkoff.piapi.contract.v1.PortfolioPosition tinkoffPosition1 = TestData.createTinkoffPortfolioPosition(
                figi1,
                instrumentType1,
                quantity1,
                averagePositionPrice1,
                expectedYield1,
                currentPrice1,
                quantityLots1,
                currency1
        );
        final ru.tinkoff.piapi.contract.v1.PortfolioPosition tinkoffPosition2 = TestData.createTinkoffPortfolioPosition(
                figi2,
                instrumentType2,
                quantity2,
                averagePositionPrice2,
                expectedYield2,
                currentPrice2,
                quantityLots2,
                currency2
        );
        final ru.tinkoff.piapi.contract.v1.PortfolioPosition tinkoffPosition3 = TestData.createTinkoffPortfolioPosition(
                figi3,
                instrumentType3,
                quantity3,
                averagePositionPrice3,
                expectedYield3,
                currentPrice3,
                quantityLots3,
                currency3
        );
        final Portfolio portfolio = TestData.createPortfolio(tinkoffPosition1, tinkoffPosition2, tinkoffPosition3);

        Mockito.when(operationsService.getPortfolioSync(accountId)).thenReturn(portfolio);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/portfolio/positions")
                .param("accountId", accountId)
                .contentType(MediaType.APPLICATION_JSON);

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
        final PortfolioPosition expectedPosition3 = TestData.createPortfolioPosition(
                ticker3,
                instrumentType3,
                quantity3,
                averagePositionPrice3,
                expectedYield3,
                currentPrice3,
                quantityLots3,
                currency3
        );
        final List<PortfolioPosition> portfolioPositions = List.of(expectedPosition1, expectedPosition2, expectedPosition3);

        performAndExpectResponse(requestBuilder, new GetPortfolioPositionsResponse(portfolioPositions));
    }

    // region getAvailableBalances tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getAvailableBalances_withBlockedValues() throws Exception {
        final String accountId = "2000124699";

        final Currency currency1 = Currency.EUR;
        final Currency currency2 = Currency.USD;

        final BigDecimal value1 = BigDecimal.valueOf(123.456);
        final BigDecimal value2 = BigDecimal.valueOf(789.012);

        final MoneyValue moneyValue1 = DataStructsHelper.createMoneyValue(currency1, value1);
        final MoneyValue moneyValue2 = DataStructsHelper.createMoneyValue(currency2, value2);
        final List<MoneyValue> moneys = List.of(moneyValue1, moneyValue2);

        final BigDecimal blockedValue1 = BigDecimal.valueOf(12.34);
        final BigDecimal blockedValue2 = BigDecimal.valueOf(56.78);

        final MoneyValue blocked1 = DataStructsHelper.createMoneyValue(currency1, blockedValue1);
        final MoneyValue blocked2 = DataStructsHelper.createMoneyValue(currency2, blockedValue2);
        final List<MoneyValue> blocked = List.of(blocked1, blocked2);

        final BigDecimal blockedGuaranteeValue1 = BigDecimal.valueOf(1.2);
        final BigDecimal blockedGuaranteeValue2 = BigDecimal.valueOf(3.4);

        final MoneyValue blockedGuarantee1 = DataStructsHelper.createMoneyValue(currency1, blockedGuaranteeValue1);
        final MoneyValue blockedGuarantee2 = DataStructsHelper.createMoneyValue(currency2, blockedGuaranteeValue2);
        final List<MoneyValue> blockedGuarantee = List.of(blockedGuarantee1, blockedGuarantee2);

        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys, blocked, blockedGuarantee);
        Mockito.when(operationsService.getWithdrawLimitsSync(accountId)).thenReturn(withdrawLimits);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/portfolio/balances")
                .param("accountId", accountId)
                .contentType(MediaType.APPLICATION_JSON);

        final Money money1 = TestData.createMoney(currency1, value1.subtract(blockedValue1).subtract(blockedGuaranteeValue1));
        final Money money2 = TestData.createMoney(currency2, value2.subtract(blockedValue2).subtract(blockedGuaranteeValue2));
        final List<Money> expectedBalances = List.of(money1, money2);

        performAndExpectResponse(requestBuilder, new GetAvailableBalancesResponse(expectedBalances));
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getAvailableBalances_withoutBlockedValues() throws Exception {
        final String accountId = "2000124699";

        final Currency currency1 = Currency.EUR;
        final Currency currency2 = Currency.USD;

        final BigDecimal value1 = BigDecimal.valueOf(123.456);
        final BigDecimal value2 = BigDecimal.valueOf(789.012);

        final MoneyValue moneyValue1 = DataStructsHelper.createMoneyValue(currency1, value1);
        final MoneyValue moneyValue2 = DataStructsHelper.createMoneyValue(currency2, value2);

        final List<MoneyValue> moneys = List.of(moneyValue1, moneyValue2);
        final List<MoneyValue> blocked = Collections.emptyList();
        final List<MoneyValue> blockedGuarantee = Collections.emptyList();

        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys, blocked, blockedGuarantee);
        Mockito.when(operationsService.getWithdrawLimitsSync(accountId)).thenReturn(withdrawLimits);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/portfolio/balances")
                .param("accountId", accountId)
                .contentType(MediaType.APPLICATION_JSON);

        final Money money1 = TestData.createMoney(currency1, value1);
        final Money money2 = TestData.createMoney(currency2, value2);
        final List<Money> expectedBalances = List.of(money1, money2);

        performAndExpectResponse(requestBuilder, new GetAvailableBalancesResponse(expectedBalances));
    }

    // endregion
}