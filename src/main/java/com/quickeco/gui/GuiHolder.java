package com.quickeco.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public interface GuiHolder extends InventoryHolder {
    void onClick(InventoryClickEvent event);
}
