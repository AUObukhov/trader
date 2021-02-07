package ru.obukhov.investor.service.impl;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.util.Assert;
import ru.obukhov.investor.bot.interfaces.TinkoffService;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.model.Interval;
import ru.obukhov.investor.model.transform.OperationMapper;
import ru.obukhov.investor.model.transform.OperationTypeMapper;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.util.DateUtils;
import ru.obukhov.investor.util.MathUtils;
import ru.obukhov.investor.web.model.SimulatedOperation;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.MoneyAmount;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.Orderbook;
import ru.tinkoff.invest.openapi.models.orders.LimitOrder;
import ru.tinkoff.invest.openapi.models.orders.MarketOrder;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;
import ru.tinkoff.invest.openapi.models.orders.Status;
import ru.tinkoff.invest.openapi.models.portfolio.InstrumentType;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Prices are loaded from real market, but any operations do not affect the real portfolio - all data is stored in
 * memory.
 */
public class FakeTinkoffService implements TinkoffService {

    private final TradingProperties tradingProperties;
    private final MarketService marketService;
    private final RealTinkoffService realTinkoffService;
    private final List<SimulatedOperation> operations;
    private final Map<String, Portfolio.PortfolioPosition> tickersToPositions;

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);
    private final OperationTypeMapper operationTypeMapper = Mappers.getMapper(OperationTypeMapper.class);

    @Getter
    private OffsetDateTime currentDateTime;
    @Getter
    private BigDecimal balance;

    public FakeTinkoffService(TradingProperties tradingProperties,
                              MarketService marketService,
                              RealTinkoffService realTinkoffService) {

        this.tradingProperties = tradingProperties;
        this.marketService = marketService;
        this.realTinkoffService = realTinkoffService;

        this.operations = new ArrayList<>();
        this.tickersToPositions = new HashMap<>();

    }

    /**
     * sets current dateTime, but moves it to nearest work time
     */
    public void init(OffsetDateTime currentDateTime, BigDecimal balance) {
        this.operations.clear();
        this.tickersToPositions.clear();

        this.currentDateTime = DateUtils.getNearestWorkTime(currentDateTime,
                tradingProperties.getWorkStartTime(),
                tradingProperties.getWorkDuration());
        this.balance = balance;
    }

    /**
     * Adds one minute to current dataTime
     */
    public void nextMinute() {

        this.currentDateTime = DateUtils.getNextWorkMinute(
                this.currentDateTime,
                tradingProperties.getWorkStartTime(),
                tradingProperties.getWorkDuration());

    }

    // region MarketContext proxy

    @Override
    public List<Instrument> getMarketStocks() {
        return realTinkoffService.getMarketStocks();
    }

    @Override
    public List<Instrument> getMarketBonds() {
        return realTinkoffService.getMarketBonds();
    }

    @Override
    public List<Instrument> getMarketEtfs() {
        return realTinkoffService.getMarketEtfs();
    }

    @Override
    public List<Instrument> getMarketCurrencies() {
        return realTinkoffService.getMarketCurrencies();
    }

    @Override
    public Orderbook getMarketOrderbook(String ticker, int depth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Candle> getMarketCandles(String ticker, Interval interval, CandleInterval candleInterval) {
        return realTinkoffService.getMarketCandles(ticker, interval, candleInterval);
    }

    @Override
    public Instrument searchMarketInstrument(String ticker) {
        return realTinkoffService.searchMarketInstrument(ticker);
    }

    // endregion

    // region OperationsContext proxy

    @Override
    public List<ru.tinkoff.invest.openapi.models.operations.Operation> getOperations(Interval interval, String ticker) {
        Stream<SimulatedOperation> operationsStream = operations.stream()
                .filter(operation -> interval.contains(operation.getDateTime()));
        if (ticker != null) {
            operationsStream = operationsStream.filter(operation -> ticker.equals(operation.getTicker()));
        }

        return operationsStream.sorted(Comparator.comparing(SimulatedOperation::getDateTime))
                .map(operationMapper::map)
                .collect(Collectors.toList());
    }

    // endregion

    // region OrdersContext proxy

    @Override
    public List<Order> getOrders() {
        return Collections.emptyList();
    }

    @Override
    public PlacedOrder placeLimitOrder(String ticker, LimitOrder limitOrder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PlacedOrder placeMarketOrder(String ticker, MarketOrder marketOrder) {
        BigDecimal currentPrice = getCurrentPrice(ticker);
        BigDecimal totalPrice = MathUtils.multiply(currentPrice, marketOrder.lots);
        BigDecimal commissionAmount = MathUtils.getFraction(totalPrice, tradingProperties.getCommission());
        MoneyAmount commission = new MoneyAmount(Currency.RUB, commissionAmount);

        if (marketOrder.operation == ru.tinkoff.invest.openapi.models.orders.Operation.Buy) {
            buyPosition(ticker, currentPrice, marketOrder.lots, totalPrice);
            updateBalance(totalPrice.negate(), commissionAmount);
        } else {
            sellPosition(ticker, marketOrder.lots);
            updateBalance(totalPrice, commissionAmount);
        }

        addOperation(ticker, currentPrice, commission, marketOrder);
        return createOrder(marketOrder, commission);
    }

    private void addOperation(String ticker,
                              BigDecimal price,
                              MoneyAmount commission,
                              MarketOrder marketOrder) {

        final SimulatedOperation operation = SimulatedOperation.builder()
                .ticker(ticker)
                .price(price)
                .quantity(marketOrder.lots)
                .commission(commission.value)
                .dateTime(this.currentDateTime)
                .operationType(operationTypeMapper.map(marketOrder.operation))
                .build();
        operations.add(operation);
    }

    public BigDecimal getCurrentPrice(String ticker) {
        return marketService.getLastCandle(ticker, currentDateTime).getClosePrice();
    }

    private void buyPosition(String ticker, BigDecimal currentPrice, int lotsCount, BigDecimal totalPrice) {
        Portfolio.PortfolioPosition existingPosition = this.tickersToPositions.get(ticker);
        Portfolio.PortfolioPosition position;
        if (existingPosition == null) {
            MoneyAmount averagePositionPrice = new MoneyAmount(Currency.RUB, currentPrice); // todo rid of hardcoded currency
            position = createNewPosition(ticker, totalPrice, averagePositionPrice, lotsCount);
        } else {
            int newLotsCount = existingPosition.lots + lotsCount;
            BigDecimal newBalance = MathUtils.
                    multiply(existingPosition.averagePositionPrice.value, existingPosition.lots)
                    .add(totalPrice);
            BigDecimal newAveragePrice = MathUtils.divide(newBalance, newLotsCount);
            position = clonePositionWithNewBalance(existingPosition, totalPrice, newAveragePrice, newLotsCount);
        }

        this.tickersToPositions.put(ticker, position);
    }

    private void sellPosition(String ticker, int lotsCount) {
        Portfolio.PortfolioPosition existingPosition = this.tickersToPositions.get(ticker);
        int newLotsCount = existingPosition.lots - lotsCount;

        if (newLotsCount == 0) {
            this.tickersToPositions.remove(ticker);
        } else if (newLotsCount > 0) {
            Portfolio.PortfolioPosition newPosition = clonePositionWithNewLotsCount(existingPosition, newLotsCount);
            this.tickersToPositions.put(ticker, newPosition);
        } else {
            final String message = "lotsCount " + lotsCount + "can't be greater than existing position lots count "
                    + existingPosition.lots;
            throw new IllegalArgumentException(message);
        }
    }

    private Portfolio.PortfolioPosition createNewPosition(String ticker,
                                                          BigDecimal balance,
                                                          MoneyAmount averagePositionPrice,
                                                          int lotsCount) {
        return new Portfolio.PortfolioPosition(
                StringUtils.EMPTY,
                ticker,
                null,
                InstrumentType.Stock,
                balance,
                null,
                null,
                lotsCount,
                averagePositionPrice,
                null,
                StringUtils.EMPTY);
    }

    private Portfolio.PortfolioPosition clonePositionWithNewBalance(Portfolio.PortfolioPosition position,
                                                                    BigDecimal balance,
                                                                    BigDecimal averagePrice,
                                                                    int lotsCount) {
        MoneyAmount averagePositionPrice = new MoneyAmount(position.averagePositionPrice.currency, averagePrice);
        return new Portfolio.PortfolioPosition(
                position.figi,
                position.ticker,
                position.isin,
                position.instrumentType,
                balance,
                position.blocked,
                position.expectedYield,
                lotsCount,
                averagePositionPrice,
                position.averagePositionPriceNoNkd,
                position.name);
    }

    private Portfolio.PortfolioPosition clonePositionWithNewLotsCount(Portfolio.PortfolioPosition position,
                                                                      int lotsCount) {
        return new Portfolio.PortfolioPosition(
                position.figi,
                position.ticker,
                position.isin,
                position.instrumentType,
                position.balance,
                position.blocked,
                position.expectedYield,
                lotsCount,
                position.averagePositionPrice,
                position.averagePositionPriceNoNkd,
                position.name);
    }

    private void updateBalance(BigDecimal totalPrice, BigDecimal commissionAmount) {
        BigDecimal newBalance = this.balance.add(totalPrice).subtract(commissionAmount);
        Assert.isTrue(newBalance.signum() >= 0, "balance can't be negative");

        this.balance = newBalance;
    }

    private PlacedOrder createOrder(MarketOrder marketOrder, MoneyAmount commission) {
        return new PlacedOrder(StringUtils.EMPTY,
                marketOrder.operation,
                Status.New,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                marketOrder.lots,
                marketOrder.lots,
                commission);
    }

    @Override
    public void cancelOrder(String orderId) {
        throw new UnsupportedOperationException();
    }

    // endregion

    // region PortfolioContext proxy

    @Override
    public List<Portfolio.PortfolioPosition> getPortfolioPositions() {
        return new ArrayList<>(this.tickersToPositions.values());
    }

    @Override
    public List<PortfolioCurrencies.PortfolioCurrency> getPortfolioCurrencies() {
        PortfolioCurrencies.PortfolioCurrency currency
                = new PortfolioCurrencies.PortfolioCurrency(Currency.RUB, this.balance, null);
        return Collections.singletonList(currency);
    }

    // endregion

}