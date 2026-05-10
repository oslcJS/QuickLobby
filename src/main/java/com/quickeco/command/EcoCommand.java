package com.quickeco.command;

import com.quickeco.QuickEco;
import com.quickeco.economy.EconomyProvider;
import com.quickeco.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EcoCommand implements CommandExecutor, TabCompleter {

    private final QuickEco plugin;

    public EcoCommand(QuickEco plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
        if (a.length == 0) { help(s); return true; }

        switch (a[0].toLowerCase()) {

            case "reload" -> {
                if (!s.hasPermission("quickeco.admin")) { Msg.send(s, "no-permission"); return true; }
                plugin.reloadConfig();
                plugin.getStorageManager().save();
                plugin.getStorageManager().load();
                Msg.send(s, "reload");
            }

            case "give" -> {
                if (!s.hasPermission("quickeco.admin")) { Msg.send(s, "no-permission"); return true; }
                if (a.length < 3) { s.sendMessage(Msg.color("&cUsage: /qe give <player> <amount>")); return true; }
                OfflinePlayer target = plugin.getServer().getOfflinePlayerIfCached(a[1]);
                if (target == null) { Msg.send(s, "player-not-found"); return true; }
                double amount;
                try { amount = Double.parseDouble(a[2]); } catch (Exception e) {
                    Msg.send(s, "invalid-amount"); return true;
                }
                EconomyProvider eco = plugin.getEconomyProvider();
                if (!eco.hasAccount(target)) eco.createAccount(target);
                eco.depositPlayer(target, amount);
                Msg.send(s, "gave", "amount", eco.format(amount), "player", target.getName());
                log("Gave " + eco.format(amount) + " to " + target.getName());
            }

            case "take" -> {
                if (!s.hasPermission("quickeco.admin")) { Msg.send(s, "no-permission"); return true; }
                if (a.length < 3) { s.sendMessage(Msg.color("&cUsage: /qe take <player> <amount>")); return true; }
                OfflinePlayer target = plugin.getServer().getOfflinePlayerIfCached(a[1]);
                if (target == null) { Msg.send(s, "player-not-found"); return true; }
                double amount;
                try { amount = Double.parseDouble(a[2]); } catch (Exception e) {
                    Msg.send(s, "invalid-amount"); return true;
                }
                EconomyProvider eco = plugin.getEconomyProvider();
                if (!eco.hasAccount(target)) { Msg.send(s, "insufficient-funds"); return true; }
                eco.withdrawPlayer(target, amount);
                Msg.send(s, "took", "amount", eco.format(amount), "player", target.getName());
                log("Took " + eco.format(amount) + " from " + target.getName());
            }

            case "set" -> {
                if (!s.hasPermission("quickeco.admin")) { Msg.send(s, "no-permission"); return true; }
                if (a.length < 3) { s.sendMessage(Msg.color("&cUsage: /qe set <player> <amount>")); return true; }
                OfflinePlayer target = plugin.getServer().getOfflinePlayerIfCached(a[1]);
                if (target == null) { Msg.send(s, "player-not-found"); return true; }
                double amount;
                try { amount = Double.parseDouble(a[2]); } catch (Exception e) {
                    Msg.send(s, "invalid-amount"); return true;
                }
                EconomyProvider eco = plugin.getEconomyProvider();
                if (!eco.hasAccount(target)) eco.createAccount(target);
                eco.setBalance(target, amount);
                Msg.send(s, "set", "amount", eco.format(amount), "player", target.getName());
                log("Set " + target.getName() + " balance to " + eco.format(amount));
            }

            case "create" -> {
                if (!s.hasPermission("quickeco.admin")) { Msg.send(s, "no-permission"); return true; }
                if (a.length < 2) { s.sendMessage(Msg.color("&cUsage: /qe create <player>")); return true; }
                OfflinePlayer target = plugin.getServer().getOfflinePlayerIfCached(a[1]);
                if (target == null) { Msg.send(s, "player-not-found"); return true; }
                EconomyProvider eco = plugin.getEconomyProvider();
                if (eco.hasAccount(target)) {
                    s.sendMessage(Msg.color("&c" + target.getName() + " already has an account."));
                    return true;
                }
                eco.createAccount(target);
                s.sendMessage(Msg.color("&aCreated account for &e" + target.getName()
                        + " &awith " + eco.format(eco.getStartingBalance())));
            }

            default -> help(s);
        }
        return true;
    }

    private void help(CommandSender s) {
        s.sendMessage(Msg.color("&2&lQuickEco &7v" + plugin.getDescription().getVersion()));
        s.sendMessage(Msg.color("&e/balance [player] &7- check balance"));
        s.sendMessage(Msg.color("&e/pay <player> <amount> &7- send money"));
        s.sendMessage(Msg.color("&e/baltop &7- balance leaderboard"));
        s.sendMessage(Msg.color("&e/qe give <player> <amount> &7- give money"));
        s.sendMessage(Msg.color("&e/qe take <player> <amount> &7- take money"));
        s.sendMessage(Msg.color("&e/qe set <player> <amount> &7- set balance"));
        s.sendMessage(Msg.color("&e/qe create <player> &7- create account"));
        s.sendMessage(Msg.color("&e/qe reload &7- reload config"));
    }

    private void log(String msg) {
        if (plugin.getConfig().getBoolean("settings.log-transactions", true))
            plugin.getLogger().info(msg);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
        if (a.length == 1) return List.of("reload", "give", "take", "set", "create");
        if (a.length == 2 && ("give".equalsIgnoreCase(a[0]) || "take".equalsIgnoreCase(a[0])
                || "set".equalsIgnoreCase(a[0]) || "create".equalsIgnoreCase(a[0]))) {
            List<String> out = new ArrayList<>();
            for (Player p : plugin.getServer().getOnlinePlayers()) out.add(p.getName());
            return out;
        }
        return Collections.emptyList();
    }
}
