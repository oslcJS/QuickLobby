<p align="center">
  <img src="https://raw.githubusercontent.com/oslcJS/.github/main/assets/VAULT.gif" width="160">
</p>

<h1 align="center">QuickEco</h1>

<p align="center">
Free, open-source economy plugin with a GUI balance editor, Vault and PlaceholderAPI support.
</p>

---

<p align="center">
  <span style="font-family: 'IBM Plex Mono', monospace; font-style: italic; border: 1px solid #1c1c1c; padding: 6px 10px; color: #555; background: #000;">
    status <span style="color:#222;">/</span>
    <span style="color:#fff; font-weight:600;">stable</span>
    <span style="color:#222;"> / </span>
    <span style="color:#fff; font-weight:600;">v2.0</span>
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

QuickEco is a lightweight economy: per-player balances, pay, baltop, and an admin GUI to edit balances without typing amounts. Vault provider is registered automatically. Async auto-save runs on a configurable interval. Settings toggle live via `/qe settings` -- no reload required for behaviour flags.

---

## features

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/2.gif)

- player balances with YAML storage and async auto-save
- GUI balance editor (`/qe edit <player>`) with deposit / withdraw preset buttons
- consistent admin verbs: `give`, `take`, `set`, `remove`, `edit`, `reload`
- runtime settings panel: `/qe settings [key] [value]`
- pay command with fee, cooldown, min/max enforcement
- baltop leaderboard
- Vault economy provider registration (auto)
- PlaceholderAPI expansion with rank, top-N, and per-player placeholders
- migrate from other plugins: `/qe import essentials` and `/qe import cmi`
- group-aware starting balance via `quickeco.group.<name>` (works with LuckPerms, PermissionsEx, GroupManager, etc.)
- transaction logging gate
- QuickLink discovery for inter-plugin awareness

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

Drop the jar into `/plugins` and restart. Optional: Vault (registers the economy provider), PlaceholderAPI (adds `%quickeco_*%` placeholders).

---

## commands

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/5.gif)

### admin (/qe)

```text
/qe help
/qe reload
/qe edit <player>                opens the GUI balance editor
/qe give <player> <amount>
/qe take <player> <amount>
/qe set <player> <amount>
/qe remove <player>              deletes the account
/qe settings [key] [value]
/qe import <essentials|cmi>      migrate balances from another plugin
```

### player

```text
/balance [player]
/pay <player> <amount>
/baltop [count]
```

---

## balance editor

`/qe edit <player>` opens a 27-slot GUI:

```
            +--------+
        ... | head   | ...               player + uuid
            +--------+
+----+ +----+ +----+ +----+ +----+ +----+
|+100| |+1k | |+10k|  BAL  |-100| |-1k | |-10k|
+----+ +----+ +----+ +----+ +----+ +----+ +----+
                  [close]
```

- Green wool buttons deposit, red wool buttons withdraw.
- Balance refreshes immediately on each click.
- Withdrawing more than the player has is clamped to their current balance instead of erroring.
- Preset amounts configurable in `config.yml`.

---

## settings

```text
/qe settings                          view current values
/qe settings debug true               enable debug feedback
/qe settings log-transactions off     disable transaction logging
/qe settings starting-balance 250     change starting balance
/qe settings max-pay 50000            change pay cap
/qe settings pay-cooldown 5
```

Saved to `config.yml` immediately. `debug` gates verbose feedback (`gave` / `took` chat lines inside the editor GUI, etc).

---

## permissions

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/11.gif)

```text
quickeco.admin                  parent for all admin nodes
  qe.reload
  qe.edit
  qe.give
  qe.take
  qe.set
  qe.remove
  qe.settings
  qe.balance.others
quickeco.use                    parent for player nodes (default on)
  qe.balance
  qe.pay
  qe.baltop
```

---

## configuration

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/12.gif)

```yaml
settings:
  debug: false
  starting-balance: 100.0
  currency-symbol: "$"
  symbol-before: true
  fractional-digits: 2
  max-pay: 10000.0
  min-pay: 0.01
  pay-fee: 0.0
  pay-fee-type: flat
  pay-cooldown: 3
  log-transactions: true
  auto-save-interval: 300

edit-gui:
  deposit-presets: [100, 1000, 10000]
  withdraw-presets: [100, 1000, 10000]
```

Balances persist to `balances.yml`. The async save task runs every `auto-save-interval` seconds. `/qe reload` flushes + reloads on demand.

---

## placeholders

```text
%quickeco_balance%                  raw double
%quickeco_balance_formatted%        "$1234.56" (uses currency-symbol)
%quickeco_balance_<uuid>%           any player's raw balance by UUID
%quickeco_balance_<name>%           any player's raw balance by name
%quickeco_balance_formatted_<x>%    formatted variant of the above
%quickeco_rank%                     viewing player's rank in baltop
%quickeco_top_<n>_name%             nth-richest player's name
%quickeco_top_<n>_balance%          nth-richest raw balance
%quickeco_top_<n>_formatted%        nth-richest formatted balance
%quickeco_has_<amount>%             "true" / "false" if player has at least <amount>
%quickeco_currency%                 currency plural
%quickeco_currency_singular%        currency singular
%quickeco_currency_symbol%          currency symbol ($)
```

---

## api

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/13.gif)

```java
QuickEcoAPI api = Bukkit.getServicesManager().load(QuickEcoAPI.class);
EconomyProvider eco = api.getEconomyProvider();

eco.depositPlayer(player, 100);
eco.withdrawPlayer(player, 50);
eco.setBalance(player, 0);
eco.getBalance(player);
eco.format(1234.56);             // "$1234.56"
eco.getTopBalances(10);          // List<Map.Entry<UUID, Double>>
```

Vault works as expected -- QuickEco registers as `Economy` provider on enable when Vault is present.

---

## license

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/6.gif)

MIT

---

## plugins

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/7.gif)

Soft-depends on PlaceholderAPI, Vault, and QuickCrates. No required plugin dependencies.

### supported integrations

| Plugin            | How it integrates                                                |
|-------------------|------------------------------------------------------------------|
| **Vault**         | Registered as `Economy` provider on enable. Any Vault consumer (Shop, EssentialsX-Chat prefix, etc.) reads QuickEco balances transparently. |
| **PlaceholderAPI**| `%quickeco_*%` expansion (balance, formatted, rank, top-N, has). |
| **LuckPerms** / any permissions plugin | Per-group starting balance via `quickeco.group.<name>` nodes in `starting-balance-by-group:`. |
| **EssentialsX**   | Migrate balances with `/qe import essentials`. Reads `plugins/Essentials/userdata/*.yml`. |
| **CMI**           | Migrate balances with `/qe import cmi`. Tries `userdata/`, `data/userdata/`, and `players/` directories and accepts `Balance`, `money`, `Money`, or `balance` keys. |
| **Treasury**      | Treasury auto-picks up the Vault registration through its compatibility layer. No additional setup required. |
| **QuickCrates**   | Crate rewards can deposit via QuickEco (handled on the QuickCrates side via QuickLink). |
| **QuickLobby**    | Discoverable via QuickLink for cross-plugin awareness. |

### import workflow

```text
/qe import essentials
```

Before running, the plugin makes a timestamped backup of `balances.yml`
(`balances.yml.bak.<millis>`). Then it walks the source plugin's userdata
directory, parses each player file, and writes the balance into the QuickEco
provider. After import, `/qe baltop` shows the migrated values immediately.

If the import gets the wrong values, restore the backup:

```bash
cp plugins/QuickEco/balances.yml.bak.<millis> plugins/QuickEco/balances.yml
# then /qe reload
```

---

## philosophy

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/8.gif)

Economy without the bloat. Six admin verbs, three player commands. A GUI for the one task that wanted six args (`/qe edit`). All messages config-driven; no hardcoded chat strings. Debug feedback exists behind a runtime toggle, not a recompile.

---

## backend

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/9.gif)

Built on Bukkit / Paper APIs. ConcurrentHashMap balances, YAML persistence, async auto-save. Single-class router for the admin tree. GUI editors implement a tiny `GuiHolder` interface so future editors plug in without new listener code.

---

## roadmap

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/10.gif)

- per-player transaction history viewer
- baltop GUI with pagination
- per-player daily pay/receive caps
- optional bank module
- multi-currency

---

## stats

![](https://raw.githubusercontent.com/oslcJS/.github/main/assets/15.gif)

```text
performance   / async auto-save
memory        / ConcurrentHashMap
design        / GUI-first admin
storage       / YAML
integrations  / Vault + PlaceholderAPI
```
