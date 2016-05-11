package com.up.sd.trigger.action;

import com.up.sd.SkyblockDungeons;
import com.up.sd.YamlLocation;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author Ricky
 */
public class MoveBlocksAction implements Action {

    Location loc;
    Location c1;
    Location c2;

    public MoveBlocksAction(Location loc, Location c1, Location c2) {
        this.loc = loc;
        this.c1 = c1;
        this.c2 = c2;
    }
    
    public MoveBlocksAction(Map<String, Object> map) {
        loc = ((YamlLocation)map.get("loc")).getLocation();
        c1 = ((YamlLocation)map.get("c1")).getLocation();
        c2 = ((YamlLocation)map.get("c2")).getLocation();
    }
    
    @Override
    public void run(Player p, HashMap<String, Integer> vars, HashMap<String, Integer> globals) {
        int xs = Math.min(c1.getBlockX(), c2.getBlockX());
        int ys = Math.min(c1.getBlockY(), c2.getBlockY());
        int zs = Math.min(c1.getBlockZ(), c2.getBlockZ());
        int xlen = Math.abs(c1.getBlockX() - c2.getBlockX()) + 1;
        int ylen = Math.abs(c1.getBlockY() - c2.getBlockY()) + 1;
        int zlen = Math.abs(c1.getBlockZ() - c2.getBlockZ()) + 1;
        BlockCopy[][][] blocks = new BlockCopy[xlen][ylen][zlen];
        for (int x = xs; x < xs + xlen; x++) {
            for (int y = ys; y < ys + ylen; y++) {
                for (int z = zs; z < zs + zlen; z++) {
                    Block b = new Location(c1.getWorld(), x, y, z).getBlock();
                    blocks[x - xs][y - ys][z - zs] = new BlockCopy(b.getTypeId(), b.getData());
                    b.setType(Material.AIR);
                }
            }
        }
        for (int x = loc.getBlockX(); x < loc.getBlockX() + xlen; x++) {
            for (int y = loc.getBlockY(); y < loc.getBlockY() + ylen; y++) {
                for (int z = loc.getBlockZ(); z < loc.getBlockZ() + zlen; z++) {
                    Block b = new Location(c1.getWorld(), x, y, z).getBlock();
                    b.setTypeIdAndData(blocks[x - loc.getBlockX()][y - loc.getBlockY()][z - loc.getBlockZ()].id, blocks[x - loc.getBlockX()][y - loc.getBlockY()][z - loc.getBlockZ()].data, false);
                }
            }
        }
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("loc", new YamlLocation(loc));
        map.put("c1", new YamlLocation(c1));
        map.put("c2", new YamlLocation(c2));
        return map;
    }

    @Override
    public String toString() {
        return "moveblocks {" + SkyblockDungeons.prettyLocation(loc) + ", " + SkyblockDungeons.prettyLocation(c1) + ", " + SkyblockDungeons.prettyLocation(c2) + "}";
    }
    
    class BlockCopy {
        int id;
        byte data;

        public BlockCopy(int id, byte data) {
            this.id = id;
            this.data = data;
        }
        
    }
    
}
