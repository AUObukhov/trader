package ru.obukhov.trader.web.model.exchange;

import lombok.Data;
import ru.obukhov.trader.web.model.pojo.SimulationUnit;
import ru.obukhov.trader.web.model.validation.constraint.SimulationUnitsAreDistinct;

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
    @SimulationUnitsAreDistinct
    private List<SimulationUnit> simulationUnits;

    @NotNull(message = "from is mandatory")
    private OffsetDateTime from;

    private OffsetDateTime to;

    private Boolean saveToFiles;

}