package ru.obukhov.trader.web.model.validation.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.obukhov.trader.web.model.validation.validator.QuotationPositiveValidator;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to validate if {@link Quotation} is positive
 * Type, annotated by @QuotientMin is validated by {@link QuotationPositiveValidator}
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(QuotationPositive.List.class)
@Documented
@Constraint(validatedBy = QuotationPositiveValidator.class)
@SuppressWarnings("unused")
public @interface QuotationPositive {

    String message() default "value must be positive";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({
            ElementType.METHOD,
            ElementType.FIELD,
            ElementType.ANNOTATION_TYPE,
            ElementType.CONSTRUCTOR,
            ElementType.PARAMETER,
            ElementType.TYPE_USE
    })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        QuotationPositive[] value();
    }

}