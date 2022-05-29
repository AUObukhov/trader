package ru.obukhov.trader.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.Application;
import ru.obukhov.trader.ContextTest;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        args = "--trading.token=i identify myself as token"
)
class TraderExceptionHandlerWebTest extends ContextTest {

    private final TestController controller = new TestController();
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new TraderExceptionHandler())
            .build();

    @Test
    @SuppressWarnings("unused")
    void handlesMethodArgumentNotValidException() throws Exception {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 10);
        final Map<String, Object> expectedResponse = Map.of(
                "time", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(mockedNow),
                "message", "Invalid request",
                "errors", List.of("validation error1", "validation error2")
        );
        final String expectedResponseString = TestUtils.OBJECT_MAPPER.writeValueAsString(expectedResponse);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            mockMvc.perform(MockMvcRequestBuilders.post("/trader/test/validation")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));
        }
    }

    @Test
    @SuppressWarnings("unused")
    void handlesMissingServletRequestParameterException() throws Exception {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 10);
        final Map<String, String> expectedResponse = Map.of(
                "time", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(mockedNow),
                "message", "Required request parameter 'param' for method parameter type String is not present"
        );
        final String expectedResponseString = TestUtils.OBJECT_MAPPER.writeValueAsString(expectedResponse);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            mockMvc.perform(MockMvcRequestBuilders.post("/trader/test/missingParam")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));
        }
    }

    @Test
    @SuppressWarnings("unused")
    void handlesRuntimeException() throws Exception {
        final OffsetDateTime mockedNow = DateTimeTestData.createDateTime(2020, 9, 23, 10);
        final Map<String, String> expectedResponse = Map.of(
                "time", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(mockedNow),
                "message", "runtime exception message"
        );
        final String expectedResponseString = TestUtils.OBJECT_MAPPER.writeValueAsString(expectedResponse);

        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            mockMvc.perform(MockMvcRequestBuilders.post("/trader/test/runtime")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                    .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));
        }
    }

    @Slf4j
    @RestController
    @RequestMapping("trader/test")
    @RequiredArgsConstructor
    @SuppressWarnings("unused")
    public static class TestController {

        @PostMapping("/validation")
        public void throwMethodArgumentNotValidException() throws MethodArgumentNotValidException, NoSuchMethodException {

            final Method method = this.getClass().getMethod("throwMethodArgumentNotValidException");
            final MethodParameter parameter = new MethodParameter(method, -1);

            final BindingResult bindingResult = new BeanPropertyBindingResult(null, StringUtils.EMPTY);
            bindingResult.addError(new ObjectError("objectName1", "validation error1"));
            bindingResult.addError(new ObjectError("objectName2", "validation error2"));

            throw new MethodArgumentNotValidException(parameter, bindingResult);
        }

        @PostMapping("/missingParam")
        public void throwMissingServletRequestParameterException(@RequestParam final String param) {
        }

        @PostMapping("/runtime")
        public void throwRuntimeException() {
            throw new RuntimeException("runtime exception message");
        }

    }
}