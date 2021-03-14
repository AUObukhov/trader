package ru.obukhov.trader.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.beans.IntrospectionException;

class ReflectionUtilsTest {

    @Test
    void getFieldByReadMethod_returnsFieldValue_whenFieldExistsAndHaveReadMethod() {
        String fieldWithReadMethodValue = "fieldWithReadMethodValue";
        String fieldWithoutReadMethodValue = "fieldWithReadMethodValue";
        FieldContainer container = new FieldContainer(fieldWithReadMethodValue, fieldWithoutReadMethodValue);

        Object readValue = ReflectionUtils.getFieldValueByReadMethod(container, "fieldWithReadMethod");

        Assertions.assertEquals(fieldWithReadMethodValue, readValue);
    }

    @Test
    void getFieldByReadMethod_throwsIntrospectionException_whenFieldNotExists() {
        String fieldWithReadMethodValue = "fieldWithReadMethodValue";
        String fieldWithoutReadMethodValue = "fieldWithReadMethodValue";
        FieldContainer container = new FieldContainer(fieldWithReadMethodValue, fieldWithoutReadMethodValue);

        AssertUtils.assertThrowsWithMessage(
                () -> ReflectionUtils.getFieldValueByReadMethod(container, "notExistingField"),
                IntrospectionException.class,
                "Method not found: isNotExistingField");
    }

    @Test
    void getFieldByReadMethod_returnsFieldValue_whenFieldExistsButDoesNotHaveReadMethod() {
        String fieldWithReadMethodValue = "fieldWithReadMethodValue";
        String fieldWithoutReadMethodValue = "fieldWithReadMethodValue";
        FieldContainer container = new FieldContainer(fieldWithReadMethodValue, fieldWithoutReadMethodValue);

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