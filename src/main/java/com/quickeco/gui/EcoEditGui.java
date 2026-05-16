package com.quickeco.gui;

import com.quickeco.QuickEco;
import com.quickeco.economy.EconomyProvider;
import com.quickeco.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class EcoEditGui implements GuiHolder {

    private static final int SLOT_HEAD = 4;
    private static final int SLOT_BALANCE = 13;
    private static final int SLOT_CLOSE = 22;

    private static final int[] DEPOSIT_SLOTS = new int[]{10, 11, 12};
    private static final int[] WITHDRAW_SLOTS = new int[]{14, 15, 16};

    private final QuickEco plugin;
    private final OfflinePlayer target;
    private final Inventory inventory;
    private final List<Double> deposits;
    private final List<Double> withdraws;

    public EcoEditGui(QuickEco plugin, OfflinePlayer target) {
        this.plugin = plugin;
        this.target = target;
        this.deposits = readPresets("edit-gui.deposit-presets");
        this.withdraws = readPresets("edit-gui.withdraw-presets");
        this.inventory = Bukkit.createInventory(this, 27,
                Msg.color("&2&lBalance Editor &7- &f" + safeName(target)));
        render();
    }

    public static void open(Player viewer, QuickEco plugin, OfflinePlayer target) {
        EcoEditGui gui = new EcoEditGui(plugin, target);
        viewer.openInventory(gui.getInventory());
        viewer.playSound(viewer.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.6f, 1.2f);
        Msg.send(viewer, "edit-opened", "player", safeName(target));
    }

    @Override
    public Inventory getInventory() { return inventory; }

    private void render() {
        inventory.clear();
        ItemStack filler = button(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < inventory.getSize(); i++) {
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == 2 || col == 0 || col == 8) {
                inventory.setItem(i, filler);
            }
        }

        inventory.setItem(SLOT_HEAD, head(target));
        inventory.setItem(SLOT_BALANCE, balanceItem());

        for (int i = 0; i < DEPOSIT_SLOTS.length; i++) {
            double amt = i < deposits.size() ? deposits.get(i) : 0;
            if (amt > 0) inventory.setItem(DEPOSIT_SLOTS[i], depositButton(amt));
        }
        for (int i = 0; i < WITHDRAW_SLOTS.length; i++) {
            double amt = i < withdraws.size() ? withdraws.get(i) : 0;
            if (amt > 0) inventory.setItem(WITHDRAW_SLOTS[i], withdrawButton(amt));
        }

        inventory.setItem(SLOT_CLOSE, button(Material.BARRIER, "&cClose", List.of()));
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= inventory.getSize()) return;

        if (slot == SLOT_CLOSE) {
            p.closeInventory();
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 0.8f);
            return;
        }

        EconomyProvider eco = plugin.getEconomyProvider();
        for (int i = 0; i < DEPOSIT_SLOTS.length; i++) {
            if (DEPOSIT_SLOTS[i] == slot && i < deposits.size()) {
                double amt = deposits.get(i);
                if (!eco.hasAccount(target)) eco.createAccount(target);
                eco.depositPlayer(target, amt);
                feedback(p, "+", amt, eco);
                render();
                return;
            }
        }
        for (int i = 0; i < WITHDRAW_SLOTS.length; i++) {
            if (WITHDRAW_SLOTS[i] == slot && i < withdraws.size()) {
                double amt = withdraws.get(i);
                if (!eco.hasAccount(target)) eco.createAccount(target);
                double current = eco.getBalance(target);
                double take = Math.min(amt, current);
                if (take > 0) eco.withdrawPlayer(target, take);
                feedback(p, "-", take, eco);
                render();
                return;
            }
        }
    }

    private void feedback(Player p, String sign, double amount, EconomyProvider eco) {
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.4f);
        Msg.debug(p, sign.equals("+") ? "gave" : "took",
                "amount", eco.format(amount),
                "player", safeName(target));
    }

    private ItemStack balanceItem() {
        EconomyProvider eco = plugin.getEconomyProvider();
        double bal = eco.getBalance(target);
        return button(Material.PAPER,
                "&e&lBalance",
                List.of(
                        "&7" + safeName(target),
                        "&f" + eco.format(bal)
                ));
    }

    private ItemStack head(OfflinePlayer p) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof SkullMeta sm) {
            try { sm.setOwningPlayer(p); } catch (Throwable ignored) {}
            sm.setDisplayName(Msg.color("&f" + safeName(p)));
            sm.setLore(List.of(
                    Msg.color("&7uuid: &8" + p.getUniqueId())
            ));
            item.setItemMeta(sm);
        }
        return item;
    }

    private ItemStack depositButton(double amt) {
        EconomyProvider eco = plugin.getEconomyProvider();
        return button(Material.LIME_WOOL,
                "&a+ " + eco.format(amt),
                List.of("&7Click to deposit &a" + eco.format(amt)));
    }

    private ItemStack withdrawButton(double amt) {
        EconomyProvider eco = plugin.getEconomyProvider();
        return button(Material.RED_WOOL,
                "&c- " + eco.format(amt),
                List.of("&7Click to withdraw &c" + eco.format(amt)));
    }

    private ItemStack button(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Msg.color(name));
            if (!lore.isEmpty()) {
                List<String> colored = new ArrayList<>();
                for (String l : lore) colored.add(Msg.color(l));
                meta.setLore(colored);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private List<Double> readPresets(String path) {
        List<Double> out = new ArrayList<>();
        for (Object o : plugin.getConfig().getList(path, List.of(100, 1000, 10000))) {
            if (o instanceof Number n) out.add(n.doubleValue());
        }
        return out;
    }

    private static String safeName(OfflinePlayer p) {
        String n = p.getName();
        return n == null ? p.getUniqueId().toString().substring(0, 8) : n;
    }
}
