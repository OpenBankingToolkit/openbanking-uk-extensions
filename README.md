[<img src="https://raw.githubusercontent.com/ForgeRock/forgerock-logo-dev/master/Logo-fr-dev.png" align="right" width="220px"/>](https://developer.forgerock.com/)

| |Current Status|
|---|---|
|Build|[![Build Status](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Factions-badge.atrox.dev%2FOpenBankingToolkit%2Fopenbanking-uk-extensions%2Fbadge%3Fref%3Dmaster&style=flat)](https://actions-badge.atrox.dev/OpenBankingToolkit/openbanking-uk-extensions/goto?ref=master)|
|Code coverage|[![codecov](https://codecov.io/gh/OpenBankingToolKit/openbanking-uk-extensions/branch/master/graph/badge.svg)](https://codecov.io/gh/OpenBankingToolkit/openbanking-aspsp)
|Bintray|[![Bintray](https://img.shields.io/bintray/v/openbanking-toolkit/OpenBankingToolKit/openbanking-uk-extensions.svg?maxAge=2592000)](https://bintray.com/openbanking-toolkit/OpenBankingToolKit/openbanking-aspsp)|
|License|![license](https://img.shields.io/github/license/ACRA/acra.svg)|

ForgeRock OpenBanking Extensions
===========================

The ForgeRock OpenBanking library Extensions contains the extensions of the specific operations and functionalities out of box of Openbanking uk specifications based on PSD2 specs.

**_This repository is part of the Open Banking Tool kit. If you just landed to that repository looking for our tool kit,_
_we recommend having a first read to_ https://github.com/OpenBankingToolkit/openbanking-toolkit**

# What is this library extensions?

This library extensions has been designed to write the ad-hoc functions and operations to extend the existing ones.

## Implementation extension conventions

- Package names: _com.forgerock.openbanking.{component}.{specific}.ext.{customer-id-name-etc}.{functionality}.{f2}..._
    - Example: _com.forgerock.openbanking.aspsp.rs.ext.lbg.file.payment_
