package ru.obukhov.investor.web.model.exchange;

import lombok.Data;
import ru.obukhov.investor.web.model.pojo.SimulationUnit;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Valid
public class SimulateRequest {

    @Valid
    @NotEmpty(message = "simulationUnits are mandatory")
    private List<SimulationUnit> simulationUnits;

    @NotNull(message = "from is mandatory")
    private OffsetDateTime from;

    private OffsetDateTime to;

    private Boolean saveToFiles;

}