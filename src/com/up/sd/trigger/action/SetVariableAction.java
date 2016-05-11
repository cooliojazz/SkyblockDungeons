package com.up.sd.trigger.action;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

/**
 *
 * @author Ricky
 */
public class SetVariableAction implements Action {
    
    String name;
    int val;

    public SetVariableAction(String name, int val) {
        this.name = name;
        this.val = val;
    }
    
    public SetVariableAction(Map<String, Object> map) {
        name = (String)map.get("name");
        val = (Integer)map.get("val");
    }
    
    @Override
    public void run(Player p, HashMap<String, Integer> vars, HashMap<String, Integer> globals) {
        if (name.startsWith("!")) {
            globals.put(name, val);
        } else {
            vars.put(name, val);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("val", val);
        return map;
    }

    @Override
    public String toString() {
        return "setvar {" + name + ", " + val + "}";
    }

}