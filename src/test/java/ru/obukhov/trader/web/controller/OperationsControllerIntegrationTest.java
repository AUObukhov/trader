package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.share.TestShare;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.PortfolioPosition;
import ru.tinkoff.piapi.core.models.Portfolio;
import ru.tinkoff.piapi.core.models.Position;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class OperationsControllerIntegrationTest extends ControllerIntegrationTest {

    @Test
    void getPositions_returnsBadRequest_whenAccountIdIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/portfolio/positions")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'accountId' for method parameter type String is not present";
        assertBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    void getPositions() throws Exception {
        final String accountId = TestAccounts.TINKOFF.getId();

        final TestShare share1 = TestShares.APPLE;
        final TestShare share2 = TestShares.SBER;
        final TestShare share3 = TestShares.YANDEX;

        final String ticker1 = share1.getTicker();
        final String figi1 = share1.getFigi();
        final InstrumentType instrumentType1 = InstrumentType.INSTRUMENT_TYPE_SHARE;
        final int quantity1 = 10;
        final int averagePositionPrice1 = 15;
        final int expectedYield1 = 50;
        final int currentPrice1 = 20;
        final String currency1 = share1.getCurrency();

        final String ticker2 = share2.getTicker();
        final String figi2 = share2.getFigi();
        final InstrumentType instrumentType2 = InstrumentType.INSTRUMENT_TYPE_SHARE;
        final int quantity2 = 20;
        final int averagePositionPrice2 = 1;
        final int expectedYield2 = 60;
        final int currentPrice2 = 4;
        final String currency2 = share2.getCurrency();

        final String ticker3 = share3.getTicker();
        final String figi3 = share3.getFigi();
        final InstrumentType instrumentType3 = InstrumentType.INSTRUMENT_TYPE_ETF;
        final int quantity3 = 5;
        final int averagePositionPrice3 = 15;
        final int expectedYield3 = -25;
        final int currentPrice3 = 10;
        final String currency3 = share3.getCurrency();

        Mocker.mockTickerByFigi(instrumentsService, ticker1, figi1);
        Mocker.mockTickerByFigi(instrumentsService, ticker2, figi2);
        Mocker.mockTickerByFigi(instrumentsService, ticker3, figi3);

        final PortfolioPosition tPosition1 = TestData.newPortfolioPosition(
                figi1,
                instrumentType1,
                quantity1,
                averagePositionPrice1,
                expectedYield1,
                currentPrice1,
                currency1
        );
        final PortfolioPosition tPosition2 = TestData.newPortfolioPosition(
                figi2,
                instrumentType2,
                quantity2,
                averagePositionPrice2,
                expectedYield2,
                currentPrice2,
                currency2
        );
        final PortfolioPosition tPosition3 = TestData.newPortfolioPosition(
                figi3,
                instrumentType3,
                quantity3,
                averagePositionPrice3,
                expectedYield3,
                currentPrice3,
                currency3
        );
        final Portfolio portfolio = TestData.newPortfolio(tPosition1, tPosition2, tPosition3);

        Mockito.when(operationsService.getPortfolioSync(accountId)).thenReturn(portfolio);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/portfolio/positions")
                .param("accountId", accountId)
                .contentType(MediaType.APPLICATION_JSON);

        final Position expectedPosition1 = new PositionBuilder()
                .setCurrency(currency1)
                .setFigi(figi1)
                .setInstrumentType(instrumentType1)
                .setQuantity(quantity1)
                .setAveragePositionPrice(averagePositionPrice1)
                .setExpectedYield(expectedYield1)
                .setCurrentNkd(0)
                .setCurrentPrice(currentPrice1)
                .setAveragePositionPriceFifo(0)
                .build();
        final Position expectedPosition2 = new PositionBuilder()
                .setCurrency(currency2)
                .setFigi(figi2)
                .setInstrumentType(instrumentType2)
                .setQuantity(quantity2)
                .setAveragePositionPrice(averagePositionPrice2)
                .setExpectedYield(expectedYield2)
                .setCurrentNkd(0)
                .setCurrentPrice(currentPrice2)
                .setAveragePositionPriceFifo(0)
                .build();
        final Position expectedPosition3 = new PositionBuilder()
                .setCurrency(currency3)
                .setFigi(figi3)
                .setInstrumentType(instrumentType3)
                .setQuantity(quantity3)
                .setAveragePositionPrice(averagePositionPrice3)
                .setExpectedYield(expectedYield3)
                .setCurrentNkd(0)
                .setCurrentPrice(currentPrice3)
                .setAveragePositionPriceFifo(0)
                .build();

        final List<Position> expectedPositions = List.of(expectedPosition1, expectedPosition2, expectedPosition3);

        assertResponse(requestBuilder, expectedPositions);
    }

    // region getAvailableBalances tests

    @Test
    void getAvailableBalances_returnsBadRequest_whenAccountIdIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/portfolio/balances")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'accountId' for method parameter type String is not present";
        assertBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    void getAvailableBalances_withBlockedValues() throws Exception {
        final String accountId = TestAccounts.TINKOFF.getId();

        final String currency1 = Currencies.EUR;
        final String currency2 = Currencies.USD;

        final BigDecimal value1 = DecimalUtils.setDefaultScale(123.456);
        final BigDecimal value2 = DecimalUtils.setDefaultScale(789.012);

        final MoneyValue moneyValue1 = DataStructsHelper.newMoneyValue(currency1, value1);
        final MoneyValue moneyValue2 = DataStructsHelper.newMoneyValue(currency2, value2);
        final List<MoneyValue> moneys = List.of(moneyValue1, moneyValue2);

        final BigDecimal blockedValue1 = DecimalUtils.setDefaultScale(12.34);
        final BigDecimal blockedValue2 = DecimalUtils.setDefaultScale(56.78);

        final MoneyValue blocked1 = DataStructsHelper.newMoneyValue(currency1, blockedValue1);
        final MoneyValue blocked2 = DataStructsHelper.newMoneyValue(currency2, blockedValue2);
        final List<MoneyValue> blocked = List.of(blocked1, blocked2);

        final BigDecimal blockedGuaranteeValue1 = DecimalUtils.setDefaultScale(1.2);
        final BigDecimal blockedGuaranteeValue2 = DecimalUtils.setDefaultScale(3.4);

        final MoneyValue blockedGuarantee1 = DataStructsHelper.newMoneyValue(currency1, blockedGuaranteeValue1);
        final MoneyValue blockedGuarantee2 = DataStructsHelper.newMoneyValue(currency2, blockedGuaranteeValue2);
        final List<MoneyValue> blockedGuarantee = List.of(blockedGuarantee1, blockedGuarantee2);

        final WithdrawLimits withdrawLimits = DataStructsHelper.newWithdrawLimits(moneys, blocked, blockedGuarantee);
        Mockito.when(operationsService.getWithdrawLimitsSync(accountId)).thenReturn(withdrawLimits);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/portfolio/balances")
                .param("accountId", accountId)
                .contentType(MediaType.APPLICATION_JSON);

        final Map<String, BigDecimal> expectedBalances = Map.of(
                currency1, value1.subtract(blockedValue1).subtract(blockedGuaranteeValue1),
                currency2, value2.subtract(blockedValue2).subtract(blockedGuaranteeValue2)
        );

        assertResponse(requestBuilder, expectedBalances);
    }

    @Test
    void getAvailableBalances_withoutBlockedValues() throws Exception {
        final String accountId = TestAccounts.TINKOFF.getId();

        final String currency1 = Currencies.EUR;
        final String currency2 = Currencies.USD;

        final BigDecimal value1 = DecimalUtils.setDefaultScale(123.456);
        final BigDecimal value2 = DecimalUtils.setDefaultScale(789.012);

        final MoneyValue moneyValue1 = DataStructsHelper.newMoneyValue(currency1, value1);
        final MoneyValue moneyValue2 = DataStructsHelper.newMoneyValue(currency2, value2);

        final List<MoneyValue> moneys = List.of(moneyValue1, moneyValue2);
        final List<MoneyValue> blocked = Collections.emptyList();
        final List<MoneyValue> blockedGuarantee = Collections.emptyList();

        final WithdrawLimits withdrawLimits = DataStructsHelper.newWithdrawLimits(moneys, blocked, blockedGuarantee);
        Mockito.when(operationsService.getWithdrawLimitsSync(accountId)).thenReturn(withdrawLimits);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/portfolio/balances")
                .param("accountId", accountId)
                .contentType(MediaType.APPLICATION_JSON);

        final Map<String, BigDecimal> expectedBalances = Map.of(currency1, value1, currency2, value2);

        assertResponse(requestBuilder, expectedBalances);
    }

    // endregion
}