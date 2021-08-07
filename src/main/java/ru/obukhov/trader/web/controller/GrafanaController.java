package ru.obukhov.trader.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.grafana.interfaces.GrafanaService;
import ru.obukhov.trader.grafana.model.GetDataRequest;
import ru.obukhov.trader.grafana.model.QueryResult;

import javax.validation.Valid;
import java.util.List;

/**
 * Controller for providing JSON API Grafana Datasource
 *
 * @see <a href="https://grafana.com/grafana/plugins/simpod-json-datasource">JSON API Grafana Datasource</a>
 */
@Api(tags = "Grafana datasource")
@RestController
@RequestMapping("/trader/grafana")
@RequiredArgsConstructor
public class GrafanaController {

    private final GrafanaService grafanaService;

    @GetMapping
    @ApiOperation("Health check. Does nothing")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public void get() {
        // health check - do nothing
    }

    @PostMapping("/query")
    @ApiOperation("Get metric's data")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public List<QueryResult> getData(@Valid @RequestBody GetDataRequest request) {
        return grafanaService.getData(request);
    }

}