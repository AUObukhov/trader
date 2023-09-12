package ru.obukhov.trader.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.test.utils.TestUtils;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class ControllerIntegrationTest extends IntegrationTest {

    protected static final JsonPathResultMatchers RESULT_MESSAGE_MATCHER = MockMvcResultMatchers.jsonPath("$.message");
    protected static final JsonPathResultMatchers ERRORS_MATCHER = MockMvcResultMatchers.jsonPath("$.errors");
    protected static final ResultMatcher JSON_CONTENT_MATCHER = MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON);

    @Autowired
    protected MockMvc mockMvc;

    protected void assertResponse(final MockHttpServletRequestBuilder builder, final String expectedResponseString) throws Exception {
        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(JSON_CONTENT_MATCHER)
                .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));
    }

    protected void assertResponse(final MockHttpServletRequestBuilder builder, final Object expectedResponse) throws Exception {
        final String expectedResponseString = TestUtils.OBJECT_MAPPER.writeValueAsString(expectedResponse);
        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(JSON_CONTENT_MATCHER)
                .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));
    }

    protected void assertBadRequestResult(final MockHttpServletRequestBuilder requestBuilder, final String expectedResultMessage)
            throws Exception {
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(RESULT_MESSAGE_MATCHER.value(expectedResultMessage))
                .andExpect(JSON_CONTENT_MATCHER);
    }

    protected void assertPostBadRequestError(final String urlTemplate, final Object request, final String expectedError) throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(urlTemplate);
        assertBadRequestError(requestBuilder, request, expectedError);
    }

    @SuppressWarnings("SameParameterValue")
    protected void assertGetBadRequestError(final String urlTemplate, final Object request, final String expectedError) throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(urlTemplate);
        assertBadRequestError(requestBuilder, request, expectedError);
    }

    protected void assertBadRequestError(
            final MockHttpServletRequestBuilder requestBuilder,
            final Object request,
            final String expectedError
    ) throws Exception {
        final String requestString = TestUtils.OBJECT_MAPPER.writeValueAsString(request);

        final MockHttpServletRequestBuilder innerRequestBuilder = requestBuilder
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(innerRequestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(RESULT_MESSAGE_MATCHER.value("Invalid request"))
                .andExpect(ERRORS_MATCHER.value(expectedError))
                .andExpect(JSON_CONTENT_MATCHER);
    }
}