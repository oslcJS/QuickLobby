package com.quickeco.command;

import com.quickeco.QuickEco;
import com.quickeco.economy.EconomyProvider;
import com.quickeco.util.Msg;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BaltopCommand implements CommandExecutor, TabCompleter {

    private final QuickEco plugin;

    public BaltopCommand(QuickEco plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c,
                             @NotNull String l, @NotNull String[] a) {
        if (!s.hasPermission("qe.baltop") && !s.hasPermission("quickeco.use")) {
            Msg.send(s, "no-permission");
            return true;
        }

        int count = 10;
        if (a.length >= 1) {
            try { count = Math.max(1, Math.min(100, Integer.parseInt(a[0]))); }
            catch (NumberFormatException ignored) {}
        }

        EconomyProvider eco = plugin.getEconomyProvider();
        List<Map.Entry<UUID, Double>> top = eco.getTopBalances(count);

        if (top.isEmpty()) { Msg.send(s, "baltop-empty"); return true; }

        Msg.send(s, "baltop-header", "count", top.size());

        int rank = 1;
        for (Map.Entry<UUID, Double> entry : top) {
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(entry.getKey());
            String name = player.getName() != null ? player.getName() : "Unknown";
            Msg.send(s, "baltop-entry",
                    "rank", rank,
                    "player", name,
                    "balance", eco.format(entry.getValue()));
            rank++;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c,
                                      @NotNull String l, @NotNull String[] a) {
        if (a.length == 1) return List.of("5", "10", "15", "20", "25", "50");
        return List.of();
    }
}
