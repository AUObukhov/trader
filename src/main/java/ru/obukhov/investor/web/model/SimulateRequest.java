package ru.obukhov.investor.web.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class SimulateRequest {

    @NotBlank(message = "ticker is mandatory")
    private String ticker;

    @NotNull(message = "balance is mandatory")
    private BigDecimal balance;

    @NotNull(message = "from is mandatory")
    private OffsetDateTime from;

    private OffsetDateTime to;

    private Boolean saveToFile;

}