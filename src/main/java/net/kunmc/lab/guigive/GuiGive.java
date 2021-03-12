package net.kunmc.lab.guigive;

import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GuiGive extends JavaPlugin implements Listener {
    public Map<String, Inventory> inventories = new HashMap<>();
    // /gives item <player>用のMap
    public List<Inventory> temporaryInventories = new ArrayList<>();
    public Inventory respawnInventory = null;

    @Override
    public void onEnable() {
        getServer().getPluginCommand("gives").setExecutor(new GivesCommandExecutor(this));
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (e.getPlayer().getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) return;
        if (respawnInventory == null) return;

        give(respawnInventory, e.getPlayer());
    }

    @EventHandler
    public void onTemporaryInventoryClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        if (temporaryInventories.contains(inv)) {
            give(inv, ((Player) inv.getHolder()));
            temporaryInventories.remove(inv);
        }
    }

    public void give(Inventory inv, Player target) {
        for (ItemStack x : inv) {
            if (x == null) continue;
            target.getInventory().addItem(x);
        }
    }
}
