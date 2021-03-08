package ru.obukhov.investor.web.model.pojo;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Valid
public class SimulationUnit {

    @NotBlank(message = "ticker in simulation unit is mandatory")
    private String ticker;

    @NotNull(message = "balance in simulation unit is mandatory")
    private BigDecimal balance;

}
