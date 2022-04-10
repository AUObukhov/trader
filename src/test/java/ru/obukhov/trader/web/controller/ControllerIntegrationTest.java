package ru.obukhov.trader.web.controller;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mockserver.model.HttpRequest;
import org.mockserver.springtest.MockServerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.TokenValidationStartupListener;
import ru.obukhov.trader.config.properties.ApiProperties;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.test.utils.TestUtils;

@MockServerTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
abstract class ControllerIntegrationTest extends TestWithMockedServer {

    protected static final JsonPathResultMatchers RESULT_MESSAGE_MATCHER = MockMvcResultMatchers.jsonPath("$.message");
    protected static final JsonPathResultMatchers ERRORS_MATCHER = MockMvcResultMatchers.jsonPath("$.errors");
    protected static final ResultMatcher JSON_CONTENT_MATCHER = MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON);

    /**
     * To prevent token validation on startup. Otherwise, validation performed before MockServer initialization
     */
    @MockBean
    private TokenValidationStartupListener tokenValidationStartupListener;

    @Autowired
    protected TradingProperties tradingProperties;
    @Autowired
    protected ApiProperties apiProperties;
    @Autowired
    protected MockMvc mockMvc;

    protected int getPort() {
        return ObjectUtils.defaultIfNull(apiProperties.port(), 8081);
    }

    protected HttpRequest createAuthorizedHttpRequest(final HttpMethod httpMethod) {
        return HttpRequest.request()
                .withHeader(HttpHeaders.AUTHORIZATION, "Bearer " + tradingProperties.getToken())
                .withMethod(httpMethod.name());
    }

    protected void performAndExpectResponse(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(StringUtils.EMPTY));
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