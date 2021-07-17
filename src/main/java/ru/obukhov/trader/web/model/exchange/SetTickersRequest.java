package ru.obukhov.trader.web.model.exchange;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Collection;

@Data
@Valid
public class SetTickersRequest {

    @NotEmpty(message = "tickers are mandatory")
    private Collection<String> tickers;

}