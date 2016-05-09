package com.up.sd.triggers;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 *
 * @author ricky.t
 */
public class Trigger implements ConfigurationSerializable {
    List<Action> actions;
    List<Condition> conditions;
    HashMap<String, Integer> vars;
    String name;

    public Trigger(String name) {
        this.name = name;
        actions = new ArrayList<>();
        conditions = new ArrayList<>();
    }
    
    public Trigger(Map<String, Object> map) {
        name = (String)map.get("name");
        actions = (List<Action>)map.get("actions");
        conditions = (List<Condition>)map.get("conditions");
    }
    
    public void execute(Player p) {
        for (Action a : actions) a.run(p, vars);
    }
    
    public boolean triggered(Player p) {
        return !conditions.stream().anyMatch(c -> !c.passes(p, vars));
    }
    
    public void addAction(Action a) {
        actions.add(a);
    }
    
    public void addCondition(Condition c) {
        conditions.add(c);
    }

    public String getName() {
        return name;
    }
    
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("actions", actions);
        map.put("conditions", conditions);
        return map;
    }
}
