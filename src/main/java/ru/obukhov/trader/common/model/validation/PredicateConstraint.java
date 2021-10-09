package ru.obukhov.trader.common.model.validation;

import lombok.SneakyThrows;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Validates that predicate given in {@link PredicateConstraint} returns true for given object
 */
@Documented
@Constraint(validatedBy = PredicateConstraint.PredicateConstraintValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface PredicateConstraint {

    String message() default "predicate condition is not met";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends Predicate<?>> predicate();

    class PredicateConstraintValidator implements ConstraintValidator<PredicateConstraint, Object> {

        @Override
        @SneakyThrows
        public boolean isValid(final Object value, final ConstraintValidatorContext context) {
            final ConstraintValidatorContextImpl contextImpl = (ConstraintValidatorContextImpl) context;
            final Map<String, Object> attributes = contextImpl.getConstraintDescriptor().getAttributes();
            final Class<? extends Predicate<Object>> predicateClass = (Class<? extends Predicate<Object>>) attributes.get("predicate");
            final Constructor<? extends Predicate<Object>> constructor = predicateClass.getDeclaredConstructor();
            constructor.trySetAccessible();

            return constructor.newInstance().test(value);
        }

    }

}