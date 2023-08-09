package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
import ru.obukhov.trader.test.utils.model.share.TestShare3;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.PortfolioPosition;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.Portfolio;
import ru.tinkoff.piapi.core.models.Position;
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
        final int quantity1 = 10;
        final int averagePositionPrice1 = 15;
        final int expectedYield1 = 50;
        final int currentPrice1 = 20;
        final int quantityLots1 = 1;
        final String currency1 = TestShare1.CURRENCY;

        final String ticker2 = TestShare2.TICKER;
        final String figi2 = TestShare2.FIGI;
        final InstrumentType instrumentType2 = InstrumentType.INSTRUMENT_TYPE_SHARE;
        final int quantity2 = 20;
        final int averagePositionPrice2 = 1;
        final int expectedYield2 = 60;
        final int currentPrice2 = 4;
        final int quantityLots2 = 2;
        final String currency2 = TestShare2.CURRENCY;

        final String ticker3 = TestShare3.TICKER;
        final String figi3 = TestShare3.FIGI;
        final InstrumentType instrumentType3 = InstrumentType.INSTRUMENT_TYPE_ETF;
        final int quantity3 = 5;
        final int averagePositionPrice3 = 15;
        final int expectedYield3 = -25;
        final int currentPrice3 = 10;
        final int quantityLots3 = 5;
        final String currency3 = TestShare3.CURRENCY;

        Mocker.mockTickerByFigi(instrumentsService, ticker1, figi1);
        Mocker.mockTickerByFigi(instrumentsService, ticker2, figi2);
        Mocker.mockTickerByFigi(instrumentsService, ticker3, figi3);

        final PortfolioPosition tinkoffPosition1 = TestData.createPortfolioPosition(
                figi1,
                instrumentType1,
                quantity1,
                averagePositionPrice1,
                expectedYield1,
                currentPrice1,
                quantityLots1,
                currency1
        );
        final PortfolioPosition tinkoffPosition2 = TestData.createPortfolioPosition(
                figi2,
                instrumentType2,
                quantity2,
                averagePositionPrice2,
                expectedYield2,
                currentPrice2,
                quantityLots2,
                currency2
        );
        final PortfolioPosition tinkoffPosition3 = TestData.createPortfolioPosition(
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

        final Money averagePositionPrice1Money = TestData.createMoney(currency1, averagePositionPrice1);
        final Money currentPrice1Money = TestData.createMoney(currency1, currentPrice1);
        final Position expectedPosition1 = Position.builder()
                .figi(figi1)
                .instrumentType(instrumentType1.toString())
                .quantity(DecimalUtils.setDefaultScale(quantity1))
                .averagePositionPrice(averagePositionPrice1Money)
                .expectedYield(DecimalUtils.setDefaultScale(expectedYield1))
                .currentNkd(TestData.createMoney(currency1, 0))
                .averagePositionPricePt(DecimalUtils.setDefaultScale(0))
                .currentPrice(currentPrice1Money)
                .averagePositionPriceFifo(TestData.createMoney(currency1, 0))
                .quantityLots(DecimalUtils.setDefaultScale(quantityLots1))
                .build();

        final Money averagePositionPrice2Money = TestData.createMoney(currency2, averagePositionPrice2);
        final Money currentPrice2Money = TestData.createMoney(currency2, currentPrice2);
        final Position expectedPosition2 = Position.builder()
                .figi(figi2)
                .instrumentType(instrumentType2.toString())
                .quantity(DecimalUtils.setDefaultScale(quantity2))
                .averagePositionPrice(averagePositionPrice2Money)
                .expectedYield(DecimalUtils.setDefaultScale(expectedYield2))
                .currentNkd(TestData.createMoney(currency2, 0))
                .averagePositionPricePt(DecimalUtils.setDefaultScale(0))
                .currentPrice(currentPrice2Money)
                .averagePositionPriceFifo(TestData.createMoney(currency2, 0))
                .quantityLots(DecimalUtils.setDefaultScale(quantityLots2))
                .build();

        final Money averagePositionPrice3Money = TestData.createMoney(currency3, averagePositionPrice3);
        final Money currentPrice3Money = TestData.createMoney(currency3, currentPrice3);
        final Position expectedPosition3 = Position.builder()
                .figi(figi3)
                .instrumentType(instrumentType3.toString())
                .quantity(DecimalUtils.setDefaultScale(quantity3))
                .averagePositionPrice(averagePositionPrice3Money)
                .expectedYield(DecimalUtils.setDefaultScale(expectedYield3))
                .currentNkd(TestData.createMoney(currency3, 0))
                .averagePositionPricePt(DecimalUtils.setDefaultScale(0))
                .currentPrice(currentPrice3Money)
                .averagePositionPriceFifo(TestData.createMoney(currency3, 0))
                .quantityLots(DecimalUtils.setDefaultScale(quantityLots3))
                .build();

        final List<Position> expectedPositions = List.of(expectedPosition1, expectedPosition2, expectedPosition3);

        performAndExpectResponse(requestBuilder, expectedPositions);
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

        final Money money1 = DataStructsHelper.createMoney(currency1, value1.subtract(blockedValue1).subtract(blockedGuaranteeValue1));
        final Money money2 = DataStructsHelper.createMoney(currency2, value2.subtract(blockedValue2).subtract(blockedGuaranteeValue2));
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

        final Money money1 = DataStructsHelper.createMoney(currency1, value1);
        final Money money2 = DataStructsHelper.createMoney(currency2, value2);
        final List<Money> expectedBalances = List.of(money1, money2);

        performAndExpectResponse(requestBuilder, expectedBalances);
    }

    // endregion
}