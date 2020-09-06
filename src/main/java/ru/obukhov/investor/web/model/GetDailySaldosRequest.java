package ru.obukhov.investor.web.model;

import lombok.Builder;
import lombok.Data;
import ru.obukhov.investor.web.model.validation.constraint.DayCandleIntervalConstraint;
import ru.obukhov.investor.web.model.validation.constraint.GetSaldosRequestIntervalConstraint;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.time.OffsetDateTime;

@Data
@Builder
@GetSaldosRequestIntervalConstraint
public class GetDailySaldosRequest implements IntervalContainer {

    @NotBlank(message = "ticker is mandatory")
    private String ticker;

    @Past(message = "'from' expected to be in past")
    private OffsetDateTime from;

    @Past(message = "'to' expected to be in past")
    private OffsetDateTime to;

    @NotNull(message = "candleInterval is mandatory")
    @DayCandleIntervalConstraint
    private CandleInterval candleInterval;

}