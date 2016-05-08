package com.up.sd;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.Core;
import com.onarandombox.MultiverseCore.api.MultiversePlugin;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Ricky
 */


public class SkyblockDungeons extends JavaPlugin implements Listener {

    Map<Player, Integer> errcools = new HashMap<>();
    Map<String, RewardPool> pools;
    List<Dungeon> duns;
    Dungeon curd = null;
    RewardPool curr = null;
    int cooldown = 72000;
    double rewardamount = 0.5;
    public static Economy econ = null;
    public static Permission permission = null;
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) { }
    
    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(RewardPool.class);
        ConfigurationSerialization.registerClass(Dungeon.class);
        ConfigurationSerialization.registerClass(YamlLocation.class);
        ((MultiverseCore)getServer().getPluginManager().getPlugin("Multiverse-Core")).getMVWorldManager().loadWorlds(false);
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Dungeon dun : duns) {
                dun.tick();
            }
            errcools.entrySet().stream().filter(ec -> ec.getValue() != 0).forEach(ec -> ec.setValue(ec.getValue() - 1));
        }, 0, 1);
        setupEconomy();
        setupPermissions();
        getLogger().info("SkyblockDungeons enabled.");
    }
    
    @Override
    public void onDisable() {
        saveConfig();
    }
    
    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        }
        return (econ != null);
    }
        
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
    
    public void loadConfig() {
        try {
            File dir = new File("plugins/SkyblockDungeons");
            if (!dir.exists()) dir.mkdir();
            File f = new File("plugins/SkyblockDungeons/config.yml");
            if (!f.exists()) f.createNewFile();
            YamlConfiguration conf = new YamlConfiguration();
            conf.load(f);
            cooldown = conf.getInt("cooldown");
            rewardamount = conf.getDouble("rewardamount");
            duns = (List<Dungeon>)conf.getList("dungeons");
            if (duns == null) duns = new ArrayList<>();
            getLogger().info("Loaded " + duns.size() + " dungeons.");
            pools = new HashMap<>();
            conf.getConfigurationSection("rewards").getValues(false).entrySet().stream().forEach(e -> pools.put(e.getKey(), (RewardPool)e.getValue()));
            getLogger().info("Loaded " + pools.size() + " rewards.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void saveConfig() {
        try {
            File f = new File("plugins/SkyblockDungeons/config.yml");
            YamlConfiguration conf = new YamlConfiguration();
            conf.load(f);
            conf.set("cooldown", cooldown);
            conf.set("rewardamount", rewardamount);
            conf.set("dungeons", duns);
            conf.set("rewards", pools);
            conf.save(f);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        duns.stream().filter((dun) -> (dun.isTriggered(p))).forEach((dun) -> {
            if (!dun.isOnCooldown(p)) {
                //Handle rewards
                RewardPool reward = pools.get(dun.getRewardName());
                reward.award(p, (int)(reward.getSize() * rewardamount), econ);
                dun.setCooldown(p, cooldown);
                p.sendMessage(ChatColor.YELLOW + "Received reward from dungeon " + dun.getName());
            } else {
                if (errcools.get(p) == null) errcools.put(p, 0);
                if (errcools.get(p) == 0) {
                    p.sendMessage(ChatColor.DARK_RED + "[ERROR]" + ChatColor.YELLOW + " You still have " + timeToString(dun.getCooldown(p)) + " left before you can receive this reward again.");
                    errcools.put(event.getPlayer(), 600);
                }
            }
        });
    }
    
    public static String timeToString(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds -= minutes * 60;
        minutes -= hours * 60;
        return hours + ":" + minutes + ":" + seconds;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player)sender;
            if (cmd.getName().equalsIgnoreCase("sd") && permission.has(p, "skyblockdungeons.sd." + args[0]) || permission.has(p, "skyblockdungeons.sd.*")) switch (args[0].toLowerCase()) {
                case "list": {
                    p.sendMessage(ChatColor.YELLOW + "Dungeons:");
                    ChatColor[] colors = new ChatColor[] {ChatColor.GREEN, ChatColor.YELLOW, ChatColor.GOLD, ChatColor.RED, ChatColor.DARK_RED};
                    for (Dungeon dun : duns) p.sendRawMessage(dun.getName() + colors[(int)(colors.length * ((double)dun.getCooldown(p) / cooldown))] + " [" + timeToString(dun.getCooldown(p)) + "]");
                    return true;
                }
                case "dun": {
                    if (permission.has(p, "skyblockdungeons.sd.dun." + args[2]) || permission.has(p, "skyblockdungeons.sd.dun.*")) switch (args[1].toLowerCase()) {
                        case "select": {
                            duns.stream().filter((dun) -> (dun.getName().equals(args[2]))).forEach((dun) -> {
                                curd = dun;
                            });
                            p.sendMessage(ChatColor.YELLOW + "Selected dungeon " + curd.getName());
                            return true;
                        }
                        case "create": {
                            duns.add(new Dungeon(args[2]));
                            p.sendMessage(ChatColor.YELLOW + "Create dungeon " + args[2]);
                            return true;
                        }
                        case "set": {
                            curd.setLocation(p.getLocation());
                            p.sendMessage(ChatColor.YELLOW + "Set dungeon to " + p.getLocation());
                            return true;
                        }
                        case "reward": {
                            curd.setRewardName(args[3]);
                            p.sendMessage(ChatColor.YELLOW + "Set dungeon reward to " + args[2]);
                            return true;
                        }
                    }
                    break;
                }
                case "reward": {
                    if (permission.has(p, "skyblockdungeons.sd.reward." + args[2]) || permission.has(p, "skyblockdungeons.sd.reward.*")) switch (args[1].toLowerCase()) {
                        case "select": {
                            pools.entrySet().stream().filter((reward) -> (reward.getKey().equals(args[2]))).forEach((reward) -> {
                                curr = reward.getValue();
                            });
                            p.sendMessage(ChatColor.YELLOW + "Selected reward " + args[2]);
                            return true;
                        }
                        case "create": {
                            pools.put(args[3], new RewardPool());
                            p.sendMessage(ChatColor.YELLOW + "Created reward " + args[2]);
                            return true;
                        }
                        case "add": {
                            curr.addItem(p.getItemInHand().clone());
                            p.sendMessage(ChatColor.YELLOW + "Added item to reward");
                            return true;
                        }
                        case "money": {
                            curr.setMoney(Double.parseDouble(args[3]));
                            p.sendMessage(ChatColor.YELLOW + "Set money to $" + args[2]);
                            return true;
                        }
                    }
                    break;
                }
                case "manage": {
                    if (permission.has(p, "skyblockdungeons.sd.manage." + args[2]) || permission.has(p, "skyblockdungeons.sd.manage.*")) switch (args[1].toLowerCase()) {
                        case "save": {
                            saveConfig();
                            p.sendMessage(ChatColor.YELLOW + "Config saved");
                            return true;
                        }
                        case "reload": {
                            loadConfig();
                            p.sendMessage(ChatColor.YELLOW + "Config reloaded");
                            return true;
                        }
                    }
                    break;
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("sd")) {
            if (args.length > 1) {
                switch (args[0].toLowerCase()) {
                    case "list": {
                        return new ArrayList<>();
                    }
                    case "dun": {
                        if (args.length > 2) {
                            switch (args[1].toLowerCase()) {
                                case "create": {
                                    return new ArrayList<>();
                                }
                                case "reward": {
                                    return pools.entrySet().stream().map(pool -> pool.getKey()).filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
                                }
                                case "select": {
                                    return duns.stream().map(dun -> dun.getName()).filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
                                }
                                case "set": {
                                    return new ArrayList<>();
                                }
                            }
                        }
                        return Arrays.asList(new String[] {"create", "reward", "select", "set"}).stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
                    }
                    case "reward": {
                        if (args.length > 2) {
                            switch (args[1].toLowerCase()) {
                                case "add": {
                                    return new ArrayList<>();
                                }
                                case "create": {
                                    return new ArrayList<>();
                                }
                                case "money": {
                                    return new ArrayList<>();
                                }
                                case "select": {
                                    return pools.entrySet().stream().map(pool -> pool.getKey()).filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
                                }
                            }
                        }
                        return Arrays.asList(new String[] {"add", "create", "money", "select"}).stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
                    }
                    case "manage": {
                        if (args.length > 2) {
                            switch (args[1].toLowerCase()) {
                                case "reload": {
                                    return new ArrayList<>();
                                }
                                case "save": {
                                    return new ArrayList<>();
                                }
                            }
                        }
                        return Arrays.asList(new String[] {"reload", "save"}).stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
                    }
                }
            }
            return Arrays.asList(new String[] {"dun", "list", "manage", "reward"}).stream().filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        }
        return null;
    }
}
