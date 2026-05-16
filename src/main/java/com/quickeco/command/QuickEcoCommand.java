package com.quickeco.command;

import com.quickeco.QuickEco;
import com.quickeco.compat.ImportManager;
import com.quickeco.economy.EconomyProvider;
import com.quickeco.gui.EcoEditGui;
import com.quickeco.util.Msg;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuickEcoCommand implements CommandExecutor, TabCompleter {

    private final QuickEco plugin;

    public QuickEcoCommand(QuickEco plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c,
                             @NotNull String l, @NotNull String[] a) {
        if (a.length == 0) { help(s); return true; }

        switch (a[0].toLowerCase()) {
            case "help" -> help(s);
            case "reload" -> reload(s);
            case "edit" -> edit(s, a);
            case "give" -> give(s, a);
            case "take" -> take(s, a);
            case "set" -> set(s, a);
            case "remove" -> remove(s, a);
            case "settings" -> settings(s, a);
            case "import" -> importCmd(s, a);
            default -> Msg.send(s, "unknown-subcommand");
        }
        return true;
    }

    private void help(CommandSender s) {
        Msg.plain(s, "&8--- &2QuickEco &8---");
        Msg.plain(s, "&e/qe reload &7- reload config + balances");
        Msg.plain(s, "&e/qe edit <player> &7- open the balance editor GUI");
        Msg.plain(s, "&e/qe give <player> <amount>");
        Msg.plain(s, "&e/qe take <player> <amount>");
        Msg.plain(s, "&e/qe set <player> <amount>");
        Msg.plain(s, "&e/qe remove <player> &7- delete account");
        Msg.plain(s, "&e/qe settings [key] [value]");
        Msg.plain(s, "&e/qe import <essentials|cmi> &7- migrate balances from another plugin");
        Msg.plain(s, "&e/balance, /pay, /baltop &7- player commands");
    }

    private void reload(CommandSender s) {
        if (!has(s, "quickeco.admin", "qe.reload")) { Msg.send(s, "no-permission"); return; }
        plugin.reloadConfig();
        plugin.getStorageManager().save();
        plugin.getStorageManager().load();
        Msg.send(s, "reload");
    }

    private void edit(CommandSender s, String[] a) {
        if (!(s instanceof Player p)) { Msg.send(s, "player-only"); return; }
        if (!has(p, "quickeco.admin", "qe.edit")) { Msg.send(p, "no-permission"); return; }
        if (a.length < 2) { Msg.plain(s, "&7Usage: &e/qe edit <player>"); return; }
        OfflinePlayer target = resolve(a[1]);
        if (target == null) { Msg.send(s, "player-not-found"); return; }
        EcoEditGui.open(p, plugin, target);
    }

    private void give(CommandSender s, String[] a) {
        if (!has(s, "quickeco.admin", "qe.give")) { Msg.send(s, "no-permission"); return; }
        if (a.length < 3) { Msg.plain(s, "&7Usage: &e/qe give <player> <amount>"); return; }
        OfflinePlayer t = resolve(a[1]);
        if (t == null) { Msg.send(s, "player-not-found"); return; }
        Double amount = parseAmount(s, a[2]);
        if (amount == null) return;
        EconomyProvider eco = plugin.getEconomyProvider();
        if (!eco.hasAccount(t)) eco.createAccount(t);
        eco.depositPlayer(t, amount);
        Msg.send(s, "gave", "amount", eco.format(amount), "player", safeName(t));
        log("Gave " + eco.format(amount) + " to " + safeName(t));
    }

    private void take(CommandSender s, String[] a) {
        if (!has(s, "quickeco.admin", "qe.take")) { Msg.send(s, "no-permission"); return; }
        if (a.length < 3) { Msg.plain(s, "&7Usage: &e/qe take <player> <amount>"); return; }
        OfflinePlayer t = resolve(a[1]);
        if (t == null) { Msg.send(s, "player-not-found"); return; }
        Double amount = parseAmount(s, a[2]);
        if (amount == null) return;
        EconomyProvider eco = plugin.getEconomyProvider();
        if (!eco.hasAccount(t)) { Msg.send(s, "no-account", "player", safeName(t)); return; }
        double take = Math.min(amount, eco.getBalance(t));
        eco.withdrawPlayer(t, take);
        Msg.send(s, "took", "amount", eco.format(take), "player", safeName(t));
        log("Took " + eco.format(take) + " from " + safeName(t));
    }

    private void set(CommandSender s, String[] a) {
        if (!has(s, "quickeco.admin", "qe.set")) { Msg.send(s, "no-permission"); return; }
        if (a.length < 3) { Msg.plain(s, "&7Usage: &e/qe set <player> <amount>"); return; }
        OfflinePlayer t = resolve(a[1]);
        if (t == null) { Msg.send(s, "player-not-found"); return; }
        Double amount = parseAmount(s, a[2]);
        if (amount == null) return;
        EconomyProvider eco = plugin.getEconomyProvider();
        if (!eco.hasAccount(t)) eco.createAccount(t);
        eco.setBalance(t, amount);
        Msg.send(s, "set", "player", safeName(t), "amount", eco.format(amount));
        log("Set " + safeName(t) + " balance to " + eco.format(amount));
    }

    private void remove(CommandSender s, String[] a) {
        if (!has(s, "quickeco.admin", "qe.remove")) { Msg.send(s, "no-permission"); return; }
        if (a.length < 2) { Msg.plain(s, "&7Usage: &e/qe remove <player>"); return; }
        OfflinePlayer t = resolve(a[1]);
        if (t == null) { Msg.send(s, "player-not-found"); return; }
        EconomyProvider eco = plugin.getEconomyProvider();
        if (!eco.hasAccount(t)) { Msg.send(s, "no-account", "player", safeName(t)); return; }
        eco.removeAccount(t);
        Msg.send(s, "removed", "player", safeName(t));
        log("Removed account: " + safeName(t));
    }

    private void settings(CommandSender s, String[] a) {
        if (!has(s, "quickeco.admin", "qe.settings")) { Msg.send(s, "no-permission"); return; }
        if (a.length < 2) {
            Msg.plain(s, "&8--- &2Settings &8---");
            Msg.plain(s, "&7debug: &e" + plugin.getConfig().getBoolean("settings.debug", false));
            Msg.plain(s, "&7log-transactions: &e" + plugin.getConfig().getBoolean("settings.log-transactions", true));
            Msg.plain(s, "&7starting-balance: &e" + plugin.getConfig().getDouble("settings.starting-balance", 100));
            Msg.plain(s, "&7max-pay: &e" + plugin.getConfig().getDouble("settings.max-pay", 10000));
            Msg.plain(s, "&7pay-cooldown: &e" + plugin.getConfig().getInt("settings.pay-cooldown", 3));
            return;
        }
        if (a.length < 3) { Msg.plain(s, "&7Usage: &e/qe settings <key> <value>"); return; }
        String key = a[1].toLowerCase();
        String val = a[2];
        switch (key) {
            case "debug" -> setBool(s, "settings.debug", val);
            case "log-transactions" -> setBool(s, "settings.log-transactions", val);
            case "starting-balance" -> setDouble(s, "settings.starting-balance", val);
            case "max-pay" -> setDouble(s, "settings.max-pay", val);
            case "pay-cooldown" -> setInt(s, "settings.pay-cooldown", val);
            default -> Msg.send(s, "settings-unknown-key", "key", key);
        }
    }

    private void importCmd(CommandSender s, String[] a) {
        if (!has(s, "quickeco.admin", "qe.import")) { Msg.send(s, "no-permission"); return; }
        if (a.length < 2) { Msg.plain(s, "&7Usage: &e/qe import <essentials|cmi>"); return; }
        ImportManager.Source src;
        switch (a[1].toLowerCase()) {
            case "essentials", "essentialsx", "ess" -> src = ImportManager.Source.ESSENTIALS;
            case "cmi" -> src = ImportManager.Source.CMI;
            default -> { Msg.send(s, "import-unknown", "source", a[1]); return; }
        }
        Msg.send(s, "import-starting", "source", src.name());
        ImportManager.Result r = new ImportManager(plugin).run(src);
        if (!r.ok) {
            Msg.send(s, "import-missing", "path", r.missingPath);
            return;
        }
        Msg.send(s, "import-done",
                "source", src.name(),
                "imported", r.imported,
                "skipped", r.skipped);
    }

    private void setBool(CommandSender s, String path, String raw) {
        boolean b = raw.equalsIgnoreCase("true") || raw.equals("1")
                || raw.equalsIgnoreCase("on") || raw.equalsIgnoreCase("yes");
        plugin.getConfig().set(path, b);
        plugin.saveConfig();
        Msg.send(s, "settings-changed",
                "key", path.substring(path.lastIndexOf('.') + 1), "value", b);
    }

    private void setDouble(CommandSender s, String path, String raw) {
        double d;
        try { d = Double.parseDouble(raw); }
        catch (NumberFormatException e) { Msg.send(s, "invalid-amount"); return; }
        plugin.getConfig().set(path, d);
        plugin.saveConfig();
        Msg.send(s, "settings-changed",
                "key", path.substring(path.lastIndexOf('.') + 1), "value", d);
    }

    private void setInt(CommandSender s, String path, String raw) {
        int n;
        try { n = Integer.parseInt(raw); }
        catch (NumberFormatException e) { Msg.send(s, "invalid-amount"); return; }
        plugin.getConfig().set(path, n);
        plugin.saveConfig();
        Msg.send(s, "settings-changed",
                "key", path.substring(path.lastIndexOf('.') + 1), "value", n);
    }

    private Double parseAmount(CommandSender s, String raw) {
        try {
            double d = Double.parseDouble(raw);
            if (d < 0) { Msg.send(s, "invalid-amount"); return null; }
            return d;
        } catch (NumberFormatException e) {
            Msg.send(s, "invalid-amount");
            return null;
        }
    }

    private OfflinePlayer resolve(String name) {
        OfflinePlayer p = plugin.getServer().getOfflinePlayerIfCached(name);
        return (p != null && p.getName() != null) ? p : null;
    }

    private String safeName(OfflinePlayer p) {
        return p.getName() == null ? p.getUniqueId().toString() : p.getName();
    }

    private void log(String msg) {
        if (plugin.getConfig().getBoolean("settings.log-transactions", true)) {
            plugin.getLogger().info(msg);
        }
    }

    private boolean has(CommandSender s, String... nodes) {
        for (String n : nodes) if (s.hasPermission(n)) return true;
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c,
                                      @NotNull String l, @NotNull String[] a) {
        if (a.length == 1) {
            return filter(List.of("help", "reload", "edit", "give", "take", "set",
                    "remove", "settings", "import"), a[0]);
        }
        if (a.length == 2) {
            if (List.of("edit", "give", "take", "set", "remove").contains(a[0].toLowerCase())) {
                return filter(onlinePlayers(), a[1]);
            }
            if (a[0].equalsIgnoreCase("settings")) {
                return filter(List.of("debug", "log-transactions", "starting-balance", "max-pay", "pay-cooldown"), a[1]);
            }
            if (a[0].equalsIgnoreCase("import")) {
                return filter(List.of("essentials", "cmi"), a[1]);
            }
        }
        if (a.length == 3 && a[0].equalsIgnoreCase("settings")) {
            if (a[1].equalsIgnoreCase("debug") || a[1].equalsIgnoreCase("log-transactions")) {
                return filter(List.of("true", "false"), a[2]);
            }
        }
        return List.of();
    }

    private List<String> onlinePlayers() {
        List<String> out = new ArrayList<>();
        for (Player p : plugin.getServer().getOnlinePlayers()) out.add(p.getName());
        return out;
    }

    private List<String> filter(List<String> options, String prefix) {
        String p = prefix.toLowerCase();
        return options.stream().filter(s -> s.toLowerCase().startsWith(p)).collect(Collectors.toList());
    }
}
