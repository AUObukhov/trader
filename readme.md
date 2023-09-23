# Project for auto trading through [Tinkoff Invest API](https://github.com/TinkoffCreditSystems/invest-openapi-java-sdk)

Contains API for getting market info. Some methods are same as at Tinkoff Api, some are extended. Also contains bots and
API to launch and test them.

## Configuration

- JDK version is 17

- For build use maven version 4.0.0

- For launch need to pass Tinkoff token as command-line argument as "trading.token", for example:

```
--trading.token=<token>
```

## Documentation

Swagger UI available at host:port/swagger-ui/index.html#/