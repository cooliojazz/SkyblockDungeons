package com.up.sd;

import com.up.sd.triggers.Trigger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

/**
 *
 * @author Ricky
 */
public class Dungeon implements ConfigurationSerializable {
    String name;
    Map<String, Integer> cooldowns = new HashMap<>();
    Location loc;
    String reward;
    List<Trigger> triggers = new ArrayList<>();

    public Dungeon(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public void tick() {
        for (String id : cooldowns.keySet()) {
            if (cooldowns.get(id) > 0) cooldowns.put(id, cooldowns.get(id) - 1);
        }
        for (Player p : Bukkit.getOnlinePlayers()) for (Trigger t : triggers) if (t.triggered(p)) t.execute(p);
    }
    
    public void setCooldown(Player p, int cooldown) {
        cooldowns.put(p.getUniqueId().toString(), cooldown);
    }
    
    public boolean isOnCooldown(Player p) {
        if (cooldowns.containsKey(p.getUniqueId().toString())) {
            return cooldowns.get(p.getUniqueId().toString()) > 0;
        }
        return false;
    }

    public void setRewardName(String reward) {
        this.reward = reward;
    }
    
    public String getRewardName() {
        return reward;
    }
    
    public boolean isTriggered(Player p) {
        if (loc == null) return false;
        Location l = p.getLocation();
        return loc.getWorld().equals(l.getWorld()) && loc.getBlockX() == l.getBlockX() && loc.getBlockY() == l.getBlockY() && loc.getBlockZ() == l.getBlockZ();
    }

    public void addTrigger(Trigger t) {
        triggers.add(t);
    }
    
    public List<Trigger> getTriggers() {
        return new ArrayList<>(triggers);
    }
    
    public void setLocation(Location loc) {
        this.loc = loc;
    }

    public int getCooldown(Player p) {
        Integer i = cooldowns.get(p.getUniqueId().toString());
        return i == null ? 0 : i;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("cooldowns", cooldowns);
        map.put("loc", new YamlLocation(loc));
        map.put("reward", reward);
        map.put("triggers", triggers);
        return map;
    }

    public Dungeon(Map<String, Object> map) {
        name = (String)map.get("name");
        cooldowns = (Map<String, Integer>)map.get("cooldowns");
        loc = ((YamlLocation)map.get("loc")).getLocation();
        reward = (String)map.get("reward");
        if (map.get("triggers") != null) triggers = (List<Trigger>)map.get("triggers");
    }
    
}
