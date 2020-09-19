package ru.obukhov.investor.web.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import java.time.OffsetDateTime;

@Data
@Builder
public class GetSaldosRequest implements IntervalContainer {

    @NotBlank(message = "ticker is mandatory")
    private String ticker;

    @Past(message = "'from' expected to be in past")
    private OffsetDateTime from;

    @Past(message = "'to' expected to be in past")
    private OffsetDateTime to;

}