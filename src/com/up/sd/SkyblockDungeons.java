package com.up.sd;

import com.up.sd.trigger.action.SetBlocksAction;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.up.sd.trigger.action.Action;
import com.up.sd.trigger.action.AddVariableAction;
import com.up.sd.trigger.condition.AreaCondition;
import com.up.sd.trigger.condition.Condition;
import com.up.sd.trigger.action.EffectAction;
import com.up.sd.trigger.action.MessageAction;
import com.up.sd.trigger.action.MoveBlocksAction;
import com.up.sd.trigger.action.SetVariableAction;
import com.up.sd.trigger.action.TeleportAction;
import com.up.sd.trigger.Trigger;
import com.up.sd.trigger.action.RelativeTeleportAction;
import com.up.sd.trigger.action.SpawnEntityAction;
import com.up.sd.trigger.condition.EntityExistsCondition;
import com.up.sd.trigger.condition.VariableEqualCondition;
import com.up.sd.trigger.condition.VariableNotEqualCondition;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * TODO:
 * - Make cur variables per player
 * - Add more actions:
 *  - VarSub
 *  - SoftTeleport
 *  - Set health
 *  - Set hunger
 *  - Give Item
 * - Add more conditions:
 *  - Click
 *  - VarLessthan
 *  - VarGreaterthan
 *  - EntityIn
 *  - Has item
 * - Add trigger mode toggle (AND|OR)
 * - Variables in message
 * - Dungeon variable saving
 * - Delete options
 * - Rename options
 * - Entity potion effects should last forever
 * - Split config files
 * 
 * @author Ricky
 */


public class SkyblockDungeons extends JavaPlugin implements Listener {

    Map<Player, Integer> errcools = new HashMap<>();
    Map<String, RewardPool> pools;
    List<Dungeon> duns;
    HashMap<Player, Dungeon> curd = new HashMap<>();
    HashMap<Player, RewardPool> curr = new HashMap<>();
    HashMap<Player, Trigger> curt = new HashMap<>();
    int cooldown = 72000;
    double rewardamount = 0.5;
    public static Economy econ = null;
    public static Permission permission = null;
    WorldEditPlugin worldEdit = (WorldEditPlugin)getServer().getPluginManager().getPlugin("WorldEdit");
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) { }
    
    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(AreaCondition.class);
        ConfigurationSerialization.registerClass(EntityExistsCondition.class);
        ConfigurationSerialization.registerClass(VariableEqualCondition.class);
        ConfigurationSerialization.registerClass(VariableNotEqualCondition.class);
        ConfigurationSerialization.registerClass(AddVariableAction.class);
        ConfigurationSerialization.registerClass(EffectAction.class);
        ConfigurationSerialization.registerClass(MessageAction.class);
        ConfigurationSerialization.registerClass(MoveBlocksAction.class);
        ConfigurationSerialization.registerClass(RelativeTeleportAction.class);
        ConfigurationSerialization.registerClass(SetBlocksAction.class);
        ConfigurationSerialization.registerClass(SetVariableAction.class);
        ConfigurationSerialization.registerClass(SpawnEntityAction.class);
        ConfigurationSerialization.registerClass(TeleportAction.class);
        ConfigurationSerialization.registerClass(Dungeon.class);
        ConfigurationSerialization.registerClass(RewardPool.class);
        ConfigurationSerialization.registerClass(YamlLocation.class);
        ConfigurationSerialization.registerClass(Trigger.class);
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
            File f = new File(dir, "config.yml");
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
            curd.clear();
            curr.clear();
            curt.clear();
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
            if (cmd.getName().equalsIgnoreCase("sd")) if (args.length > 0) {
                if (permission.has(p, "skyblockdungeons.sd." + args[0]) || permission.has(p, "skyblockdungeons.sd.*")) switch (args[0].toLowerCase()) {
                    case "list": {
                        p.sendMessage(ChatColor.YELLOW + "Dungeons:");
                        ChatColor[] colors = new ChatColor[] {ChatColor.GREEN, ChatColor.YELLOW, ChatColor.GOLD, ChatColor.RED, ChatColor.DARK_RED};
                        for (Dungeon dun : duns) p.sendRawMessage(dun.getName() + colors[(int)(colors.length * ((double)dun.getCooldown(p) / cooldown))] + " [" + timeToString(dun.getCooldown(p)) + "]");
                        return true;
                    }
                    case "dun": {
                        if (args.length > 1) {
                            if (permission.has(p, "skyblockdungeons.sd.dun." + args[1]) || permission.has(p, "skyblockdungeons.sd.dun.*")) switch (args[1].toLowerCase()) {
                                case "create": {
                                    if (args.length > 2) {
                                        duns.add(new Dungeon(args[2]));
                                        p.sendMessage(ChatColor.YELLOW + "Created dungeon " + args[2]);
                                        return true;
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Usage: /sd dun create <name>");
                                        return true;
                                    }
                                }
                                case "select": {
                                    Dungeon d = duns.stream().filter((dun) -> (dun.getName().equals(args[2]))).findFirst().orElse(null);
                                    System.out.println(curd);
                                    System.out.println(p);
                                    System.out.println(d);
                                    curd.put(p, d);
                                    if (curd.get(p) != null) {
                                        p.sendMessage(ChatColor.YELLOW + "Selected dungeon " + curd.get(p).getName());
                                    } else {
                                        p.sendMessage(ChatColor.RED + "ERROR: No such dungeon.");
                                    }
                                    return true;
                                }
                                case "set": {
                                    if (curd.get(p) != null) {
                                        curd.get(p).setLocation(p.getLocation());
                                        p.sendMessage(ChatColor.YELLOW + "Set dungeon to " + p.getLocation());
                                    } else {
                                        p.sendMessage(ChatColor.RED + "ERROR: No dungeon selected.");
                                    }
                                    return true;
                                }
                                case "reward": {
                                    if (curd.get(p) != null) {
                                        curd.get(p).setRewardName(args[2]);
                                        p.sendMessage(ChatColor.YELLOW + "Set dungeon reward to " + args[2]);
                                    } else {
                                        p.sendMessage(ChatColor.RED + "ERROR: No dungeon selected.");
                                    }
                                    return true;
                                }
                                case "trigger": {
                                    if (args.length > 2) {
                                        if (curd.get(p) != null) {
                                            Selection sel = worldEdit.getSelection(p);
                                            if (permission.has(p, "skyblockdungeons.sd.dun.trigger." + args[1]) || permission.has(p, "skyblockdungeons.sd.dun.trigger.*")) switch (args[2].toLowerCase()) {
                                                case "action": {
                                                    if (args.length > 3) {
                                                        switch (args[3].toLowerCase()) {
                                                            case "add": {
                                                                if (curt.get(p) != null) {
                                                                    if (args.length > 4) {
                                                                        switch (args[4].toLowerCase()) {
                                                                            case "addvar": {
                                                                                if (args.length > 6) {
                                                                                    curt.get(p).addAction(new AddVariableAction(args[5], Integer.parseInt(args[6])));
                                                                                    p.sendMessage(ChatColor.YELLOW + "Added new addvar action.");
                                                                                    return true;
                                                                                } else {
                                                                                    p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger action add addvar <var> <value>");
                                                                                    return true;
                                                                                }
                                                                            }
                                                                            case "effect": {
                                                                                if (args.length > 7) {
                                                                                    curt.get(p).addAction(new EffectAction(new PotionEffect(PotionEffectType.getByName(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]))));
                                                                                    p.sendMessage(ChatColor.YELLOW + "Added new effect action.");
                                                                                    return true;
                                                                                } else {
                                                                                    p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger action add effect <effect> <duration> <level>");
                                                                                    return true;
                                                                                }
                                                                            }
                                                                            case "message": {
                                                                                curt.get(p).addAction(new MessageAction(String.join(" ", Arrays.copyOfRange(args, 5, args.length))));
                                                                                p.sendMessage(ChatColor.YELLOW + "Added new message action.");
                                                                                return true;
                                                                            }
                                                                            case "moveblocks": {
                                                                                if (sel != null && sel.getMinimumPoint() != null && sel.getMaximumPoint() != null) {
                                                                                    curt.get(p).addAction(new MoveBlocksAction(p.getLocation().subtract(0, 1, 0), sel.getMinimumPoint(), sel.getMaximumPoint()));
                                                                                    p.sendMessage(ChatColor.YELLOW + "Added new moveblocks action.");
                                                                                    return true;
                                                                                } else {
                                                                                    p.sendMessage(ChatColor.RED + "Must use WorldEdit to select an area");
                                                                                    return true;
                                                                                }
                                                                            }
                                                                            case "relteleport": {
                                                                                curt.get(p).addAction(new RelativeTeleportAction(p.getLocation()));
                                                                                p.sendMessage(ChatColor.YELLOW + "Added new relative teleport action.");
                                                                                return true;
                                                                            }
                                                                            case "setblocks": {
                                                                                if (sel != null && sel.getMinimumPoint() != null && sel.getMaximumPoint() != null) {
                                                                                    curt.get(p).addAction(new SetBlocksAction(sel.getMinimumPoint(), sel.getMaximumPoint(), p.getItemInHand()));
                                                                                    p.sendMessage(ChatColor.YELLOW + "Added new setblocks action.");
                                                                                    return true;
                                                                                } else {
                                                                                    p.sendMessage(ChatColor.RED + "Must use WorldEdit to select an area");
                                                                                    return true;
                                                                                }
                                                                            }
                                                                            case "setvar": {
                                                                                if (args.length > 6) {
                                                                                    curt.get(p).addAction(new SetVariableAction(args[5], Integer.parseInt(args[6])));
                                                                                    p.sendMessage(ChatColor.YELLOW + "Added new setvar action.");
                                                                                    return true;
                                                                                } else {
                                                                                    p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger action add setvar <var> <value>");
                                                                                    return true;
                                                                                }
                                                                            }
                                                                            case "spawn": {
                                                                                if (args.length > 6) {
                                                                                    int health = -1;
                                                                                    String name = null;
                                                                                    if (args.length > 7) {
                                                                                        health = Integer.parseInt(args[7]);
                                                                                    }
                                                                                    if (args.length > 8) {
                                                                                        name = args[8];
                                                                                    }
                                                                                    ArrayList<PotionEffect> pes = new ArrayList<>();
                                                                                    for (ItemStack i : p.getInventory().getContents()) if (i != null && i.getType() != null && i.getType().equals(Material.POTION)) pes.addAll(Potion.fromItemStack(i).getEffects());
                                                                                    curt.get(p).addAction(new SpawnEntityAction(EntityType.valueOf(args[5]), Integer.parseInt(args[6]), p.getLocation(), health, name, pes, p.getInventory().getArmorContents(), p.getItemInHand()));
                                                                                    p.sendMessage(ChatColor.YELLOW + "Added new spawn action.");
                                                                                    return true;
                                                                                } else {
                                                                                    p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger action add spawn <type> <amount> [health] [name]");
                                                                                    return true;
                                                                                }
                                                                            }
                                                                            case "teleport": {
                                                                                curt.get(p).addAction(new TeleportAction(p.getLocation()));
                                                                                p.sendMessage(ChatColor.YELLOW + "Added new teleport action.");
                                                                                return true;
                                                                            }
                                                                            default: {
                                                                                p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger action add <addvar|effect|message|moveblocks|setblocks|setvar|spawn|teleport>");
                                                                                return true;
                                                                            }
                                                                        }
                                                                    } else {
                                                                        p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger action add <addvar|effect|message|moveblocks|setblocks|setvar|spawn|teleport>");
                                                                        return true;
                                                                    }
                                                                } else {
                                                                    p.sendMessage(ChatColor.RED + "ERROR: No trigger selected.");
                                                                    return true;
                                                                }
                                                            }
                                                            case "list": {
                                                                if (curt.get(p) != null) {
                                                                    p.sendMessage(ChatColor.YELLOW + "Actions in " + curt.get(p).getName() + ":");
                                                                    for (int i = 0; i < curt.get(p).getActions().size(); i++) {
                                                                        Action a  = curt.get(p).getActions().get(i);
                                                                        p.sendMessage(ChatColor.YELLOW + "  " + i + ": " + a);
                                                                    }
                                                                    return true;
                                                                } else {
                                                                    p.sendMessage(ChatColor.RED + "ERROR: No trigger selected.");
                                                                    return true;
                                                                }
                                                            }
                                                            case "remove": {
                                                                if (curt.get(p) != null) {
                                                                    if (args.length > 4) {
                                                                        curt.get(p).removeAction(Integer.parseInt(args[4]));
                                                                        p.sendMessage(ChatColor.YELLOW + "Removed action " + args[4]);
                                                                        return true;
                                                                    } else {
                                                                        p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger action remove <number>");
                                                                        return true;
                                                                    }
                                                                } else {
                                                                    p.sendMessage(ChatColor.RED + "ERROR: No trigger selected.");
                                                                    return true;
                                                                }
                                                            }
                                                            default: {
                                                                p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger action add <add|list|remove>");
                                                                return true;
                                                            }
                                                        }
                                                    } else {
                                                        p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger action <add|list|remove>");
                                                        return true;
                                                    }
                                                }
                                                case "condition": {
                                                    if (args.length > 3) {
                                                        switch (args[3].toLowerCase()) {
                                                            case "add": {
                                                                if (curt.get(p) != null) {
                                                                    if (args.length > 4) {
                                                                        switch (args[4].toLowerCase()) {
                                                                            case "area": {
                                                                                if (sel != null && sel.getMinimumPoint() != null && sel.getMaximumPoint() != null) {
                                                                                    curt.get(p).addCondition(new AreaCondition(sel.getMinimumPoint(), sel.getMaximumPoint()));
                                                                                    p.sendMessage(ChatColor.YELLOW + "Added new area condition.");
                                                                                    return true;
                                                                                } else {
                                                                                    p.sendMessage(ChatColor.RED + "Must use WorldEdit to select an area");
                                                                                    return true;
                                                                                }
                                                                            }
                                                                            case "entityexists": {
                                                                                if (args.length > 6) {
                                                                                    if (sel != null && sel.getMinimumPoint() != null && sel.getMaximumPoint() != null) {
                                                                                        String name = null;
                                                                                        if (args.length > 7) {
                                                                                            name = args[7];
                                                                                        }
                                                                                        curt.get(p).addCondition(new EntityExistsCondition(sel.getMinimumPoint(), sel.getMaximumPoint(), EntityType.valueOf(args[5]), name));
                                                                                        p.sendMessage(ChatColor.YELLOW + "Added new entityexists condition.");
                                                                                        return true;
                                                                                    } else {
                                                                                        p.sendMessage(ChatColor.RED + "Must use WorldEdit to select an area");
                                                                                        return true;
                                                                                    }
                                                                                } else {
                                                                                    p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger condition add entityexists <type> [name]");
                                                                                    return true;
                                                                                }
                                                                            }
                                                                            case "varequals": {
                                                                                if (args.length > 5) {
                                                                                    curt.get(p).addCondition(new VariableEqualCondition(args[5], Integer.parseInt(args[6])));
                                                                                    p.sendMessage(ChatColor.YELLOW + "Added new varequals condition.");
                                                                                    return true;
                                                                                } else {
                                                                                    p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger condition add varequals <var> <value>");
                                                                                    return true;
                                                                                }
                                                                            }
                                                                            case "varnotequals": {
                                                                                if (args.length > 5) {
                                                                                    curt.get(p).addCondition(new VariableNotEqualCondition(args[5], Integer.parseInt(args[6])));
                                                                                    p.sendMessage(ChatColor.YELLOW + "Added new varnotequals condition.");
                                                                                    return true;
                                                                                } else {
                                                                                    p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger condition add varnotequals <var> <value>");
                                                                                    return true;
                                                                                }
                                                                            }
                                                                            default: {
                                                                                p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger condition add <area|entityexists|varequals|varnotequals>");
                                                                                return true;
                                                                            }
                                                                        }
                                                                    } else {
                                                                        p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger condition add <area|entityexists|varequals|varnotequals>");
                                                                        return true;
                                                                    }
                                                                } else {
                                                                    p.sendMessage(ChatColor.RED + "ERROR: No trigger selected.");
                                                                    return true;
                                                                }
                                                            }
                                                            case "list": {
                                                                if (curt.get(p) != null) {
                                                                    p.sendMessage(ChatColor.YELLOW + "Conditions in " + curt.get(p).getName() + ":");
                                                                    for (int i = 0; i < curt.get(p).getConditions().size(); i++) {
                                                                        Condition c  = curt.get(p).getConditions().get(i);
                                                                        p.sendMessage(ChatColor.YELLOW + "  " + i + ": " + c);
                                                                    }
                                                                    return true;
                                                                } else {
                                                                    p.sendMessage(ChatColor.RED + "ERROR: No trigger selected.");
                                                                    return true;
                                                                }
                                                            }
                                                            case "remove": {
                                                                if (curt.get(p) != null) {
                                                                    if (args.length > 4) {
                                                                        curt.get(p).removeCondition(Integer.parseInt(args[4]));
                                                                        p.sendMessage(ChatColor.YELLOW + "Removed condition " + args[4]);
                                                                        return true;
                                                                    } else {
                                                                        p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger condition remove <number>");
                                                                        return true;
                                                                    }
                                                                } else {
                                                                    p.sendMessage(ChatColor.RED + "ERROR: No trigger selected.");
                                                                    return true;
                                                                }
                                                            }
                                                            default: {
                                                                p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger condition <add|list|remove>");
                                                                return true;
                                                            }
                                                        }
                                                    } else {
                                                        p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger condition <add|list|remove>");
                                                        return true;
                                                    }
                                                }
                                                case "create": {
                                                    if (args.length > 3) {
                                                        Trigger t = new Trigger(args[3]);
                                                        curd.get(p).addTrigger(t);
                                                        p.sendMessage(ChatColor.YELLOW + "Created trigger " + t.getName());
                                                        return true;
                                                    } else {
                                                        p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger create <name>");
                                                        return true;
                                                    }
                                                }
                                                case "inverted": {
                                                    if (args.length > 3) {
                                                        curt.get(p).setInverted(Boolean.parseBoolean(args[3]));
                                                        p.sendMessage(ChatColor.YELLOW + "Set trigger to be " + (curt.get(p).isInverted() ? "" : "not ") + "inverted");
                                                        return true;
                                                    } else {
                                                        p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger inverted <true|false>");
                                                        return true;
                                                    }
                                                }
                                                case "list": {
                                                    p.sendMessage(ChatColor.YELLOW + "Triggers in " + curd.get(p).getName() + ":");
                                                    for (Trigger t : curd.get(p).getTriggers()) p.sendMessage(ChatColor.YELLOW + "  " + t.getName());
                                                    return true;
                                                }
                                                case "select": {
                                                    curt.put(p, curd.get(p).getTriggers().stream().filter((t) -> (t.getName().equals(args[3]))).findFirst().orElse(null));
                                                    if (curt.get(p) != null) {
                                                        p.sendMessage(ChatColor.YELLOW + "Selected trigger " + curt.get(p).getName());
                                                    } else {
                                                        p.sendMessage(ChatColor.RED + "ERROR: No such trigger.");
                                                    }
                                                    return true;
                                                }
                                                default: {
                                                    p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger <action|condition|inverted|list|create|select>");
                                                    return true;
                                                }
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED + "ERROR: No dungeon selected.");
                                            return true;
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Usage: /sd dun trigger <action|condition|inverted|list|create|select>");
                                        return true;
                                    }
                                }
                                case "vars": {
                                    if (curd.get(p) != null) {
                                        if (args.length > 2) {
                                            p.sendMessage(ChatColor.YELLOW + "Player " + p.getName() + "'s variables:");
                                            for (Map.Entry<String, Integer> e : curd.get(p).vars.get(p).entrySet()) p.sendMessage(ChatColor.YELLOW + "  " + e.getKey() + ": " + e.getValue());
                                        } else {
                                            p.sendMessage(ChatColor.YELLOW + "Dungeon global variables:");
                                            for (Map.Entry<String, Integer> e : curd.get(p).globals.entrySet()) p.sendMessage(ChatColor.YELLOW + "  " + e.getKey() + ": " + e.getValue());
                                            
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "ERROR: No dungeon selected.");
                                    }
                                    return true;
                                }
                                default: {
                                    p.sendMessage(ChatColor.RED + "Usage: /sd dun <create|select|set|reward|trigger|vars>");
                                    return true;
                                }
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "Usage: /sd dun <create|select|set|reward|trigger|vars>");
                            return true;
                        }
                        break;
                    }
                    case "reward": {
                        if (args.length > 1) {
                            if (permission.has(p, "skyblockdungeons.sd.reward." + args[1]) || permission.has(p, "skyblockdungeons.sd.reward.*")) switch (args[1].toLowerCase()) {
                                case "add": {
                                    if (curr.get(p) != null) {
                                        curr.get(p).addItem(p.getItemInHand().clone());
                                        p.sendMessage(ChatColor.YELLOW + "Added item to reward " + getCurrentRewardName(p));
                                    } else {
                                        p.sendMessage(ChatColor.RED + "ERROR: No reward selected.");
                                    }
                                    return true;
                                }
                                case "items": {
                                    if (curr.get(p) != null) {
                                        p.sendMessage(ChatColor.YELLOW + "Items in " + getCurrentRewardName(p) + ":");
                                        curr.get(p).getItems().stream()
                                                .collect(
                                                        () -> new HashMap<Double, Double>(), 
                                                        (a, i) -> {
                                                            double d = i.getTypeId() + i.getData().getData() / 100d;
                                                            if (a.containsKey(d)) a.put(d, a.get(d) + i.getAmount() + 0.01); else a.put(d, i.getAmount() + 0.01);
                                                        }, 
                                                        (a, b) -> a.putAll(b)
                                                )
                                                .forEach((d, a) -> p.sendMessage(ChatColor.YELLOW + "#" + (int)Math.floor(d) + ":" + (int)((d - Math.floor(d)) * 100) + " - " + (int)Math.floor(a) + "/" + (Math.floor(a) / ((a - Math.floor(a)) * 100))));
                                    } else {
                                        p.sendMessage(ChatColor.RED + "ERROR: No reward selected.");
                                    }
                                    return true;
                                }
                                case "create": {
                                    if (args.length > 2) {
                                        pools.put(args[2], new RewardPool());
                                        p.sendMessage(ChatColor.YELLOW + "Created reward " + args[2]);
                                        return true;
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Usage: /sd reward create <name>");
                                        return true;
                                    }
                                }
                                case "money": {
                                    if (curr.get(p) != null) {
                                        if (args.length > 2) {
                                            curr.get(p).setMoney(Double.parseDouble(args[2]));
                                            p.sendMessage(ChatColor.YELLOW + "Set money for " + getCurrentRewardName(p) + " to $" + args[2]);
                                            return true;
                                        } else {
                                            p.sendMessage(ChatColor.RED + "Usage: /sd reward money <amount>");
                                            return true;
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "ERROR: No reward selected.");
                                        return true;
                                    }
                                }
                                case "select": {
                                    curr.put(p, pools.entrySet().stream().filter((reward) -> (reward.getKey().equals(args[2]))).map(reward -> reward.getValue()).findFirst().orElse(null));
                                    if (curr.get(p) != null) {
                                        p.sendMessage(ChatColor.YELLOW + "Selected reward " + args[2]);
                                    } else {
                                        p.sendMessage(ChatColor.RED + "ERROR: No such reward.");
                                    }
                                    return true;
                                }
                                default: {
                                    p.sendMessage(ChatColor.RED + "Usage: /sd reward <add|create|money|select>");
                                    return true;
                                }
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "Usage: /sd reward <add|create|money|select>");
                            return true;
                        }
                        break;
                    }
                    case "manage": {
                        if (args.length > 1) {
                            if (permission.has(p, "skyblockdungeons.sd.manage." + args[1]) || permission.has(p, "skyblockdungeons.sd.manage.*")) switch (args[1].toLowerCase()) {
                                case "reload": {
                                    loadConfig();
                                    p.sendMessage(ChatColor.YELLOW + "Config reloaded");
                                    return true;
                                }
                                case "save": {
                                    saveConfig();
                                    p.sendMessage(ChatColor.YELLOW + "Config saved");
                                    return true;
                                }
                                default: {
                                    p.sendMessage(ChatColor.RED + "Usage: /sd manage <reload|save>");
                                    return true;
                                }
                            }
                            break;
                        } else {
                            p.sendMessage(ChatColor.RED + "Usage: /sd manage <reload|save>");
                            return true;
                        }
                    }
                    default: {
                        p.sendMessage(ChatColor.RED + "Usage: /sd <dun|list|manage|reward>");
                        return true;
                    }
                }
                return true;
            } else {
                p.sendMessage(ChatColor.RED + "Usage: /sd <dun|list|manage|reward>");
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player)sender;
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
                                    case "trigger": {
                                        if (args.length > 3) {
                                            switch (args[2].toLowerCase()) {
                                                case "action": {
                                                    if (args.length > 4) {
                                                        switch (args[3].toLowerCase()) {
                                                            case "add": {
                                                                if (args.length > 5) {
                                                                    switch (args[4].toLowerCase()) {
                                                                        case "addvar": {
                                                                            if (curd.get(p) != null) {
                                                                                return curd.get(p).getAllVariableNames().stream().filter(s -> s.startsWith(args[5])).collect(Collectors.toList());
                                                                            } else {
                                                                                return new ArrayList<>();
                                                                            }
                                                                        }
                                                                        case "effect": {
                                                                            if (args.length == 6) {
                                                                                return Arrays.asList(PotionEffectType.values()).stream().filter(e -> e != null).map(e -> e.getName()).filter(s -> s.startsWith(args[5])).collect(Collectors.toList());
                                                                            }
                                                                            return new ArrayList<>();
                                                                        }
                                                                        case "message":
                                                                        case "moveblocks":
                                                                        case "relteleport":
                                                                        case "setblocks": {
                                                                            return new ArrayList<>();
                                                                        }
                                                                        case "setvar": {
                                                                            if (curd.get(p) != null) {
                                                                                return curd.get(p).getAllVariableNames().stream().filter(s -> s.startsWith(args[5])).collect(Collectors.toList());
                                                                            } else {
                                                                                return new ArrayList<>();
                                                                            }
                                                                        }
                                                                        case "spawn": {
                                                                            if (args.length == 6) {
                                                                                return Arrays.asList(EntityType.values()).stream().filter(e -> e != null).map(e -> e.name()).filter(s -> s.startsWith(args[5])).collect(Collectors.toList());
                                                                            }
                                                                            return new ArrayList<>();
                                                                        }
                                                                        case "teleport": {
                                                                            return new ArrayList<>();
                                                                        }
                                                                    }
                                                                }
                                                                return Arrays.asList(new String[] {"addvar", "effect", "message", "moveblocks", "relteleport", "setblocks", "setvar", "spawn", "teleport"}).stream().filter(s -> s.startsWith(args[4])).collect(Collectors.toList());
                                                            }
                                                            case "list": {
                                                                return new ArrayList<>();
                                                            }
                                                            case "remove": {
                                                                if (curt.get(p) != null) {
                                                                    ArrayList<String> tabs = new ArrayList<>();
                                                                    for (int i = 0; i < curt.get(p).getActions().size(); i++) tabs.add(i + "");
                                                                    return tabs;
                                                                } else {
                                                                    return new ArrayList<>();
                                                                }
                                                            }
                                                        }
                                                    }
                                                    return Arrays.asList(new String[] {"add", "list", "remove"}).stream().filter(s -> s.startsWith(args[3])).collect(Collectors.toList());
                                                }
                                                case "condition": {
                                                    if (args.length > 4) {
                                                        switch (args[3].toLowerCase()) {
                                                            case "add": {
                                                                if (args.length > 5) {
                                                                    switch (args[4].toLowerCase()) {
                                                                        case "area": {
                                                                            return new ArrayList<>();
                                                                        }
                                                                        case "entityexists": {
                                                                            if (args.length == 6) {
                                                                                return Arrays.asList(EntityType.values()).stream().filter(e -> e != null).map(e -> e.name()).filter(s -> s.startsWith(args[5])).collect(Collectors.toList());
                                                                            }
                                                                            return new ArrayList<>();
                                                                        }
                                                                        case "varequals": {
                                                                            if (curt.get(p) != null) {
                                                                                return curd.get(p).getAllVariableNames().stream().filter(s -> s.startsWith(args[5])).collect(Collectors.toList());
                                                                            } else {
                                                                                return new ArrayList<>();
                                                                            }
                                                                        }
                                                                        case "varnotequals": {
                                                                            if (curt.get(p) != null) {
                                                                                return curd.get(p).getAllVariableNames().stream().filter(s -> s.startsWith(args[5])).collect(Collectors.toList());
                                                                            } else {
                                                                                return new ArrayList<>();
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                return Arrays.asList(new String[] {"area", "entityexists", "varequals", "varnotequals"}).stream().filter(s -> s.startsWith(args[4])).collect(Collectors.toList());
                                                            }
                                                            case "list": {
                                                                return new ArrayList<>();
                                                            }
                                                            case "remove": {
                                                                if (curt.get(p) != null) {
                                                                    ArrayList<String> tabs = new ArrayList<>();
                                                                    for (int i = 0; i < curt.get(p).getConditions().size(); i++) tabs.add(i + "");
                                                                    return tabs;
                                                                } else {
                                                                    return new ArrayList<>();
                                                                }
                                                            }
                                                        }
                                                    }
                                                    return Arrays.asList(new String[] {"add", "list", "remove"}).stream().filter(s -> s.startsWith(args[3])).collect(Collectors.toList());
                                                }
                                                case "create":
                                                case "list": {
                                                    return new ArrayList<>();
                                                }
                                                case "inverted": {
                                                    return Arrays.asList(new String[] {"true", "false"}).stream().filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
                                                }
                                                case "select": {
                                                    if (curd.get(p) != null) {
                                                        return curd.get(p).getTriggers().stream().map(t -> t.getName()).filter(s -> s.startsWith(args[3])).collect(Collectors.toList());
                                                    } else {
                                                        return new ArrayList<>();
                                                    }
                                                }
                                            }
                                        }
                                        return Arrays.asList(new String[] {"action", "condition", "create", "inverted", "list", "select"}).stream().filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
                                    }
                                    case "vars": {
                                        return Arrays.asList(Bukkit.getOnlinePlayers()).stream().map(pl -> pl.getName()).filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
                                    }
                                }
                            }
                            return Arrays.asList(new String[] {"create", "reward", "select", "set", "trigger", "vars"}).stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
                        }
                        case "reward": {
                            if (args.length > 2) {
                                switch (args[1].toLowerCase()) {
                                    case "add":
                                    case "create":
                                    case "items":
                                    case "money": {
                                        return new ArrayList<>();
                                    }
                                    case "select": {
                                        return pools.entrySet().stream().map(pool -> pool.getKey()).filter(s -> s.startsWith(args[2])).collect(Collectors.toList());
                                    }
                                }
                            }
                            return Arrays.asList(new String[] {"add", "create", "items", "money", "select"}).stream().filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
                        }
                        case "manage": {
                            if (args.length > 2) {
                                switch (args[1].toLowerCase()) {
                                    case "reload":
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
        }
        return null;
    }
    
    public static String prettyLocation(Location l) {
        return "Location {" + l.getWorld().getName() + ", " + Math.round(l.getX() * 10) * 0.1 + ", " + Math.round(l.getY() * 10) * 0.1 + ", " + Math.round(l.getZ() * 10) * 0.1 + "}";
    }
    
    public String getCurrentRewardName(Player p) {
        return pools.entrySet().stream().filter(e -> e.getValue().equals(curr.get(p))).findFirst().get().getKey();
    }
}
