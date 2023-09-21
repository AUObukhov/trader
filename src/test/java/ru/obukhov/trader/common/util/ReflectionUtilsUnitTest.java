package ru.obukhov.trader.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.util.Arrays;

class ReflectionUtilsUnitTest {

    // region getFieldValueByReadMethod tests

    @Test
    void getFieldValueByReadMethod_returnsFieldValue_whenFieldExistsAndHaveReadMethod() {
        final String fieldWithReadMethodValue = "fieldWithReadMethodValue";
        final String fieldWithoutReadMethodValue = "fieldWithoutReadMethodValue";
        final FieldContainer container = new FieldContainer(fieldWithReadMethodValue, fieldWithoutReadMethodValue);

        final Object readValue = ReflectionUtils.getFieldValueByReadMethod(container, "fieldWithReadMethod");

        Assertions.assertEquals(fieldWithReadMethodValue, readValue);
    }

    @Test
    void getFieldValueByReadMethod_throwsIntrospectionException_whenFieldNotExists() {
        final String fieldWithReadMethodValue = "fieldWithReadMethodValue";
        final String fieldWithoutReadMethodValue = "fieldWithoutReadMethodValue";
        final FieldContainer container = new FieldContainer(fieldWithReadMethodValue, fieldWithoutReadMethodValue);

        final Executable executable = () -> ReflectionUtils.getFieldValueByReadMethod(container, "notExistingField");
        AssertUtils.assertThrowsWithMessage(IntrospectionException.class, executable, "Method not found: isNotExistingField");
    }

    @Test
    void getFieldValueByReadMethod_returnsFieldValue_whenFieldExistsButDoesNotHaveReadMethod() {
        final String fieldWithReadMethodValue = "fieldWithReadMethodValue";
        final String fieldWithoutReadMethodValue = "fieldWithoutReadMethodValue";
        final FieldContainer container = new FieldContainer(fieldWithReadMethodValue, fieldWithoutReadMethodValue);

        final Executable executable = () -> ReflectionUtils.getFieldValueByReadMethod(container, "fieldWithoutReadMethod");
        AssertUtils.assertThrowsWithMessage(IntrospectionException.class, executable, "Method not found: isFieldWithoutReadMethod");
    }

    // endregion

    // region fieldIsNotStatic tests

    @Test
    void fieldIsNotStatic_returnTrueWhenFieldIsNotStatic() {
        final Field fieldWithReadMethod = Arrays.stream(FieldContainer.class.getDeclaredFields())
                .filter(field -> "fieldWithReadMethod".equals(field.getName()))
                .findAny()
                .orElseThrow();
        Assertions.assertTrue(ReflectionUtils.fieldIsNotStatic(fieldWithReadMethod));
    }

    @Test
    void fieldIsNotStatic_returnFalseWhenFieldIsStatic() {
        final Field staticField = Arrays.stream(FieldContainer.class.getDeclaredFields())
                .filter(field -> "staticField".equals(field.getName()))
                .findAny()
                .orElseThrow();
        Assertions.assertFalse(ReflectionUtils.fieldIsNotStatic(staticField));
    }

    // endregion

    @NoArgsConstructor
    @AllArgsConstructor
    private static class FieldContainer {

        @SuppressWarnings("unused")
        public static String staticField;

        @Getter
        @Setter
        public String fieldWithReadMethod;

        public String fieldWithoutReadMethod;

    }

}