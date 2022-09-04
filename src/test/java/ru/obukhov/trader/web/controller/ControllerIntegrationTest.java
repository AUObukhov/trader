package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.UsersService;

import java.util.Collections;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
public abstract class ControllerIntegrationTest {

    protected static final JsonPathResultMatchers RESULT_MESSAGE_MATCHER = MockMvcResultMatchers.jsonPath("$.message");
    protected static final JsonPathResultMatchers ERRORS_MATCHER = MockMvcResultMatchers.jsonPath("$.errors");
    protected static final ResultMatcher JSON_CONTENT_MATCHER = MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON);

    @MockBean
    protected InstrumentsService instrumentsService;
    @MockBean
    protected MarketDataService marketDataService;
    @MockBean
    protected UsersService usersService;

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    void init() {
        Mockito.when(usersService.getAccountsSync()).thenReturn(Collections.emptyList());
    }

    protected void performAndExpectResponse(final MockHttpServletRequestBuilder builder, final Object expectedResponse) throws Exception {
        final String expectedResponseString = TestUtils.OBJECT_MAPPER.writeValueAsString(expectedResponse);
        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(JSON_CONTENT_MATCHER)
                .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));
    }

    protected void performAndExpectBadRequestResult(final MockHttpServletRequestBuilder requestBuilder, final String expectedResultMessage)
            throws Exception {
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(RESULT_MESSAGE_MATCHER.value(expectedResultMessage))
                .andExpect(JSON_CONTENT_MATCHER);
    }

    protected void performAndExpectBadRequestError(final String urlTemplate, final Object request, final String expectedError) throws Exception {
        final String requestString = TestUtils.OBJECT_MAPPER.writeValueAsString(request);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(urlTemplate)
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(RESULT_MESSAGE_MATCHER.value("Invalid request"))
                .andExpect(ERRORS_MATCHER.value(expectedError))
                .andExpect(JSON_CONTENT_MATCHER);
    }

}