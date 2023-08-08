package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.PortfolioPositionBuilder;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
import ru.obukhov.trader.test.utils.model.share.TestShare3;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
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
    void getPositions_returnsBadRequest_whenAccountIdIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/portfolio/positions")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'accountId' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getPositions() throws Exception {
        final String accountId = TestData.ACCOUNT_ID1;

        final String ticker1 = TestShare1.TICKER;
        final String figi1 = TestShare1.FIGI;
        final InstrumentType instrumentType1 = InstrumentType.INSTRUMENT_TYPE_SHARE;
        final int averagePositionPrice1 = 15;
        final int expectedYield1 = 50;
        final int currentPrice1 = 20;
        final int quantityLots1 = 1;
        final int lotSize1 = 10;
        final String currency1 = TestShare1.CURRENCY;

        final String ticker2 = TestShare2.TICKER;
        final String figi2 = TestShare2.FIGI;
        final InstrumentType instrumentType2 = InstrumentType.INSTRUMENT_TYPE_SHARE;
        final int averagePositionPrice2 = 1;
        final int expectedYield2 = 60;
        final int currentPrice2 = 4;
        final int quantityLots2 = 2;
        final int lotSize2 = 10;
        final String currency2 = TestShare2.CURRENCY;

        final String ticker3 = TestShare3.TICKER;
        final String figi3 = TestShare3.FIGI;
        final InstrumentType instrumentType3 = InstrumentType.INSTRUMENT_TYPE_ETF;
        final int averagePositionPrice3 = 15;
        final int expectedYield3 = -25;
        final int currentPrice3 = 10;
        final int quantityLots3 = 5;
        final int lotSize3 = 1;
        final String currency3 = TestShare3.CURRENCY;

        Mocker.mockTickerByFigi(instrumentsService, ticker1, figi1);
        Mocker.mockTickerByFigi(instrumentsService, ticker2, figi2);
        Mocker.mockTickerByFigi(instrumentsService, ticker3, figi3);

        final ru.tinkoff.piapi.contract.v1.PortfolioPosition tinkoffPosition1 = TestData.createTinkoffPortfolioPosition(
                figi1,
                instrumentType1,
                quantityLots1 * lotSize1,
                averagePositionPrice1,
                expectedYield1,
                currentPrice1,
                quantityLots1,
                currency1
        );
        final ru.tinkoff.piapi.contract.v1.PortfolioPosition tinkoffPosition2 = TestData.createTinkoffPortfolioPosition(
                figi2,
                instrumentType2,
                quantityLots2 * lotSize2,
                averagePositionPrice2,
                expectedYield2,
                currentPrice2,
                quantityLots2,
                currency2
        );
        final ru.tinkoff.piapi.contract.v1.PortfolioPosition tinkoffPosition3 = TestData.createTinkoffPortfolioPosition(
                figi3,
                instrumentType3,
                quantityLots3 * lotSize3,
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

        final PortfolioPosition expectedPosition1 = new PortfolioPositionBuilder()
                .setFigi(figi1)
                .setInstrumentType(instrumentType1)
                .setAveragePositionPrice(averagePositionPrice1)
                .setExpectedYield(expectedYield1)
                .setCurrentPrice(currentPrice1)
                .setQuantityLots(quantityLots1)
                .setCurrency(currency1)
                .setLotSize(lotSize1)
                .build();
        final PortfolioPosition expectedPosition2 = new PortfolioPositionBuilder()
                .setFigi(figi2)
                .setInstrumentType(instrumentType2)
                .setAveragePositionPrice(averagePositionPrice2)
                .setExpectedYield(expectedYield2)
                .setCurrentPrice(currentPrice2)
                .setQuantityLots(quantityLots2)
                .setCurrency(currency2)
                .setLotSize(lotSize2)
                .build();
        final PortfolioPosition expectedPosition3 = new PortfolioPositionBuilder()
                .setFigi(figi3)
                .setInstrumentType(instrumentType3)
                .setAveragePositionPrice(averagePositionPrice3)
                .setExpectedYield(expectedYield3)
                .setCurrentPrice(currentPrice3)
                .setQuantityLots(quantityLots3)
                .setCurrency(currency3)
                .setLotSize(lotSize3)
                .build();

        final List<PortfolioPosition> portfolioPositions = List.of(expectedPosition1, expectedPosition2, expectedPosition3);

        performAndExpectResponse(requestBuilder, portfolioPositions);
    }

    // region getAvailableBalances tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getAvailableBalances_returnsBadRequest_whenAccountIdIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/portfolio/balances")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'accountId' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getAvailableBalances_withBlockedValues() throws Exception {
        final String accountId = TestData.ACCOUNT_ID1;

        final String currency1 = Currencies.EUR;
        final String currency2 = Currencies.USD;

        final BigDecimal value1 = DecimalUtils.setDefaultScale(123.456);
        final BigDecimal value2 = DecimalUtils.setDefaultScale(789.012);

        final MoneyValue moneyValue1 = DataStructsHelper.createMoneyValue(currency1, value1);
        final MoneyValue moneyValue2 = DataStructsHelper.createMoneyValue(currency2, value2);
        final List<MoneyValue> moneys = List.of(moneyValue1, moneyValue2);

        final BigDecimal blockedValue1 = DecimalUtils.setDefaultScale(12.34);
        final BigDecimal blockedValue2 = DecimalUtils.setDefaultScale(56.78);

        final MoneyValue blocked1 = DataStructsHelper.createMoneyValue(currency1, blockedValue1);
        final MoneyValue blocked2 = DataStructsHelper.createMoneyValue(currency2, blockedValue2);
        final List<MoneyValue> blocked = List.of(blocked1, blocked2);

        final BigDecimal blockedGuaranteeValue1 = DecimalUtils.setDefaultScale(1.2);
        final BigDecimal blockedGuaranteeValue2 = DecimalUtils.setDefaultScale(3.4);

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

        performAndExpectResponse(requestBuilder, expectedBalances);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getAvailableBalances_withoutBlockedValues() throws Exception {
        final String accountId = TestData.ACCOUNT_ID1;

        final String currency1 = Currencies.EUR;
        final String currency2 = Currencies.USD;

        final BigDecimal value1 = DecimalUtils.setDefaultScale(123.456);
        final BigDecimal value2 = DecimalUtils.setDefaultScale(789.012);

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

        performAndExpectResponse(requestBuilder, expectedBalances);
    }

    // endregion
}