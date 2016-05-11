package com.up.sd.trigger.condition;

import java.util.HashMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

/**
 *
 * @author ricky.t
 */
public interface Condition extends ConfigurationSerializable  {
    
    public boolean passes(Player p, HashMap<String, Integer> vars, HashMap<String, Integer> globals);
    
}
