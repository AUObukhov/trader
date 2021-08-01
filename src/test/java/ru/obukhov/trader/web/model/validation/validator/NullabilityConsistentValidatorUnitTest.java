package ru.obukhov.trader.web.model.validation.validator;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.web.model.validation.constraint.NullabilityConsistent;

import javax.validation.ConstraintValidatorContext;
import javax.validation.metadata.ConstraintDescriptor;

class NullabilityConsistentValidatorUnitTest {

    @Test
    void isValid_returnsTrue_whenAllValidatedFieldsAreNull() {
        final NullabilityConsistentValidator validator = new NullabilityConsistentValidator();
        final TestClass testObject = new TestClass(null, null, null, "field4Value");

        final boolean result = validator.isValid(testObject, createValidationContext());

        Assertions.assertTrue(result);
    }

    @Test
    void isValid_returnsTrue_whenAllValidatedFieldsAreNotNull() {
        final NullabilityConsistentValidator validator = new NullabilityConsistentValidator();
        final TestClass testObject = new TestClass("field1Value", "field2Value", "field3Value", null);

        final boolean result = validator.isValid(testObject, createValidationContext());

        Assertions.assertTrue(result);
    }

    @Test
    void isValid_returnsFalse_whenOnlyOneOfValidatedFieldsIsNull() {
        final NullabilityConsistentValidator validator = new NullabilityConsistentValidator();
        final TestClass testObject = new TestClass(null, "field2Value", "field3Value", null);

        final boolean result = validator.isValid(testObject, createValidationContext());

        Assertions.assertFalse(result);
    }

    @Test
    void isValid_returnsFalse_whenOnlyOneOfValidatedFieldsIsNotNull() {
        final NullabilityConsistentValidator validator = new NullabilityConsistentValidator();
        final TestClass testObject = new TestClass("field1Value", null, null, null);

        final boolean result = validator.isValid(testObject, createValidationContext());

        Assertions.assertFalse(result);
    }

    private ConstraintValidatorContext createValidationContext() {
        final NullabilityConsistent annotation = TestClass.class.getAnnotation(NullabilityConsistent.class);

        final ConstraintAnnotationDescriptor<NullabilityConsistent> constraintAnnotationDescriptor =
                new ConstraintAnnotationDescriptor<>(annotation);

        final ConstraintDescriptor<NullabilityConsistent> constraintDescriptor = new ConstraintDescriptorImpl<>(
                ConstraintHelper.forAllBuiltinConstraints(),
                null,
                constraintAnnotationDescriptor,
                ConstraintLocation.ConstraintLocationKind.TYPE);

        return new ConstraintValidatorContextImpl(
                null,
                null,
                constraintDescriptor,
                null,
                ExpressionLanguageFeatureLevel.DEFAULT,
                ExpressionLanguageFeatureLevel.DEFAULT
        );
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