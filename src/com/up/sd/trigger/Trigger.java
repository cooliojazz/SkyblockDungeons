package com.up.sd.trigger;

import com.up.sd.trigger.condition.Condition;
import com.up.sd.trigger.action.Action;
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
    String name;
    boolean inverted = false;

    public Trigger(String name) {
        this.name = name;
        actions = new ArrayList<>();
        conditions = new ArrayList<>();
    }
    
    public Trigger(Map<String, Object> map) {
        name = (String)map.get("name");
        actions = (List<Action>)map.get("actions");
        conditions = (List<Condition>)map.get("conditions");
        if ((Boolean)map.get("inverted") != null) inverted = (Boolean)map.get("inverted");
    }
    
    public void execute(Player p, HashMap<String, Integer> vars, HashMap<String, Integer> globals) {
        for (Action a : actions) a.run(p, vars, globals);
    }
    
    public boolean triggered(Player p, HashMap<String, Integer> vars, HashMap<String, Integer> globals) {
        if (inverted) {
            return !conditions.stream().anyMatch(c -> c.passes(p, vars, globals));
        } else {
            return !conditions.stream().anyMatch(c -> !c.passes(p, vars, globals));
        }
    }
    
    public void addAction(Action a) {
        actions.add(a);
    }
    
    public void addCondition(Condition c) {
        conditions.add(c);
    }
    
    public void removeAction(int i) {
        actions.remove(i);
    }
    
    public void removeCondition(int i) {
        conditions.remove(i);
    }

    public String getName() {
        return name;
    }

    public List<Action> getActions() {
        return actions;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public boolean isInverted() {
        return inverted;
    }
    
    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("actions", actions);
        map.put("conditions", conditions);
        map.put("name", name);
        map.put("inverted", inverted);
        return map;
    }
}
