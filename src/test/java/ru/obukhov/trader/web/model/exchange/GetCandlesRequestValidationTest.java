package ru.obukhov.trader.web.model.exchange;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.time.OffsetDateTime;

class GetCandlesRequestValidationTest {

    @Test
    void validationSucceeds_whenEverythingIsValid() {
        final GetCandlesRequest request = createValidGetCandlesRequest();

        AssertUtils.assertNoViolations(request);
    }

    @Test
    void validationFails_whenFigiIsNull() {
        final GetCandlesRequest request = createValidGetCandlesRequest();
        request.setFigi(null);

        AssertUtils.assertViolation(request, "figi is mandatory");
    }

    @Test
    void validationFails_whenIntervalIsNull() {
        final GetCandlesRequest request = createValidGetCandlesRequest();
        request.setInterval(null);

        AssertUtils.assertViolation(request, "interval is mandatory");
    }

    @Test
    void validationFails_whenCandleIntervalIsNull() {
        final GetCandlesRequest request = createValidGetCandlesRequest();
        request.setCandleInterval(null);

        AssertUtils.assertViolation(request, "candleInterval is mandatory");
    }

    @Test
    void validationFails_whenMovingAverageTypeIsNull() {
        final GetCandlesRequest request = createValidGetCandlesRequest();
        request.setMovingAverageType(null);

        AssertUtils.assertViolation(request, "movingAverageType is mandatory");
    }

    @Test
    void validationFails_whenSmallWindowIsNull() {
        final GetCandlesRequest request = createValidGetCandlesRequest();
        request.setSmallWindow(null);

        AssertUtils.assertViolation(request, "smallWindow is mandatory");
    }

    @Test
    void validationFails_whenBigWindowIsNull() {
        final GetCandlesRequest request = createValidGetCandlesRequest();
        request.setBigWindow(null);

        AssertUtils.assertViolation(request, "bigWindow is mandatory");
    }

    private GetCandlesRequest createValidGetCandlesRequest() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        final GetCandlesRequest request = new GetCandlesRequest();
        request.setFigi(TestShares.APPLE.share().figi());
        request.setInterval(Interval.of(from, to));
        request.setCandleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN);
        request.setMovingAverageType(MovingAverageType.SIMPLE);
        request.setSmallWindow(1);
        request.setBigWindow(2);
        request.setSaveToFile(true);

        return request;
    }

}