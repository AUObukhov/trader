package ru.obukhov.trader.common.model.validation.validator;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.model.validation.constraint.PredicateConstraint;

import javax.validation.ConstraintValidatorContext;
import javax.validation.metadata.ConstraintDescriptor;
import java.util.function.Predicate;

class PredicateConstraintValidatorUnitTest {

    @Test
    void isValid_returnsTrue_whenPredicateReturnsTrue() {
        PredicateConstraintValidator validator = new PredicateConstraintValidator();
        TestClass testObject = new TestClass("value", "value");

        boolean result = validator.isValid(testObject, createValidationContext());

        Assertions.assertTrue(result);
    }

    @Test
    void isValid_returnsFalse_whenPredicateReturnsFalse() {
        PredicateConstraintValidator validator = new PredicateConstraintValidator();
        TestClass testObject = new TestClass("value1", "value2");

        boolean result = validator.isValid(testObject, createValidationContext());

        Assertions.assertFalse(result);
    }

    private ConstraintValidatorContext createValidationContext() {
        PredicateConstraint annotation = TestClass.class.getAnnotation(PredicateConstraint.class);

        ConstraintAnnotationDescriptor<PredicateConstraint> constraintAnnotationDescriptor =
                new ConstraintAnnotationDescriptor<>(annotation);

        ConstraintDescriptor<PredicateConstraint> constraintDescriptor = new ConstraintDescriptorImpl<>(
                ConstraintHelper.forAllBuiltinConstraints(),
                null,
                constraintAnnotationDescriptor,
                ConstraintLocation.ConstraintLocationKind.TYPE);

        return new ConstraintValidatorContextImpl(null, null, constraintDescriptor, null);
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