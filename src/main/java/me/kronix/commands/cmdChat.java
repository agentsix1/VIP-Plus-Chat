package me.kronix.staffchat.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
                    int chatID = staffchat.detectChatID(cmd.replace("/", ""));
                    if (chatID > -1 & ply.hasPermission("vippluschat.chat." + staffchat.getCustomChats().getString(chatID + ".permission"))) {
                        if (staffchat.serverAntiSwear(chatID)) {
                            msg = staffchat.antiswearServerCheck(msg, chatID);
                                if (msg.equalsIgnoreCase("ERROR: 1337 - Banned Message")) {
                                    staffchat.pMessage(staffchat.getConfig().getString("Messages.antiswear-banned").replace("%CHAT_TAG%", staffchat.getCustomChats().getString(chatID + ".layout-tag")).replace("%CHAT_NAME%", staffchat.getCustomChats().getString(chatID + ".name")), ply);
                                } else {
                                    staffchat.sendMessages(msg, ply, "vippluschat.chat." + staffchat.getCustomChats().getString(chatID + ".permission"), chatID + ".layout", chatID);
                                }
                        } else {
                            staffchat.sendMessages(msg, ply, "vippluschat.chat." + staffchat.getCustomChats().getString(chatID + ".permission"), chatID + ".layout", chatID);
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException catcherror) {
                    int chatID = staffchat.detectChatID(cmd.replace("/", ""));
                    if (chatID > -1) {
                        if (ply.hasPermission("vippluschat.chat." + staffchat.getCustomChats().getString(chatID + ".permission"))) {
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