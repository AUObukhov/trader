package ru.obukhov.trader.web.model.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.web.model.validation.constraint.NotAllNull;

class NotAllNullValidatorUnitTest extends ValidatorUnitTest {

    @Test
    void isValid_returnsTrue_whenOneFieldIsNotNull() {
        final NotAllNullValidator validator = new NotAllNullValidator();
        final TestClass testObject = new TestClass(null, "field2Value", null);
        final ConstraintValidatorContext validationContext = createValidationContext(TestClass.class, NotAllNull.class);

        final boolean result = validator.isValid(testObject, validationContext);

        Assertions.assertTrue(result);
    }

    @Test
    void isValid_returnsFalse_whenAllFieldsAreNull() {
        final NotAllNullValidator validator = new NotAllNullValidator();
        final TestClass testObject = new TestClass(null, null, null);
        final ConstraintValidatorContext validationContext = createValidationContext(TestClass.class, NotAllNull.class);

        final boolean result = validator.isValid(testObject, validationContext);

        Assertions.assertFalse(result);
    }

    @Data
    @NotAllNull
    @AllArgsConstructor
    public static class TestClass {
        private String field1;
        private String field2;
        private String field3;
    }

}