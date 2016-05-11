package com.up.sd.trigger.action;

import com.up.sd.SkyblockDungeons;
import com.up.sd.YamlLocation;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 *
 * @author Ricky
 */
public class TeleportAction implements Action {

    Location loc;

    public TeleportAction(Location loc) {
        this.loc = loc;
    }
    
    public TeleportAction(Map<String, Object> map) {
        loc = ((YamlLocation)map.get("loc")).getLocation();
    }
    
    @Override
    public void run(Player p, HashMap<String, Integer> vars, HashMap<String, Integer> globals) {
        p.setVelocity(new Vector(0, 0, 0));
        p.setFallDistance(0);
        p.teleport(loc);
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("loc", new YamlLocation(loc));
        return map;
    }

    @Override
    public String toString() {
        return "teleport {" + SkyblockDungeons.prettyLocation(loc) + "}";
    }

}
