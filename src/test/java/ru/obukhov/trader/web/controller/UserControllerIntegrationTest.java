package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.market.model.BrokerAccountType;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.web.model.exchange.GetUserAccountsResponse;

import java.util.List;

class UserControllerIntegrationTest extends ControllerIntegrationTest {

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getAccounts() throws Exception {
        final List<UserAccount> userAccounts = List.of(
                new UserAccount(BrokerAccountType.TINKOFF_IIS, "2008941383"),
                new UserAccount(BrokerAccountType.TINKOFF, "2000124699")
        );

        mockUserAccounts(userAccounts);

        final GetUserAccountsResponse getUserAccountsResponse = new GetUserAccountsResponse(userAccounts);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/user/accounts")
                .contentType(MediaType.APPLICATION_JSON);
        performAndExpectResponse(requestBuilder, getUserAccountsResponse);
    }

}