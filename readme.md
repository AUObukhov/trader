# Project for auto trading through [Tinkoff Invest API](https://github.com/TinkoffCreditSystems/invest-openapi-java-sdk)

Contains API for getting market info. Some methods are same as at Tinkoff Api, some are extended. Also contains bots and
API to launch and test them.

## Configuration

- JDK version is 11

- For build use maven version 4.0.0

- Due to [Mapstruct and Intellij IDEA incompatibility problem](https://github.com/mapstruct/mapstruct/issues/2215) need
  to set VM option for building:

```
-Djps.track.ap.dependencies=false
```

at

`File` -> `Settings` -> `Build, Execution, Deployment` -> `Compiler` -> `User-local build process VM options`

- For launch need to pass Tinkoff token as command-line argument as "trading.token", for example:

```
--trading.token=<token>
```

- For enabling sandbox mode need to set command-line argument as "trading.sandbox" to true, for example:

```
--trading.sandbox=true
```
