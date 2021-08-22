package ru.obukhov.trader.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

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

        final Executable executable = () -> ReflectionUtils.getFieldValueByReadMethod(container, "notExistingField");
        Assertions.assertThrows(IntrospectionException.class, executable, "Method not found: isNotExistingField");
    }

    @Test
    void getFieldByReadMethod_returnsFieldValue_whenFieldExistsButDoesNotHaveReadMethod() {
        final String fieldWithReadMethodValue = "fieldWithReadMethodValue";
        final String fieldWithoutReadMethodValue = "fieldWithReadMethodValue";
        final FieldContainer container = new FieldContainer(fieldWithReadMethodValue, fieldWithoutReadMethodValue);

        final Executable executable = () -> ReflectionUtils.getFieldValueByReadMethod(container, "fieldWithoutReadMethod");
        Assertions.assertThrows(IntrospectionException.class, executable, "Method not found: isFieldWithoutReadMethod");
    }

    @AllArgsConstructor
    private static class FieldContainer {

        @Getter
        @Setter
        public String fieldWithReadMethod;

        public String fieldWithoutReadMethod;

    }

}