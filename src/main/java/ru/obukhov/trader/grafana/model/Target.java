package ru.obukhov.trader.grafana.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class Target {

    @NotNull(message = "target.metric is mandatory")
    @JsonProperty("target")
    @ApiModelProperty(value = "name of metric to load data", required = true, position = 1)
    private Metric metric;

    @NotNull(message = "target.type is mandatory")
    @ApiModelProperty(required = true, position = 2, example = "timeseries")
    private TargetType type;

    @ApiModelProperty(value = "additional data with custom structure", position = 3)
    private Object data;

}