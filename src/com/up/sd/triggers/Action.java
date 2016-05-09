package com.up.sd.triggers;

import java.util.HashMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

/**
 *
 * @author ricky.t
 */
interface Action extends ConfigurationSerializable {
    
//    enum ACTIONTYPE {
//        TELEPORT,
//        SETBLOCKS,
//        MOVEBLOCKS,
//        GIVE
//    }

    void run(Player p, HashMap<String, Integer> vars);
    
}
 