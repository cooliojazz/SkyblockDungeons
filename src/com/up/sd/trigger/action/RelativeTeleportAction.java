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
public class RelativeTeleportAction implements Action {

    Location loc;

    public RelativeTeleportAction(Location loc) {
        this.loc = loc;
    }
    
    public RelativeTeleportAction(Map<String, Object> map) {
        loc = ((YamlLocation)map.get("loc")).getLocation();
    }
    
    @Override
    public void run(Player p, HashMap<String, Integer> vars, HashMap<String, Integer> globals) {
        p.setVelocity(new Vector(0, 0, 0));
        p.setFallDistance(0);
        p.teleport(alignLoc(p.getLocation()));
    }
    
    private Location alignLoc(Location l) {
        Location ret = l.clone();
        ret.setX(loc.getBlockX() + l.getX() - l.getBlockX());
        ret.setY(loc.getBlockY() + l.getY() - l.getBlockY());
        ret.setZ(loc.getBlockZ() + l.getZ() - l.getBlockZ());
        return ret;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("loc", new YamlLocation(loc));
        return map;
    }

    @Override
    public String toString() {
        return "relteleport {" + SkyblockDungeons.prettyLocation(loc) + "}";
    }

}
