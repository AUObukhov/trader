package ru.obukhov.trader.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trader/")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class HealthCheckController {

    @GetMapping
    public void get() {
        // health check - do nothing
    }

}