package com.up.sd.triggers;

import java.util.HashMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

/**
 *
 * @author ricky.t
 */
interface Condition extends ConfigurationSerializable  {
    
    public boolean passes(Player p, HashMap<String, Integer> vars);
    
}
