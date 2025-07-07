package me.kronix.staffchat.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.kronix.staffchat.StaffChat;


public class cmdFastFocus extends Command {

    private StaffChat staffchat;
    private ConfigurationSection chat;
    public cmdFastFocus(String name, ConfigurationSection chat, StaffChat staffchat) {
        super(name);
        this.chat = chat;
        this.staffchat = staffchat; 
        this.setDescription("Quickly Focus on Chat");
        this.setUsage("/" + name);
    }

    @Override
    public boolean execute(CommandSender sender, String cmd, String[] args) {
        if (!(sender instanceof Player) ) { return false; }
            Player ply = (Player) sender;
            ConfigurationSection chat = this.chat;

            if (chat != null) {
                if (ply.hasPermission("vippluschat.chat.focus." + chat.getString("permission")) || ply.hasPermission("vippluschat.chat.focus.*")) {
                    if (staffchat.toggleFocusMode(ply, chat.getString("name"))) {
                        staffchat.pMessage(staffchat.ct(staffchat.getConfig().getString("Messages.focus-mode-toggle").replace("%PLAYER%", ply.getName())
                        .replace("%DISPLAY_NAME%", ply.getDisplayName()).replace("%STATE%", "&aEnabled&r")
                        .replace("%CHAT_TAG%", chat.getString("layout-tag"))
                        .replace("%CHAT_NAME%", chat.getString("name"))), ply);
                        return true;
                    } else {
                        staffchat.pMessage(staffchat.ct(staffchat.getConfig().getString("Messages.focus-mode-toggle").replace("%PLAYER%", ply.getName())
                        .replace("%DISPLAY_NAME%", ply.getDisplayName()).replace("%STATE%", "&cDisabled&r").replace("%CHAT_TAG%", chat.getString("layout-tag"))
                        .replace("%CHAT_NAME%", chat.getString("name"))), ply);
                        return true;
                    }
                } else {
                    
                }
            } else {
                ply.sendMessage(staffchat.ct(staffchat.getConfig().getString("Messages.no-permissions")));
                return true;
            }
            return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        completions.add("");
        return completions;  // Provide tab completion options here
    }
		
}