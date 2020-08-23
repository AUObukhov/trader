package ru.obukhov.investor.web.model;

import lombok.Builder;
import lombok.Data;
import ru.obukhov.investor.web.model.validation.GetSaldosRequestIntervalConstraint;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
@Builder
@GetSaldosRequestIntervalConstraint
public class GetSaldosRequest {

    @NotBlank(message = "token is mandatory")
    private String token;

    @NotBlank(message = "ticker is mandatory")
    private String ticker;

    private OffsetDateTime from;

    private OffsetDateTime to;

    @NotNull(message = "candleInterval is mandatory")
    private CandleInterval candleInterval;

}