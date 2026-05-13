<p align="center">
  <img src="https://raw.githubusercontent.com/oslcJS/.github/main/assets/VAULT.gif" width="160">
</p>

<h1 align="center">QuickEco</h1>

<p align="center">
Free, open-source economy plugin with multi-currency, bank accounts, transaction history, Vault, and PlaceholderAPI support.
</p>

---

<p align="center">
  <span style="font-family: 'IBM Plex Mono', monospace; font-style: italic; border: 1px solid #1c1c1c; padding: 6px 10px; color: #555; background: #000;">
    status <span style="color:#222;">/</span>
    <span style="color:#fff; font-weight:600;">stable</span>
  </span>
</p>

<div align="center">
  <table>
    <tr>
      <td><img src="https://raw.githubusercontent.com/oslcJS/.github/main/assets/logo_03.png" width="72"></td>
      <td><strong>QuickPlugins</strong><br>Small, fast Minecraft plugins built for modern Paper, Spigot, and Purpur servers.</td>
    </tr>
  </table>
</div>

---

## overview

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/14.gif)

QuickEco provides a lightweight server economy with player balances, payments, balance leaderboards, admin economy controls, transaction logging, optional bank accounts, and compatibility hooks for Vault, PlaceholderAPI, and QuickCrates.

---

## features

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/2.gif)

- player balances and payments
- admin give, take, set, and reload tools
- balance top leaderboard
- optional bank account system
- transaction logging
- Vault economy provider
- PlaceholderAPI support
- QuickCrates integration

---

## compatibility

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/3.gif)

- Paper
- Spigot
- Purpur
- 1.20.4 - 1.21.8
- Paper 26.1.1 - 26.1.2

---

## installs

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/4.gif)

Drop the jar into `/plugins` and restart the server. Add Vault and PlaceholderAPI if you want economy-provider and placeholder integration.

---

## commands

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/5.gif)

```text
/quickeco
/qe help
/balance [player]
/pay <player> <amount>
/baltop
```

---

## license

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/6.gif)

MIT

---

## plugins

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/7.gif)

Supports PlaceholderAPI, Vault, and QuickCrates through optional runtime hooks.

---

## philosophy

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/8.gif)

QuickEco keeps economy management direct, readable, and server-owner friendly without forcing a large platform or database dependency.

---

## backend

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/9.gif)

Built on Bukkit/Paper APIs with YAML storage, Vault provider registration, PlaceholderAPI expansion support, and a reflection-safe QuickCrates connection.

---

## roadmap

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/10.gif)

- expand bank account commands
- add more reward trigger options
- improve transaction browsing tools

---

## permissions

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/11.gif)

```text
quickeco.*
quickeco.admin
quickeco.use
quickeco.pay
quickeco.baltop
```

---

## configuration

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/12.gif)

```yaml
settings:
  starting-balance: 100.0
  currency-symbol: "$"
  max-pay: 10000.0
  pay-cooldown: 3
storage:
  type: yaml
bank:
  enabled: false
```

---

## api

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/13.gif)

```java
QuickEcoAPI api = QuickEco.getApi();
```

---

## stats

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/15.gif)

```text
performance   / lightweight
memory        / yaml-backed
design        / economy core
```
