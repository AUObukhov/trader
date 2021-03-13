package ru.obukhov.trader.market.impl;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.MathUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.MoneyAmount;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.transform.MoneyAmountMapper;
import ru.obukhov.trader.market.model.transform.OperationMapper;
import ru.obukhov.trader.market.model.transform.OperationTypeMapper;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.Orderbook;
import ru.tinkoff.invest.openapi.models.orders.LimitOrder;
import ru.tinkoff.invest.openapi.models.orders.MarketOrder;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;
import ru.tinkoff.invest.openapi.models.orders.Status;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
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
    private final Map<String, PortfolioPosition> tickersToPositions;

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);
    private final OperationTypeMapper operationTypeMapper = Mappers.getMapper(OperationTypeMapper.class);
    private final MoneyAmountMapper moneyAmountMapper = Mappers.getMapper(MoneyAmountMapper.class);
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
     * changes currentDateTime to nearest work time after it
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

    private BigDecimal getCurrentPrice(String ticker) {
        return marketService.getLastCandle(ticker, currentDateTime).getClosePrice();
    }

    private void buyPosition(String ticker, BigDecimal currentPrice, int lotsCount, BigDecimal totalPrice) {
        Instrument instrument = realTinkoffService.searchMarketInstrument(ticker);
        PortfolioPosition existingPosition = this.tickersToPositions.get(ticker);
        PortfolioPosition position;
        if (existingPosition == null) {
            position = createNewPosition(ticker, totalPrice, instrument.currency, currentPrice, lotsCount);
        } else {
            position = addValuesToPosition(existingPosition, lotsCount, totalPrice);
        }

        this.tickersToPositions.put(ticker, position);
    }

    private PortfolioPosition createNewPosition(String ticker,
                                                BigDecimal balance,
                                                Currency currency,
                                                BigDecimal averagePositionPrice,
                                                int lotsCount) {
        return new PortfolioPosition(
                ticker,
                balance,
                null,
                currency,
                null,
                lotsCount,
                averagePositionPrice,
                null,
                StringUtils.EMPTY);
    }

    private PortfolioPosition addValuesToPosition(PortfolioPosition existingPosition,
                                                  int lotsCount,
                                                  BigDecimal totalPrice) {
        BigDecimal newBalance = existingPosition.getBalance().add(totalPrice);
        int newLotsCount = existingPosition.getLotsCount() + lotsCount;
        BigDecimal newAveragePrice = MathUtils.divide(newBalance, newLotsCount);
        return clonePositionWithNewValues(existingPosition, newBalance, newLotsCount, newAveragePrice);
    }

    private PortfolioPosition clonePositionWithNewValues(PortfolioPosition position,
                                                         BigDecimal balance,
                                                         int lotsCount,
                                                         BigDecimal averagePositionPrice) {
        return new PortfolioPosition(
                position.getTicker(),
                balance,
                position.getBlocked(),
                position.getCurrency(),
                position.getExpectedYield(),
                lotsCount,
                averagePositionPrice,
                position.getAveragePositionPriceNoNkd(),
                position.getName());
    }

    private void updateBalance(BigDecimal totalPrice, BigDecimal commissionAmount) {
        BigDecimal newBalance = this.balance.add(totalPrice).subtract(commissionAmount);
        Assert.isTrue(newBalance.signum() >= 0, "balance can't be negative");

        this.balance = newBalance;
    }

    private void sellPosition(String ticker, int lotsCount) {
        PortfolioPosition existingPosition = this.tickersToPositions.get(ticker);
        int newLotsCount = existingPosition.getLotsCount() - lotsCount;

        if (newLotsCount == 0) {
            this.tickersToPositions.remove(ticker);
        } else if (newLotsCount > 0) {
            PortfolioPosition newPosition = clonePositionWithNewLotsCount(existingPosition, newLotsCount);
            this.tickersToPositions.put(ticker, newPosition);
        } else {
            final String message = "lotsCount " + lotsCount + "can't be greater than existing position lots count "
                    + existingPosition.getLotsCount();
            throw new IllegalArgumentException(message);
        }
    }

    private PortfolioPosition clonePositionWithNewLotsCount(PortfolioPosition position, int lotsCount) {
        return new PortfolioPosition(
                position.getTicker(),
                position.getBalance(),
                position.getBlocked(),
                position.getCurrency(),
                position.getExpectedYield(),
                lotsCount,
                position.getAveragePositionPrice(),
                position.getAveragePositionPriceNoNkd(),
                position.getName());
    }

    private void addOperation(String ticker,
                              BigDecimal price,
                              MoneyAmount commission,
                              MarketOrder marketOrder) {

        final SimulatedOperation operation = SimulatedOperation.builder()
                .ticker(ticker)
                .price(price)
                .quantity(marketOrder.lots)
                .commission(commission.getValue())
                .dateTime(this.currentDateTime)
                .operationType(operationTypeMapper.map(marketOrder.operation))
                .build();
        operations.add(operation);
    }

    private PlacedOrder createOrder(MarketOrder marketOrder, MoneyAmount commission) {
        return new PlacedOrder(StringUtils.EMPTY,
                marketOrder.operation,
                Status.New,
                StringUtils.EMPTY,
                StringUtils.EMPTY,
                marketOrder.lots,
                marketOrder.lots,
                moneyAmountMapper.map(commission));
    }

    @Override
    public void cancelOrder(String orderId) {
        throw new UnsupportedOperationException();
    }

    // endregion

    // region PortfolioContext proxy

    @Override
    public Collection<PortfolioPosition> getPortfolioPositions() {
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