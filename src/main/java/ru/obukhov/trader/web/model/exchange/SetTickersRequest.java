package ru.obukhov.trader.web.model.exchange;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Collection;

@Data
@Valid
public class SetTickersRequest {

    @NotEmpty(message = "tickers are mandatory")
    @ApiModelProperty(example = "[\"FXIT\", \"BABA\"]", required = true)
    private Collection<String> tickers;

}