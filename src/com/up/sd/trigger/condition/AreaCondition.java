package com.up.sd.trigger.condition;

import com.up.sd.SkyblockDungeons;
import com.up.sd.YamlLocation;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author Ricky
 */
public class AreaCondition implements Condition {
    
    Location c1;
    Location c2;

    public AreaCondition(Location c1, Location c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public AreaCondition(Map<String, Object> map) {
        c1 = ((YamlLocation)map.get("c1")).getLocation();
        c2 = ((YamlLocation)map.get("c2")).getLocation();
    }
    
    @Override
    public boolean passes(Player p, HashMap<String, Integer> vars, HashMap<String, Integer> globals) {
        Location l = p.getLocation();
        return c1.getWorld().equals(l.getWorld()) && between(c1.getBlockX(), c2.getBlockX(), l.getX()) && between(c1.getBlockY(), c2.getBlockY(), l.getY()) && between(c1.getBlockZ(), c2.getBlockZ(), l.getZ());
    }
    
    private boolean between(double d1, double d2, double v) {
        if (d1 < d2) {
            return v >= d1 && v < d2 + 1;
        } else {
            return v >= d2 && v < d1 + 1;
        }
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("c1", new YamlLocation(c1));
        map.put("c2", new YamlLocation(c2));
        return map;
    }

    @Override
    public String toString() {
        return "area {" + SkyblockDungeons.prettyLocation(c1) + ", " + SkyblockDungeons.prettyLocation(c2) + "}";
    }

}
