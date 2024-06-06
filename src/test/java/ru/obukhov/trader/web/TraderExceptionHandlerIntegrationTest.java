package ru.obukhov.trader.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
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
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.common.exception.InstrumentNotFoundException;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;

@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        args = "--trading.token=i identify myself as token"
)
@AutoConfigureMockMvc
class TraderExceptionHandlerIntegrationTest extends IntegrationTest {

    private final TestController controller = new TestController();
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new TraderExceptionHandler())
            .build();

    @Test
    void handlesMethodArgumentNotValidException() throws Exception {
        final OffsetDateTime mockedNow = DateTimeTestData.newDateTime(2020, 9, 23, 10);
        final SequencedMap<String, Object> expectedResponse = new LinkedHashMap<>();
        expectedResponse.put("time", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(mockedNow));
        expectedResponse.put("message", "Invalid request");
        expectedResponse.put("errors", List.of("validation error1", "validation error2"));

        final String expectedResponseString = TestUtils.OBJECT_MAPPER.writeValueAsString(expectedResponse);

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            mockMvc.perform(MockMvcRequestBuilders.post("/trader/test/methodArgumentNotValid")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));
        }
    }

    @Test
    void handlesConstraintViolationException() throws Exception {
        final OffsetDateTime mockedNow = DateTimeTestData.newDateTime(2020, 9, 23, 10);
        final SequencedMap<String, Object> expectedResponse = new LinkedHashMap<>(3, 1);
        expectedResponse.put("time", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(mockedNow));
        expectedResponse.put("message", "Invalid request");
        expectedResponse.put("errors", List.of("validation error1", "validation error2"));

        final String expectedResponseString = TestUtils.OBJECT_MAPPER.writeValueAsString(expectedResponse);

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            mockMvc.perform(MockMvcRequestBuilders.post("/trader/test/constraintViolation")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));
        }
    }

    @Test
    void handlesMissingServletRequestParameterException() throws Exception {
        final OffsetDateTime mockedNow = DateTimeTestData.newDateTime(2020, 9, 23, 10);
        final Map<String, String> expectedResponse = Map.of(
                "time", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(mockedNow),
                "message", "Required request parameter 'param' for method parameter type String is not present"
        );
        final String expectedResponseString = TestUtils.OBJECT_MAPPER.writeValueAsString(expectedResponse);

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            mockMvc.perform(MockMvcRequestBuilders.post("/trader/test/missingParam")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));
        }
    }

    @Test
    void handlesRuntimeException() throws Exception {
        final OffsetDateTime mockedNow = DateTimeTestData.newDateTime(2020, 9, 23, 10);
        final Map<String, String> expectedResponse = Map.of(
                "time", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(mockedNow),
                "message", "runtime exception message"
        );
        final String expectedResponseString = TestUtils.OBJECT_MAPPER.writeValueAsString(expectedResponse);

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            mockMvc.perform(MockMvcRequestBuilders.post("/trader/test/runtime")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isInternalServerError())
                    .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));
        }
    }

    @Test
    void handlesInstrumentNotFoundException() throws Exception {
        final OffsetDateTime mockedNow = DateTimeTestData.newDateTime(2020, 9, 23, 10);
        final Map<String, String> expectedResponse = Map.of(
                "time", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(mockedNow),
                "message", "Instrument not found for id instrumentId"
        );
        final String expectedResponseString = TestUtils.OBJECT_MAPPER.writeValueAsString(expectedResponse);

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            mockMvc.perform(MockMvcRequestBuilders.post("/trader/test/instrumentNotFound")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
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

        @PostMapping("/methodArgumentNotValid")
        public void throwMethodArgumentNotValidException() throws MethodArgumentNotValidException, NoSuchMethodException {

            final Method method = this.getClass().getMethod("throwMethodArgumentNotValidException");
            final MethodParameter parameter = new MethodParameter(method, -1);

            final BindingResult bindingResult = new BeanPropertyBindingResult(null, StringUtils.EMPTY);
            bindingResult.addError(new ObjectError("objectName1", "validation error1"));
            bindingResult.addError(new ObjectError("objectName2", "validation error2"));

            throw new MethodArgumentNotValidException(parameter, bindingResult);
        }

        @PostMapping("/constraintViolation")
        public void throwConstraintViolationException() throws ConstraintViolationException {
            final ConstraintViolation<Object> constraintViolation1 = new TestConstraintViolation("validation error1");
            final ConstraintViolation<Object> constraintViolation2 = new TestConstraintViolation("validation error2");
            final Set<ConstraintViolation<Object>> constraintViolations = new LinkedHashSet<>(2, 1);
            constraintViolations.add(constraintViolation1);
            constraintViolations.add(constraintViolation2);
            throw new ConstraintViolationException(StringUtils.EMPTY, constraintViolations);
        }

        @PostMapping("/missingParam")
        public void throwMissingServletRequestParameterException(@RequestParam final String param) {
        }

        @PostMapping("/runtime")
        public void throwRuntimeException() {
            throw new RuntimeException("runtime exception message");
        }

        @PostMapping("/instrumentNotFound")
        public void throwInstrumentNotFoundException() {
            throw new InstrumentNotFoundException("instrumentId");
        }

    }

    @AllArgsConstructor
    private static class TestConstraintViolation implements ConstraintViolation<Object> {

        private final String message;

        @Override
        public String getMessage() {
            return this.message;
        }

        @Override
        public String getMessageTemplate() {
            return null;
        }

        @Override
        public Object getRootBean() {
            return null;
        }

        @Override
        public Class<Object> getRootBeanClass() {
            return null;
        }

        @Override
        public Object getLeafBean() {
            return null;
        }

        @Override
        public Object[] getExecutableParameters() {
            return new Object[0];
        }

        @Override
        public Object getExecutableReturnValue() {
            return null;
        }

        @Override
        public Path getPropertyPath() {
            return null;
        }

        @Override
        public Object getInvalidValue() {
            return null;
        }

        @Override
        public ConstraintDescriptor<?> getConstraintDescriptor() {
            return null;
        }

        @Override
        public <U> U unwrap(Class<U> type) {
            return null;
        }
    }

}