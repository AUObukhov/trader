package ru.obukhov.investor.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/investor/v1")
@RequiredArgsConstructor
public class ApiController {

    @GetMapping("/get-info")
    public void getInfo() {

    }

}