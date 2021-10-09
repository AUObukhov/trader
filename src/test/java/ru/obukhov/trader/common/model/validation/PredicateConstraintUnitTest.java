package ru.obukhov.trader.common.model.validation;

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

import javax.validation.ConstraintValidatorContext;
import javax.validation.metadata.ConstraintDescriptor;
import java.util.function.Predicate;

class PredicateConstraintUnitTest {

    private final PredicateConstraint.PredicateConstraintValidator validator = new PredicateConstraint.PredicateConstraintValidator();

    @Test
    void isValid_returnsTrue_whenPredicateReturnsTrue() {
        final TestClass testObject = new TestClass("value", "value");

        final boolean result = validator.isValid(testObject, createValidationContext());

        Assertions.assertTrue(result);
    }

    @Test
    void isValid_returnsFalse_whenPredicateReturnsFalse() {
        final TestClass testObject = new TestClass("value1", "value2");

        final boolean result = validator.isValid(testObject, createValidationContext());

        Assertions.assertFalse(result);
    }

    private ConstraintValidatorContext createValidationContext() {
        final PredicateConstraint annotation = TestClass.class.getAnnotation(PredicateConstraint.class);

        final ConstraintAnnotationDescriptor<PredicateConstraint> constraintAnnotationDescriptor = new ConstraintAnnotationDescriptor<>(annotation);

        final ConstraintDescriptor<PredicateConstraint> constraintDescriptor = new ConstraintDescriptorImpl<>(
                ConstraintHelper.forAllBuiltinConstraints(),
                null,
                constraintAnnotationDescriptor,
                ConstraintLocation.ConstraintLocationKind.TYPE
        );

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
    @PredicateConstraint(predicate = EqualsPredicate.class)
    public static class TestClass {
        private String field1;
        private String field2;
    }

    private static class EqualsPredicate implements Predicate<TestClass> {
        @Override
        public boolean test(TestClass config) {
            return config.getField1().equals(config.getField2());
        }
    }

}