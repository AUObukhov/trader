package ru.obukhov.trader.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.beans.IntrospectionException;

class ReflectionUtilsUnitTest {

    @Test
    void getFieldByReadMethod_returnsFieldValue_whenFieldExistsAndHaveReadMethod() {
        final String fieldWithReadMethodValue = "fieldWithReadMethodValue";
        final String fieldWithoutReadMethodValue = "fieldWithReadMethodValue";
        final FieldContainer container = new FieldContainer(fieldWithReadMethodValue, fieldWithoutReadMethodValue);

        final Object readValue = ReflectionUtils.getFieldValueByReadMethod(container, "fieldWithReadMethod");

        Assertions.assertEquals(fieldWithReadMethodValue, readValue);
    }

    @Test
    void getFieldByReadMethod_throwsIntrospectionException_whenFieldNotExists() {
        final String fieldWithReadMethodValue = "fieldWithReadMethodValue";
        final String fieldWithoutReadMethodValue = "fieldWithReadMethodValue";
        final FieldContainer container = new FieldContainer(fieldWithReadMethodValue, fieldWithoutReadMethodValue);

        AssertUtils.assertThrowsWithMessage(
                () -> ReflectionUtils.getFieldValueByReadMethod(container, "notExistingField"),
                IntrospectionException.class,
                "Method not found: isNotExistingField");
    }

    @Test
    void getFieldByReadMethod_returnsFieldValue_whenFieldExistsButDoesNotHaveReadMethod() {
        final String fieldWithReadMethodValue = "fieldWithReadMethodValue";
        final String fieldWithoutReadMethodValue = "fieldWithReadMethodValue";
        final FieldContainer container = new FieldContainer(fieldWithReadMethodValue, fieldWithoutReadMethodValue);

        AssertUtils.assertThrowsWithMessage(
                () -> ReflectionUtils.getFieldValueByReadMethod(container, "fieldWithoutReadMethod"),
                IntrospectionException.class,
                "Method not found: isFieldWithoutReadMethod");
    }

    @AllArgsConstructor
    private static class FieldContainer {

        @Getter
        @Setter
        public String fieldWithReadMethod;

        public String fieldWithoutReadMethod;

    }

}