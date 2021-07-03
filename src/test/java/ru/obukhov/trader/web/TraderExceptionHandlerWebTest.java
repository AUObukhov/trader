package ru.obukhov.trader.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
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
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        args = "--trading.token=i identify myself as token"
)
class TraderExceptionHandlerWebTest {

    private final TestController controller = new TestController();
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new TraderExceptionHandler())
            .build();

    @Test
    @SuppressWarnings("unused")
    void handlesMethodArgumentNotValidException() throws Exception {
        final OffsetDateTime mockedNow = DateUtils.getDateTime(
                2020,
                9,
                23,
                10,
                0,
                0
        );
        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            final String expectedResponse = ResourceUtils.getTestDataAsString("TestValidationResponse.json");

            mockMvc.perform(MockMvcRequestBuilders.post("/trader/test/validation")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
        }
    }

    @Test
    @SuppressWarnings("unused")
    void handlesMissingServletRequestParameterException() throws Exception {
        final OffsetDateTime mockedNow = DateUtils.getDateTime(
                2020,
                9,
                23,
                10,
                0,
                0
        );
        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            final String expectedResponse = ResourceUtils.getTestDataAsString("TestMissingParamResponse.json");

            mockMvc.perform(MockMvcRequestBuilders.post("/trader/test/missingParam")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
        }
    }

    @Test
    @SuppressWarnings("unused")
    void handlesRuntimeException() throws Exception {
        final OffsetDateTime mockedNow =
                DateUtils.getDateTime(2020, 9, 23, 10, 0, 0);
        try (final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = TestDataHelper.mockNow(mockedNow)) {
            final String expectedResponse = ResourceUtils.getTestDataAsString("RuntimeExceptionResponse.json");

            mockMvc.perform(MockMvcRequestBuilders.post("/trader/test/runtime")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                    .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
        }
    }

    @Slf4j
    @RestController
    @RequestMapping("trader/test")
    @RequiredArgsConstructor
    @SuppressWarnings("unused")
    public static class TestController {

        @PostMapping("/validation")
        public void throwMethodArgumentNotValidException()
                throws MethodArgumentNotValidException, NoSuchMethodException {

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