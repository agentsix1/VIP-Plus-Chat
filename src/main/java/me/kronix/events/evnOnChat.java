package me.kronix.staffchat.events;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.kronix.staffchat.StaffChat;

public class evnOnChat implements Listener {

    private final StaffChat staffchat;

    public evnOnChat(StaffChat plugin) {
        this.staffchat = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String msg = event.getMessage();
        Player p = event.getPlayer();
		
		if (p.hasPermission("vippluschat.allowchat")) {
			
			try {

                int chatID = -1;
                String symbol = "";

                int i = 0;
                do {
                    if ( staffchat.getCustomChats().getString( i + ".chat-symbol" ).equalsIgnoreCase( "none" ) ) { continue; }
                    if ( msg.startsWith( staffchat.getCustomChats().getString( i + ".chat-symbol" ) ) ) {
                        symbol = staffchat.getCustomChats().getString( i + ".chat-symbol" );
                        chatID = i;
                        break;
                    }
                } while (i++ < staffchat.getCustomChats().getInt("Chat Count") - 1);

                if ( chatID == -1 ) { return; }
				
				if (p.hasPermission("vippluschat.chat." + staffchat.getCustomChats().getString(chatID + ".permission"))) {
						msg = msg.replace( symbol, "" );
						event.setCancelled(true);
						if (staffchat.serverAntiSwear(chatID)) {
							 msg = staffchat.antiswearServerCheck(msg, chatID);
								if (msg.equalsIgnoreCase("ERROR: 1337 - Banned Message")) {
									staffchat.pMessage(staffchat.getConfig().getString("Messages.antiswear-banned").replace("%CHAT_TAG%", staffchat.getCustomChats().getString(chatID + ".layout-tag")).replace("%CHAT_NAME%", staffchat.getCustomChats().getString(chatID + ".name")), p);
								} else {
									staffchat.sendMessages(msg, p, "vippluschat.chat." + staffchat.getCustomChats().getString(chatID + ".permission"), chatID + ".layout", chatID);
								}
						} else {
							staffchat.sendMessages(msg, p, "vippluschat.chat." + staffchat.getCustomChats().getString(chatID + ".permission"), chatID + ".layout", chatID);
						}
				}
			} catch (ArrayIndexOutOfBoundsException catcherror) {
			}
		}
    }
}
