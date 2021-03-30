package net.kunmc.lab.guigive;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GuiGive extends JavaPlugin implements Listener {
    public Map<String, Inventory> inventories = new HashMap<>();
    public Map<Inventory, String> invDesc = new HashMap<>();
    // /gives item <player>用のMap
    public List<Inventory> temporaryInventories = new ArrayList<>();
    public Inventory respawnInventory = null;

    @Override
    public void onEnable() {
        getDataFolder().mkdir();
        for (String s : getDataFolder().list()) {
            try {
                if (s.equals("config.yml")) continue;
                loadInventory(s);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();
        String invName = ((String) config.get("respawnInventory"));
        if (invName != null && inventories.containsKey(invName)) {
            respawnInventory = inventories.get(invName);
            getLogger().info("リスポーン時のインベントリが" + invName + "に設定されました.");
        } else {
            getLogger().info("リスポーン時のインベントリは設定されていません.");
        }

        getServer().getPluginCommand("gives").setExecutor(new GivesCommandExecutor(this));
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (respawnInventory == null) return;

        boolean GameRuleClassExists = true;
        try {
            Class.forName("org.bukkit.GameRule");
        } catch (ClassNotFoundException classNotFoundException) {
            GameRuleClassExists = false;
        }

        if (GameRuleClassExists) {
            if (e.getPlayer().getWorld().isGameRule("keepInventory") &&
                    e.getPlayer().getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) return;
        } else {
            if (e.getPlayer().getWorld().getGameRuleValue("keepInventory").equals("true")) return;
        }

        give(respawnInventory, e.getPlayer());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        String invTitle = e.getView().getTitle();
        if (inventories.containsValue(e.getInventory())) {
            try {
                saveInventory(inventories.get(invTitle), invTitle, invDesc.get(inventories.get(invTitle)));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onTemporaryInventoryClose(InventoryCloseEvent e) {
        Inventory inv = e.getInventory();
        if (temporaryInventories.contains(inv)) {
            if (inv.getHolder() == null) {
                int cnt = giveAll(inv);
                e.getPlayer().sendMessage(Message.Success(Bukkit.getOnlinePlayers().size() + "人のプレイヤーに" + cnt + "個のアイテムを配りました."));
            } else {
                Player target = (Player) inv.getHolder();
                int cnt = give(inv, target);
                e.getPlayer().sendMessage(Message.Success(target.getName() + "に" + cnt + "個のアイテムを配りました."));
            }
            temporaryInventories.remove(inv);
        }
    }

    public int give(Inventory inv, Player target) {
        int cnt = 0;
        for (int i = 27; i < 36; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) continue;
            cnt += item.getAmount();
            if (target.getInventory().getItem(i - 27) == null) {
                target.getInventory().setItem(i - 27, item);
            } else {
                target.getInventory().addItem(item);
            }
        }
        for (int i = 0; i < 27; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) continue;
            cnt += item.getAmount();
            if (target.getInventory().getItem(i + 9) == null) {
                target.getInventory().setItem(i + 9, item);
            } else {
                target.getInventory().addItem(item);
            }
        }
        return cnt;
    }

    public int giveAll(Inventory inv) {
        int cnt = 0;
        for (Player p : getServer().getOnlinePlayers()) {
            cnt = give(inv, p);
        }
        return cnt;
    }

    public void saveInventory(Inventory inv, String filename, String description) throws IOException {
        try (BukkitObjectOutputStream out = new BukkitObjectOutputStream(new FileOutputStream(new File(getDataFolder(), filename)))) {
            out.writeInt(inv.getSize());
            for (ItemStack item : inv) {
                out.writeObject(item);
            }
            if (description == null) description = "";
            out.writeUTF(description);
        }
    }

    public void loadInventory(String filename) throws IOException, ClassNotFoundException {
        Inventory inv;
        String description;
        try (BukkitObjectInputStream in = new BukkitObjectInputStream(new FileInputStream(new File(getDataFolder(), filename)))) {
            int size = in.readInt();
            inv = Bukkit.createInventory(null, size, filename);
            for (int i = 0; i < size; i++) {
                ItemStack item = (ItemStack) in.readObject();
                if (item != null) inv.setItem(i, item);
            }
            description = in.readUTF();
        }
        inventories.put(filename, inv);
        invDesc.put(inv, description);
    }
}
