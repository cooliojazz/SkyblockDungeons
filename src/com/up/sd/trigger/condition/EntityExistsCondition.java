package com.up.sd.trigger.condition;

import com.up.sd.SkyblockDungeons;
import com.up.sd.YamlLocation;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 *
 * @author Ricky
 */
public class EntityExistsCondition implements Condition {

    Location c1;
    Location c2;
    EntityType entity;
    String name;

    public EntityExistsCondition(Location c1, Location c2, EntityType entity, String name) {
        this.c1 = c1;
        this.c2 = c2;
        this.entity = entity;
        this.name = name;
    }
    
    public EntityExistsCondition(Map<String, Object> map) {
        c1 = ((YamlLocation)map.get("c1")).getLocation();
        c2 = ((YamlLocation)map.get("c2")).getLocation();
        entity = EntityType.valueOf((String)map.get("entity"));
        name = (String)map.get("name");
    }
    
    @Override
    public boolean passes(Player p, HashMap<String, Integer> vars, HashMap<String, Integer> globals) {
        return c1.getWorld().getEntities().stream().anyMatch(e -> e instanceof LivingEntity && e.getType().equals(entity) && (name != null ? name.equals(((LivingEntity)e).getCustomName()) : true) && contains(e.getLocation()));
    }
    
    public boolean contains(Location l) {
        return between(c1.getBlockX(), c2.getBlockX(), l.getX()) && between(c1.getBlockY(), c2.getBlockY(), l.getY()) && between(c1.getBlockZ(), c2.getBlockZ(), l.getZ());
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
        map.put("entity", entity.name());
        map.put("name", name);
        return map;
    }

    @Override
    public String toString() {
        return "entityexists {" + SkyblockDungeons.prettyLocation(c1) + ", " + SkyblockDungeons.prettyLocation(c2) + ", " + entity.name() + (name != null ? ", " + name : "") + "}";
    }
}
