package net.kunmc.lab.guigive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GivesCommandExecutor implements CommandExecutor, TabCompleter {
    GuiGive plugin;
    Map<String, SubCommand> subCmds = new HashMap<>();

    GivesCommandExecutor(GuiGive plugin) {
        this.plugin = plugin;
        subCmds.put("add", (sender, command, args) -> {
            if (!(sender instanceof HumanEntity)) {
                sender.sendMessage(Message.Failure("このコマンドはプレイヤーから実行してください."));
                return;
            }
            if (args.length < 2) {
                sender.sendMessage(Message.Failure("/gives add <name> [description]"));
                return;
            }

            String invName = getBasenamse(args[1]); //DirectoryTraversal対策
            if (plugin.inventories.containsKey(invName)) {
                sender.sendMessage(Message.Failure(invName + "は存在しています."));
                return;
            }
            Inventory inv = plugin.inventories.computeIfAbsent(invName, name -> {
                Inventory tmp = Bukkit.createInventory(((HumanEntity) sender), 36, name);
                tmp.all(new ItemStack(Material.AIR));
                return tmp;
            });
            ((HumanEntity) sender).openInventory(inv);

            if (args.length >= 3) {
                String desc = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));
                plugin.invDesc.put(inv, desc);
            }
        });
        subCmds.put("apply", (sender, command, args) -> {
            if (args.length < 3) {
                sender.sendMessage(Message.Failure("/gives apply <name> <player>"));
                return;
            }
            String invName = args[1];
            if (!plugin.inventories.containsKey(invName)) {
                sender.sendMessage(Message.Failure(invName + "は存在しません."));
                return;
            }

            if (args[2].equals("@a")) {
                int cnt = plugin.giveAll(plugin.inventories.get(invName));
                sender.sendMessage(Message.Success(Bukkit.getOnlinePlayers().size() + "人のプレイヤーに" + cnt + "個のアイテムを配りました."));
            } else {
                Player p = Bukkit.getPlayer(args[2]);
                if (p == null) {
                    sender.sendMessage(Message.Failure(args[2] + "は存在しません."));
                    return;
                }
                int cnt = plugin.give(plugin.inventories.get(invName), p);
                sender.sendMessage(Message.Success(p.getName() + "に" + cnt + "個のアイテムを配りました."));
            }
        });
        subCmds.put("clear-default-item", ((sender, command, args) -> {
            plugin.respawnInventory = null;
            plugin.getConfig().set("respawnInventory", null);
            plugin.saveConfig();
            sender.sendMessage(Message.Success("default-itemを解除しました."));
        }));
        subCmds.put("edit", (sender, command, args) -> {
            if (!(sender instanceof HumanEntity)) {
                sender.sendMessage(Message.Failure("このコマンドはプレイヤーから実行してください."));
                return;
            }
            if (args.length < 2) {
                sender.sendMessage(Message.Failure("/gives edit <name> [description]"));
                return;
            }

            String invName = args[1];
            if (!plugin.inventories.containsKey(invName)) {
                sender.sendMessage(Message.Failure(invName + "は存在しません."));
                return;
            }

            Inventory inv = plugin.inventories.get(invName);
            ((HumanEntity) sender).openInventory(inv);

            if (args.length >= 3) {
                String desc = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));
                plugin.invDesc.put(inv, desc);
            }
        });
        subCmds.put("item", (sender, command, args) -> {
            if (!(sender instanceof HumanEntity)) {
                sender.sendMessage(Message.Failure("このコマンドはプレイヤーから実行してください."));
                return;
            }
            if (args.length < 2) {
                sender.sendMessage(Message.Failure("/gives item <player>"));
                return;
            }

            Player p;
            if (args[1].equals("@a")) {
                p = null;
            } else {
                p = Bukkit.getPlayer(args[1]);
                if (p == null) {
                    sender.sendMessage(Message.Failure(args[1] + "は存在しません."));
                    return;
                }
            }

            Inventory inv = Bukkit.createInventory(p, 36, "閉じるとGiveします");
            ((HumanEntity) sender).openInventory(inv);
            plugin.temporaryInventories.add(inv);
        });
        subCmds.put("default-item", (sender, command, args) -> {
            if (args.length < 2) {
                sender.sendMessage(Message.Failure("/gives default-item <name>"));
                return;
            }

            String invName = args[1];
            if (plugin.inventories.containsKey(invName)) {
                plugin.respawnInventory = plugin.inventories.get(invName);
                plugin.getConfig().set("respawnInventory", invName);
                plugin.saveConfig();
                sender.sendMessage(Message.Success(invName + "はリスポーン時のインベントリに設定されました."));
            } else {
                sender.sendMessage(Message.Failure(invName + "は存在しません."));
            }
        });
        subCmds.put("remove", (sender, command, args) -> {
            if (args.length < 2) {
                sender.sendMessage(Message.Failure("/gives remove <name>"));
                return;
            }

            if (new File(plugin.getDataFolder(), getBasenamse(args[1])).delete()) {
                Inventory inv = plugin.inventories.remove(args[1]);
                if (plugin.respawnInventory.equals(inv)) {
                    plugin.respawnInventory = null;
                    plugin.getConfig().set("respawnInventory", null);
                    plugin.saveConfig();
                }
                sender.sendMessage(Message.Success(args[1] + "は正常に削除されました."));
            } else {
                sender.sendMessage(Message.Failure("削除に失敗しました."));
            }
        });
        subCmds.put("list", (sender, command, args) -> {
            sender.sendMessage(Message.Success("登録インベントリ一覧"));
            plugin.inventories.forEach((name, inv) -> {
                String desc = plugin.invDesc.get(inv);
                if (desc == null || desc.isEmpty()) desc = "説明はありません";
                sender.sendMessage(Message.Success(name + " - " + desc));
            });
        });
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) return false;
        subCmds.get(args[0].toLowerCase()).execute(sender, command, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1)
            return subCmds.keySet().stream()
                    .filter(x -> x.startsWith(args[0]))
                    .collect(Collectors.toList());

        String subCmd = args[0].toLowerCase();
        if (args.length == 2) {
            switch (subCmd) {
                case "add":
                    return Collections.singletonList("<name>");
                case "apply":
                case "default-item":
                case "edit":
                case "remove":
                    return new ArrayList<>(plugin.inventories.keySet()).stream()
                            .filter(x -> x.startsWith(args[1]))
                            .collect(Collectors.toList());
                case "item":
                    return Stream.concat(Bukkit.getOnlinePlayers().stream().map(Player::getName), Stream.of("@a"))
                            .filter(x -> x.startsWith(args[1]))
                            .collect(Collectors.toList());
            }
        }

        if (args.length == 3) {
            switch (subCmd) {
                case "add":
                case "edit":
                    return Collections.singletonList("[description]");
                case "apply":
                    return Stream.concat(Bukkit.getOnlinePlayers().stream().map(Player::getName), Stream.of("@a"))
                            .filter(x -> x.startsWith(args[2]))
                            .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    private String getBasenamse(String pathname) {
        return new File(pathname).getName();
    }
}
