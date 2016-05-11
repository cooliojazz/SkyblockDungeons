package com.up.sd.trigger.action;

import com.up.sd.SkyblockDungeons;
import com.up.sd.YamlLocation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 *
 * @author Ricky
 */
public class SpawnEntityAction implements Action {
    
    EntityType entity;
    int quantity;
    Location loc;
    int health;
    String name;
    List<PotionEffect> effects;
    ItemStack[] armor;
    ItemStack hand;

    public SpawnEntityAction(EntityType entity, int quantity, Location loc, int health, String name, List<PotionEffect> pes, ItemStack[] armor, ItemStack hand) {
        this.entity = entity;
        this.quantity = quantity;
        this.loc = loc;
        this.health = health;
        this.name = name;
        this.effects = pes;
        this.armor = armor;
        this.hand = hand;
    }
    
    public SpawnEntityAction(Map<String, Object> map) {
        entity = EntityType.valueOf((String)map.get("entity"));
        quantity = (Integer)map.get("quantity");
        loc = ((YamlLocation)map.get("loc")).getLocation();
        health = (Integer)map.get("health");
        name = (String)map.get("name");
        effects = (List<PotionEffect>)map.get("effects");
        armor = ((List<ItemStack>)map.get("armor")).toArray(new ItemStack[0]);
        hand = (ItemStack)map.get("hand");
    }
    
    @Override
    public void run(Player p, HashMap<String, Integer> vars, HashMap<String, Integer> globals) {
        for (int i = 0; i < quantity; i++) {
            LivingEntity e = (LivingEntity)loc.getWorld().spawnEntity(loc, entity);
            for (PotionEffect pe : effects) pe.apply(e);
            if (health != -1) {
                e.setMaxHealth(health);
                e.setHealth(health);
            }
            e.getEquipment().setArmorContents(armor);
            e.getEquipment().setItemInHand(hand);
            e.setCustomName(name);
            e.setCustomNameVisible(true);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("entity", entity.name());
        map.put("quantity", quantity);
        map.put("loc", new YamlLocation(loc));
        map.put("health", health);
        map.put("name", name);
        map.put("effects", effects);
        map.put("armor", armor);
        map.put("hand", hand);
        return map;
    }
    
    @Override
    public String toString() {
        return "spawnentity {" + entity.name() + ", " + quantity + ", " + SkyblockDungeons.prettyLocation(loc) + (health != -1 ? ", " + health : "") + (name != null ? ", " + name : "") + ", " + effects + ", " + Arrays.asList(armor) + ", " + hand + "}";
    }
    
}
