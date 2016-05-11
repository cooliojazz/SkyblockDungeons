package com.up.sd.trigger.action;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Ricky
 */
public class MessageAction implements Action {

    String msg;

    public MessageAction(String msg) {
        this.msg = msg;
    }
    
    public MessageAction(Map<String, Object> map) {
        msg = (String)map.get("msg");
    }
    
    @Override
    public void run(Player p, HashMap<String, Integer> vars, HashMap<String, Integer> globals) {
        p.sendMessage(ChatColor.DARK_AQUA + "[Dun] " + msg);
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("msg", msg);
        return map;
    }

    @Override
    public String toString() {
        return "message {" + msg + "}";
    }

}
