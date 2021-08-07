package ru.obukhov.trader.grafana.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import ru.obukhov.trader.common.model.Interval;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GetDataRequest {

    @NotNull(message = "range is mandatory")
    @ApiModelProperty(required = true, position = 1)
    private Interval range;

    @Valid
    @NotEmpty(message = "targets is mandatory")
    @ApiModelProperty(required = true, position = 2)
    private List<Target> targets;

}