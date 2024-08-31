package ru.obukhov.trader.web.model.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.SneakyThrows;
import ru.obukhov.trader.common.util.ReflectionUtils;
import ru.obukhov.trader.web.model.validation.constraint.NotAllNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

public class NotAllNullValidator implements ConstraintValidator<NotAllNull, Object> {

    @Override
    public boolean isValid(final Object object, final ConstraintValidatorContext constraintValidatorContext) {
        final Field[] fields = object.getClass().getDeclaredFields();
        return Arrays.stream(fields)
                .filter(ReflectionUtils::fieldIsNotStatic)
                .filter(field -> !field.getName().contains("$")) // ignore generated fields
                .map(field -> getFieldValue(object, field))
                .anyMatch(Objects::nonNull);
    }

    @SneakyThrows
    @SuppressWarnings("java:S3011")
    private Object getFieldValue(final Object object, final Field field) {
        field.setAccessible(true);
        return field.get(object);
    }

}