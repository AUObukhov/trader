package ru.obukhov.trader.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to do health check. Used by Grafana.
 */
@Api(tags = "Health check")
@RestController
@RequestMapping("/trader/")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class HealthCheckController {

    @GetMapping
    @ApiOperation("Health check. Does nothing")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public void get() {
        // health check - do nothing
    }

}