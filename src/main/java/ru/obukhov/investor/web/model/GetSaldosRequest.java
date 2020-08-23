package ru.obukhov.investor.web.model;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class GetSaldosRequest {
    private String token;
    private String ticker;
    private OffsetDateTime from;
    private OffsetDateTime to;
}