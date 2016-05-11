package com.up.sd.trigger.action;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

/**
 *
 * @author Ricky
 */
public class AddVariableAction implements Action {
    
    String name;
    int val;

    public AddVariableAction(String name, int val) {
        this.name = name;
        this.val = val;
    }
    
    public AddVariableAction(Map<String, Object> map) {
        name = (String)map.get("name");
        val = (Integer)map.get("val");
    }
    
    @Override
    public void run(Player p, HashMap<String, Integer> vars, HashMap<String, Integer> globals) {
        if (name.startsWith("!")) {
            if (!globals.containsKey(name)) {
                globals.put(name, val);
            } else {
                globals.put(name, globals.get(name) + val);
            }
        } else {
            if (!vars.containsKey(name)) {
                vars.put(name, val);
            } else {
                vars.put(name, vars.get(name) + val);
            }
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
        return "addvar {" + name + ", " + val + "}";
    }

}