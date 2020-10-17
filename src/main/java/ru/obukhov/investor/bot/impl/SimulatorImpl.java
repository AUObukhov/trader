package ru.obukhov.investor.bot.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.investor.bot.interfaces.Bot;
import ru.obukhov.investor.bot.interfaces.MarketMock;
import ru.obukhov.investor.bot.interfaces.Simulator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

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
    public BigDecimal simulate(String ticker, BigDecimal balance, OffsetDateTime from, OffsetDateTime to) {

        OffsetDateTime innerTo = to == null ? OffsetDateTime.now() : to;

        marketMock.setBalance(balance);
        marketMock.setCurrentDateTime(from);

        do {

            bot.processTicker(ticker);

            marketMock.nextMinute();
        } while (marketMock.getCurrentDateTime().isBefore(innerTo));

        return marketMock.getBalance();
    }

}