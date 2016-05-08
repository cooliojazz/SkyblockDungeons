package com.up.sd;

import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 *
 * @author ricky.t
 */
public class Trigger {
    ArrayList<Action> actions;
    ArrayList<Condition> conditions;
    
    public void execute() {
        for (Action a : actions) a.run();
    }
    
    public boolean triggered(Player p) {
        return conditions.stream().anyMatch(c -> c.passes(p));
    }
}
