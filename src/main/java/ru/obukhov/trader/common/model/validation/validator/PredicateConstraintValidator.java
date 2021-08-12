package ru.obukhov.trader.common.model.validation.validator;

import lombok.SneakyThrows;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import ru.obukhov.trader.common.model.validation.constraint.PredicateConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Validates that predicate given in {@link PredicateConstraint} returns true for given object
 */
public class PredicateConstraintValidator implements ConstraintValidator<PredicateConstraint, Object> {

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