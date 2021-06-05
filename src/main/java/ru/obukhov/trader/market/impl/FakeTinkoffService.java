package ru.obukhov.trader.market.impl;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.factory.Mappers;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.FakeContext;
import ru.obukhov.trader.market.model.MoneyAmount;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.transform.MoneyAmountMapper;
import ru.obukhov.trader.market.model.transform.OperationMapper;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;
import ru.tinkoff.invest.openapi.model.rest.LimitOrderRequest;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.MarketOrderRequest;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.Order;
import ru.tinkoff.invest.openapi.model.rest.OrderStatus;
import ru.tinkoff.invest.openapi.model.rest.Orderbook;
import ru.tinkoff.invest.openapi.model.rest.PlacedLimitOrder;
import ru.tinkoff.invest.openapi.model.rest.PlacedMarketOrder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
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

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);
    private final MoneyAmountMapper moneyAmountMapper = Mappers.getMapper(MoneyAmountMapper.class);

    private FakeContext fakeContext;

    public FakeTinkoffService(
            final TradingProperties tradingProperties,
            final MarketService marketService,
            final RealTinkoffService realTinkoffService
    ) {
        this.tradingProperties = tradingProperties;
        this.marketService = marketService;
        this.realTinkoffService = realTinkoffService;
    }

    /**
     * sets current dateTime, but moves it to nearest work time
     */
    public void init(final OffsetDateTime currentDateTime) {
        init(currentDateTime, null, null);
    }

    /**
     * Initializes current dateTime and one currency.
     * current dateTime is initialized by nearest work time to given {@code currentDateTime}
     *
     * @param currentDateTime start dateTime for search dateTime to set as current
     * @param currency        currency which balance is initialized not by zero.
     * @param balance         initial balance of {@code currency}. When null {@code currency} is not initialized.
     *                        {@code currency} and {@code balance} must be both null or both not null.
     */
    public void init(
            final OffsetDateTime currentDateTime,
            @Nullable final Currency currency,
            @Nullable final BigDecimal balance
    ) {
        final OffsetDateTime shiftedCurrentDateTime = DateUtils.getNearestWorkTime(currentDateTime,
                tradingProperties.getWorkStartTime(),
                tradingProperties.getWorkDuration());

        this.fakeContext = new FakeContext(shiftedCurrentDateTime);
        if (balance != null) {
            this.fakeContext.addInvestment(currency, balance);
        }
    }

    /**
     * changes currentDateTime to nearest work time after it
     *
     * @return new value of currentDateTime
     */
    public OffsetDateTime nextMinute() {
        final OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(
                fakeContext.getCurrentDateTime(),
                tradingProperties.getWorkStartTime(),
                tradingProperties.getWorkDuration());
        fakeContext.setCurrentDateTime(nextWorkMinute);

        return nextWorkMinute;
    }

    // region MarketContext proxy

    @Override
    public List<MarketInstrument> getMarketStocks() {
        return realTinkoffService.getMarketStocks();
    }

    @Override
    public List<MarketInstrument> getMarketBonds() {
        return realTinkoffService.getMarketBonds();
    }

    @Override
    public List<MarketInstrument> getMarketEtfs() {
        return realTinkoffService.getMarketEtfs();
    }

    @Override
    public List<MarketInstrument> getMarketCurrencies() {
        return realTinkoffService.getMarketCurrencies();
    }

    @Override
    public Orderbook getMarketOrderbook(final String ticker, final int depth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Candle> getMarketCandles(
            final String ticker,
            final Interval interval,
            final CandleResolution candleResolution
    ) {
        return realTinkoffService.getMarketCandles(ticker, interval, candleResolution);
    }

    @Override
    public MarketInstrument searchMarketInstrument(final String ticker) {
        return realTinkoffService.searchMarketInstrument(ticker);
    }

    // endregion

    // region OperationsContext proxy

    /**
     * Searches operations which satisfy given conditions
     *
     * @param interval interval of dateTime of searchable operations, including extreme values
     * @param ticker   ticker of searchable operations, if operations with any ticker are satisfying
     * @return found operations in natural order of their dateTime
     */
    @Override
    public List<Operation> getOperations(final Interval interval, @Nullable final String ticker) {
        Stream<SimulatedOperation> operationsStream = fakeContext.getOperations().stream()
                .filter(operation -> interval.contains(operation.getDateTime()));
        if (ticker != null) {
            operationsStream = operationsStream.filter(operation -> ticker.equals(operation.getTicker()));
        }

        return operationsStream
                .sorted(Comparator.comparing(SimulatedOperation::getDateTime))
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
    public PlacedLimitOrder placeLimitOrder(final String ticker, final LimitOrderRequest orderRequest) {
        throw new UnsupportedOperationException("Only market orders supported in simulation");
    }

    /**
     * Performs market order with fake portfolio
     *
     * @param ticker       ticker of executed order
     * @param orderRequest model of executed order
     * @return result of order execution
     */
    @Override
    public PlacedMarketOrder placeMarketOrder(final String ticker, final MarketOrderRequest orderRequest) {
        final BigDecimal currentPrice = getCurrentPrice(ticker);
        final BigDecimal totalPrice = DecimalUtils.multiply(currentPrice, orderRequest.getLots());
        final BigDecimal commissionAmount = DecimalUtils.getFraction(totalPrice, tradingProperties.getCommission());
        final MoneyAmount commission = new MoneyAmount(Currency.RUB, commissionAmount);

        if (orderRequest.getOperation() == OperationType.BUY) {
            buyPosition(ticker, currentPrice, orderRequest.getLots(), totalPrice, commissionAmount);
        } else {
            sellPosition(ticker, orderRequest.getLots(), totalPrice, commissionAmount);
        }

        addOperation(ticker, currentPrice, commission, orderRequest);
        return createOrder(orderRequest, commission);
    }

    private BigDecimal getCurrentPrice(final String ticker) {
        return marketService.getLastCandle(ticker, fakeContext.getCurrentDateTime()).getOpenPrice();
    }

    private void buyPosition(
            final String ticker,
            final BigDecimal currentPrice,
            final int lotsCount,
            final BigDecimal totalPrice,
            final BigDecimal commissionAmount
    ) {
        final MarketInstrument instrument = searchMarketInstrument(ticker);

        updateBalance(instrument.getCurrency(), totalPrice.negate().subtract(commissionAmount));

        final PortfolioPosition existingPosition = fakeContext.getPosition(ticker);
        PortfolioPosition position;
        if (existingPosition == null) {
            position = createNewPosition(ticker, totalPrice, instrument.getCurrency(), currentPrice, lotsCount);
        } else {
            position = addValuesToPosition(existingPosition, lotsCount, totalPrice);
        }

        fakeContext.addPosition(ticker, position);
    }

    private PortfolioPosition createNewPosition(
            final String ticker,
            final BigDecimal balance,
            final Currency currency,
            final BigDecimal averagePositionPrice,
            final int lotsCount
    ) {
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

    private PortfolioPosition addValuesToPosition(
            final PortfolioPosition existingPosition,
            final int lotsCount,
            final BigDecimal totalPrice
    ) {
        final BigDecimal newBalance = existingPosition.getBalance().add(totalPrice);
        final int newLotsCount = existingPosition.getLotsCount() + lotsCount;
        final BigDecimal newAveragePrice = DecimalUtils.divide(newBalance, newLotsCount);
        return clonePositionWithNewValues(existingPosition, newBalance, newLotsCount, newAveragePrice);
    }

    private PortfolioPosition clonePositionWithNewValues(
            final PortfolioPosition position,
            final BigDecimal balance,
            final int lotsCount,
            final BigDecimal averagePositionPrice
    ) {
        return new PortfolioPosition(
                position.getTicker(),
                balance,
                position.getBlocked(),
                position.getCurrency(),
                position.getExpectedYield(),
                lotsCount,
                averagePositionPrice,
                position.getAveragePositionPriceNoNkd(),
                position.getName()
        );
    }

    private void updateBalance(final Currency currency, final BigDecimal increment) {
        final BigDecimal newBalance = fakeContext.getBalance(currency).add(increment);
        Assert.isTrue(newBalance.signum() >= 0, "balance can't be negative");

        fakeContext.setCurrentBalance(currency, newBalance);
    }

    private void sellPosition(
            final String ticker,
            final int lotsCount,
            final BigDecimal totalPrice,
            final BigDecimal commissionAmount
    ) {
        final PortfolioPosition existingPosition = fakeContext.getPosition(ticker);
        final int newLotsCount = existingPosition.getLotsCount() - lotsCount;
        if (newLotsCount < 0) {
            final String message = "lotsCount " + lotsCount + " can't be greater than existing position lots count "
                    + existingPosition.getLotsCount();
            throw new IllegalArgumentException(message);
        }

        final MarketInstrument instrument = searchMarketInstrument(ticker);

        updateBalance(instrument.getCurrency(), totalPrice.subtract(commissionAmount));
        if (newLotsCount == 0) {
            fakeContext.removePosition(ticker);
        } else {
            PortfolioPosition newPosition = clonePositionWithNewLotsCount(existingPosition, newLotsCount);
            fakeContext.addPosition(ticker, newPosition);
        }
    }

    private PortfolioPosition clonePositionWithNewLotsCount(final PortfolioPosition position, final int lotsCount) {
        return new PortfolioPosition(
                position.getTicker(),
                position.getBalance(),
                position.getBlocked(),
                position.getCurrency(),
                position.getExpectedYield(),
                lotsCount,
                position.getAveragePositionPrice(),
                position.getAveragePositionPriceNoNkd(),
                position.getName()
        );
    }

    private void addOperation(
            final String ticker,
            final BigDecimal price,
            final MoneyAmount commission,
            final MarketOrderRequest orderRequest
    ) {
        final SimulatedOperation operation = SimulatedOperation.builder()
                .ticker(ticker)
                .price(price)
                .quantity(orderRequest.getLots())
                .commission(commission.getValue())
                .dateTime(fakeContext.getCurrentDateTime())
                .operationType(orderRequest.getOperation())
                .build();
        fakeContext.addOperation(operation);
    }

    private PlacedMarketOrder createOrder(final MarketOrderRequest orderRequest, final MoneyAmount commission) {
        return new PlacedMarketOrder()
                .operation(orderRequest.getOperation())
                .status(OrderStatus.NEW)
                .executedLots(orderRequest.getLots())
                .requestedLots(orderRequest.getLots())
                .commission(moneyAmountMapper.map(commission));
    }

    @Override
    public void cancelOrder(final String orderId) {
        throw new UnsupportedOperationException();
    }

    // endregion

    // region PortfolioContext proxy

    @Override
    public Collection<PortfolioPosition> getPortfolioPositions() {
        return fakeContext.getPositions();
    }

    @Override
    public List<CurrencyPosition> getPortfolioCurrencies() {
        return fakeContext.getBalances().entrySet().stream()
                .map(entry -> {
                    final CurrencyPosition currencyPosition = new CurrencyPosition();
                    currencyPosition.setCurrency(entry.getKey());
                    currencyPosition.setBalance(entry.getValue());
                    return currencyPosition;
                })
                .collect(Collectors.toList());
    }

    // endregion

    // region methods for simulation

    @Override
    public OffsetDateTime getCurrentDateTime() {
        return fakeContext.getCurrentDateTime();
    }

    public BigDecimal getCurrentBalance(final Currency currency) {
        return fakeContext.getBalance(currency);
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments(final Currency currency) {
        return fakeContext.getInvestments(currency);
    }

    public void incrementBalance(final Currency currency, final BigDecimal increment) {
        fakeContext.addInvestment(currency, increment);
    }

    // endregion
}