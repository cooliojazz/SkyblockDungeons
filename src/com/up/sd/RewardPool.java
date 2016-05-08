package com.up.sd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Ricky
 */
public class RewardPool implements ConfigurationSerializable {
    private List<ItemStack> items = new ArrayList<>();
    double money = 0;

    public RewardPool() {
        
    }
    
    public int getSize() {
        return items.size();
    }
    
    public void addItem(ItemStack i) {
        items.add(i);
    }

    public void setMoney(double money) {
        this.money = money;
    }
    
    public void award(Player p, int quantity, Economy econ) {
        ArrayList<ItemStack> reward = new ArrayList<>();
        while (reward.size() < quantity) {
            ItemStack is = items.get((int)(Math.random() * items.size()));
            if (!reward.contains(is)) reward.add(is);
        }
        reward.stream().forEach(System.out::println);
        p.getInventory().addItem(reward.toArray(new ItemStack[0]));
        econ.depositPlayer(p, money);
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("items", items);
        map.put("money", money);
        return map;
    }

    public RewardPool(Map<String, Object> map) {
        items = (List<ItemStack>)map.get("items");
        Double m = (Double)map.get("money");
        money = m == null ? 0 : m;
    }
}
