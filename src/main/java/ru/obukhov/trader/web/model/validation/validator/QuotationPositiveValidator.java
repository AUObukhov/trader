package ru.obukhov.trader.web.model.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.web.model.validation.constraint.QuotationPositive;
import ru.tinkoff.piapi.contract.v1.Quotation;

/**
 * Validator to check if {@link Quotation} value is positive or null
 */
public class QuotationPositiveValidator implements ConstraintValidator<QuotationPositive, Quotation> {

    @Override
    public boolean isValid(final Quotation quotation, final ConstraintValidatorContext constraintValidatorContext) {
        return quotation == null || QuotationUtils.compare(quotation, 0) > 0;
    }

}