package ru.obukhov.trader.web.model.exchange;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetWeightsRequest {
    @NotEmpty(message = "shareFigies are mandatory")
    @ApiModelProperty(
            value = "Share figies",
            required = true,
            position = 1,
            example = "[BBG000B9XRY4, BBG004730N88]")
    private List<String> shareFigies;

}