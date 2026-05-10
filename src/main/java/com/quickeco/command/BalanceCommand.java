package com.quickeco.command;

import com.quickeco.QuickEco;
import com.quickeco.economy.EconomyProvider;
import com.quickeco.util.Msg;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BalanceCommand implements CommandExecutor, TabCompleter {

    private final QuickEco plugin;

    public BalanceCommand(QuickEco plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
        EconomyProvider eco = plugin.getEconomyProvider();

        if (a.length == 0) {
            if (!(s instanceof Player p)) {
                s.sendMessage(Msg.color("&cUsage: /balance <player>"));
                return true;
            }
            if (!eco.hasAccount(p)) eco.createAccount(p);
            Msg.send(s, "balance", "balance", eco.format(eco.getBalance(p)));
            return true;
        }

        if (!s.hasPermission("quickeco.admin") && !s.getName().equalsIgnoreCase(a[0])) {
            Msg.send(s, "no-permission");
            return true;
        }

        OfflinePlayer target = plugin.getServer().getOfflinePlayerIfCached(a[0]);
        if (target == null) { Msg.send(s, "player-not-found"); return true; }
        if (!eco.hasAccount(target)) { Msg.send(s, "player-not-found"); return true; }

        Msg.send(s, "balance-other", "player", target.getName(), "balance", eco.format(eco.getBalance(target)));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String l, @NotNull String[] a) {
        if (a.length == 1) {
            List<String> out = new ArrayList<>();
            for (Player p : plugin.getServer().getOnlinePlayers()) out.add(p.getName());
            return out;
        }
        return Collections.emptyList();
    }
}
