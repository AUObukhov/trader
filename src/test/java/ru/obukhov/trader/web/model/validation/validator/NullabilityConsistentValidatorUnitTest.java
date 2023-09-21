package ru.obukhov.trader.web.model.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.web.model.validation.constraint.NullabilityConsistent;

class NullabilityConsistentValidatorUnitTest extends ValidatorUnitTest {

    @Test
    void isValid_returnsTrue_whenAllValidatedFieldsAreNull() {
        final NullabilityConsistentValidator validator = new NullabilityConsistentValidator();
        final TestClass testObject = new TestClass(null, null, null, "field4Value");
        final ConstraintValidatorContext validationContext = createValidationContext(TestClass.class, NullabilityConsistent.class);

        final boolean result = validator.isValid(testObject, validationContext);

        Assertions.assertTrue(result);
    }

    @Test
    void isValid_returnsTrue_whenAllValidatedFieldsAreNotNull() {
        final NullabilityConsistentValidator validator = new NullabilityConsistentValidator();
        final TestClass testObject = new TestClass("field1Value", "field2Value", "field3Value", null);
        final ConstraintValidatorContext validationContext = createValidationContext(TestClass.class, NullabilityConsistent.class);

        final boolean result = validator.isValid(testObject, validationContext);

        Assertions.assertTrue(result);
    }

    @Test
    void isValid_returnsFalse_whenOnlyOneOfValidatedFieldsIsNull() {
        final NullabilityConsistentValidator validator = new NullabilityConsistentValidator();
        final TestClass testObject = new TestClass(null, "field2Value", "field3Value", null);
        final ConstraintValidatorContext validationContext = createValidationContext(TestClass.class, NullabilityConsistent.class);

        final boolean result = validator.isValid(testObject, validationContext);

        Assertions.assertFalse(result);
    }

    @Test
    void isValid_returnsFalse_whenOnlyOneOfValidatedFieldsIsNotNull() {
        final NullabilityConsistentValidator validator = new NullabilityConsistentValidator();
        final TestClass testObject = new TestClass("field1Value", null, null, null);
        final ConstraintValidatorContext validationContext = createValidationContext(TestClass.class, NullabilityConsistent.class);

        final boolean result = validator.isValid(testObject, validationContext);

        Assertions.assertFalse(result);
    }

    @Data
    @AllArgsConstructor
    @NullabilityConsistent(fields = {"field1", "field2", "field3"})
    public static class TestClass {
        private String field1;
        private String field2;
        private String field3;
        private String field4;
    }

}