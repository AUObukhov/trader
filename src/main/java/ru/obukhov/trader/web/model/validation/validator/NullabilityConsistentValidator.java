package ru.obukhov.trader.web.model.validation.validator;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import ru.obukhov.trader.common.util.ReflectionUtils;
import ru.obukhov.trader.web.model.validation.constraint.NullabilityConsistent;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Map;

/**
 * Validates that fields of given object, listed in {@link NullabilityConsistent} are all null or all not null
 */
public class NullabilityConsistentValidator implements ConstraintValidator<NullabilityConsistent, Object> {

    @Override
    public boolean isValid(final Object object, final ConstraintValidatorContext constraintValidatorContext) {
        final ConstraintValidatorContextImpl contextImpl = (ConstraintValidatorContextImpl) constraintValidatorContext;
        final Map<String, Object> attributes = contextImpl.getConstraintDescriptor().getAttributes();
        final String[] fieldNames = (String[]) attributes.get("fields");
        final Object firstFieldValue = ReflectionUtils.getFieldValueByReadMethod(object, fieldNames[0]);
        if (firstFieldValue == null) {
            for (int i = 1; i < fieldNames.length; i++) {
                if (ReflectionUtils.getFieldValueByReadMethod(object, fieldNames[i]) != null) {
                    return false;
                }
            }
        } else {
            for (int i = 1; i < fieldNames.length; i++) {
                if (ReflectionUtils.getFieldValueByReadMethod(object, fieldNames[i]) == null) {
                    return false;
                }
            }
        }
        return true;
    }

}