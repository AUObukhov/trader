package ru.obukhov.trader.web.model.exchange;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.share.TestShares;

import java.util.List;

class FigiesListRequestValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final List<String> shareFigies = List.of(TestShares.APPLE.getFigi());
        final FigiesListRequest request = new FigiesListRequest();
        request.setFigies(shareFigies);

        AssertUtils.assertNoViolations(request);
    }

    @Test
    void validationFails_whenFigiIsNull() {
        final FigiesListRequest request = new FigiesListRequest();
        request.setFigies(null);

        AssertUtils.assertViolation(request, "figies are mandatory");
    }

}