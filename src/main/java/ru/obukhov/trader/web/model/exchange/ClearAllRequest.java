package ru.obukhov.trader.web.model.exchange;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class ClearAllRequest {

    @Nullable
    @ApiModelProperty(example = "2000124699")
    private String accountId;

}