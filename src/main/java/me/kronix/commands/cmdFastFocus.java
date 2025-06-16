package me.kronix.staffchat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.kronix.staffchat.StaffChat;


public class cmdFastFocus extends Command {

    private StaffChat staffchat;
    private Integer chatid;
    public cmdFastFocus(String name, int chatid, StaffChat staffchat) {
        super(name);
        this.chatid = chatid;
        this.staffchat = staffchat; 
        this.setDescription("Quickly Focus on Chat");
        this.setUsage("/" + name);
    }

    @Override
    public boolean execute(CommandSender sender, String cmd, String[] args) {
        if (!(sender instanceof Player) ) { return false; }
            Player ply = (Player) sender;
            int chatID = this.chatid;

            if (chatID > -1) {
                if (ply.hasPermission("vippluschat.chat.focus." + staffchat.getCustomChats().getString(chatID + ".permission")) || ply.hasPermission("vippluschat.chat.focus.*")) {
                    if (staffchat.toggleFocusMode(ply, staffchat.getCustomChats().getString(chatID + ".name"))) {
                        staffchat.pMessage(staffchat.ct(staffchat.getConfig().getString("Messages.focus-mode-toggle").replace("%PLAYER%", ply.getName()).replace("%DISPLAY_NAME%", ply.getDisplayName()).replace("%STATE%", "&aEnabled&r").replace("%CHAT_TAG%", staffchat.getCustomChats().getString(chatID + ".layout-tag")).replace("%CHAT_NAME%", staffchat.getCustomChats().getString(chatID + ".name")).replace("%CHAT_ID%", chatID + "")), ply);
                        return true;
                    } else {
                        staffchat.pMessage(staffchat.ct(staffchat.getConfig().getString("Messages.focus-mode-toggle").replace("%PLAYER%", ply.getName()).replace("%DISPLAY_NAME%", ply.getDisplayName()).replace("%STATE%", "&cDisabled&r").replace("%CHAT_TAG%", staffchat.getCustomChats().getString(chatID + ".layout-tag")).replace("%CHAT_NAME%", staffchat.getCustomChats().getString(chatID + ".name")).replace("%CHAT_ID%", chatID + "")), ply);
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
		
}