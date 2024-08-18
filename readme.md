# Project for auto trading through [TBank Invest API](https://github.com/Tinkoff/invest-openapi-java-sdk)

Contains API for getting market info. Some methods are same as at TBank Api, some are extended. Also contains bots and
API to launch and test them.

## Configuration

- JDK version is 21

- For build use maven version 4.0.0

- For launch need to pass TBank token as command-line argument as "trading.token", for example:

```
--trading.token=<token>
```