package com.up.sd.triggers;

import com.up.sd.YamlLocation;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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
    public void run(Player p, HashMap<String, Integer> vars) {
        p.teleport(loc);
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("loc", new YamlLocation(loc));
        return map;
    }

}
