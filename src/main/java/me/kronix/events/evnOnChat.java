package me.kronix.staffchat.events;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.kronix.staffchat.StaffChat;

public class evnOnChat implements Listener {

    private final StaffChat staffchat;

    public evnOnChat(StaffChat plugin) {
        this.staffchat = plugin;
    }
    //---- On player chat this handles Symbol Chat and Anti-swear of messaging -- Removed 7/7/2025 0.8.7
    /*
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
    */
    //---- On player chat this handles Symbol Chat and Anti-swear of messaging -- Updated 7/7/2025 0.8.7
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String msg = event.getMessage();
        Player p = event.getPlayer();
		
		if (p.hasPermission("vippluschat.allowchat")) {
			
			try {
                String symbol = "";
                ConfigurationSection chat = null;
                for (String key : staffchat.getCustomChats().getKeys( false )) {
                    ConfigurationSection tchat = staffchat.getCustomChats().getConfigurationSection( key );
                    if ( tchat.getString("chat-symbol" ).equalsIgnoreCase( "none" ) ) { continue; }
                    if ( msg.startsWith( tchat.getString("chat-symbol" ) ) ) {
                        symbol = tchat.getString("chat-symbol" );
                        chat = tchat;
                        break;
                    }
                }

                if ( chat == null ) { return; }
				
				if (p.hasPermission("vippluschat.chat." + chat.getString("permission"))) {
						msg = msg.replace( symbol, "" );
						event.setCancelled(true);
						if (chat.getBoolean("anti-swear.server.enabled")) {
							 msg = staffchat.antiswearServerCheck(msg, chat);
								if (msg.equalsIgnoreCase("ERROR: 1337 - Banned Message")) {
									staffchat.pMessage(staffchat.getConfig().getString("Messages.antiswear-banned")
                                    .replace("%CHAT_TAG%", chat.getString("layout-tag"))
                                    .replace("%CHAT_NAME%", chat.getString("name")), p);
								} else {
									staffchat.sendMessages(msg, p, "vippluschat.chat." + chat.getString("permission"), "layout", chat);
								}
						} else {
							staffchat.sendMessages(msg, p, "vippluschat.chat." + chat.getString("permission"), "layout", chat);
						}
				}
			} catch (ArrayIndexOutOfBoundsException catcherror) {
			}
		}
    }
}
