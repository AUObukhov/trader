package ru.obukhov.trader.grafana.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.obukhov.trader.common.model.Interval;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Accessors(chain = true)
public class GetDataRequest {

    @NotNull(message = "interval is mandatory")
    @ApiModelProperty(required = true, position = 1)
    @JsonProperty("range")
    private Interval interval;

    @Valid
    @NotEmpty(message = "targets is mandatory")
    @ApiModelProperty(required = true, position = 2)
    private List<Target> targets;

}