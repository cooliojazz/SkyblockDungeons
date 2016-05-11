package com.up.sd.trigger.action;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

/**
 *
 * @author Ricky
 */
public class EffectAction implements Action {

    PotionEffect effect;

    public EffectAction(PotionEffect effect) {
        this.effect = effect;
    }
    
    public EffectAction(Map<String, Object> map) {
        effect = (PotionEffect)map.get("effect");
    }
    
    @Override
    public void run(Player p, HashMap<String, Integer> vars, HashMap<String, Integer> globals) {
        p.addPotionEffect(effect);
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("effect", effect);
        return map;
    }

    @Override
    public String toString() {
        return "effect {" + effect + "}";
    }

}