package com.up.sd;

import com.up.sd.triggers.Action;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ricky.t
 */
public class SetBlockAction implements Action {

    Location loc;

    public SetBlockAction(String name, int val) {
        this.name = name;
        this.val = val;
    }
    
    public SetBlockAction(Map<String, Object> map) {
        name = (String)map.get("name");
        val = (Integer)map.get("val");
    }
    
    @Override
    public void run(Player p, HashMap<String, Integer> vars) {
        vars.put(name, val);
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("val", val);
        return map;
    }

}
