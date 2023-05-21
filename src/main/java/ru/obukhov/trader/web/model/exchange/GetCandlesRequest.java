package ru.obukhov.trader.web.model.exchange;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

@Getter
@Setter
public class GetCandlesRequest {
    @NotNull(message = "ticker is mandatory")
    @ApiModelProperty(
            value = "Interval to load candles",
            required = true,
            position = 1,
            example = "FXIT")
    private String ticker;

    @NotNull(message = "interval is mandatory")
    @ApiModelProperty(
            value = "Interval to load candles",
            required = true,
            position = 2,
            example = "2019-01-01T00:00:00+03:00")
    private Interval interval;

    @NotNull(message = "candleInterval is mandatory")
    @ApiModelProperty(
            value = "Candle interval",
            required = true,
            position = 3,
            example = "CANDLE_INTERVAL_1_MIN")
    private CandleInterval candleInterval;

    @NotNull(message = "movingAverageType is mandatory")
    @ApiModelProperty(
            value = "Moving average algorithm type",
            required = true,
            position = 4,
            example = "LWMA")
    private MovingAverageType movingAverageType;

    @NotNull(message = "smallWindow is mandatory")
    @ApiModelProperty(
            value = "Window of short-term moving average",
            required = true,
            position = 5,
            example = "50")
    private Integer smallWindow;

    @NotNull(message = "bigWindow is mandatory")
    @ApiModelProperty(
            value = "Window of long-term moving average",
            required = true,
            position = 6,
            example = "200")
    private Integer bigWindow;

    @ApiModelProperty(
            value = "Flag indicating to save the back test result to a file. Default value is false",
            position = 7,
            example = "true")
    private boolean saveToFile;
}