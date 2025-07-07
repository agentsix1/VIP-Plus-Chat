package me.kronix.staffchat.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.kronix.staffchat.StaffChat;


public class cmdChat extends Command {

    private StaffChat staffchat;

    public cmdChat(String name, StaffChat staffchat) {
        super(name);
        this.staffchat = staffchat; 
        this.setDescription("A private chat");
        this.setUsage("/" + name);
    }

    @Override
    public boolean execute(CommandSender sender, String cmd, String[] args) {
        if (sender instanceof Player) {
            String msg = ""; 
            for( String word : args ){
                if (msg.equalsIgnoreCase("")) {
                    msg = word;
                } else {
                    msg = msg + " " + word;
                }
            }
            Player ply = (Player) sender;

            if ( msg.equalsIgnoreCase( "" ) ) { return false; }
		
            if (ply.hasPermission("vippluschat.allowchat")) {
                try {
                    ConfigurationSection chat = staffchat.detectChat(cmd.replace("/", ""));
                    if (chat != null && ply.hasPermission("vippluschat.chat." + chat.getString("permission"))) {
                        if (chat.getBoolean("anti-swear.server.enabled")) {
                            msg = staffchat.antiswearServerCheck(msg, chat);
                                if (msg.equalsIgnoreCase("ERROR: 1337 - Banned Message")) {
                                    staffchat.pMessage(staffchat.getConfig().getString("Messages.antiswear-banned")
                                    .replace("%CHAT_TAG%", chat.getString("layout-tag"))
                                    .replace("%CHAT_NAME%", chat.getString("name")), ply);
                                } else {
                                    staffchat.sendMessages(msg, ply, "vippluschat.chat." + chat.getString("permission"), ".layout", chat);
                                }
                        } else {
                            staffchat.sendMessages(msg, ply, "vippluschat.chat." + chat.getString("permission"), ".layout", chat);
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException catcherror) {
                    ConfigurationSection chat = staffchat.detectChat(cmd.replace("/", ""));
                    if (chat != null) {
                        if (ply.hasPermission("vippluschat.chat." + chat.getString("permission"))) {
                            return true;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        completions.add("<message>");
        return completions;  // Provide tab completion options here
    }
		
}