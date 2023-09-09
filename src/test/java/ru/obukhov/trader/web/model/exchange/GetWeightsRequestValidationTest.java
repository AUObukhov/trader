package ru.obukhov.trader.web.model.exchange;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.share.TestShares;

import java.util.List;

class GetWeightsRequestValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final List<String> shareFigies = List.of(TestShares.APPLE.share().figi());
        final GetWeightsRequest request = new GetWeightsRequest();
        request.setShareFigies(shareFigies);

        AssertUtils.assertNoViolations(request);
    }

    @Test
    void validationFails_whenFigiIsNull() {
        final GetWeightsRequest request = new GetWeightsRequest();
        request.setShareFigies(null);

        AssertUtils.assertViolation(request, "shareFigies are mandatory");
    }

}