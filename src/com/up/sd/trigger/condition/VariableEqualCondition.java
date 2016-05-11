package com.up.sd.trigger.condition;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

/**
 *
 * @author Ricky
 */
public class VariableEqualCondition implements Condition {

    String name;
    int val;

    public VariableEqualCondition(String name, int val) {
        this.name = name;
        this.val = val;
    }
    
    public VariableEqualCondition(Map<String, Object> map) {
        name = (String)map.get("name");
        val = (Integer)map.get("val");
    }
    
    @Override
    public boolean passes(Player p, HashMap<String, Integer> vars, HashMap<String, Integer> globals) {
        if (name.startsWith("!")) {
            if (globals.get(name) == null) return val == 0;
            return val == globals.get(name);
        } else {
            if (vars.get(name) == null) return val == 0;
            return val == vars.get(name);
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
        return "varequals {" + name + ", " + val + "}";
    }
}
