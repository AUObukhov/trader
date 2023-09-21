package ru.obukhov.trader.web.model.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;

import java.lang.annotation.Annotation;

abstract class ValidatorUnitTest {

    protected ConstraintValidatorContext createValidationContext(final Class<?> clazz, final Class<? extends Annotation> annotationClass) {
        final Annotation annotation = clazz.getAnnotation(annotationClass);

        final ConstraintAnnotationDescriptor<Annotation> constraintAnnotationDescriptor =
                new ConstraintAnnotationDescriptor<>(annotation);

        final ConstraintDescriptor<Annotation> constraintDescriptor = new ConstraintDescriptorImpl<>(
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

}