package com.up.sd;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

/**
 *
 * @author Ricky
 */
public class YamlLocation implements ConfigurationSerializable {
    
    private Location l;

    public YamlLocation(Location l) {
        this.l = l;
    }
    public YamlLocation(Map<String, Object> map) {
        if (map.size() >= 7) {
            l = new Location(Bukkit.createWorld(new WorldCreator((String)map.get("world"))), (double)map.get("x"), (double)map.get("y"), (double)map.get("z"), (float)(double)map.get("yaw"), (float)(double)map.get("pitch"));
        } else {
            Bukkit.getLogger().info("Warning: Empty location loaded.");
            l = null;
        }
    }

    public Location getLocation() {
        return l;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        if (l != null) {
            map.put("world", l.getWorld().getName());
            map.put("x", l.getX());
            map.put("y", l.getY());
            map.put("z", l.getZ());
            map.put("yaw", l.getYaw());
            map.put("pitch", l.getPitch());
        }
        return map;
    }
    
}
