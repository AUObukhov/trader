package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.market.model.BrokerAccountType;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.market.model.UserAccounts;
import ru.obukhov.trader.web.client.exchange.UserAccountsResponse;
import ru.obukhov.trader.web.model.exchange.GetUserAccountsResponse;

import java.util.List;

class UserControllerIntegrationTest extends ControllerIntegrationTest {

    @Test
    void getAccounts() throws Exception {
        final HttpRequest apiRequest = HttpRequest.request()
                .withHeader(HttpHeaders.AUTHORIZATION, getAuthorizationHeader())
                .withMethod(HttpMethod.GET.name())
                .withPath("/openapi/user/accounts");

        final UserAccount userAccount1 = new UserAccount();
        userAccount1.setBrokerAccountType(BrokerAccountType.TINKOFF_IIS);
        userAccount1.setBrokerAccountId("2008941383");

        final UserAccount userAccount2 = new UserAccount();
        userAccount2.setBrokerAccountType(BrokerAccountType.TINKOFF);
        userAccount2.setBrokerAccountId("2000124699");

        final List<UserAccount> userAccountsList = List.of(userAccount1, userAccount2);
        final UserAccounts userAccounts = new UserAccounts();
        userAccounts.setAccounts(userAccountsList);

        final UserAccountsResponse userAccountsResponse = new UserAccountsResponse();
        userAccountsResponse.setPayload(userAccounts);
        mockResponse(apiRequest, userAccountsResponse);

        final GetUserAccountsResponse getUserAccountsResponse = new GetUserAccountsResponse(userAccountsList);
        final String expectedResponse = objectMapper.writeValueAsString(getUserAccountsResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/trader/user/accounts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
    }

}