package net.kunmc.lab.guigive;

import org.bukkit.plugin.java.JavaPlugin;
import sun.misc.JavaAWTAccess;

public final class GuiGive extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginCommand("gives").setExecutor(new GivesCommandExecutor());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
