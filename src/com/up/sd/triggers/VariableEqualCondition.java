package com.up.sd.triggers;

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

    public VariableEqualCondition(int val) {
        this.val = val;
    }
    
    public VariableEqualCondition(Map<String, Object> map) {
        name = (String)map.get("name");
        val = (Integer)map.get("val");
    }
    
    @Override
    public boolean passes(Player p, HashMap<String, Integer> vars) {
        return val == vars.get(name);
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("val", val);
        return map;
    }
}
