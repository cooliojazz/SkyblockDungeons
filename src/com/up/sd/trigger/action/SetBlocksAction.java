package com.up.sd.trigger.action;

import com.up.sd.SkyblockDungeons;
import com.up.sd.YamlLocation;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author ricky.t
 */
public class SetBlocksAction implements Action {

    Location c1;
    Location c2;
    ItemStack block;

    public SetBlocksAction(Location c1, Location c2, ItemStack s) {
        this.c1 = c1;
        this.c2 = c2;
        this.block = s;
    }
    
    public SetBlocksAction(Map<String, Object> map) {
        c1 = ((YamlLocation)map.get("c1")).getLocation();
        c2 = ((YamlLocation)map.get("c2")).getLocation();
        block = (ItemStack)map.get("block");
    }
    
    @Override
    public void run(Player p, HashMap<String, Integer> vars, HashMap<String, Integer> globals) {
        for (int x = Math.min(c1.getBlockX(), c2.getBlockX()); x <= Math.max(c1.getBlockX(), c2.getBlockX()); x++) {
            for (int y = Math.min(c1.getBlockY(), c2.getBlockY()); y <= Math.max(c1.getBlockY(), c2.getBlockY()); y++) {
                for (int z = Math.min(c1.getBlockZ(), c2.getBlockZ()); z <= Math.max(c1.getBlockZ(), c2.getBlockZ()); z++) {
                    Block b = new Location(c1.getWorld(), x, y, z).getBlock();
                    b.setTypeIdAndData(block.getTypeId(), block.getData().getData(), false);
                }
            }
        }
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("c1", new YamlLocation(c1));
        map.put("c2", new YamlLocation(c2));
        map.put("block", block);
        return map;
    }

    @Override
    public String toString() {
        return "setblocks {" + SkyblockDungeons.prettyLocation(c1) + ", " + SkyblockDungeons.prettyLocation(c2) + ", " + block.getTypeId() + ":" + block.getData().getData() + "}";
    }

}
