package ru.obukhov.investor.web.model;

import lombok.Data;
import ru.obukhov.investor.model.TickerType;

import javax.validation.constraints.NotBlank;

@Data
public class GetInstrumentsRequest {

    @NotBlank(message = "token is mandatory")
    private String token;

    private TickerType tickerType;

}
