package ru.obukhov.trader.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.Application;

@AutoConfigureMockMvc
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        args = "--trading.token=i identify myself as token"
)
abstract class ControllerWebTest {

    private static final JsonPathResultMatchers RESULT_MESSAGE_MATCHER =
            MockMvcResultMatchers.jsonPath("$.message");
    private static final JsonPathResultMatchers ERRORS_MATCHER =
            MockMvcResultMatchers.jsonPath("$.errors");

    @Autowired
    protected MockMvc mockMvc;

    protected ResultMatcher getJsonPathMessageMatcher(final String expectedMessage) {
        return RESULT_MESSAGE_MATCHER.value(expectedMessage);
    }

    protected ResultMatcher getJsonErrorsMatcher(final String expectedError) {
        return ERRORS_MATCHER.value(expectedError);
    }

}