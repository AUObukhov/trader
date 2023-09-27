package ru.obukhov.trader.web.model.exchange;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetWeightsRequest {
    @NotEmpty(message = "shareFigies are mandatory")
    private List<String> shareFigies;

}