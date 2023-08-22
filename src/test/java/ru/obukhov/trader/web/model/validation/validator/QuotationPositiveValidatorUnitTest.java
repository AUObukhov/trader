package ru.obukhov.trader.web.model.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

class QuotationPositiveValidatorUnitTest {

    @ParameterizedTest
    @CsvSource(value = {
            "-1.0, false",
            "-0.1, false",
            "0.0, false",
            "1.0, true",
            "0.1, true",
    })
    void test(final double value, final boolean expectedResult) {
        final Quotation quotation = QuotationUtils.newQuotation(value);
        final QuotationPositiveValidator validator = new QuotationPositiveValidator();

        final boolean actualResult = validator.isValid(quotation, createValidationContext());

        Assertions.assertEquals(expectedResult, actualResult);
    }

    private ConstraintValidatorContext createValidationContext() {
        return new ConstraintValidatorContextImpl(
                null,
                null,
                null,
                null,
                ExpressionLanguageFeatureLevel.DEFAULT,
                ExpressionLanguageFeatureLevel.DEFAULT
        );
    }

}