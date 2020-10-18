package ru.obukhov.investor.bot.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.investor.bot.interfaces.Bot;
import ru.obukhov.investor.bot.interfaces.MarketMock;
import ru.obukhov.investor.bot.interfaces.Simulator;
import ru.obukhov.investor.web.model.SimulateResponse;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Simulates trading by bot
 */
@Slf4j
@RequiredArgsConstructor
public class SimulatorImpl implements Simulator {

    private final Bot bot;
    private final MarketMock marketMock;

    /**
     * @param ticker  simulated ticker
     * @param balance balance before simulation
     * @param from    start simulation time
     * @param to      end simulation time
     * @return balance after simulation
     */
    @Override
    public SimulateResponse simulate(String ticker, BigDecimal balance, OffsetDateTime from, OffsetDateTime to) {

        log.info("Simulation for ticker = '" + ticker + "' started");

        OffsetDateTime innerTo = to == null ? OffsetDateTime.now() : to;

        marketMock.init(from, balance);

        do {

            bot.processTicker(ticker);

            marketMock.nextMinute();
        } while (marketMock.getCurrentDateTime().isBefore(innerTo));

        log.info("Simulation for ticker = '" + ticker + "' ended");

        List<Portfolio.PortfolioPosition> positions = newArrayList(marketMock.getPosition(ticker));
        return SimulateResponse.builder()
                .balance(marketMock.getBalance())
                .fullBalance(getFullBalance(positions))
                .positions(positions)
                .operations(marketMock.getOperations())
                .build();
    }

    private BigDecimal getFullBalance(List<Portfolio.PortfolioPosition> positions) {
        BigDecimal fullBalance = marketMock.getBalance();
        for (Portfolio.PortfolioPosition position : positions) {
            fullBalance = fullBalance.add(position.balance);
        }
        return fullBalance;
    }

}