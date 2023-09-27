package ru.obukhov.trader.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.trading.backtest.interfaces.BackTester;
import ru.obukhov.trader.trading.model.BackTestResult;
import ru.obukhov.trader.web.model.exchange.BackTestRequest;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/trader/bot")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class BotController {

    private final BackTester backTester;
    private final SchedulingProperties schedulingProperties;

    @PostMapping("/back-test")
    public List<BackTestResult> backTest(@Valid @RequestBody final BackTestRequest request) {
        final Interval interval = DateUtils.getIntervalWithDefaultOffsets(request.getFrom(), request.getTo());
        final boolean saveToFiles = BooleanUtils.isTrue(request.getSaveToFiles());

        return backTester.test(request.getBotConfigs(), request.getBalanceConfig(), interval, saveToFiles);
    }

    @PostMapping("/enable-scheduling")
    public void enableScheduling() {
        schedulingProperties.setEnabled(true);
    }

    @PostMapping("/disable-scheduling")
    public void disableScheduling() {
        schedulingProperties.setEnabled(false);
    }

}