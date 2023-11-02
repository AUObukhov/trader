package ru.obukhov.trader.web.model.exchange;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Common request for several endpoint
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FigiesListRequest {

    @NotEmpty(message = "figies are mandatory")
    private List<String> figies;

}