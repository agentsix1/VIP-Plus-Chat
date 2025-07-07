package me.kronix.staffchat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.kronix.staffchat.commands.cmdChat;
import me.kronix.staffchat.commands.cmdFastFocus;
import me.kronix.staffchat.events.evnOnChat;


/*
	Change Log v0.8.6 - Changed Oct 29th 2024
	- Added tab complete to all custom chats
	- Added tab complete to all commands
	- Fixed checkver to use proper updated url (https)
	- Added ability to add custom command for quickly focusing chat (fastfocus-command)
	- Complete recode of the entire custom chat system to properly register commands
	- You can now use symbols defined in chat.yml to quickly type in chat
	- /vpc info now displays your information such as blacklist state, focused chats, ignored players, ignored chats
	- /vpc info {player} now displays the players information such as blacklist state, focused chats, ignored players, ignored chats
	- /vpc focused will now display what channel you are currently focused on
 */

/* List of commands - Added v0.8.2 - Updated v0.8.5 - 2/14/2017, 2/15/2017
    /{chat} {message} - The chat command is defied with in the chat.yml config. This command allows you to chat in the chats --- v0.1
	Permission:
	- vippluschat.chat.{permission} - The permission is defined in the chat.yml
	- vippluschat.chat.* - Gives you permission to all the chats
	- vippluschat.allowchat - This is required on top of the permission used for the indivual chats.
    
    /vpc help - This allows the player to see any commands he can use with this plugin. --- (As bad as it sounds this was added v0.8.5....) v0.8.5
    Permission:
    - vippluschat.user.help
    
    /vpc pstatus | Optional 1: {blacklisted/ignored} | Optionatl 2: {chat} - This is used to check the players current status. He can see what he may be blacklisted or ignoring. He may also specify the chat if he wants. --- v0.8.5 - 2/23/2017
    Permission:
    - vippluschat.user.pstatus.blacklisted
    - vippluschat.user.pstatus.ignored
    - vippluschat.user.pstatus
    
	/vpc info | Optional 1: {player} - This will show the info for the player defined or for you --- v0.8.6 - 10/29/2024
	Permission:
	- vippluschat.user.info - For you only
	- vippluschat.admin.info - For other players

    /vpc focus {chat} - Toggles a player in/out focus mode for the specified chat. --- v0.8.5 - 2/14/2017
    Permission:
    - vippluschat.chat.focus.{chat}
    - vippluschat.chat.focus.*
    
    /vpc ignoreplayer {player/clear} | Optional: {+,add,rem,remove,del,delete,-,clear} - Allows a player to ignore another player inside chats. --- v0.8.5 2/27/2017
    Permission:
    - vippluschat.user.playerignore
    
    /vpc listplayers | Optional: {#} - Allows players to view the list of people they are ignoring in private chats --- v0.8.5 2/27/2017
    Permission:
    - vippluschat.user.playerignore.list
    
    /vpc toggleswear {chat} - Toggles the swear filter for this player for the speicified player --- v0.8.5
    Permission:
    - vippluschat.chat.filter.{chat}
    - vippluschat.chat.filter.*
    - vippluschat.chat.filter.toggle.{chat}
    - vippluschat.chat.filter.toggle.*
    
    /vpc modifyswear {add,+,rem,del,remove,delete,clear} {chat/shared} {word} | Optional: {replacement word char max set in config} - Sets the word dictionary for this players dictionary. Which allows him to edit it --- v0.8.5
    Permission:
    - vippluschat.chat.filter.{chat}
    - vippluschat.chat.filter.*
    - vippluschat.chat.filter.modify.{chat}
    - vippluschat.chat.filter.modify.*    
    
    /vpc listswear {chat/shared} - Lists all the words in the players dictionary of his swear filter --- v0.8.5
    Permission:
    - vippluschat.chat.filter.{chat}
    - vippluschat.chat.filter.*
    - vippluschat.chat.filter.list.{chat}
    - vippluschat.chat.filter.list.*
    
    /vpc antiswear {chat} - Gives the player his basic info reguarding the player antiswear system --- v0.8.5
    Permission:
    - vippluschat.chat.filter.{chat}
    - vippluschat.chat.filter.*
    - vippluschat.chat.filter.stats.{chat}
    - vippluschat.chat.filter.stats.*
    
   	/vpc cic toggle - Allows you to toggle the state of Can Ignore Chats. --- v0.8.5
   	Permission:
   	- vippluschat.admin.cic-toggle
   	
   	/vpc ctic toggle - Allows you to toggle the state of Can talk in chats. --- v0.8.5
   	Permission:
   	- vippluschat.admin.ctic-toggle
    
    /vpc cvc toggle - Allows you to toggle the state of Can view Chats. --- v0.8.5
   	Permission:
   	- vippluschat.admin.cvc-toggle
   	
   	/vpc cic {add/remove} {chat} - Allows you to add or remove players from can ignore chats. --- v0.8.5
   	Permission:
   	- vippluschat.admin.cic-modify
   	
   	/vpc ctic {add/remove} {chat} - Allows you to add or remove players from can talk in chats. --- v0.8.5
   	Permission:
   	- vippluschat.admin.ctic-modify
   	
   	/vpc cvc {add/remove} {chat} - Allows you to add or remove players from can view chats. --- v0.8.5
   	Permission:
   	- vippluschat.admin.cvc-modify
   	
    /vpc createchat - Enters the current player into create custom chat mode. Only one player can be in this mode at a time. How ever after the defined amout of time the player will be kicked from the mode. --- v0.8.5
    Permission:
    - vippluschat.admin.create-chat
    
    /vpc swear toggle {chat} - Enables/Disables the player/server swear filter for  the chat specified --- v0.8.5 2/27/2017 3/1/2017
    Permission:
    - vippluschat.admin
    - vippluschat.admin.swear-toggle-server
   	
    /vpc swear toggle {username} {chat} - Enables/Disables the players specified swear filter for  the chat specified --- v0.8.5 
    Permission:
    - vippluschat.admin
    - vippluschat.admin.swear-toggle-player
    
    /vpc swear {username} - Shows the toggle state of the player specified's swear filter. As well it will show the length of all the players swear dictionary. --- v0.8.5
    Permission:
    - vippluschat.admin
    - vippluschat.admin.swear-info
    
    /vpc swear {add,+,-,del,delte,rem,remove,clear} {chat} {word} | Optional: {replacement word} - Allows you to modify the server swear directory that is specified. --- v0.8.5 3/1/2017
    Permission:
    - vippluschat.admin
    - vippluschat.admin.swear-modify
    
    /vpc swear list {chat} - Shows the list of words in the chat specified's dictionary. --- v0.8.5
    Permission:
    - vippluschat.admin
    - vippluschat.admin.swear-list
    
	/vpc add chat - Puts you into the add custom chat mode --- v0.8.5
    Permission:
    - vippluschat.admin
    - vippluschat.admin.addchat
    	
	/vpc checkver - Check the latest version of plugin and what the current release is
	Permission:
	- vippluschat.admin
	- vippluschat.admin.checkversion
	
	/vpc reload - Reload the Chat.yml and Config.yml files
	Permission:
	- vippluschat.admin
	- vippluschat.admin.reload
	
	/vpc {ignore/unignore} {chat} - Set your self to ignore/unignore the specified chat
	Permission:
	- vippluschat.chat.ignore
	
	/vpc ignored {chat} | optional: {show/#/all} - View the list of ignored users with in the chat specified
	Permission:
	- vippluschat.admin
	- vippluschat.admin.ignored
	
	/vpc toggle {chat} - Enable/Disable the chat specified
	Permission:
	- vippluschat.admin
	- vippluschat.admin.toggle
	- vippluschat.toggle.{permission} - The permission is defined in the chat.yml
	
	/vpc blacklist {+/add/del/rem/delete/remove/-/clear} {username} - Add/remove/clear the User{s} to/from the blacklist
	Permission:
	- vippluschat.admin
	- vippluschat.admin.blacklist
	
	/vpc {fui/forceunignore} {chat} {username} - Force the user to uningore the chat specified
	Permission:
	- vippluschat.admin
	- vippluschat.admin.forceunignore
	
	/vpc blacklisted | Optional: {all/#/show} - View the blacklisted users
	Permission:
	- vippluschat.admin
	- vippluschat.admin.blacklisted
	
	/vpc colors {chat} - The command allows you to see what colors are enabled for this command --- v0.8.2
	Permission:
	- vippluschat.admin
	- vippluschat.admin.viewcolors
	
	/vpc colors toggle {chat} {color-code} - This will allow you to toggle the state of the color code entered --- v0.8.2
	Permission:
	- vippluschat.admin
	- vippluschat.admin.togglecolors

 */


public class StaffChat extends JavaPlugin implements Listener, TabCompleter  {
	//---- This is used to easily grab the current version of the program - Added v0.8
public String version = "v0.8.6";
public HashMap<Player, String> focus = new HashMap<>();
public final String blacklist_location = "\\"+"files"+"\\"+"blacklist.yml";
public final String ignorelist_location = "\\"+"files"+"\\"+"ignorelist.yml";
//---- End Version String
	@Override
	public void onEnable(){
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		checkOldConfgAndPort();
		loadConfiguration();
		loadChats();
		loadOther();
		loadChatCommands();
		loadChatFocusCommands();
		getServer().getPluginManager().registerEvents(new evnOnChat(this), this);
	}
	//---- This is used to create the default config.yml and was added in v0.6 
	public void loadConfiguration(){
	    getConfig().options().copyDefaults(true);
	    saveConfig();   
	
	}
	//---- End config.yml
	//---- This is used to create the default chat.yml and was added in v0.8.2 to allow for custom chats and to clean up the original config.yml
	public void loadChats(){
	    getCustomChats().options().copyDefaults(true);
	    saveCustomConfig();
	}
	//---- End Chat.yml
	
	@SuppressWarnings("deprecation")
	public void loadOther() {
		getOther(getConfig().getString("Other.focus.location")).options().copyDefaults(true);
		saveOther(getConfig().getString("Other.focus.location"));
		 getOther(blacklist_location).options().copyDefaults(true);
		 saveOther(blacklist_location);
		 tcOther(blacklist_location, "b");
		 getOther(ignorelist_location).options().copyDefaults(true);
		 saveOther(ignorelist_location);
		 tcOther(ignorelist_location, "i");
		 int i = 0;
		 do {
			 if (getCustomChats().getBoolean(i + ".anti-swear.enabled")) {
				 getOther(getCustomChats().getString(i + ".anti-swear.server.file-name").replace("%CHAT_NAME%", getCustomChats().getString(i + ".name"))).options().copyDefaults(true);
				 saveOther(getCustomChats().getString(i + ".anti-swear.server.file-name").replace("%CHAT_NAME%", getCustomChats().getString(i + ".name")));
				 tcOther(getCustomChats().getString(i + ".anti-swear.server.file-name").replace("%CHAT_NAME%", getCustomChats().getString(i + ".name")), "d");
			 }
			 i++;
		 } while (i < getCustomChats().getInt("Chat Count"));
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (getOther(getConfig().getString("Other.focus.location")).contains(p.getUniqueId().toString())) {
				focus.put(p, getOther(getConfig().getString("Other.focus.location")).getString(p.getUniqueId().toString() + ".chat"));
			}
		}
		checkPortOldBlacklistIgnore();
	    
	}
	
	//---- Catches messages on the outbound for focus mode - v0.8.5 -- Updated v0.8.7
	@EventHandler
	public void onPlayerMessage(AsyncPlayerChatEvent e) {
		if (inFocus(e.getPlayer())) {
			e.setCancelled(true);
			
			ConfigurationSection chat = detectChat(focus.get(e.getPlayer()));
			if ( chat == null ) { return; }
			
			String msg = e.getMessage();
			if (chat.getBoolean( "anti-swear.server.enabled" )) {
				 msg = antiswearServerCheck(msg, chat);
					if (msg.equalsIgnoreCase("ERROR: 1337 - Banned Message")) {
						pMessage(getConfig().getString("Messages.antiswear-banned").replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")), e.getPlayer());
					} else {
						sendMessages(msg, e.getPlayer(), "vippluschat.chat." + chat.getString("permission"), ".layout", chat);	
					}
			} else {
				sendMessages(msg, e.getPlayer(), "vippluschat.chat." + chat.getString("permission"), ".layout", chat);
			}
			
		}
	}
	
	//---- Catch messages on outbound
	
	//---- This is just a little code that I added so when I join the server I can view if you are using my plugin and if so what version - Added v0.8
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = (Player) e.getPlayer();
		if (p.getUniqueId().toString().equalsIgnoreCase("87acd853-989b-496f-8eb0-9744f160bf15") && p.getName().equals("agentsix1")) { p.sendMessage(ct("&7Oh hey there. It appears this server is using your plugin!. The current version is &9" + version)); System.out.println("The plugin dev for VIP+ Chat has just joined your server! - agentsix1 (uuid: 87acd853-989b-496f-8eb0-9744f160bf15)");}
		if (getOther(getConfig().getString("Other.focus.location")).contains(p.getUniqueId().toString())) {
			focus.put(p, getOther(getConfig().getString("Other.focus.location")).getString(p.getUniqueId().toString() + ".chat"));
		}
	}
	//---- End Do you own me?
	//---- Detect Custom Chat - This was added to allow custom commands with custom chats! - Added v0.8.2 - Removed v0.8.6
	@EventHandler
	public void detectCustomChat(PlayerCommandPreprocessEvent e) {
		/* Removed for version 1.16+ due to tab completion being added - v0.8.6 10-23-2024
		Player p = e.getPlayer();
		
		if (p.hasPermission("vippluschat.allowchat")) {
			
			try {
				String[] cmd = e.getMessage().split(" ");
				int chatID = detectChatID(cmd[0].replace("/", ""));
				if (chatID > -1 & p.hasPermission("vippluschat.chat." + getCustomChats().getString(chatID + ".permission"))) {
						String msg = ""; 
						for(int i = 0; i < cmd.length; i++){
		    			    if (i != 0) {
		    			    	String fmsg = cmd[i] + " ";
		    			    	msg = msg + fmsg;
		    			    }
		    			}
						e.setCancelled(true);
						if (serverAntiSwear(chatID)) {
							 msg = antiswearServerCheck(msg, chatID);
								if (msg.equalsIgnoreCase("ERROR: 1337 - Banned Message")) {
									pMessage(getConfig().getString("Messages.antiswear-banned").replace("%CHAT_TAG%", getCustomChats().getString(chatID + ".layout-tag")).replace("%CHAT_NAME%", getCustomChats().getString(chatID + ".name")), e.getPlayer());
								} else {
									sendMessages(msg, e.getPlayer(), "vippluschat.chat." + getCustomChats().getString(chatID + ".permission"), chatID + ".layout", chatID);
								}
						} else {
							sendMessages(msg, e.getPlayer(), "vippluschat.chat." + getCustomChats().getString(chatID + ".permission"), chatID + ".layout", chatID);
						}
				}
			} catch (ArrayIndexOutOfBoundsException catcherror) {
				int chatID = detectChatID(e.getMessage().replace("/", ""));
				if (chatID > -1) {
					if (p.hasPermission("vippluschat.chat." + getCustomChats().getString(chatID + ".permission"))) {
						e.setCancelled(true);
					}
				}
			}
		}
		*/
	}

	//---- Load all the custom chat commands so they are properly registered -- Added v0.8.6 - Oct 28th 2024
	private void loadChatCommands() {
		for ( String key : getCustomChats().getKeys(false)) {
			for ( String cmd : getCustomChats().getStringList(key + ".command")) {
				registerChatCommand( cmd );
			}
		}
	}

	private void registerChatCommand(String commandName) {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            // Create a new instance of the dynamic command with shared tab completions
            cmdChat command = new cmdChat(commandName, this);

            // Register the command
            commandMap.register("myplugin", command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	//---- End Load/Register Chat Commands

	//---- Load all the custom fast focus commands. -- Added v0.8.6 - Oct 28th 2024

	private void loadChatFocusCommands() {
		for (String key : getCustomChats().getKeys(false)) {
			ConfigurationSection chat = getCustomChats().getConfigurationSection(key);
			String fc = chat.getString("fastfocus-command");
			registerFocusChatCommand( fc, chat );
		}
	}

	private void registerFocusChatCommand(String commandName, ConfigurationSection chat) {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap;
            commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            // Create a new instance of the dynamic command with shared tab completions
            cmdFastFocus command = new cmdFastFocus(commandName, chat, this);

            // Register the command
            commandMap.register("myplugin", command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	//---- End Load/Register Fast Focus Commands


	//---- End Detect Custom Chat
	//---- In Focus Mode - v0.8.5 - 2/14/17
	public boolean inFocus(Player p) {
		if (focus.containsKey(p)) {
			return true;
		} else {
			return false;
		}
	}
	//---- End In Focus Mode	

	public List<String> getAllChatList() {
		List<String> chats = new ArrayList<String>();
		for ( String key : getCustomChats().getKeys(false) ) {
			chats.add( getCustomChats().getString(key + ".name") );
		}
		return chats;
	}

	public List<String> getChatList( Player p ) {
		List<String> chats = new ArrayList<String>();
		for ( String key : getCustomChats().getKeys(false) ) {
			if (p.hasPermission("vippluschat.chat.*") || p.hasPermission("vippluschat.chat." + getCustomChats().getString(key + ".permission"))) {
				chats.add( getCustomChats().getString(key + ".name") );
			}
		}
		return chats;
	}
	//---- Detect Chat ID - This was added to detect what what chat you are trying to reach - Added v0.8.2
	//---- Removed during v0.8.7 to use chat name instead
	/*
	public int detectChatID(String j) {
		int i = 0;
		do {
			if (getCustomChats().getString(i + ".name").toLowerCase().equalsIgnoreCase(j.toLowerCase())) {
				return i;
			}
	 		for ( String cmd : getCustomChats().getStringList(i + ".command")) {
				if (cmd.equalsIgnoreCase(j)) {
					return i;
				}
			}
		} while (i++ < getCustomChats().getInt("Chat Count") - 1);
		return -1;
	}
	*/
	//---- End Detect Chat ID
	
	//----- Detect Chat Name - This was added to detect what chat you are trying to reach - Added v0.8.7
	public ConfigurationSection detectChat( String j ) {
		for (String key : getCustomChats().getKeys( false )) {
			ConfigurationSection chat = getCustomChats().getConfigurationSection( key );
			if ( chat.getString( "name" ).equalsIgnoreCase(j)) return chat;
			for ( String cmd : chat.getStringList( "command" ) ) {
				if (cmd.equalsIgnoreCase( j ) ) return chat;
			}

		}
		return null;
	}
	//---- End Detect Chat Name



	public String pstatusGetChats(Player p, ConfigurationSection chat) {
		
		
		//---- Creates a list of Chat ID's -- Removed 7/7/2025 v0.8.7
		/*
		int i = 0;
		String out = "";
		List<Integer> permissions = new ArrayList<Integer>(); 
		do {
			if (p.hasPermission("vippluschat.chat.ignore") && (p.hasPermission("vippluschat.chat.*") || p.hasPermission("vippluschat.chat." + chat.getString(i + ".permission")))) {
				permissions.add(i);
			}
			i++; 
		} while (i < getCustomChats().getInt("Chat Count"));
		*/
		//---- Cycles through all the chats and determines the status using the ID -- Removed 7/7/2025 v0.8.7
		/*
		if (permissions.size() > 0) {
			for (int c : permissions) {
				if (chat != -1) {
					String stats = "&cfalse";
					if (inIgnorelist(p.getUniqueId().toString(), p.getName(), c)) { stats = "&atrue"; } 
					if (chat == -2) {
						
						if (out.equalsIgnoreCase("")) {
							out = getConfig().getString("Messages.pstatus-ignored-items").replace("%CHAT_TAG%", getCustomChats().getString(c + ".layout-tag")).replace("%CHAT_NAME%", getCustomChats().getString(c + ".name")).replace("%CHAT_ID%", c + "").replace("%STATUS%", stats).replace("%NL%", "\n");
						} else {
							out = out + getConfig().getString("Messages.pstatus-ignored-items").replace("%CHAT_TAG%", getCustomChats().getString(c + ".layout-tag")).replace("%CHAT_NAME%", getCustomChats().getString(c + ".name")).replace("%CHAT_ID%", c + "").replace("%STATUS%", stats).replace("%NL%", "\n");
						}
					} else if (chat > -1){
						if (p.hasPermission("vippluschat.chat.ignore") && (p.hasPermission("vippluschat.chat.*") || p.hasPermission("vippluschat.chat." + getCustomChats().getString(chat + ".permission")))) {
							stats = "&cfalse";
							if (inIgnorelist(p.getUniqueId().toString(), p.getName(), chat)) { stats = "&atrue"; } 
							if (out.equalsIgnoreCase("")) {
								out = getConfig().getString("Messages.pstatus-ignored-items").replace("%CHAT_TAG%", getCustomChats().getString(chat + ".layout-tag")).replace("%CHAT_NAME%", getCustomChats().getString(chat + ".name")).replace("%CHAT_ID%", chat + "").replace("%STATUS%", stats).replace("%NL%", "\n");
							} else {
								out = out + getConfig().getString("Messages.pstatus-ignored-items").replace("%CHAT_TAG%", getCustomChats().getString(chat + ".layout-tag")).replace("%CHAT_NAME%", getCustomChats().getString(chat + ".name")).replace("%CHAT_ID%", chat + "").replace("%STATUS%", stats);
							}
						} else {
							pMessage(getConfig().getString("Messages.no-permissions"), p);
							return "no";
						}
						
					}
				} else {
					
				}
			}
		}
		*/
		
		//---- Creates a list of Chat Configuration Section's -- Added 7/7/2025 v0.8.7
		String out = "";

		List<ConfigurationSection> permissions = new ArrayList<ConfigurationSection>(); 
		for ( String key : getCustomChats().getKeys( false ) ) {
			if (p.hasPermission("vippluschat.chat.ignore") && (p.hasPermission("vippluschat.chat.*") || p.hasPermission("vippluschat.chat." + getCustomChats().getConfigurationSection( key ).getString("permission")))) {
				permissions.add(getCustomChats().getConfigurationSection( key ));
			}
		}
		//---- Creates a list of Chat ID's -- Added 7/7/2025 v0.8.7
		if (permissions.size() > 0) {
			for (ConfigurationSection c : permissions) {
				String stats = "&cfalse";
				if (inIgnorelist(p.getUniqueId().toString(), p.getName(), c)) { stats = "&atrue"; } 
				if (chat == null) {
					
					if (out.equalsIgnoreCase("")) {
						out = getConfig().getString("Messages.pstatus-ignored-items").replace("%CHAT_TAG%", c.getString("layout-tag")).replace("%CHAT_NAME%", c.getString("name")).replace("%STATUS%", stats).replace("%NL%", "\n");
					} else {
						out = out + getConfig().getString("Messages.pstatus-ignored-items").replace("%CHAT_TAG%", c.getString("layout-tag")).replace("%CHAT_NAME%", c.getString("name")).replace("%STATUS%", stats).replace("%NL%", "\n");
					}
				} else {
					if (p.hasPermission("vippluschat.chat.ignore") && (p.hasPermission("vippluschat.chat.*") || p.hasPermission("vippluschat.chat." + chat.getString("permission")))) {
						stats = "&cfalse";
						if (inIgnorelist(p.getUniqueId().toString(), p.getName(), chat)) { stats = "&atrue"; } 
						if (out.equalsIgnoreCase("")) {
							out = getConfig().getString("Messages.pstatus-ignored-items").replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")).replace("%STATUS%", stats).replace("%NL%", "\n");
						} else {
							out = out + getConfig().getString("Messages.pstatus-ignored-items").replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")).replace("%STATUS%", stats);
						}
					} else {
						pMessage(getConfig().getString("Messages.no-permissions"), p);
						return "no";
					}
					
				}
			}
		}
		return out;
	}
	
	public String help(Player p, int page) {
		String out = "";
		switch (page) {
		case 0:
		case 1:
			out = ""
					+ "&6----- &cHelp Menu &6-----\n"
					+ "&7/{chat} {message} &f- &6Allows you to send a message to the chat.\n"
					+ "&7/vpc focus {chat} &f- &6Allows you to toggle your focus mode for the specified chat.\n"
					+ "&7/vpc ignore {chat} &f- &6Allows you to ignore messages for the specified chat.\n"
					+ "&7/vpc unignore {chat} &f- &6Allows you to unignore messages for the specified chat.\n"
					+ "&7/vpc pstatus | Optional: {ignored/blacklisted} &f- &6Allows you to see the status for the specified chat.\n"
					+ "&7/vpc ignoreplayer {player/clear} &f- &6Allows you to ignore messages from the player specified in all chats.\n"
					+ "&7/vpc listplayers | Optional: {#} &f- &6Allows you to see who all you are ignoring.";
			if (p.hasPermission("vippluschat.admin")) {
				out = out + ""
						+ "\n&7/vpc help | Optional: {1-3} &f- &6Allows you to view other pages.";
			}
			break;
		case 2:
			if (p.hasPermission("vippluschat.admin")) {
				out = ""
						+ "&6----- &cHelp Menu &6-----\n"
						+ "&7/vpc checkver &f- &6Allows you to view the current version of the plugin as well as the lastest release version.\n"
						+ "&7/vpc reload &f- &6Allows you to reload all of the config files.\n"
						+ "&7/vpc ignored {chat} | Optional: {show/#/all} &f- &6Allows you to view the current list of people ingoring that chat.\n"
						+ "&7/vpc blacklist {+/add/del/delete/rem/remove/clear/-} {username} &f- &6Allows you to modify the blacklist.\n"
						+ "&7/vpc {fui/forceunignore} {chat} {username} &f- &6Allows you to force a player to unignore a chat.\n"
						+ "&7/vpc blacklisted | Optional: {show/#/all} &f- &6Allows you to view the players currently blacklisted.\n"
						+ "&7/vpc colors {chat} &f- &6Allows you to view the status of the color codes.\n"
						+ "&7/vpc colors toggle {chat} {color-code} &f- &6Allows you to modify the status of the color codes.\n"
						+ "&7/vpc help | Optional: {1-3} &f- &6Allows you to view other pages.";
			} else {
				out = getConfig().getString("Messages.no-permissions");
			}
			break;
		case 3:
			if (p.hasPermission("vippluschat.admin")) {
				out = ""
						+ "&6----- &cHelp Menu &6-----\n"
						+ "&7/vpc swear toggle {chat} &f- &6Allows you to toggle the state of the anti swear.\n"
						+ "&7/vpc swear {add,+,del,delete,rem,remove,clear} {chat} {word} | Optional: {replacemeent} &f- &6Allows you to modify the swear dictionary.\n"
						+ "&7/vpc swear list {chat} &f- &6Allows you to view the current dictionary.\n"
						+ "&7/vpc help | Optional: {1-3} &f- &6Allows you to view other pages.";
			} else {
				out = getConfig().getString("Messages.no-permissions");
			}
			break;
		default:
			break;
		}
		return out;
	}
	
	private String pstatusGetBlacklist(Player p) {
		String out = "";
		if (inBlacklist(p.getUniqueId().toString(), p.getName())) {
			out = "&atrue";
		} else {
			out = "&cfalse";
		}
		return out;	
	}

	private String arrayToList(List<String> list) {
		if (list.size() > 1) { 
			return String.join(", ", list);  // Joins list items with a comma and space
		} else if (!list.isEmpty()) {
			return list.get(0);  // Returns the single item if only one item exists
		}
		return "";  // Returns an empty string if the list is empty
	}

	private void getPlayerInfo( Player r, Player p ) {
			String msg = "";

			/* 
			 * Focused Chat
			 */
			if (focus.containsKey(p)) {
				msg += getConfig().getString("Messages.info-player-items")
					.replace("%INFO_TITLE%", "Focused")
					.replace("%INFO_DETAILS%", focus.get(p))
					.replace("%NL%", "\n");
			} else {
				msg += getConfig().getString("Messages.info-player-items")
					.replace("%INFO_TITLE%", "Focused")
					.replace("%INFO_DETAILS%", "Nothing")
					.replace("%NL%", "\n");
			}

			/*
			 * Blacklisted
			 */
			
			if (inBlacklist(p.getUniqueId().toString(), p.getName()) ) {
				msg += getConfig().getString("Messages.info-player-items")
					.replace("%INFO_TITLE%", "Blacklisted")
					.replace("%INFO_DETAILS%", "&aTrue")
					.replace("%NL%", "\n");
			} else {
				msg += getConfig().getString("Messages.info-player-items")
					.replace("%INFO_TITLE%", "Blacklisted")
					.replace("%INFO_DETAILS%", "&cFalse")
					.replace("%NL%", "\n");
			}
			
			/*
			 *  Ignored Channels
			 */
			List<String> ignoredchannels = getOther(ignorelist_location).getStringList("ignorelist." + p.getUniqueId().toString() + ".chats");
			String tic = arrayToList( ignoredchannels );
			if (tic.equalsIgnoreCase("")) { tic = "None"; }
			msg += getConfig().getString("Messages.info-player-items")
					.replace("%INFO_TITLE%", "Ignored Channels")
					.replace("%INFO_DETAILS%", tic )
					.replace("%NL%", "\n");

			/*
			 *  Ignored Players
			 */
			List<String> ignoredplayers = getOther(ignorelist_location).getStringList("ignorelist." + p.getUniqueId().toString() + ".players");
			List<String> tip = new ArrayList<String>();
			for (String u : ignoredplayers ) {
				tip.add( u.split( ";" )[1]);
			}
			tic = arrayToList( tip );
			if (tic.equalsIgnoreCase("")) { tic = "None"; }
			msg += getConfig().getString("Messages.info-player-items")
					.replace("%INFO_TITLE%", "Ignored Players")
					.replace("%INFO_DETAILS%", tic )
					.replace("%NL%", "\n");
			

			/*
			 * Send the message to the player
			 */
			pMessage( 

				getConfig().getString("Messages.info-player-layout")
					.replace("%PLAYER%", p.getName())
					.replace("%DISPLAY_NAME%", p.getDisplayName())
					.replace("%NL%", "\n")
					.replace("%ITEMS%", msg ),
				
				r );
	}
	
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
    	if(sender instanceof Player) {
    		Player p = (Player) sender;
			//---- No Arguments found v0.8.7 7/7/2025 - /vpc
			if (cmdLabel.equalsIgnoreCase("vpc") && args.length == 0) {
				pMessage(getConfig().getString( "Messages.no-args").replace( "%VERSION%", version ), p);
				return false;
			}
			//---- Focused info v0.8.6 10/29/2024 - /vpc focused --- v0.8.6
			if (cmdLabel.equalsIgnoreCase("vpc") && args.length == 1 && args[0].equalsIgnoreCase( "focused" )) {
				if (focus.containsKey(p)) {
					pMessage(getConfig().getString("Messages.focused")
					.replace("%PLAYER%", p.getName()).replace("%DISPLAY_NAME%", p.getDisplayName())
					.replace("%CHAT_NAME%", focus.get(p)), p);
				} else {
					pMessage(getConfig().getString("Messages.focused")
					.replace("%PLAYER%", p.getName()).replace("%DISPLAY_NAME%", p.getDisplayName())
					.replace("%CHAT_NAME%", "Nothing"), p);
				}
				return true;
			}
			//---- Focused End

			//---- Player info v0.8.6 10/29/2024 - /vpc info | Optional 1: {player} --- v0.8.6
			if (cmdLabel.equalsIgnoreCase("vpc") && args.length == 1 && args[0].equalsIgnoreCase( "info" )) {
				if (p.hasPermission( "vippluschat.user.info" )) {
					getPlayerInfo( p, p );
					return true;
				}
			}
			
			if (cmdLabel.equalsIgnoreCase("vpc") && args.length == 2 && args[0].equalsIgnoreCase( "info" )) {
				if (p.hasPermission( "vippluschat.admin.info" )) {
					OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);

					if (op.hasPlayedBefore()) {
						// Check if the player is currently online
						Player ply = op.getPlayer();
						getPlayerInfo(p, ply);
					} else {
						pMessage(getConfig().getString("Messages.info-player-not-found")
							.replace("%PLAYER%", p.getName()).replace("%DISPLAY_NAME%", p.getDisplayName()),
						p);
					}
					
					return true;
				}
			}
			//---- Player info End

    		//---- Help Command v0.8.5 3/16/2017 - /vpc help | Optional: {#} --- v0.8.5
    		if (cmdLabel.equalsIgnoreCase("vpc") && args.length > 0 && args.length < 3) {
    			if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
    				pMessage(help(p, 1), p);
    			}
    			if (args.length == 2 && args[0].equalsIgnoreCase("help")) {
    				pMessage(help(p, Integer.parseInt(args[1])), p);
    			}
    			
    		}
    		//---- End Help Command
    		
    		//---- View Swear Dictionary v0.8.5 3/1/2017 - /vpc swear list {chat}| Optional: {#} - Shows the list of words in the chat specified's dictionary. --- v0.8.5
    		if (cmdLabel.equalsIgnoreCase("vpc") && (args.length == 3 || args.length == 4)) {
    			if (args[0].equalsIgnoreCase("swear") && args[1].equalsIgnoreCase("list")) {
    				ConfigurationSection chat = detectChat(args[2]);
    				if (chat != null) {
    					if (args.length == 3) {
    						pMessage(showSwearListServer(1, chat), p);
    					} else if (args.length == 4 & StringUtils.isNumericSpace(args[3])) {
    						pMessage(showSwearListServer(Integer.parseInt(args[3]), chat), p);
    					}
    					
    				}
    			}
    		}
    		//---- End View Swear Dictionary
    		
    		//---- Toggle Swear - Added v0.8.5 2/27/2017 - /vpc swear toggle {chat} - Enables/Disables the server swear filter for  the chat specified --- v0.8.5
    		if (cmdLabel.equalsIgnoreCase("vpc") && (args.length == 3)) {
    			if (args[0].equalsIgnoreCase("swear") && args[1].equalsIgnoreCase("toggle")) {
    				if (p.hasPermission("vippluschat.admin.swear-toggle-server") || p.hasPermission("vippluschat.admin")) {
	    				ConfigurationSection chat = detectChat(args[2]);
	    				if (chat != null) {
	    					String state = "";
	    					if (chat.getBoolean("anti-swear.server.enabled")) {
	    						chat.set("anti-swear.server.enabled", false);
	    						saveCustomConfig();
	    						reloadChats();
	    						state = "&cDisabled";
	    						pMessage(getConfig().getString("Messages.antiswear-toggle").replace("%PLAYER%", p.getName()).replace("%DISPLAY_NAME%", p.getDisplayName())
								.replace("%STATUS%", state).replace("%CHAT_TAG%", chat.getString("layout-tag"))
								.replace("%CHAT_NAME%", chat.getString("name")), p);
	    					} else { 
	    						chat.set("anti-swear.server.enabled", true);
	    						saveCustomConfig();
	    						reloadChats();
	    						state = "&aEnabled";
	    						getOther(chat.getString("anti-swear.server.file-name").replace("%CHAT_NAME%", chat.getString("name"))).options().copyDefaults(true);
	    						saveOther(chat.getString("anti-swear.server.file-name").replace("%CHAT_NAME%", chat.getString("name")));
	    						tcOther(chat.getString("anti-swear.server.file-name").replace("%CHAT_NAME%", chat.getString("name")), "d");
	    						pMessage(getConfig().getString("Messages.antiswear-toggle").replace("%PLAYER%", p.getName()).replace("%DISPLAY_NAME%", p.getDisplayName()).replace("%STATUS%", state).replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")), p);
	    					}
	    					
	    				} else {
	    					pMessage(getConfig().getString("Messages.antiswear-chat-syntax").replace("%COMMAND%", "/vpc swear toggle {chat}"), p);
	    				}
    				} else {
    					pMessage(getConfig().getString("Messages.no-permissions"), p);
						return false;
    				}
    			}
    		}
    		//---- End Toggle Swear
    		
    		//---- Modify Swear Dictionaries - Added v0.8.5 2/27/2017 - /vpc swear {add,+,-,del,delte,rem,remove,clear} {chat} {word} | Optional: {replacement word} - Allows you to modify the server swear directory that is specified. --- v0.8.5
    		
    		if (cmdLabel.equalsIgnoreCase("vpc") && (args.length == 4 || args.length == 5) ||args.length == 3 ) {
    			if (args[0].equalsIgnoreCase("swear")) {
    				if (p.hasPermission("vippluschat.admin.swear-modify") || p.hasPermission("vippluschat.admin")) {
	    				ConfigurationSection chat = detectChat(args[2]);
	    				String loc = "";
	    				if (chat != null) {
	    					loc = chat.getString("anti-swear.server.file-name").replace("%CHAT_NAME%", chat.getString("name"));
	    				}
	    				switch (args[1]) {
	    				case "+":
	    				case "add":
	        				if (chat != null) {
	        					/*
	        					 * Dictionary:
	        					 * - shit: - Replaces the word with nothing
	        					 * - bitch:linch - Replaces the word
	        					 * - %shit%:rip - Replaces the word containing
	        					 * - #shit#: - Blacks message containing
	        					 */
	        					
	        					if (getOther(loc).getStringList("Dictionary").size() > 0) {
	        						List<String> l = getOther(loc).getStringList("Dictionary");
	        						if (args.length == 4) {
	        							if (l.remove(args[3] + ":")) {
	        								pMessage(getConfig().getString("Messages.antiswear-word-already").replace("%WORD%", args[3])
											.replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")), p);
	        							} else {
	        								l.add(args[3] + ":");
		        							ssOther(loc, "Dictionary", l);
		        							pMessage(getConfig().getString("Messages.antiswear-word-add").replace("%WORD%", args[3])
											.replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")), p);
	        							}
	        						} else if (args.length == 5){
	        							if (l.remove(args[3] + ":" + args[4])) {
	        								pMessage(getConfig().getString("Messages.antiswear-word-already").replace("%WORD%", args[3] + ":" + args[4])
											.replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")), p);
	        							} else {
	        								l.add(args[3] + ":" + args[4]);
		        							ssOther(loc, "Dictionary", l);
		        							pMessage(getConfig().getString("Messages.antiswear-word-add").replace("%WORD%", args[3] + ":" + args[4])
											.replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")), p);
	        							}
	        						}
	        						
	        					} else {
	        						List<String> l = new ArrayList<>();
	        						if (args.length == 4) {
	        							l.add(args[3] + ":");
	        							ssOther(loc, "Dictionary", l);
	        							pMessage(getConfig().getString("Messages.antiswear-word-add").replace("%WORD%", args[3])
										.replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")), p);
	        						} else if (args.length == 5){
	        							l.add(args[3] + ":" + args[4]);
	        							ssOther(loc, "Dictionary", l);
	        							pMessage(getConfig().getString("Messages.antiswear-word-add").replace("%WORD%", args[3] + ":" + args[4])
										.replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")), p);
	        						}
	        					}
	        					
	        				} else {
	        					pMessage(getConfig().getString("Messages.antiswear-chat-syntax").replace("%COMMAND%", "/vpc swear {add,+} {chat} {word} | Optional: {replacement word}"), p);
	        					return false;
	        				}
	    					break;
	    				case "-":
	    				case "remove":
	    				case "rem":
	    				case "del":
	    				case "delete":
	    					if (chat != null) {
	    						if (args.length == 4) {
		    						if (getOther(loc).getStringList("Dictionary").size() > 0) {
		    							List<String> l = getOther(loc).getStringList("Dictionary");
		    							// word:replacement
		    							// word - remove it
		    							for (String w : l) {
			    							if (w.split(":").length == 1) {
			    								if (w.split(":")[0].equalsIgnoreCase(args[3])) {
			    									l.remove(w);
			    									ssOther(loc, "Dictionary", l);
			    									pMessage(getConfig().getString("Messages.antiswear-word-remove").replace("%WORD%", args[3]).replace("%STATE%", "&aRemoved")
													.replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")), p);
			    									return true;
			    								}
			    							}
		    							}
		    							pMessage(getConfig().getString("Messages.antiswear-word-remove").replace("%WORD%", args[3]).replace("%STATE%", "&cNot Removed")
										.replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")), p);
		        					} else {
		        						pMessage(getConfig().getString("Messages.antiswear-word-remove").replace("%WORD%", args[3]).replace("%STATE%", "&cNot Removed")
										.replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")), p);
		        					}
	    						} else if (args.length == 5) {
	    							if (getOther(loc).getStringList("Dictionary").size() > 0) {
		    							List<String> l = getOther(loc).getStringList("Dictionary");
		    							// word:replacement
		    							// word - remove it
		    							for (String w : l) {
			    							if (w.split(":").length == 2) {
			    								if (w.equalsIgnoreCase(args[3] + ":" + args[4])) {
			    									l.remove(w);
			    									ssOther(loc, "Dictionary", l);
			    									pMessage(getConfig().getString("Messages.antiswear-word-remove").replace("%WORD%", args[3]).replace("%STATE%", "&aRemoved")
													.replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")), p);
			    									return true;
			    								}
			    							}
		    							}
		    							pMessage(getConfig().getString("Messages.antiswear-word-remove").replace("%WORD%", args[3]).replace("%STATE%", "&cNot Removed")
										.replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")), p);
		        					} else {
		        						pMessage(getConfig().getString("Messages.antiswear-word-remove").replace("%WORD%", args[3]).replace("%STATE%", "&cNot Removed")
										.replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")), p);
		        					}
	    						}
	    					} else {
	    						pMessage(getConfig().getString("Messages.antiswear-chat-syntax").replace("%COMMAND%", "/vpc swear {-,del,delte,rem,remove} {chat} {word} {replacement}"), p);
	    					}
	    					
	    					break;
	    				case "clear":
	    					if (args.length > 2) {
	    						if (args[1].equalsIgnoreCase("clear")) {
	    							if (chat != null) {
	    								ssOther(loc, "Dictionary", new ArrayList<>());
	    								pMessage(getConfig().getString("Messages.antiswear-word-clear").replace("%CHAT_TAG%", chat.getString("layout-tag"))
										.replace("%CHAT_NAME%", chat.getString("name")), p);
	    							} else {
	    								pMessage(getConfig().getString("Messages.antiswear-chat-syntax")
										.replace("%COMMAND%", "/vpc swear {add,+,-,del,delte,rem,remove,clear} {chat} {word} | Optional: {replacement word}"), p);
	    							}
	    						} else {
	    							pMessage(getConfig().getString("Messages.antiswear-syntax").replace("%COMMAND%", "/vpc swear clear {chat}"), p);
	    						}
	    					}
	    				}
	    			} else {
	    				pMessage(getConfig().getString("Messages.no-permissions"), p);
	    			}
	    		}
    		}
    		
    		//---- End Modify Swear Dictionaties
    		
    		//---- Player Ignore List - Added v0.8.5 2/27/2017 - /vpc listplayers | Optional: {#} - Allows players to view the list of people they are ignoring in private chats --- v0.8.5 2/27/2017
    		if (cmdLabel.equalsIgnoreCase("vpc") && (args.length == 1 || args.length == 2)) {
    			if (args[0].equalsIgnoreCase("listplayers")) {
    				if (p.hasPermission("vippluschat.user.playerignore.list")) {
    					if (args.length == 1) {
	        				    int page = 1;
	        				    pMessage(showPlayerIgnore(p, page), p);
    					} else if (args.length == 2){
    						try {
	        				    int page = Integer.parseInt(args[1]);
	        				    pMessage(showPlayerIgnore(p, page), p);
	        				} catch (NumberFormatException e) {
	        					pMessage(showPlayerIgnore(p, 1), p);   				
	        				}
    					} else {
    						pMessage(getConfig().getString("Messages.player-ignore-list-syntax"), p);
    					}
    				} else {
    					pMessage(getConfig().getString("Messages.no-permissions"), p);
    				}
    			}
    		}
    		//---- End Palyer Ignore List
    		
    		//---- Player Ingore - Added v0.8.5 2/27/2017 - /vpc ignoreplayer {player/clear} | Optional: {+/add/rem/del/delete/remove/-/clear}- Allows a player to ignore another player inside chats. --- v0.8.5
    		if (cmdLabel.equalsIgnoreCase("vpc") && (args.length == 2 || args.length == 3)) {
    			if (args[0].equalsIgnoreCase("ignoreplayer")) {
    				if (p.hasPermission("vippluschat.user.playerignore")) {
    					if (args.length == 2) {
	    					if (args[1].equalsIgnoreCase("clear")) {
	    						modifyPlayerIgnore(getServer().getOfflinePlayer(args[1]), p, "clear");
	        					pMessage(getConfig().getString("Messages.player-ignore").replace("%IGNORE_PLAYER%",  "All Entries").replace("%IGNORE_UUID%", "")
								.replace("%PLAYER%", p.getName()).replace("%DISPLAY_NAME%", p.getDisplayName()).replace("%PLAYER_UUID%", p.getUniqueId().toString())
								.replace("%STATUS%", "&cunignored"), p);
	    					} else {
	    						modifyPlayerIgnore(getServer().getOfflinePlayer(args[1]), p, "toggle");
	    						pMessage(getConfig().getString("Messages.player-ignore").replace("%IGNORE_PLAYER%",  getServer().getOfflinePlayer(args[1]).getName())
								.replace("%IGNORE_UUID%", getServer().getOfflinePlayer(args[1]).getUniqueId().toString()).replace("%PLAYER%", p.getName())
								.replace("%DISPLAY_NAME%", p.getDisplayName()).replace("%PLAYER_UUID%", p.getUniqueId().toString())
								.replace("%STATUS%", getPlayerIgnoreStatus(getServer().getOfflinePlayer(args[1]), p)), p);
	    					}
    					}
    					
    					if (args.length == 3) {
    						switch (args[2]) {
    						case "+":
    						case "add":
    							modifyPlayerIgnore(getServer().getOfflinePlayer(args[1]), p, "+");
	        					pMessage(getConfig().getString("Messages.player-ignore").replace("%IGNORE_PLAYER%",  getServer().getOfflinePlayer(args[1]).getName())
								.replace("%IGNORE_UUID%", getServer().getOfflinePlayer(args[1]).getUniqueId().toString()).replace("%PLAYER%", p.getName())
								.replace("%DISPLAY_NAME%", p.getDisplayName()).replace("%PLAYER_UUID%", p.getUniqueId().toString())
								.replace("%STATUS%", getPlayerIgnoreStatus(getServer().getOfflinePlayer(args[1]), p)), p);
    							break;
    							
    						case "-":
    						case "rem":
    						case "remove":
    						case "del":
    						case "delete":
    							modifyPlayerIgnore(getServer().getOfflinePlayer(args[1]), p, "-");
	        					pMessage(getConfig().getString("Messages.player-ignore").replace("%IGNORE_PLAYER%",  getServer().getOfflinePlayer(args[1]).getName())
								.replace("%IGNORE_UUID%", getServer().getOfflinePlayer(args[1]).getUniqueId().toString()).replace("%PLAYER%", p.getName())
								.replace("%DISPLAY_NAME%", p.getDisplayName()).replace("%PLAYER_UUID%", p.getUniqueId().toString())
								.replace("%STATUS%", getPlayerIgnoreStatus(getServer().getOfflinePlayer(args[1]), p)), p);
    							break;
    						
    						case "clear":
    							modifyPlayerIgnore(getServer().getOfflinePlayer(args[1]), p, "clear");
	        					pMessage(getConfig().getString("Messages.player-ignore").replace("%IGNORE_PLAYER%",  "All Entries").replace("%IGNORE_UUID%", "")
								.replace("%PLAYER%", p.getName()).replace("%DISPLAY_NAME%", p.getDisplayName()).replace("%PLAYER_UUID%", p.getUniqueId().toString())
								.replace("%STATUS%", "&cunignored"), p);
	        					break;
	        				default:
	        					pMessage(getConfig().getString("Messages.player-ignore-syntax"), p);
	        					break;
    						}
    					}
					} else {
						pMessage(getConfig().getString("Messages.no-permissions"), p);
					}
    			}
    				
    					
    		}
    		
    		
    		//---- End Player Ignore
    		
    		//---- pstatus - Added v0.8.5 2/23/2017 - /vpc pstatus | Optional 1: {blacklisted/ignored} | Optionatl 2: {chat}
    		if (cmdLabel.equalsIgnoreCase("vpc") && (args.length >= 1 && args.length <= 3)) {
    			if (args[0].equalsIgnoreCase("pstatus")) {
    				if (args.length == 1) {
    					if (p.hasPermission("vippluschat.user.pstatus") || (p.hasPermission("vippluschat.user.pstatus.ignored") && p.hasPermission("vippluschat.user.pstatus.blacklisted"))) {
        					pMessage(getConfig().getString("Messages.pstatus-layout").replace("%STATUS_BLACKLISTED%", pstatusGetBlacklist(p)).replace("%PLAYER_NAME%", p.getName())
							.replace("%PLAYER_UUID%", p.getUniqueId().toString()).replace("%DISPLAY_NAME%", p.getDisplayName()).replace("%NL%", "\n").replace("%IGNORED_START%", "")
							.replace("%IGNORED_END%", "").replace("%BLACKLISTED_START%", "").replace("%BLACKLISTED_END%", "").replace("%BOTH_START%", "").replace("%BOTH_END%", "")
							.replace("%IGNORED_ITEMS%", pstatusGetChats(p, null)), p);
        							//.replaceAll("(%IGNORED_START%)[^&]*(%IGNORED_END%)", "$1$2")
        				} else if (p.hasPermission("vippluschat.user.pstatus.ignored")) {
        					pMessage(getConfig().getString("Messages.pstatus-layout").replaceAll("(%BOTH_START%).*(%BOTH_END%)", "").replaceAll("(%BLACKLISTED_START%).*(%BLACKLISTED_END%)", "")
							.replace("%PLAYER_NAME%", p.getName()).replace("%PLAYER_UUID%", p.getUniqueId().toString()).replace("%DISPLAY_NAME%", p.getDisplayName()).replace("%NL%", "\n")
							.replace("%IGNORED_START%", "").replace("%IGNORED_END%", "").replace("%BOTH_START%", "").replace("%BOTH_END%", "").replace("%IGNORED_ITEMS%", pstatusGetChats(p, null)), p);
							//.replaceAll("(%BLACKLISTED_START%)[^&]*(%BLACKLISTED_END%)", "$1$2")
        				} else if (p.hasPermission("vippluschat.user.pstatus.blacklisted")) {
        					pMessage(getConfig().getString("Messages.pstatus-layout").replaceAll("(%BOTH_START%).*(%BOTH_END%)", "").replaceAll("(%IGNORED_START%).*(%IGNORED_END%)", "")
							.replace("%STATUS_BLACKLISTED%", pstatusGetBlacklist(p)).replace("%PLAYER_NAME%", p.getName()).replace("%PLAYER_UUID%", p.getUniqueId().toString())
							.replace("%BLACKLISTED_START%", "").replace("%BLACKLISTED_END%", "").replace("%DISPLAY_NAME%", p.getDisplayName()).replace("%NL%", "\n").replace("%IGNORED_START%", "")
							.replace("%IGNORED_END%", "").replace("%BOTH_START%", "").replace("%BOTH_END%", "").replace("%IGNORED_ITEMS%", pstatusGetChats(p, null)), p);
        				} else {
        					pMessage(getConfig().getString("Messages.no-permissions"), p);
        				}
    				} else if (args.length == 2 && (args[1].equalsIgnoreCase("ignored") || args[1].equalsIgnoreCase("blacklisted"))) {
    					if (p.hasPermission("vippluschat.user.pstatus.blacklisted") && args[1].equalsIgnoreCase("blacklisted")) {
    						pMessage(getConfig().getString("Messages.pstatus-layout").replaceAll("(%BOTH_START%).*(%BOTH_END%)", "").replaceAll("(%IGNORED_START%).*(%IGNORED_END%)", "")
							.replace("%STATUS_BLACKLISTED%", pstatusGetBlacklist(p)).replace("%PLAYER_NAME%", p.getName()).replace("%PLAYER_UUID%", p.getUniqueId().toString())
							.replace("%BLACKLISTED_START%", "").replace("%BLACKLISTED_END%", "").replace("%DISPLAY_NAME%", p.getDisplayName()).replace("%NL%", "\n").replace("%IGNORED_START%", "")
							.replace("%IGNORED_END%", "").replace("%BOTH_START%", "").replace("%BOTH_END%", "").replace("%IGNORED_ITEMS%", pstatusGetChats(p, null)), p);
        				} else if (p.hasPermission("vippluschat.user.pstatus.ignored") && args[1].equalsIgnoreCase("ignored")) {
        					pMessage(getConfig().getString("Messages.pstatus-layout").replaceAll("(%BOTH_START%).*(%BOTH_END%)", "").replaceAll("(%BLACKLISTED_START%).*(%BLACKLISTED_END%)", "")
							.replace("%PLAYER_NAME%", p.getName()).replace("%PLAYER_UUID%", p.getUniqueId().toString()).replace("%DISPLAY_NAME%", p.getDisplayName()).replace("%NL%", "\n")
							.replace("%IGNORED_START%", "").replace("%IGNORED_END%", "").replace("%BOTH_START%", "").replace("%BOTH_END%", "").replace("%IGNORED_ITEMS%", pstatusGetChats(p, null)), p);
        				} else {
        					pMessage(getConfig().getString("Messages.no-permissions"), p);
        				}
    				} else if (args.length == 3) {
    					ConfigurationSection chat = detectChat(args[2]);
    					if (args[1].equalsIgnoreCase("ignored")) {
    						if (chat != null) {
    							if (p.hasPermission("vippluschat.user.pstatus.ignored")) {
    								String output = pstatusGetChats(p, chat);
    								if (!output.equalsIgnoreCase("no")) {
    									pMessage(getConfig().getString("Messages.pstatus-layout").replaceAll("(%BOTH_START%).*(%BOTH_END%)", "")
										.replaceAll("(%BLACKLISTED_START%).*(%BLACKLISTED_END%)", "").replace("%PLAYER_NAME%", p.getName()).replace("%PLAYER_UUID%", p.getUniqueId().toString())
										.replace("%DISPLAY_NAME%", p.getDisplayName()).replace("%NL%", "\n").replace("%IGNORED_START%", "").replace("%IGNORED_END%", "").replace("%BOTH_START%", "")
										.replace("%BOTH_END%", "").replace("%IGNORED_ITEMS%", output), p);
    								}
    								return true;
    		    				} else {
    	        					pMessage(getConfig().getString("Messages.no-permissions"), p);
    	        				}
    						} else if (!p.hasPermission("vippluschat.user.pstatus.ignored")) {
    							pMessage(getConfig().getString("Messages.no-permissions"), p);
    						} else {
    							pMessage(getConfig().getString("Messages.pstatus-invalid-chat"), p);
        						return false;
    						}
    					} else {
    						pMessage(getConfig().getString("Messages.pstatus-syntax"), p);
    						return false;
    					}
    					
    				}
    			}
    		}
    		//---- End pstatus
    		
    		//---- View color status - Added v0.8.2 - /vpc colors {chat}
    		if (cmdLabel.equalsIgnoreCase("vpc") && args.length == 2) {
   				if (args[0].equalsIgnoreCase("colors")) {
   					if (p.hasPermission("vippluschat.admin") || p.hasPermission("vippluschat.admin.viewcolors")) {
	       				ConfigurationSection chat = detectChat(args[1]);
	       				if (chat != null) {
	           				pMessage(outputColorCodes(chat), p);
	           			}
   					} else {
   	    				p.sendMessage(ct(getConfig().getString("Messages.no-permissions")));
   	    			}
       			}
    		}
    		//---- End view color status
    		
    		//---- Toggle Colors - Added v0.8.2 - /vpc colors toggle {chat} {color-code}
    		if (cmdLabel.equalsIgnoreCase("vpc") && args.length == 4) {
    			if (args[0].equalsIgnoreCase("colors") && args[1].equalsIgnoreCase("toggle")) {
    				if (p.hasPermission("vippluschat.admin") || p.hasPermission("vippluschat.admin.togglecolors")) {
    					ConfigurationSection chat = detectChat(args[2]);
        				if (chat != null) {
            				if (toggleColor(args[3], chat)) {
            					pMessage("Chat color has been toggled", p);
            				} else {
            					pMessage("Chat color has failed to toggle", p);
            				}
            			}	
    				} else {
    					p.sendMessage(ct(getConfig().getString("Messages.no-permissions")));
    				}
    				
    			}
    			
    		}
    		//---- End Toggle Colors
    		
    		//---- Focus Mode - /vpc focus {} - Added v0.8.5 - 2/14/17 3:09pm 
    		if (cmdLabel.equalsIgnoreCase("vpc") && args.length == 2) {
    			if (args[0].equalsIgnoreCase("focus")) {
    				ConfigurationSection chat = detectChat(args[1]);
    				if (chat != null) {
    					if (p.hasPermission("vippluschat.chat.focus." + chat.getString("permission")) || p.hasPermission("vippluschat.chat.focus.*")) {
    						if (toggleFocusMode(p, chat.getString("name"))) {
    							pMessage(ct(getConfig().getString("Messages.focus-mode-toggle").replace("%PLAYER%", p.getName()).replace("%DISPLAY_NAME%", p.getDisplayName())
								.replace("%STATE%", "&aEnabled&r").replace("%CHAT_TAG%", chat.getString("layout-tag"))
								.replace("%CHAT_NAME%", chat.getString("name"))), p);
    						} else {
    							pMessage(ct(getConfig().getString("Messages.focus-mode-toggle").replace("%PLAYER%", p.getName()).replace("%DISPLAY_NAME%", p.getDisplayName())
								.replace("%STATE%", "&cDisabled&r").replace("%CHAT_TAG%", chat.getString("layout-tag"))
								.replace("%CHAT_NAME%", chat.getString("name"))), p);
    						}
    					} else {
    						
    					}
    				} else {
    					p.sendMessage(ct(getConfig().getString("Messages.no-permissions")));
    				}
    			}
    		}
    		//---- End Focus Mode
    		
    		//---- Toggle ignore chats - Added v0.8.2
    		
    		//---- End Toggle ignore chats
    		
    		//---- Check Version - Added 7/02/16 v0.8 - /vpc checkver
    		if (cmdLabel.equalsIgnoreCase("vpc") && args.length == 1) {
    			if (args[0].equalsIgnoreCase("checkver")) {
    				if (p.hasPermission("vippluschat.admin") || p.hasPermission("vippluschat.admin.checkversion")) {
    					if (getConfig().getBoolean("Check Version")) {
	    					try {
	    						URL u = new URL("https://pastebin.com/raw/2ik6fXLb");
	    						URLConnection conn = u.openConnection();
	    	    				BufferedReader in = new BufferedReader(
	    	    						new InputStreamReader(
	    	    								conn.getInputStream()));
	    	    				StringBuffer buffer = new StringBuffer();
	    	    				String inputLine;
	    	    				while ((inputLine = in.readLine()) != null) 
	    	    				    buffer.append(inputLine);
	    	    				in.close();
	    	    				System.out.println(buffer.toString());
	    	    				p.sendMessage(ct("&7Your current version is: &9" + version + " &7The last released version is: &9" + buffer.toString()));
	    	    				return true;
	    					} catch (MalformedURLException e) {
	    						e.printStackTrace();
	    						return false;
	    					} catch (IOException e) {
	    						e.printStackTrace();
	    						return false;
	    					}
	    				} else {
	    					p.sendMessage(ct("&cYou currently have check version disabled inside of the config."));
	    					return true;
	    				}
    				} else {
    					p.sendMessage(ct(getConfig().getString("Messages.no-permissions")));
    				}
    			}
    		}
    		//---- End Check Version
    		
    		//---- Reload - Added PRE v0.8 - /vpc reload
    		if(cmdLabel.equalsIgnoreCase("vpc") && args[0].equalsIgnoreCase("reload")) {
    			if (p.hasPermission("vippluschat.admin") || p.hasPermission("vippluschat.admin.reload")) {
    				this.reloadConfig();
        			this.reloadChats();
        			loadChats();
        			loadConfiguration();
        			loadOther();
        			pMessage(getConfig().getString("Messages.reload"), p);
        			return true;
    			} else {
    				p.sendMessage(ct(getConfig().getString("Messages.no-permissions")));
    			}
    		}
    		//---- End Reload
    		
    		//---- Chat - Added PRE v0.8 - /{chat} {message}
    		/*
    		if((cmdLabel.equalsIgnoreCase("staff") || cmdLabel.equalsIgnoreCase("st")) && p.hasPermission("vippluschat.chat.staff")) {
    			String msg = "";    

    			for(int i = 0; i < args.length; i++){
    			    String arg = args[i] + " ";
    			    msg = msg + arg;
    			}
    			if (getConfig().getString("Enabled.Staff") == "true") {
    				sendMessages(msg, p, "vippluschat.chat.staff", "Staff");
    			} else {
    				pMessage(getConfig().getString("Messages.chat-disabled").replace("%CHAT_NAME%", "Staff").replace("%PLAYER%", p.getName()), p);
    			}
    			return true;
    			
    		}
    		---- Removed v0.8.2 - This is the old chat system which is useless now but is here for future notes!*/
    		//---- Chat End
    		
    		//---- Un/Ignore - Added PRE v0.8 - /vpc {ignore/unignore} {chat} - Updated v0.8.5 - 2/16/2017
    		
    		if (cmdLabel.equalsIgnoreCase("vpc") && args.length == 2) {
    			if (args[0].equalsIgnoreCase("ignore") || args[0].equalsIgnoreCase("unignore") ) {
    				if (p.hasPermission("vippluschat.chat.ignore")) {
						ConfigurationSection chat = detectChat(args[1]);
	    				if (chat != null) {
	    					if (checkIgnoreChatSpecified(p.getName(), args[1])) {
		    					if (chat.getBoolean("allow-ignore")) {
			    					if (args[0].equalsIgnoreCase("ignore")) {
			    						if (!inIgnorelist(p.getUniqueId().toString(), p.getName(), chat)) {
			    							modifyIgnorelist(p.getUniqueId().toString(), p.getName(), chat, "+");
			    							pMessage(ct(getConfig().getString("Messages.ignore").replace("%CHAT_TAG%", chat.getString("layout-tag"))
											.replace("%CHAT_NAME%", chat.getString("name"))
											.replace("%STATUS%", "ignored").replace("%PLAYER%", p.getName())), p);
			    						} else {
			    							pMessage(ct(getConfig().getString("Messages.ignore-already").replace("%CHAT_TAG%", chat.getString("layout-tag"))
											.replace("%CHAT_NAME%", chat.getString("name")).replace("%STATUS%", "ignored")
											.replace("%PLAYER%", p.getName())), p);
			    						}
			    					} else if (args[0].equalsIgnoreCase("unignore")) {
										if (inIgnorelist(p.getUniqueId().toString(), p.getName(), chat)) {
			    							modifyIgnorelist(p.getUniqueId().toString(), p.getName(), chat, "-");
			    							pMessage(ct(getConfig().getString("Messages.ignore").replace("%CHAT_TAG%", chat.getString("layout-tag"))
											.replace("%CHAT_NAME%", chat.getString("name"))
											.replace("%STATUS%", "unignored").replace("%PLAYER%", p.getName())), p);
			    						} else {
			    							pMessage(ct(getConfig().getString("Messages.ignore-already").replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")).replace("%STATUS%", "unignored").replace("%PLAYER%", p.getName())), p);
			    						}
									}
		    				/* ---- Removed v0.8.5 when ignore and blacklist system got a complete overhaul - 2/15/2017
	    					boolean b = false;
		    				reloadConfig();
		    				/* ---- Removed v0.8.2 when new black list system was added
		    				for (String a: getConfig().getString("Other.blacklist").split(",")) {
		    					if (a.equalsIgnoreCase(p.getName())) {
		    						pMessage(getConfig().getString("Messages.blacklist"), p);
		    						return false;
		    					}
		    				}
		    				for (String a: getCustomChats().getString(chatID + ".ignore-list").split(",")) {
		    					if (a.equalsIgnoreCase(p.getName())) {
		    						b = true;
	    					}
		    				
	    					}
	    					
	    				if (!b) {
	    					if (getCustomChats().getString(chatID + ".ignore-list").equalsIgnoreCase("")) {
	    						getCustomChats().set(chatID + ".ignore-list", p.getName().toLowerCase());
	    						saveCustomConfig();
	    						pMessage(ct(getConfig().getString("Messages.ignore").replace("%CHAT_NAME%", getCustomChats().getString(chatID + ".name")).replace("%STATUS%", "ignored").replace("%PLAYER%", p.getName())), p);
	    						return true;
	    					} else {
	    						getCustomChats().set(chatID + ".ignore-list", p.getName().toLowerCase() + "," + getConfig().getString(chatID + ".ignore-list".toLowerCase()));
	    						saveCustomConfig();
	    						pMessage(ct(getConfig().getString("Messages.ignore").replace("%CHAT_NAME%", getCustomChats().getString(chatID + ".name")).replace("%STATUS%", "ignored").replace("%PLAYER%", p.getName())), p);
	    						return true;
	    					}
	    				} else {
	    					pMessage(ct(getConfig().getString("Messages.ignore-already").replace("%CHAT_NAME%", getCustomChats().getString(chatID + ".name")).replace("%STATUS%", "ignored").replace("%PLAYER%", p.getName())), p);
	    					return true;
	    				}
	        			} else {
	    					boolean b = false;
		    				String c = "";
		    				int d = 0;
		    				for (String a: getCustomChats().getString(chatID + ".ignore-list").split(",")) {
		    					if (a.equalsIgnoreCase(p.getName())) {
		    						b = true;
		    					} else {
		    						if (d == 0) {
			    						d += 1;
				    					c = a;
			    					} else {
			    						String e = c;
				    					c = e + "," + a;
			    					}
		    					}
		    		        }
		    				if (!b) {
		    					pMessage(ct(getConfig().getString("Messages.ignore-not-found").replace("%CHAT_NAME%", getCustomChats().getString(chatID + ".name")).replace("%STATUS%", "unignored").replace("%PLAYER%", p.getName())), p);
		    					return true;
		    				} else {
		    					getCustomChats().set(chatID + ".ignore-list", c);
		    					saveCustomConfig();
		    					if (c.length() == 0) {
		    						getCustomChats().set(chatID + ".ignore-list", "");
			    					saveCustomConfig();
		    					}
		    					
		    					pMessage(ct(getConfig().getString("Messages.ignore").replace("%CHAT_NAME%", getCustomChats().getString(chatID + ".name")).replace("%STATUS%", "unignored").replace("%PLAYER%", p.getName())), p);
		    					return true;
		    				}
	    				}
							*/
								} else {
									pMessage(getConfig().getString("Messages.ignore-disabled").replace("%CHAT_TAG%", chat.getString("layout-tag"))
									.replace("%CHAT_NAME%", chat.getString("name")),p);
								}
	        				} else {
	        					pMessage("You are blacklisted from ignoring this chat.", p);	
	        				}
	    				} else {
	    					pMessage(ct(getConfig().getString("Messages.ignore-fail").replace("%CHAT_NAME%", args[1]).replace("%STATUS%", "unignored").replace("%PLAYER%", p.getName())), p);
	        			}
    				} else { 
    					p.sendMessage(ct(getConfig().getString("Messages.no-permissions")));
    				}
    			}
    		}
    		//---- Ignore End
    		
    		//---- Get Ignore List - Added PRE v0.8 - /vpc ignored {chat} : {show/#/all} - Updated v0.8.2  Modified to accept custom chats - Updated v0.8.5  Modified to accept the new system 2/17/2017
    		if (cmdLabel.equalsIgnoreCase("vpc") &&  (args.length == 2|| args.length == 3)) {
    			if (args[0].equalsIgnoreCase("ignored")) {
    				if (p.hasPermission("vippluschat.admin") || p.hasPermission("vippluschat.admin.ignored")) {
						ConfigurationSection chat = detectChat(args[1]);
	    				if (chat != null) {
	    					int page = 1;
	        				try {
	        				    page = Integer.parseInt(args[2]);
	        				    pMessage(showIgnored(p, page, chat), p);
	        				} catch (NumberFormatException e) {
	        				    if (args[2].equalsIgnoreCase("all")) {
	        				    	pMessage(showIgnored(p, -1, chat), p);
	        				    	return true;
	        				    } else if (args[2].equalsIgnoreCase("show")) {
	        				    	pMessage(showIgnored(p, 1, chat), p);
	        				    } else {
	        				    	return false;
	        				    }
	        				
	        				} catch (ArrayIndexOutOfBoundsException d) {
	        					pMessage(showIgnored(p, 1, chat), p);
	        				}
	        			} else {
	        				pMessage(getConfig().getString("Messages.ignore-fail"), p);
	        				return true;
	        			}
    				} else {
    					p.sendMessage(ct(getConfig().getString("Messages.no-permissions")));
    				}
    			}
    		}
    		
    		//---- Toggle Chat - Added PRE v0.8 - /vpc toggle {chat} : {true/false}
    		if (cmdLabel.equalsIgnoreCase("vpc") && args.length == 2 || args.length == 3) {
				ConfigurationSection chat = detectChat(args[1]);
    			if(args[0].equalsIgnoreCase("toggle") && chat != null) {
        			if (args.length == 2) {
        				if (p.hasPermission("vippluschat.admin") || p.hasPermission("vippluschat.admin.toggle") || p.hasPermission("vippluschat.toggle." + chat.getString("permission"))) {
        				  if (chat.getBoolean("enabled")) {
        					  chat.set("enabled", false);
           					  saveCustomConfig();
           					  reloadChats();
           					pMessage(getConfig().getString("Messages.toggle").replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")).replace("%STATE%", "false"), p);
        				  } else {
        					  chat.set("enabled", true);
           					  saveCustomConfig();
           					  reloadChats();
           					pMessage(getConfig().getString("Messages.toggle").replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")).replace("%STATE%", "true"), p);
        				  }
        				  return true;
        				} else {
        					p.sendMessage(ct(getConfig().getString("Messages.no-permissions")));
        					return false;
        				}
        			} else if (args.length == 3 ) {
        				try{
            				if (p.hasPermission("vippluschat.admin") || p.hasPermission("vippluschat.admin.toggle") || p.hasPermission("vippluschat.toggle." + chat.getString("permission"))) {  
                					  boolean st = Boolean.valueOf(args[2]);
                					  chat.set("enabled", st);
                					  saveCustomConfig();
                					  reloadChats();
                					  
                					  pMessage(getConfig().getString("Messages.toggle").replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")).replace("%STATE%", args[2]), p);
                			
            				} else {
            					p.sendMessage(ct(getConfig().getString("Messages.no-permissions")));
            				}
            				} catch (NullPointerException e){
            					pMessage(getConfig().getString("Messages.error"), p);
            				}
            				return true;
    				}
    				
        		}
    		}
    		//---- End Toggle Chat
    		
    		//---- Blacklist Add/Rem/Del - Added 7/2/16 v0.8 - /vpc blacklist {+/add/del/rem/delete/remove/-/clear/all} {username} - Updated v0.8.5 - 2/16/2017
    		if (cmdLabel.equalsIgnoreCase("vpc") && (args.length == 3 || args.length == 2)) {
    			if (args[0].equalsIgnoreCase("blacklist")) {
    				if (p.hasPermission("vippluschat.admin") || p.hasPermission("vippluschat.admin.blacklist")) {
	    				if (args[1].equalsIgnoreCase("clear")) { 
	    					modifyBlacklist(null, null, "c");
	    					/* ---- Removed v0.8.5 when there was a complete overhaul to the blacklist and ignorelist system - 2/15/2017
	    					 * getConfig().set("Other.blacklist", ""); 
	    					 * saveConfig();
	    					 */
	    					if (getConfig().getBoolean("Other.broadcast")) {
	            				Broadcast(getConfig().getString("Messages.remove-blacklisted").replace("%PLAYER%", "Everyone"));
	            				return true;
	            			} else {
	            				pMessage(getConfig().getString("Messages.remove-blacklisted").replace("%PLAYER%", "Everyone"), p);
	            				return true;
	            			}
	    				}
	    				if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("+")) { 
	    					if (!inBlacklist(getServer().getOfflinePlayer(args[2]).getUniqueId().toString(), args[2])) {
	    						modifyBlacklist(getServer().getOfflinePlayer(args[2]).getUniqueId().toString(), args[2], "+");
	    						if (getConfig().getBoolean("Other.broadcast")) {
	        						Broadcast(getConfig().getString("Messages.blacklisted").replace("%PLAYER%", args[2]));
	        						return true;
	        					} else {
	        						pMessage(getConfig().getString("Messages.blacklisted").replace("%PLAYER%", args[2]), p);
	        						return true;
	        					}
	    					} else {
	    						//Already in blacklist
	    					}
	    				}
	    					/* ---- Removed v0.8.5 when there was a complete overhaul to the blacklist and ignorelist system - 2/15/2017
	    					if (!getConfig().getString("Other.blacklist").equalsIgnoreCase("")) {
	    						blacklist = getConfig().getString("Other.blacklist"); 
	    					}
	        					
	        					if (getConfig().getString("Other.blacklist").equalsIgnoreCase("")) {
	        					 
	        						getConfig().set("Other.blacklist", args[2]); 
	            					saveConfig();
	            					forceUnIgnore(args[2], -1);
	        					} else {
	        						getConfig().set("Other.blacklist", args[2] + "," + blacklist); 
	            					saveConfig();
	            					forceUnIgnore(args[2], -1);
	        					}
	        					*/
	        					//forceUnIgnore(args[2], -1);
	        					
	            					
	    				if (args[1].equalsIgnoreCase("rem") || args[1].equalsIgnoreCase("del") || args[1].equalsIgnoreCase("delete")|| args[1].equalsIgnoreCase("remove")|| args[1].equalsIgnoreCase("-") ) {
	    					/* ---- Removed v0.8.5 when there was a complete overhaul to the blacklist and ignorelist system - 2/15/2017
	    					if (!getConfig().getString("Other.blacklist").equalsIgnoreCase("")) { 
	    					 
	    						blacklist = getConfig().getString("Other.blacklist"); 
	    					} 
		    					getConfig().set("Other.blacklist", blacklist.replace(args[2] + ",", "").replace(args[2], ""));
	    					saveConfig();
	        				*/
	    					if (inBlacklist(getServer().getOfflinePlayer(args[2]).getUniqueId().toString(), args[2])) {
	    						modifyBlacklist(getServer().getOfflinePlayer(args[2]).getUniqueId().toString(), args[2], "-");
	    						if (getConfig().getBoolean("Other.broadcast")) {
		            				Broadcast(getConfig().getString("Messages.remove-blacklisted").replace("%PLAYER%", args[2]));
		            				return true;
		            			} else {
		            				pMessage(getConfig().getString("Messages.remove-blacklisted").replace("%PLAYER%", args[2]), p);
		            				return true;
		            			}
	    					}
	   					}
	    				if (args[1].equalsIgnoreCase("clear") && args[2].equalsIgnoreCase("all")) {
		    					modifyBlacklist(null, null, "c");
		    					/* ---- Removed v0.8.5 when there was a complete overhaul to the blacklist and ignorelist system - 2/15/2017
		    					 * getConfig().set("Other.blacklist", ""); 
		    					 * saveConfig();
		    					 */
		    					if (getConfig().getBoolean("Other.broadcast")) {
		            				Broadcast(getConfig().getString("Messages.remove-blacklisted").replace("%PLAYER%", "Everyone"));
		            				return true;
		            			} else {
		            				pMessage(getConfig().getString("Messages.remove-blacklisted").replace("%PLAYER%", "Everyone"), p);
		            				return true;
		            			}
	   					}
	    			} else {
						p.sendMessage(ct(getConfig().getString("Messages.no-permissions")));
					}
   				}
   			} 		
    		//---- End Blacklist Add/Rem/Del - /vpc blacklist {+/add/del/rem/delete/remove/-/clear} {username}
    		
    		//---- Remove Player Ignore - Added 7/2/16 v0.8 - /vpc {fui/forceunignore} {chat} {username} - Updated v0.8.5 - 2/15/2017
    		if (cmdLabel.equalsIgnoreCase("vpc") && args.length == 3) {
    			if (args[0].equalsIgnoreCase("fui") || (args[0].equalsIgnoreCase("forceunignore"))) {
    				if (p.hasPermission("vippluschat.admin") || p.hasPermission("vippluschat.admin.forceunignore")) {
						ConfigurationSection chat = detectChat(args[1]);
	    				if (chat != null) {
							if (inIgnorelist(getServer().getOfflinePlayer(args[2]).getUniqueId().toString(), args[2], chat)) {
								modifyIgnorelist(getServer().getOfflinePlayer(args[2]).getUniqueId().toString(), args[2], chat, "-");
							}
							//forceUnIgnore(args[2], chatID); -- Removed v0.8.5 when the ignore system and blacklist system got a huge overhaul    						
							pMessage(getConfig().getString("Messages.force-unignore").replace("%PLAYER%", args[2]).replace("%CHAT_TAG%", chat.getString("layout-tag"))
							.replace("%CHAT_NAME%", chat.getString("name")), p);
						}
    				} else {
    					p.sendMessage(ct(getConfig().getString("Messages.no-permissions")));
    				}
    			}
    		}
    		//---- End Remove Player Ignore
    		
    		//---- Blacklisted - Added 7/2/16 v0.8 - /vpc blacklisted : {all/#/show} - Updated v0.8.2  Modified to accept custom chats - Updated v0.8.5  Modified to accept the new system 2/17/2017
    		if (cmdLabel.equalsIgnoreCase("vpc") && (args.length == 2 || args.length == 1)) {
    			if (args[0].equalsIgnoreCase("blacklisted")) {
    				if (p.hasPermission("vippluschat.admin") || p.hasPermission("vippluschat.admin.blacklisted")) {
	    				int page = 1;
	    				try {
	    				    page = Integer.parseInt(args[1]);
	    				    pMessage(showBlacklisted(p, page), p);
	    				} catch (NumberFormatException e) {
	    				    if (args[1].equalsIgnoreCase("all")) {
	    				    	pMessage(showBlacklisted(p, -1), p);
	    				    	//pMessage(getConfig().getString("Messages.blacklisted-list").replace("%LIST%", getConfig().getString("Other.blacklist").replace(",", ", ")).replace("%COUNT%", String.valueOf(listLength).replace(".0", "") + ""), p);
	    				    	return true;
	    				    } else if (args[1].equalsIgnoreCase("show")) {
	    				    	pMessage(showBlacklisted(p, 1), p);
	    				    } else if (args.length == 1){
	    				    	pMessage(showBlacklisted(p, 1), p);
	    				    	return true;
	    				    } else {
	    				    	return false;
	    				    }
	    				    	
	    				} catch (ArrayIndexOutOfBoundsException d) {
	    					 if (args.length == 1){
		    				    	pMessage(showBlacklisted(p, 1), p);
		    				    	return true;
		    				  } else {
		    					  return false;
		    				  }
	    				}
	    				//pMessage(getConfig().getString("Messages.blacklisted-list").replace("%LIST%", blacklisted).replace("%COUNT%", String.valueOf(listLength).replace(".0", "") + ""), p);
	        			return true;
    				} else {
    					p.sendMessage(ct(getConfig().getString("Messages.no-permissions")));
    				}
    			}
    		}
    		//---- End Blacklisted
    		
    		
    	}
    	return false;
    }

	public List<String> getPlayerNames() {
		List<String> plys = new ArrayList<>();
		for ( Player p : Bukkit.getOnlinePlayers() ) {
			plys.add( p.getName() );
		}
		return plys;
	}

	@Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<String>();
		if (!(sender instanceof Player )) { return completions; }
		Player ply = (Player) sender;
		List<String> plys = getPlayerNames();
		List<String> temp = new ArrayList<String>();
        if (command.getName().equalsIgnoreCase("vpc")) {
            if (args.length == 1) {
                completions.addAll(Arrays.asList("focus", "focused", "ignore", "unignore", "pstatus", "info", "ignoreplayer", "listplayers",
                        "checkver", "reload", "ignored", "blacklist", "fui", "forceunignore", "blacklisted", "colors",
                        "help", "swear"));
            } else if (args.length == 2) {
                switch (args[0].toLowerCase()) {
                    case "focus":
						completions.addAll(getChatList( ply ));
						break;
					case "ignore":
						completions.addAll(getChatList( ply ));
						break;
                    case "unignore":
						completions.addAll(getChatList( ply ));
						break;
                    case "ignored":
						completions.addAll(getChatList( ply ));
						break;
                    case "colors":
						temp = getChatList( ply );
						temp.add( "toggle" );
						completions.addAll(temp);
                        break;
                    case "pstatus":
                        completions.addAll(Arrays.asList("ignored", "blacklisted"));
                        break;
                    case "ignoreplayer":
						temp = plys; 
						temp.add( "clear" );
                        completions.addAll( temp );
                        break;
                    case "listplayers":
						completions.addAll(Arrays.asList("#"));
						break;
                    case "help":
                        completions.addAll(Arrays.asList("1", "2", "3"));
                        break;
                    case "blacklist":
                        completions.addAll(Arrays.asList("+", "add", "del", "delete", "rem", "remove", "clear", "-"));
                        break;
                    case "checkver":
						completions = new ArrayList<String>();
						break;
                    case "reload":
						completions = new ArrayList<String>();
						break;
                    case "blacklisted":
                        completions.addAll(Arrays.asList("show", "#", "all"));
                        break;
                    case "fui":
						completions.addAll(getChatList( ply ));
						break;
                    case "forceunignore":
						completions.addAll(getChatList( ply ));
                        break;
                    case "swear":
                        completions.addAll(Arrays.asList("toggle", "list", "add", "+", "del", "delete", "rem", "remove", "clear"));
                        break;
					case "focused":
						completions = new ArrayList<String>();
						break;
					case "info":
						completions.addAll( plys );
						break;
                }
			} else if ( args.length == 3 && args[0].equalsIgnoreCase( "colors" ) && args[1].equalsIgnoreCase("toggle") ) {
				completions.addAll(getChatList( ply ));
			} else if ( args.length == 4 && args[0].equalsIgnoreCase( "colors" ) && args[1].equalsIgnoreCase("toggle") ) {
				completions.addAll(Arrays.asList("black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white", "obfuscated", "bold", "strikethrough"));
			} else if ( args.length == 3 && ( args[0].equalsIgnoreCase( "blacklist" )) ) {
				completions.addAll( plys );
			} else if ( args.length == 3 && ( args[0].equalsIgnoreCase( "ignored" )) ) {
				completions.addAll(Arrays.asList( "show", "#", "all" ) );
			} else if ( args.length == 3 && ( args[0].equalsIgnoreCase( "fui" ) || args[0].equalsIgnoreCase( "forceunignore" ) ) ) {
				completions.addAll( plys );
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("swear")) {
					
                    switch (args[1].toLowerCase()) {
                        case "toggle":
							completions = new ArrayList<String>();
							break;
                        case "list":
                            completions.addAll(getChatList( ply ));
                            break;
                        case "add":
							completions = new ArrayList<String>();
							break;
                        case "+":
							completions = new ArrayList<String>();
							break;
                        case "del":
							completions = new ArrayList<String>();
							break;
                        case "delete":
							completions = new ArrayList<String>();
							break;
                        case "rem":
							completions = new ArrayList<String>();
							break;
                        case "remove":
							completions = new ArrayList<String>();
							break;
                        case "clear":
                            completions.addAll(getChatList( ply )); // Replace with actual chat options
                            break;
                    }
                }
            } else if (args.length == 4 && args[0].equalsIgnoreCase("swear") && Arrays.asList("add", "+", "del", "delete", "rem", "remove", "clear").contains(args[1].toLowerCase())) {
                completions.addAll(Arrays.asList("exampleWord1", "exampleWord2")); // Replace with actual words
            } else if (args.length == 5 && args[0].equalsIgnoreCase("swear") && Arrays.asList("add", "+", "del", "delete", "rem", "remove", "clear").contains(args[1].toLowerCase())) {
                completions.add("replacementText"); // Replace with actual replacement suggestions
            }
        }
        return completions;
    }

	// Generates the message for the player ingore list - Added v0.8.5 - 2/27/2017
		public String showPlayerIgnore(Player p, int page) {
			String player_ignore = "";
			String thed = "";
			double listLength = getOther(ignorelist_location).getStringList("ignorelist." + p.getUniqueId().toString() + ".players").size();
			double length = getConfig().getDouble("Other.player-ignore-length");
			double pages = Math.ceil((double)listLength/(double)length);
			int i = 1;
			ssOther(ignorelist_location, "ignorelist." +p.getUniqueId().toString() + ".name", p.getName());
			if (getOther(ignorelist_location).getStringList("ignorelist." + p.getUniqueId().toString() + ".players") != null) {
				for (String p_string : getOther(ignorelist_location).getStringList("ignorelist." + p.getUniqueId().toString() + ".players")) {
					String uuid = p_string.split(";")[0];
					String user = p_string.split(";")[1];
					if (i < (getConfig().getInt("Other.player-ignore-length") * page)) {
						if (i >= getConfig().getInt("Other.player-ignore-length") * (page - 1)) {
							if (player_ignore.equalsIgnoreCase("")) {
								player_ignore = getConfig().getString("Messages.player-ignore-list-items").replace("%#%", i + "").replace("%NL%", "\n").replace("%PLAYER_NAME%", user).replace("%PLAYER_UUID%", uuid);
			    			} else {
			    				if (!user.equalsIgnoreCase("")) {
			    					player_ignore = player_ignore + getConfig().getString("Messages.player-ignore-list-items").replace("%#%", i + "").replace("%NL%", "\n").replace("%PLAYER_NAME%", user).replace("%PLAYER_UUID%", uuid);
			    				}
			    			}
						}
					}
					thed = getConfig().getString("Messages.player-ignore-list-layout").replace("%ITEMS%", player_ignore).replace("%PH#%", (pages + "").replace(".0", "")).replace("%PL#%", (page + "").replace(".0", "")).replace("%COUNT%", (listLength + "").replace(".0", "")).replace("%PLAYER_NAME%", p.getName()).replace("%PLAYER_UUID%", p.getUniqueId().toString()).replace("%DISPLAY_NAME%", p.getDisplayName()).replace("%NL%", "\n");
					i++;
				}
			} else {
				thed = "&cNo one was found on the player ignore list\n";
			}
			if (player_ignore.equalsIgnoreCase("")) {
				thed = "&cNo one was found on the player ignore list\n";
			}
			
			return thed;
		}
		//---- End Player Ignore Message Generator
		
	// Generates the message for the blacklisted users - Added v0.8.5 - 2/17/2017
	public String showBlacklisted(Player p, int page) {
			String blacklisted = "";
			if ( getOther(blacklist_location).getConfigurationSection("blacklist") == null ) {
				return getConfig().getString("Messages.blacklisted-layout").replace("%ITEMS%", blacklisted).replace("%PH#%", (0 + "").replace(".0", "")).replace("%PL#%", (page + "").replace(".0", "")).replace("%COUNT%", (0 + "").replace(".0", "")).replace("%PLAYER_NAME%", p.getName()).replace("%PLAYER_UUID%", p.getUniqueId().toString()).replace("%DISPLAY_NAME%", p.getDisplayName()).replace("%NL%", "\n");
			}
			double listLength = getOther(blacklist_location).getConfigurationSection("blacklist").getKeys(false).size();
			double length = getConfig().getDouble("Other.blacklisted-length");
			double pages = Math.ceil((double)listLength/(double)length);
			int i = 1;
			for (String uuid : getOther(blacklist_location).getConfigurationSection("blacklist").getKeys(false)) {
				String user = getOther(blacklist_location).getString("blacklist." + uuid);
				if (i <= (getConfig().getInt("Other.blacklisted-length") * page)) {
					if (i > getConfig().getInt("Other.blacklisted-length") * (page - 1)) {
						if (blacklisted.equalsIgnoreCase("")) {
							blacklisted = getConfig().getString("Messages.blacklisted-items").replace("%#%", i + "").replace("%NL%", "\n").replace("%PLAYER_NAME%", getOther(blacklist_location).getString("blacklist." + uuid)).replace("%PLAYER_UUID%", uuid);
    					} else {
    						if (!user.equalsIgnoreCase("")) {
    							blacklisted = blacklisted + getConfig().getString("Messages.blacklisted-items").replace("%#%", i + "").replace("%NL%", "\n").replace("%PLAYER_NAME%", getOther(blacklist_location).getString("blacklist." + uuid)).replace("%PLAYER_UUID%", uuid);
    						}
    					}
					}
				}
				i++;
			}
			String thed = getConfig().getString("Messages.blacklisted-layout").replace("%ITEMS%", blacklisted).replace("%PH#%", (pages + "").replace(".0", "")).replace("%PL#%", (page + "").replace(".0", "")).replace("%COUNT%", (listLength + "").replace(".0", "")).replace("%PLAYER_NAME%", p.getName()).replace("%PLAYER_UUID%", p.getUniqueId().toString()).replace("%DISPLAY_NAME%", p.getDisplayName()).replace("%NL%", "\n");
			if (blacklisted.equalsIgnoreCase("")) {
				thed = "&cNo one was found on the blacklist\n";
			}
			
		return thed;
	}
	//---- End Blacklisted Message Generator
	
	// Generates the message for the ignored users - Added v0.8.5 - 2/17/2017
	@SuppressWarnings("unused")
	public String showIgnored(Player p, int page, ConfigurationSection chat) {
		String ignored = "";
		//double listLength = getOther(ignorelist_location).getConfigurationSection("ignorelist").getKeys(false).size();
		double length = getConfig().getDouble("Other.ignored-length");
		
		int i = 1;
		int b = 1;
		for (String uuid : getOther(ignorelist_location).getConfigurationSection("ignorelist").getKeys(false)) {
			List<String> chats = getOther(ignorelist_location).getStringList("ignorelist." + uuid + ".chats");
			for (String key : getCustomChats().getKeys(false)) {
				ConfigurationSection tChat = getCustomChats().getConfigurationSection(key);
				if (tChat.getString("name").equalsIgnoreCase(chat.getString("name"))) {
					String user = getOther(ignorelist_location).getString("ignorelist." + uuid + ".username");
					if (i <= (getConfig().getInt("Other.ignored-length") * page)) {
						if (i > getConfig().getInt("Other.ignored-length") * (page- 1)) {
							if (ignored.equalsIgnoreCase("")) {
								ignored = getConfig().getString("Messages.ignored-items").replace("%#%", i + "")
								.replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name"))
								.replace("%NL%", "\n").replace("%PLAYER_NAME%", getOther(ignorelist_location).getString("ignorelist." + uuid + ".username")).replace("%PLAYER_UUID%", uuid);
								i++;
							} else {
								if (!user.equalsIgnoreCase("")) {
									ignored = ignored + getConfig().getString("Messages.ignored-items").replace("%CHAT_TAG%", chat.getString("layout-tag"))
									.replace("%CHAT_NAME%", chat.getString("name")).replace("%#%", i + "").replace("%NL%", "\n")
									.replace("%PLAYER_NAME%", getOther(ignorelist_location).getString("ignorelist." + uuid + ".username")).replace("%PLAYER_UUID%", uuid);
									i++;
								}
							}
						}
					}
				}
				
			}			
			
			b++;
		}
		double pages = Math.ceil((double)(i-1)/(double)length);
		String thed = getConfig().getString("Messages.ignored-layout").replace("%CHAT_TAG%", chat.getString("layout-tag"))
		.replace("%CHAT_NAME%", chat.getString("name")).replace("%ITEMS%", ignored)
		.replace("%PH#%", (pages + "").replace(".0", "")).replace("%PL#%", (page + "").replace(".0", "")).replace("%COUNT%", ((i-1) + "").replace(".0", ""))
		.replace("%PLAYER_NAME%", p.getName()).replace("%PLAYER_UUID%", p.getUniqueId().toString()).replace("%DISPLAY_NAME%", p.getDisplayName()).replace("%NL%", "\n");
		if (ignored.equalsIgnoreCase("")) {
			thed = "&cNo one was found on the ignored list\n";
		}
		
	return thed;
	}
	//---- End Ignored Message Generator
	
	// Generates the message for the swear list - Added v0.8.5 - 2/17/2017
	@SuppressWarnings("unused")
	public String showSwearListServer(int page, ConfigurationSection chat) {
		String word = "";
		String replace = "";
		String items = "";
		//double listLength = getOther(ignorelist_location).getConfigurationSection("ignorelist").getKeys(false).size();
		double length = getOther(chat.getString("anti-swear.server.file-name").replace("%CHAT_NAME%", chat.getString("name"))).getStringList("Dictionary").size();
		double pageLength = getConfig().getInt("Other.antiswear-list-server-length");
		int i = 1;
		for (String swear : getOther(chat.getString("anti-swear.server.file-name").replace("%CHAT_NAME%", chat.getString("name"))).getStringList("Dictionary")) {
			if (i <= (pageLength * page)) { // 15 * 2 = 30
				if (i > pageLength * (page - 1)) { // 15 * 1 = 15
					if (items.equalsIgnoreCase("")) {
						if (swear.split(":").length > 1) {
							items = getConfig().getString("Messages.antiswear-list-items").replace("%#%", i + "").replace("%CHAT_TAG%", chat.getString("ayout-tag"))
							.replace("%CHAT_NAME%",chat.getString("name")).replace("%NL%", "\n").replace("%WORD%", swear.split(":")[0]).replace("%REPLACEMENT%", swear.split(":")[1]);	
						} else {
							items = getConfig().getString("Messages.antiswear-list-items").replace("%#%", i + "").replace("%CHAT_TAG%", chat.getString("layout-tag"))
							.replace("%CHAT_NAME%", chat.getString("name")).replace("%NL%", "\n").replace("%WORD%", swear.split(":")[0]).replace("%REPLACEMENT%", "");
						}
					} else {
						if (length != 0) {
							if (swear.split(":").length > 1) {
								items = items + getConfig().getString("Messages.antiswear-list-items").replace("%#%", i + "").replace("%CHAT_TAG%", chat.getString("layout-tag"))
								.replace("%CHAT_NAME%", chat.getString("name")).replace("%NL%", "\n").replace("%WORD%", swear.split(":")[0]).replace("%REPLACEMENT%", swear.split(":")[1]);	
							} else {
								items = items + getConfig().getString("Messages.antiswear-list-items").replace("%#%", i + "").replace("%CHAT_TAG%", chat.getString("layout-tag"))
								.replace("%CHAT_NAME%", chat.getString("name")).replace("%NL%", "\n").replace("%WORD%", swear.split(":")[0]).replace("%REPLACEMENT%", "");
							}
						}
					}
					
				}
				
			}
			i++;
		}
		double pages = Math.ceil((double)(length)/(double)pageLength);
		String thed = getConfig().getString("Messages.antiswear-list-layout").replace("%CHAT_TAG%", chat.getString("layout-tag"))
		.replace("%CHAT_NAME%", chat.getString("name")).replace("%ITEMS%", items).replace("%PH#%", (pages + "").replace(".0", "")).replace("%PL#%", (page + "").replace(".0", ""))
		.replace("%COUNT%", length + "").replace(".0", "").replace("%NL%", "\n");
		if (items.equalsIgnoreCase("")) {
			thed = "&cThe swear list appears to be empty\n";
		}
		
	return thed;
	}
	//---- End swear list Message Generator
	
	//---- Toggle Focus Mode - v0.8.5 - 2/14/17
	public boolean toggleFocusMode(Player p, String chat) {
		if (focus.containsKey(p)) {
			if (focus.get(p).equalsIgnoreCase(chat)) {
				focus.remove(p);
				if (getConfig().getBoolean("Other.focus.saved")) {
					ssOther(getConfig().getString("Other.focus.location"), p.getUniqueId().toString(), null);
				}
				return false;
			} else {
				focus.remove(p);
				focus.put(p, chat);
				if (getConfig().getBoolean("Other.focus.saved")) {
					ssOther(getConfig().getString("Other.focus.location"), p.getUniqueId().toString() + ".username", p.getName());
					ssOther(getConfig().getString("Other.focus.location"), p.getUniqueId().toString() + ".chat", chat);
				}
				return true;
			}
		} else {
			focus.put(p, chat);
			if (getConfig().getBoolean("Other.focus.saved")) {
				ssOther(getConfig().getString("Other.focus.location"), p.getUniqueId().toString() + ".username", p.getName());
				ssOther(getConfig().getString("Other.focus.location"), p.getUniqueId().toString() + ".chat", chat);
			}
			return true;
		}
	}
	//---- End Toggle Focus Mode

	//---- Toggle Color Codes - v0.8.2
	public boolean toggleColor(String code, ConfigurationSection chat) {
		String[] color_codes = chat.getString("color-codes").split(" ");
		for (String color : color_codes) {
			if (color.equalsIgnoreCase(code + ":true")) {
				String theColors = "";
				for (String newColors : color_codes) {
					if (newColors.equalsIgnoreCase(code + ":true")) { 
						if (theColors.equalsIgnoreCase("")) {
							theColors = newColors.replace("true", "false");
						} else { 
							theColors = theColors + " " + newColors.replace("true", "false");
						} 
					} else {
						if (theColors.equalsIgnoreCase("")) {
							theColors = newColors;
						} else { 
							theColors = theColors + " " + newColors;
						} 
					}
				}
				chat.set("color-codes", theColors);
				saveCustomConfig();
				reloadChats();
				return true;
			} else if (color.equalsIgnoreCase(code + ":false")) { 
				String theColors = "";
				for (String newColors : color_codes) {
					if (newColors.equalsIgnoreCase(code + ":false")) { 
						if (theColors.equalsIgnoreCase("")) {
							theColors = newColors.replace("false", "true");
						} else { 
							theColors = theColors + " " + newColors.replace("false", "true");
						} 
					} else {
						if (theColors.equalsIgnoreCase("")) {
							theColors = newColors;
						} else { 
							theColors = theColors + " " + newColors;
						} 
					}
				}
				chat.set("color-codes", theColors);
				saveCustomConfig();
				reloadChats();
				return true;
			}
		}
		return false;			
	}
	
	//---- End Toggle Color Codes
	
	//---- Used to detect old blacklist or ignore systems and convert them to the new system - Added v0.8.5 - 2/22/2017
	@SuppressWarnings("deprecation")
	public void checkPortOldBlacklistIgnore() {
		if (!getConfig().getString("Other.blacklist").equalsIgnoreCase("")) {
			for (String name : getConfig().getString("Other.blacklist").split(",")) {
				if (!name.equalsIgnoreCase("")) {
					modifyBlacklist(getServer().getOfflinePlayer(name).getUniqueId().toString(),getServer().getOfflinePlayer(name).getName(), "+");
				}
			}
			getConfig().set("Other.blacklist", "");
			saveConfig();
			reloadConfig();
		}
		//---- Modifies the Ignore list using the chat count -- Removed 7/7/2025 v0.8.7
		/*
		int i = 0;
		do {
			for (String name : getCustomChats().getString(i + ".ignore-list").split(",")) {
				if (!name.equalsIgnoreCase("")) {
						if (getServer().getOfflinePlayer(name) != null) {
							modifyIgnorelist(getServer().getOfflinePlayer(name).getUniqueId().toString(), getServer().getOfflinePlayer(name).getName(), i, "+");
						}					
				}
				
			}
			getCustomChats().set(i + ".ignore-list", "");
			saveCustomConfig();
			reloadChats();
			i++;
		} while (i < getCustomChats().getInt("Chat Count"));
		*/

		//---- Modifies the Ignore list -- Updated 7/7/2025 v0.8.7
		for (String key : getCustomChats().getKeys( false )) {
			ConfigurationSection chat = getCustomChats().getConfigurationSection( key );
			for (String name : chat.getString("ignore-list").split(",")) {
				if (!name.equalsIgnoreCase("")) {
					if (getServer().getOfflinePlayer(name) != null) {
						modifyIgnorelist(getServer().getOfflinePlayer(name).getUniqueId().toString(), getServer().getOfflinePlayer(name).getName(), chat, "+");
					}					
				}
			}
			chat.set("ignore-list", "");
			saveCustomConfig();
			reloadChats();
		}
	}
	//---- End detect old blacklist ignore system

	//----- Used to detect old chat.yml and port them over to the new system - v0.8.7


	//---- Used to detect old configs and port them over to the new system - v0.8.2
	public void checkOldConfgAndPort() {
		try {
			
			String a = getConfig().getString("Check Version");
			String b = getConfig().getString("Tag.mod.allow-ignore");
			if (b.equalsIgnoreCase("null")) {
				return;
			}
			File file = new File(getDataFolder(), "config.yml");
			File file2 = new File(getDataFolder(), "oldConfig.yml");
			file.renameTo(file2);
			saveDefaultConfig();
			reloadConfig();
			reloadOld();
			getConfig().set("Check Version", getOldConfig().getBoolean("Check Version"));
			saveConfig();
			getConfig().set("Other.blacklist",getOldConfig().getString("Other.ignore-blacklist").replaceAll("none", ""));
			saveConfig();
			getConfig().set("Messages.no-permission", getOldConfig().getString("Messages.no-permission"));
			saveConfig();
			getConfig().set("Messages.chat-disabled", getOldConfig().getString("Messages.chat-disabled"));
			saveConfig();
			getConfig().set("Messages.reload", getOldConfig().getString("Messages.reload"));
			saveConfig();
			getConfig().set("Messages.toggle" ,getOldConfig().getString("Messages.toggle"));
			saveConfig();
			getConfig().set("Messages.error", getOldConfig().getString("Messages.error"));
			saveConfig();
			getConfig().set("Messages.ignore", getOldConfig().getString("Messages.ignore"));
			saveConfig();
			getConfig().set("Messages.ignore-fail", getOldConfig().getString("Messages.ignore-fail"));
			saveConfig();
			getConfig().set("Messages.ignore-already", getOldConfig().getString("Messages.ignore-already"));
			saveConfig();
			getConfig().set("Messages.ignore-not-found", getOldConfig().getString("Messages.ignore-not-found"));
			saveConfig();
			getConfig().set("Messages.ignore-disabled", getOldConfig().getString("Messages.ignore-disabled"));
			saveConfig();
			getConfig().set("Messages.force-unignore", getOldConfig().getString("Messages.force-unignore").replace("%CHAT%", "%CHAT_NAME%"));
			saveConfig();
			getConfig().set("Messages.remove-blacklisted",getOldConfig().getString("Messages.remove-blacklisted"));
            saveConfig();
			getCustomChats().set("0.allow-ignore", getOldConfig().getBoolean("Tag.mod.allow-ignore"));
			saveCustomConfig();
			getCustomChats().set("0.layout" , getOldConfig().getString("Tag.mod.layout"));
			saveCustomConfig();
			getCustomChats().set("0.enabled" , getOldConfig().getBoolean("Enabled.Mod"));
			saveCustomConfig();
			getCustomChats().set("0.ignore-list" , getOldConfig().getString("Other.ignore.mod").replaceAll("none", ""));
			saveCustomConfig();
		    getCustomChats().set("1.allow-ignore" , getOldConfig().getBoolean("Tag.admin.allow-ignore"));
			saveCustomConfig();
			getCustomChats().set("1.layout" , getOldConfig().getString("Tag.admin.layout"));
			saveCustomConfig();
			getCustomChats().set("1.enabled" , getOldConfig().getBoolean("Enabled.Admin"));
			saveCustomConfig();
			getCustomChats().set("1.ignore-list" , getOldConfig().getString("Other.ignore.admin").replaceAll("none", ""));
			saveCustomConfig();
			getCustomChats().set("2.allow-ignore" , getOldConfig().getBoolean("Tag.owner.allow-ignore"));
			saveCustomConfig();
			getCustomChats().set("2.layout" , getOldConfig().getString("Tag.owner.layout"));
			saveCustomConfig();
			getCustomChats().set("2.enabled" , getOldConfig().getBoolean("Enabled.Owner"));
			saveCustomConfig();
			getCustomChats().set("2.ignore-list" , getOldConfig().getString("Other.ignore.owner").replaceAll("none", ""));
			saveCustomConfig();
			getCustomChats().set("3.allow-ignore" , getOldConfig().getBoolean("Tag.staff.allow-ignore"));
			saveCustomConfig();
			getCustomChats().set("3.layout" , getOldConfig().getString("Tag.staff.layout"));
			saveCustomConfig();
			getCustomChats().set("3.enabled" , getOldConfig().getBoolean("Enabled.Staff"));
			saveCustomConfig();
			getCustomChats().set("3.ignore-list" , getOldConfig().getString("Other.ignore.staff").replaceAll("none", ""));
			saveCustomConfig();
			getCustomChats().set("4.allow-ignore" , getOldConfig().getBoolean("Tag.dev.allow-ignore"));
			saveCustomConfig();
			getCustomChats().set("4.layout" , getOldConfig().getString("Tag.dev.layout"));
			saveCustomConfig();
			getCustomChats().set("4.enabled" , getOldConfig().getBoolean("Enabled.Dev"));
			saveCustomConfig();
			getCustomChats().set("4.ignore-list" , getOldConfig().getString("Other.ignore.dev").replaceAll("none", ""));
			saveCustomConfig();
			getCustomChats().set("5.allow-ignore" , getOldConfig().getBoolean("Tag.vip.allow-ignore"));
			saveCustomConfig();
			getCustomChats().set("5.layout" , getOldConfig().getString("Tag.vip.layout"));
			saveCustomConfig();
			getCustomChats().set("5.enabled" , getOldConfig().getBoolean("Enabled.VIP"));
			saveCustomConfig();
			getCustomChats().set("5.ignore-list" , getOldConfig().getString("Other.ignore.vip").replaceAll("none", ""));
			saveCustomConfig();
			getCustomChats().set("6.allow-ignore" , getOldConfig().getBoolean("Tag.donator.allow-ignore"));
			saveCustomConfig();
			getCustomChats().set("6.layout" , getOldConfig().getString("Tag.donator.layout"));
			saveCustomConfig();
			getCustomChats().set("6.enabled" , getOldConfig().getBoolean("Enabled.Donator"));
			saveCustomConfig();
			getCustomChats().set("6.ignore-list" , getOldConfig().getString("Other.ignore.donator").replaceAll("none", ""));
			saveCustomConfig();
			getCustomChats().set("7.allow-ignore" , getOldConfig().getBoolean("Tag.special.allow-ignore"));
			saveCustomConfig();
			getCustomChats().set("7.layout" , getOldConfig().getString("Tag.special.layout"));
			saveCustomConfig();
			getCustomChats().set("7.enabled" , getOldConfig().getBoolean("Enabled.Special"));
			saveCustomConfig();
			getCustomChats().set("7.ignore-list" , getOldConfig().getString("Other.ignore.special").replaceAll("none", ""));
			saveCustomConfig();
			getCustomChats().set("8.allow-ignore" , getOldConfig().getBoolean("Tag.elite.allow-ignore"));
			saveCustomConfig();
			getCustomChats().set("8.layout" , getOldConfig().getString("Tag.elite.layout"));
			saveCustomConfig();
			getCustomChats().set("8.enabled" , getOldConfig().getBoolean("Enabled.Elite"));
			saveCustomConfig();
			getCustomChats().set("8.ignore-list" , getOldConfig().getString("Other.ignore.elite").replaceAll("none", ""));
			saveCustomConfig();
			File file3 = new File(getDataFolder(), "oldConfig.yml");
			File file4 = new File(getDataFolder(), "oldConfig.yml.bak");
			file3.renameTo(file4);
		} catch (NullPointerException failed) {
			
		}
	}
	
	//---- End Detect Old Config
	
	//---- Check and port configs to new config system! - v0.8.2
	
	public void portConfig() {
	}
	
	//---- End Check and port configs to new config system!
	
	//---- Check black list to see if the player is on the blacklist - v0.8.2
	@Deprecated // -- Deprecated in v0.8.5 when the ignore system and blacklist system were rewritten to use custom files. - 2/15/2017
	public boolean checkBlacklist(String player) {
		List<String> blacklisted = Arrays.asList(getConfig().getString("Other.blacklist").split(","));
		for (String p : blacklisted) {
			if (p.toLowerCase().equalsIgnoreCase(player.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	//---- End blacklist check player
		
	//---- Check blacklist options to see if they can view chat - v0.8.2 - Updated v0.8.5 - 2/15/2017
	
	@SuppressWarnings("deprecation")
	public boolean checkView(String player) {
		if (inBlacklist(getServer().getOfflinePlayer(player).getUniqueId().toString(), player) & getConfig().getBoolean("Other.Blacklist-Settings.can-view-chats.enabled")) {
			return true;
		} else if (inBlacklist(getServer().getOfflinePlayer(player).getUniqueId().toString(), player) & !getConfig().getBoolean("Other.Blacklist-Settings.can-view-chats.enabled")){
			return false;
		}
		return false;
	}
	
	//---- End blacklist check options view chat
			
	//---- Check blacklist options to see if they can view chat is specified - v0.8.2 - Updated v0.8.5 - 2/15/2017
	
	@SuppressWarnings("deprecation")
	public boolean checkViewChatSpecified(String player, String chat) {
		if (inBlacklist(getServer().getOfflinePlayer(player).getUniqueId().toString(), player)) {
			if (!getConfig().getBoolean("Other.Blacklist-Settings.can-view-chats.enabled")) {
				return true;
			}
			if (checkView(player)) {
				for (String c : getConfig().getStringList("Other.Blacklist-Settings.can-view-chats.specified-chats")) {
					if (c.toLowerCase().equalsIgnoreCase(chat.toLowerCase())) { return true; }
				}
				return false;
			}
		}
		
		return true;
	}
			
	//---- End blacklist check options view chat if chat is specified
		
	//---- Check blacklist options to see if they can talk chat - v0.8.2 - Updated v0.8.5 - 2/15/2017

	@SuppressWarnings("deprecation")
	public boolean checkTalk(String player) {
		if (inBlacklist(getServer().getOfflinePlayer(player).getUniqueId().toString(), player) & getConfig().getBoolean("Other.Blacklist-Settings.can-talk-in-chats.enabled")) {
			return true;
		} else if (inBlacklist(getServer().getOfflinePlayer(player).getUniqueId().toString(), player) & !getConfig().getBoolean("Other.Blacklist-Settings.can-talk-in-chats.enabled")){
			return false;
		}
		return false;
	}
	
	//---- End blacklist check options talk chat

	//---- Check blacklist options to see if they can talk chat is specified - v0.8.2 - Updated v0.8.5 - 2/15/2017
	
	@SuppressWarnings("deprecation")
	public boolean checkTalkChatSpecified(String player, String chat) {
		if (inBlacklist(getServer().getOfflinePlayer(player).getUniqueId().toString(), player)) {
			if (!getConfig().getBoolean("Other.Blacklist-Settings.can-talk-in-chats.enabled")) {
				return true;
			}
			if (checkTalk(player)) {
				for (String c : getConfig().getStringList("Other.Blacklist-Settings.can-talk-in-chats.specified-chats")) {
					if (c.toLowerCase().equalsIgnoreCase(chat.toLowerCase())) { return true; }
				}
				return false;
			}
		}
		
		return true;
	}
			
	//---- End blacklist check options talk chat if chat is specified
    
	//---- Check ignore options to see if they can ignore chat - v0.8.2 - Updated v0.8.5 - 2/15/2017
	
	@SuppressWarnings("deprecation")
	public boolean checkIgnore(String player) {
		if (inBlacklist(getServer().getOfflinePlayer(player).getUniqueId().toString(), player) & getConfig().getBoolean("Other.Blacklist-Settings.can-ignore-chats.enabled")) {
			return true;
		} else if (inBlacklist(getServer().getOfflinePlayer(player).getUniqueId().toString(), player) & !getConfig().getBoolean("Other.Blacklist-Settings.can-ignore-chats.enabled")){
			return false;
		}
		return false;
	}
	
	//---- End ignore check options ignore chat
		
	//---- Check ignore options to see if they can ignore chat is specified - v0.8.2 - Updated v0.8.5 - 2/15/2017
	@SuppressWarnings("deprecation")
	public boolean checkIgnoreChatSpecified(String player, String chat) {
		if (inBlacklist(getServer().getOfflinePlayer(player).getUniqueId().toString(), player)) {
			if (!getConfig().getBoolean("Other.Blacklist-Settings.can-ignore-chats.enabled")) {
				return true;
			} else {
				if (inIgnorelist(getServer().getOfflinePlayer(player).getUniqueId().toString(), player, detectChat(chat))) {
					for (String c : getConfig().getStringList("Other.Blacklist-Settings.can-ignore-chats.specified-chats")) {
						if (c.toLowerCase().equalsIgnoreCase(chat.toLowerCase())) { return true; }
					}
					return false;
				}
				return false;
			}
		}
		
		return true;
	}
	
	//---- End ignore check options ignore chat if chat is specified
	
	@SuppressWarnings("unused")
	//---- This is called when force unignoring chats. This was modified to accept custom chats in v0.8.2 - Created in v0.8
	@Deprecated // -- Depracated after huge overhaul of ignore and black list system in v0.8.5 - 2/15/2017
	private void forceUnIgnore(String player, int id) {
		if (id == -1) {
			int i = 0;
			if (player.equalsIgnoreCase("*")) {
				do {
					getCustomChats().set(i + ".ignore-list", "");
					saveCustomConfig();
				} while (i++ < getCustomChats().getInt("Chat Count") - 1);
				
			} else {
				do {
					getCustomChats().set(i + ".ignore-list", getCustomChats().getString(i + ".ignore-list").toLowerCase().replace(player.toLowerCase() + ",", "").replace(player.toLowerCase(), ""));
					saveCustomConfig();
				} while (i++ < getCustomChats().getInt("Chat Count") - 1);
			}
			return;
		}
		if (player.equalsIgnoreCase("*")) {
			getCustomChats().set(id + ".ignore-list", "");
			saveCustomConfig();
		} else {
			if (getCustomChats().getString(id + ".ignore-list").equalsIgnoreCase("")){ 
				
			} else {
				getCustomChats().set(id + ".ignore-list", getCustomChats().getString(id + ".ignore-list").toLowerCase().replace(player.toLowerCase() + ",", "").replace(player.toLowerCase(), ""));
				saveCustomConfig();
				if (getCustomChats().getString(id + ".ignore-list").equalsIgnoreCase("null")) {
					getCustomChats().set(id + ".ignore-list", "");
					saveCustomConfig();
				}
			}
		}
		
		/* - Removed v0.8.2 - Removed with when custom chats were added
	    switch (chat.toLowerCase()) {
		case "mod":
			getConfig().set("Other.ignore.mod", getConfig().getString("Other.ignore.mod").replace(a.getName() + ",", "").replace(a.getName(), ""));
			saveConfig();
			break;
		case "all":
			getConfig().set("Other.ignore.elite", getConfig().getString("Other.ignore.elite").replace(a.getName() + ",", "").replace(a.getName(), ""));
			saveConfig();
			getConfig().set("Other.ignore.special", getConfig().getString("Other.ignore.special").replace(a.getName() + ",", "").replace(a.getName(), ""));
			saveConfig();
			getConfig().set("Other.ignore.donator", getConfig().getString("Other.ignore.donator").replace(a.getName() + ",", "").replace(a.getName(), ""));
			saveConfig();
			getConfig().set("Other.ignore.vip", getConfig().getString("Other.ignore.vip").replace(a.getName() + ",", "").replace(a.getName(), ""));
			saveConfig();
			getConfig().set("Other.ignore.dev", getConfig().getString("Other.ignore.dev").replace(a.getName() + ",", "").replace(a.getName(), ""));
			saveConfig();
			getConfig().set("Other.ignore.staff", getConfig().getString("Other.ignore.staff").replace(a.getName() + ",", "").replace(a.getName(), ""));
			saveConfig();
			getConfig().set("Other.ignore.owner", getConfig().getString("Other.ignore.owner").replace(a.getName() + ",", "").replace(a.getName(), ""));
			saveConfig();
			getConfig().set("Other.ignore.admin", getConfig().getString("Other.ignore.admin").replace(a.getName() + ",", "").replace(a.getName(), ""));
			saveConfig();
			getConfig().set("Other.ignore.mod", getConfig().getString("Other.ignore.mod").replace(a.getName() + ",", "").replace(a.getName(), ""));
			saveConfig();
			break;
			
		}*/
	}
	//---- End forceunignore
	//---- Used to color not message chats - Added v0.7
	public static String ct(String text) {
		return text.replace("&0", ChatColor.BLACK + "").replace("&1", ChatColor.DARK_BLUE + "").replace("&2", ChatColor.DARK_GREEN + "").replace("&3", ChatColor.DARK_AQUA + "").replace("&4", ChatColor.DARK_RED + "").replace("&5", ChatColor.DARK_PURPLE + "").replace("&6", ChatColor.GOLD + "").replace("&7", ChatColor.GRAY + "").replace("&8", ChatColor.DARK_GRAY + "").replace("&9", ChatColor.BLUE + "").replace("&a", ChatColor.GREEN + "").replace("&b", ChatColor.AQUA + "").replace("&c", ChatColor.RED + "").replace("&d", ChatColor.LIGHT_PURPLE + "").replace("&e", ChatColor.YELLOW + "").replace("&f", ChatColor.WHITE + "").replace("&l", ChatColor.BOLD + "").replace("&m", ChatColor.STRIKETHROUGH + "").replace("&n", ChatColor.UNDERLINE + "").replace("&o", ChatColor.ITALIC + "").replace("&r", ChatColor.RESET + "").replace("&k", ChatColor.MAGIC + "");
	}
	//---- End of color
	//---- Used to broadcast message - Added v0.7
	public static void Broadcast(String msg) {
		String tag = ct("&8[&9VPC&8] &2");
		Bukkit.getServer().broadcastMessage(tag + ct(msg));
	}
	//---- End of broad cast
	//---- Used to detect what colors are enabled for what chats. This was modified in v0.8.2 to accept custom chats
	public Boolean checkColorCodes(String code, ConfigurationSection chat) {
		System.out.println(code + " - End of list");
		for (String precolors: Arrays.asList(chat.getString("color-codes").split(" "))) {
			List<String> temp = Arrays.asList(precolors.split(":"));
			String colors = temp.get(0);
			Boolean colorsState = Boolean.parseBoolean(temp.get(1));
			if (colors.equalsIgnoreCase(code) && colorsState) {
				return true;
			}
        }
		return false;
	}
	//---- End of color checking
	//---- Ingore Check
	@Deprecated // -- Deprecated in v0.8.5 when the ignore system and blacklist system were rewritten to use custom files. - 2/15/2017
	public Boolean checkIgnore(String user, int id) {
		boolean b = false;
		
		for (String a: getCustomChats().getString(id + ".ignore-list").toLowerCase().split(",")) {
			if (a.equalsIgnoreCase(user)) {
				b = true;
			}
        }
		if (b) {
			return true;
		}
		return false;
	}
	//---- Ignore Check End
	
	//---- Output Color Codes - Added v0.8.2
	public String outputColorCodes(ConfigurationSection chat) {
		String text = chat.getString("name") + " Current Color Code Status\n";
		@SuppressWarnings("unused")
		int i = 0;
		for (String precolors: Arrays.asList(chat.getString("color-codes").split(" "))) {
			List<String> temp = Arrays.asList(precolors.split(":"));
			String colors = temp.get(0);
			Boolean colorsState = Boolean.parseBoolean(temp.get(1));
			if (colors.equalsIgnoreCase("black") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&91. &0Black").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("black") & !colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&91. &0Black").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("dark_blue") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&92. &1Dark Blue").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("dark_blue") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&92. &1Dark Blue").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("dark_green") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&93. &2Dark Green").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("dark_green") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&93. &2Dark Green").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("dark_aqua") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&94. &3Dark Aqua").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("dark_aqua") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&94. &3Dark Aqua").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("dark_red") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&95. &4Dark Red").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("dark_red") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&95. &4Dark Red").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("dark_purple") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&96. &5Dark Purple").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("dark_purple") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&96. &5Dark Purple").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("gold") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&97. &6Gold").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("gold") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&97. &6Gold").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("gray") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&98. &7Gray").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("gray") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&98. &7Gray").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("dark_gray") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&99. &8Dark Gray").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("dark_gray") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&99. &8Dark Gray").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("blue") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&910. &9Blue").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("blue") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&910. &9Blue").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("green") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&911. &aGreen").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("green") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&911. &aGreen").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("aqua") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&912. &bAqua").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("aqua") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&912. &bAqua").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("red") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&913. &cRed").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("red") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&913. &cRed").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("light_purple") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&914. &dLight Purple").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("light_purple") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&914. &dLight Purple").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("yellow") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&915. &eYellow").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("yellow") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&915. &eYellow").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("white") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&916. &fWhite").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("white") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&916. &fWhite").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("obfuscated") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&917. &fObfuscated").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("obfuscated") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&917. &fObfuscated").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("bold") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&918. &f&lBold").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("bold") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&918. &f&lBold").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("strikethrough") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&919. &f&mStrikethrough").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("strikethrough") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&919. &f&mStrikethrough").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("underline") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&920. &f&nUnderline").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("underline") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&920. &f&nUnderline").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("italic") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&921. &f&oItalic").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("italic") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&921. &f&oItalic").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			if (colors.equalsIgnoreCase("reset") & colorsState) { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&922. &f&rReset").replace("%STATE%", "&aEnabled").replace("%NL%", "\n");} else if (colors.equalsIgnoreCase("reset") & !colorsState)  { text = text + getConfig().getString("Messages.colors").replace("%COLOR%", "&922. &f&rReset").replace("%STATE%", "&cDisabled").replace("%NL%", "\n");}
			i++;
        }
		return ct(text);
	}
	//---- End Output Color Codes
		
	//---- Used to send messages - Modified in v0.8.2 to accept custom chats
	public String chatCleaner(String text , ConfigurationSection chat) {
		if (text.contains("&0") && checkColorCodes("black", chat) == true) { text = text.replace("&0", ChatColor.BLACK + ""); } else { text = text.replace("&0", "");}
		if (text.contains("&1") && checkColorCodes("dark_blue", chat) == true) { text = text.replace("&1", ChatColor.DARK_BLUE + ""); } else { text = text.replace("&1", "");}
		if (text.contains("&2") && checkColorCodes("dark_green", chat) == true) { text = text.replace("&2", ChatColor.DARK_GREEN + ""); } else { text = text.replace("&2", "");}
		if (text.contains("&3") && checkColorCodes("dark_aqua", chat) == true) { text = text.replace("&3", ChatColor.DARK_AQUA + ""); } else { text = text.replace("&3", "");}
		if (text.contains("&4") && checkColorCodes("dark_red", chat) == true) { text = text.replace("&4", ChatColor.DARK_RED + ""); } else { text = text.replace("&4", "");}
		if (text.contains("&5") && checkColorCodes("dark_purple", chat) == true) { text = text.replace("&5", ChatColor.DARK_PURPLE + ""); } else { text = text.replace("&5", "");}
		if (text.contains("&6") && checkColorCodes("gold", chat) == true) { text = text.replace("&6", ChatColor.GOLD + ""); } else { text = text.replace("&6", "");}
		if (text.contains("&7") && checkColorCodes("gray", chat) == true) { text = text.replace("&7", ChatColor.GRAY + ""); } else { text = text.replace("&7", "");}
		if (text.contains("&8") && checkColorCodes("dark_gray", chat) == true) { text = text.replace("&8", ChatColor.DARK_GRAY + ""); } else { text = text.replace("&8", "");}
		if (text.contains("&9") && checkColorCodes("blue", chat) == true) { text = text.replace("&9", ChatColor.BLUE + ""); } else { text = text.replace("&9", "");}
		if (text.contains("&a") && checkColorCodes("green", chat) == true) { text = text.replace("&a", ChatColor.GREEN + ""); } else { text = text.replace("&a", "");}
		if (text.contains("&b") && checkColorCodes("aqua", chat) == true) { text = text.replace("&b", ChatColor.AQUA + ""); } else { text = text.replace("&b", "");}
		if (text.contains("&c") && checkColorCodes("red", chat) == true) { text = text.replace("&c", ChatColor.RED + ""); } else { text = text.replace("&c", "");}
		if (text.contains("&d") && checkColorCodes("light_purple", chat) == true) { text = text.replace("&d", ChatColor.LIGHT_PURPLE + ""); } else { text = text.replace("&d", "");}
		if (text.contains("&e") && checkColorCodes("yellow", chat) == true) { text = text.replace("&e", ChatColor.YELLOW + ""); } else { text = text.replace("&e", "");}
		if (text.contains("&f") && checkColorCodes("white", chat) == true) { text = text.replace("&f", ChatColor.WHITE + ""); } else { text = text.replace("&f", "");}
		if (text.contains("&k") && checkColorCodes("obfuscated", chat) == true) { text = text.replace("&k", ChatColor.MAGIC + ""); } else { text = text.replace("&k", "");}
		if (text.contains("&l") && checkColorCodes("bold", chat) == true) { text = text.replace("&l", ChatColor.BOLD + ""); } else { text = text.replace("&l", "");}
		if (text.contains("&m") && checkColorCodes("strikethrough", chat) == true) { text = text.replace("&m", ChatColor.STRIKETHROUGH + ""); } else { text = text.replace("&m", "");}
		if (text.contains("&n") && checkColorCodes("underline", chat) == true) { text = text.replace("&n", ChatColor.UNDERLINE + ""); } else { text = text.replace("&n", "");}
		if (text.contains("&o") && checkColorCodes("italic", chat) == true) { text = text.replace("&o", ChatColor.ITALIC + ""); } else { text = text.replace("&o", "");}
		if (text.contains("&r") && checkColorCodes("reset", chat) == true) { text = text.replace("&r", ChatColor.RESET + ""); } else { text = text.replace("&r", "");}
		//---- RGB Supported Coming Soon -- Started 7/7/2025
		//if (text.contains("&#") && checkColorCodes("reset", chat) == true) { text = text.replace("&r", ChatColor.RESET + ""); } else { text = text.replace("&r", "");}
		
		return text;
	}
	public void pMessage(String msg, Player p) { 
		p.sendMessage(ct("&8[&9VPC&8] &2" + msg));
	}
	public String pSend(String msg, Player p1, Player p2, String layout, ConfigurationSection chat) {
		String tag = ct(chat.getString(layout)).replace("%PLAYER%", p2.getName()).replace("%DISPLAY_NAME%", p2.getDisplayName()).replace("%MESSAGE%", chatCleaner(msg, chat)).replace("%WORLD%", p1.getWorld().getName()).replace("%GAMEMODE%", p1.getGameMode().toString());
		/* - Removed v0.8.2 - Removed wheen we added the custom chats - 10/3/16
		if(rank == "Mod") { tag = ct(getConfig().getString("Tag.mod.layout")).replace("%PLAYER%", p2.getName()).replace("%MESSAGE%", chatCleaner(msg, "mod")).replace("%WORLD%", p1.getWorld().getName()).replace("%GAMEMODE%", p1.getGameMode().toString()); }
		if(rank == "Admin") { tag = ct(getConfig().getString("Tag.admin.layout")).replace("%PLAYER%", p2.getName()).replace("%MESSAGE%", chatCleaner(msg, "admin")).replace("%WORLD%", p1.getWorld().getName()).replace("%GAMEMODE%", p1.getGameMode().toString()); }
		if(rank == "Staff") { tag = ct(getConfig().getString("Tag.staff.layout")).replace("%PLAYER%", p2.getName()).replace("%MESSAGE%", chatCleaner(msg, "staff")).replace("%WORLD%", p1.getWorld().getName()).replace("%GAMEMODE%", p1.getGameMode().toString()); }
		if(rank == "Owner") { tag = ct(getConfig().getString("Tag.owner.layout")).replace("%PLAYER%", p2.getName()).replace("%MESSAGE%", chatCleaner(msg, "owner")).replace("%WORLD%", p1.getWorld().getName()).replace("%GAMEMODE%", p1.getGameMode().toString()); }
		if(rank == "Dev") { tag = ct(getConfig().getString("Tag.dev.layout")).replace("%PLAYER%", p2.getName()).replace("%MESSAGE%", chatCleaner(msg, "dev")).replace("%WORLD%", p1.getWorld().getName()).replace("%GAMEMODE%", p1.getGameMode().toString()); }
		if(rank == "VIP") { tag = ct(getConfig().getString("Tag.vip.layout")).replace("%PLAYER%", p2.getName()).replace("%MESSAGE%", chatCleaner(msg, "vip")).replace("%WORLD%", p1.getWorld().getName()).replace("%GAMEMODE%", p1.getGameMode().toString()); }
		if(rank == "Donator") { tag = ct(getConfig().getString("Tag.donator.layout")).replace("%PLAYER%", p2.getName()).replace("%MESSAGE%", chatCleaner(msg, "donator")).replace("%WORLD%", p1.getWorld().getName()).replace("%GAMEMODE%", p1.getGameMode().toString()); }
		if(rank == "Special") { tag = ct(getConfig().getString("Tag.special.layout")).replace("%PLAYER%", p2.getName()).replace("%MESSAGE%", chatCleaner(msg, "special")).replace("%WORLD%", p1.getWorld().getName()).replace("%GAMEMODE%", p1.getGameMode().toString()); }
		if(rank == "Elite") { tag = ct(getConfig().getString("Tag.elite.layout")).replace("%PLAYER%", p2.getName()).replace("%MESSAGE%", chatCleaner(msg, "elite")).replace("%WORLD%", p1.getWorld().getName()).replace("%GAMEMODE%", p1.getGameMode().toString()); }
		*/
		System.out.println(tag);
		p1.sendMessage(tag);
		return "";
	}
    
	@SuppressWarnings("deprecation")
	public void sendMessages(String msg, Player player, String permission, String layout, ConfigurationSection chat) {
    	if(player.hasPermission(permission) && !inIgnorelist(player.getUniqueId().toString(), player.getName(), chat)) {
    		if (checkTalkChatSpecified(player.getName(), chat.getString("name").toLowerCase())) {
	    		if (chat.getBoolean("enabled")) {
	    			for(Player p : Bukkit.getOnlinePlayers()) {
	    	    		if(p.hasPermission(permission) && !inIgnorelist(p.getUniqueId().toString(), p.getName(), chat) && checkViewChatSpecified(p.getName().toLowerCase(), chat.getString("name").toLowerCase()) && getPlayerIgnoreStatus(getServer().getOfflinePlayer(player.getName()), p).equalsIgnoreCase("&cunignored")) {
    	    				pSend(msg, p, player, layout, chat);	    	    			
	    	    		}
	    	    	}
	    		} else { 
	    			pMessage(getConfig().getString("Messages.chat-disabled").replace("%CHAT_TAG%", chat.getString("layout-tag")).replace("%CHAT_NAME%", chat.getString("name")).replace("%PLAYER%", player.getName()).replace("%DISPLAY_PLAYER%", player.getDisplayName().replace("%STATE%", "Disabled")).replace("%STATUS%", "").replace("%LIST%", "").replace("%STATUS%", ""), player);    			
	    		}
    		} else { 
    			pMessage("You are currently blacklisted from this chat and may not chat in this chat.", player);
    		}
    	} else {
    		pMessage("You are currently ignoring this chat.", player);
    	}
    }
    
    //---- End of sending messages
    
	
	// Modify Blacklist - v0.8.5 - 2/15/2017 - Added to simplify blacklist system
		public void modifyBlacklist(String uuid, String name, String modification) {
			switch (modification) {
			case "+":
				ssOther(blacklist_location, "blacklist." + uuid, name);
				break;
			case "-":
				ssOther(blacklist_location, "blacklist." + uuid, null);
				break;
			case "c":
				delOther(blacklist_location);
				loadOther();
				break;
			}
		}
		//---- End Modify Blacklist
		
		// Modify IgnoreList - v0.8.5 - 2/15/2017 - Added to simplify ignore list system
		public void modifyIgnorelist(String uuid, String name, ConfigurationSection chat, String modification) {
			List<String> l = new ArrayList<>();
			switch (modification) {
			case "+":
				ssOther(ignorelist_location, "ignorelist." + uuid + ".username", name);
				if (getOther(ignorelist_location).get("ignorelist." + uuid) != null) {
					l = getOther(ignorelist_location).getStringList("ignorelist." + uuid + ".chats");
					for (String c : l) {
						if (c.equalsIgnoreCase(chat.getString("name"))) {
							return;
						}
					}
					l.add(chat.getString("name"));
				} else {
					l.add(chat.getString("name"));
				}
				ssOther(ignorelist_location, "ignorelist." + uuid + ".chats", l);
				break;
			case "-":
				try {
					ssOther(ignorelist_location, "ignorelist." + uuid + ".username", name);
					if (getOther(ignorelist_location).contains("ignorelist." + uuid)) {
						l = getOther(ignorelist_location).getStringList("ignorelist." + uuid + ".chats");
						if (l.size() == 1 & l.get(0).equalsIgnoreCase(chat.getString("name"))) {
							if (getOther(ignorelist_location).get("ignorelist." + uuid + ".players") != null) {
								ssOther(ignorelist_location, "ignorelist." + uuid + ".chats", new ArrayList<>());
							} else {
								ssOther(ignorelist_location, "ignorelist." + uuid, null);
							}
							return;
						} else if (l.size() == 1 && !l.get(0).equalsIgnoreCase(chat.getString("name"))){
							ssOther(ignorelist_location, "ignorelist." + uuid + ".username", name);
							return;
						}
						l.remove(chat.getString("name"));
						ssOther(ignorelist_location, "ignorelist." + uuid + ".chats", l);	
					} else {
					}
				} catch (NullPointerException e) {
					
				}
				
				
				break;
			case "c":
				if (getOther(ignorelist_location).get("ignorelist." + uuid + ".players") != null) {
					ssOther(blacklist_location, "ignorelist." + uuid + ".chats", new ArrayList<>());
				} else {
					ssOther(blacklist_location, "ignorelist." + uuid, null);
				}
				
				break;
			}
		}
		//---- End Modify ignore list
		
		// Detect Player Blacklist - v0.8.5 - 2/15/2017 - Added to detect if a player is inside the blacklist
		public boolean inBlacklist(String uuid, String name) {
			if (getOther(blacklist_location).contains("blacklist." + uuid)) {
				return true;
			}
			return false;
		}
		// ---- End Detect Player Blacklist
		
		// Detect Player Blacklist - v0.8.5 - 2/15/2017 - Added to detect if a player is inside the blacklist
		public boolean inIgnorelist(String uuid, String name, ConfigurationSection chat) {
			if (getOther(ignorelist_location).contains("ignorelist." + uuid)) {
				for (String c : getOther(ignorelist_location).getStringList("ignorelist." + uuid + ".chats")) {
					if (c.equalsIgnoreCase(chat.getString("name"))) {
						return true;
					}
				}
			}
			return false;
		}
		// ---- End Detect Player Blacklist
		
		public void modifyPlayerIgnore(OfflinePlayer offline, Player actor, String action) {
			String uuid = actor.getUniqueId().toString();
			String name = actor.getName();
			String offline_uuid = offline.getUniqueId().toString();
			String offline_name = offline.getName();
			List<String> l = new ArrayList<>();
			switch (action) {
			case "+":
				ssOther(ignorelist_location, "ignorelist." + uuid + ".username", name);
				if (getOther(ignorelist_location).get("ignorelist." + uuid + ".chats") == null) {
					ssOther(ignorelist_location, "ignorelist." + uuid + ".chats", new ArrayList<>());
				}
				
				if (getOther(ignorelist_location).get("ignorelist." + uuid) == null) {
					ssOther(ignorelist_location, "ignorelist." + uuid + ".username", name);
					ssOther(ignorelist_location, "ignorelist." + uuid + ".chats", new ArrayList<>());
				}
					if (getPlayerIgnoreStatus(offline, actor).equalsIgnoreCase("&aignored")) {
						return;
					} else {
						if (getOther(ignorelist_location).get("ignorelist." + uuid + ".players") != null) {
							l = getOther(ignorelist_location).getStringList("ignorelist." + uuid + ".players");
							l.add(offline_uuid + ";" + offline_name);
							ssOther(ignorelist_location, "ignorelist." + uuid + ".players", l);
						} else {
							l.clear();
							l.add(offline_uuid + ";" + offline_name);
							ssOther(ignorelist_location, "ignorelist." + uuid + ".players", l);
						}
					}
				break;
			case "-":
				ssOther(ignorelist_location, "ignorelist." + uuid + ".username", name);
				if (getOther(ignorelist_location).get("ignorelist." + uuid + ".chats") == null) {
					ssOther(ignorelist_location, "ignorelist." + uuid + ".chats", new ArrayList<>());
				}
					if (getOther(ignorelist_location).get("ignorelist." + uuid) != null) {
						if (getPlayerIgnoreStatus(offline, actor).equalsIgnoreCase("&aignored")) {
							if (getOther(ignorelist_location).get("ignorelist." + uuid + ".players") != null) {
								l = getOther(ignorelist_location).getStringList("ignorelist." + uuid + ".players");
								l.remove(offline_uuid + ";" + offline_name);
								ssOther(ignorelist_location, "ignorelist." + uuid + ".players", l);
							}
						}
					}
				break;
			case "clear":
				ssOther(ignorelist_location, "ignorelist." + uuid + ".username", name);
				if (getOther(ignorelist_location).get("ignorelist." + uuid + ".chats") == null) {
					ssOther(ignorelist_location, "ignorelist." + uuid + ".chats", new ArrayList<>());
				}
				if (getOther(ignorelist_location).get("ignorelist." + uuid + ".players") != null) {
					ssOther(ignorelist_location, "ignorelist." + uuid + ".players", new ArrayList<>());
				} else {
					ssOther(ignorelist_location, "ignorelist." + uuid + ".players", new ArrayList<>());
				}
				break;
			case "toggle":
				if (getPlayerIgnoreStatus(offline, actor).equalsIgnoreCase("&aignored")){
					modifyPlayerIgnore(offline, actor, "-");
				} else if (getPlayerIgnoreStatus(offline, actor).equalsIgnoreCase("&cunignored")) {
					modifyPlayerIgnore(offline, actor, "+");
				}
				break;
			}
			
		}
		
		public String getPlayerIgnoreStatus(OfflinePlayer offline, Player actor) {
			if (getOther(ignorelist_location).get("ignorelist." + actor.getUniqueId().toString() + ".players") != null) {
				List<String> l = getOther(ignorelist_location).getStringList("ignorelist." + actor.getUniqueId().toString() + ".players");
				for (String e : l) {
					if (e.equalsIgnoreCase(offline.getUniqueId().toString() + ";" + offline.getName())) {
						return "&aignored";
					}
				}
				return "&cunignored";
			} else {
				return "&cunignored";
			}
		}
		
		//---- Updated the way we handle chats - v0.8.7
		public String antiswearServerCheck(String msg, ConfigurationSection chat) {
			//removed messages will equal - ERROR: 1337 - Banned Message
			getLogger().info( "banned word:1" );
			List<String> l = getOther(chat.getString(".anti-swear.server.file-name").replace("%CHAT_NAME%", chat.getString("name"))).getStringList("Dictionary");
			String[] msgSplit = msg.split(" ");
			String final_msg = "";
			for (String msgWord : msgSplit) {	
				boolean cont = false;
				for (String word : l) {
					String replace = "";
					if (word.split(":").length > 1) {
						replace = word.split(":")[1];
					}
					
					word = word.split(":")[0];
					if (word.contains("<%>")) {
						word = word.replace("<%>", "");
						// Removing Like Word - <%>WORD<%>			
						if (msgWord.toLowerCase().contains(word.toLowerCase())) {
							if (!replace.equalsIgnoreCase("")) {
								if (final_msg.equalsIgnoreCase("")) {
									final_msg = replace;
								} else {
									final_msg = final_msg + " " + replace;
								}
								cont = true;
							}
							cont = true;
						}
					} else if (word.contains("<@>")) {
						word = word.replace("<@>", "");
						// Removing Banned Word - <@>WORD<@>
						if (msgWord.toLowerCase().equalsIgnoreCase(word.toLowerCase())) {
							final_msg = "ERROR: 1337 - Banned Message";
							return final_msg;
						}
					} else if (word.contains("<@%>")) {
						word = word.replace("<@%>", "");
						// Removing Like Banned Word <@%>WORD<@%>
						if (msgWord.toLowerCase().contains(word.toLowerCase())) {
							final_msg = "ERROR: 1337 - Banned Message";
							return final_msg;
						}
					} else {
						// Removing Exact Word - WORD
						if (msgWord.toLowerCase().equalsIgnoreCase(word.toLowerCase()) && !cont) {
							if (!replace.equalsIgnoreCase("")) {
								if (final_msg.equalsIgnoreCase("")) {
									final_msg = replace;
								} else {
									final_msg = final_msg + " " + replace;
								}
								cont = true;
								continue;
							} else {
								cont = true;
								continue;
							}
						}
					}
				}
				if (!cont) {
					if (final_msg.equalsIgnoreCase("")) {
						final_msg = msgWord;
					} else {
						final_msg = final_msg + " " + msgWord;
					}
				}
			}
			return final_msg;
		}
		

		//---- Gets the server anti swerar toggle state ---- v?
		//---- Removed v0.8.7 - This is no longer required
		/*
		public boolean serverAntiSwear(int chatID) {
			if (getCustomChats().getBoolean(chatID + ".anti-swear.server.enabled")) {
				return true;
			} else {
				return false;
			}
			
		}
		*/
		// End Anti Swear Toggle State
		
	 // Added the ability to have a custom chats config! - v0.8.2 - 10/3/2016
    private FileConfiguration customChatsConfig = null; //customConfig 
    private File customChats = null; //customConfigFile
    
    public void reloadChats() {
        if (customChats == null) {
        	customChats = new File(getDataFolder(), "chat.yml");
        }
        customChatsConfig = YamlConfiguration.loadConfiguration(customChats);
        // Look for defaults in the jar
        Reader defConfigStream;
		try {
			defConfigStream = new InputStreamReader(this.getResource("chat.yml"), "UTF8");
		
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            customChatsConfig.setDefaults(defConfig);
        }
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    }
    
    public FileConfiguration getCustomChats() {
        if (customChatsConfig == null) {
            reloadChats();
        }
        return customChatsConfig;
    }
    
    public void saveCustomConfig() {
        if (customChatsConfig == null || customChatsConfig == null) {
            return;
        }
        try {
        	getCustomChats().save(customChats);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + customChats, ex);
        }
    }
    
    public void saveDefaultConfig() {
        if (customChats == null) {
        	customChats = new File(getDataFolder(), "chat.yml");
        }
        if (!customChats.exists()) {            
             this.saveResource("chat.yml", false);
         }
    }
    
    //--- End of chats.yml writing tools
    
    
    // Added the ability to have unlimited custom configs! - v0.8.5 - 2/15/2017
    private FileConfiguration otherConfig = null; //customConfig 
    private File otherFiles = null; //customConfigFile
    
    public void reloadOther(String file) {
    	file = file.replace("\\", File.separator);
    	otherFiles = new File(getDataFolder(), file);
        otherConfig = YamlConfiguration.loadConfiguration(otherFiles);
        // Look for defaults in the jar
        Reader defConfigStream;
        InputStream is = null;
		try {
			
			try {
				is = new FileInputStream(getDataFolder().toString() + file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			defConfigStream = new InputStreamReader(is, "UTF8");
		
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            otherConfig.setDefaults(defConfig);
        }
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    }
    
    public FileConfiguration getOther(String file) {
    	file = file.replace("\\", File.separator);
    	otherFiles = new File(getDataFolder(), file);
        otherConfig = YamlConfiguration.loadConfiguration(otherFiles);
        if (otherConfig == null) {
            reloadOther(file);
        }
        return otherConfig;
    }
    
    public void ssOther(String file, String path, Object value) {
    	file = file.replace("\\", File.separator);
    	getOther(file).set(path, value);
    	if (otherConfig == null || otherFiles == null) {
            return;
        }
        try {
        	otherConfig.save(otherFiles);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + otherFiles, ex);
        }
        reloadOther(file);
    }
    
    public void saveOther(String file) {
    	file = file.replace("\\", File.separator);
        if (otherConfig == null || otherFiles == null) {
            return;
        }
        try {
        	getOther(file).save(otherFiles);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + otherFiles, ex);
        }
    }
    public void tcOther(String file, String type) {
    	file = file.replace("\\", File.separator);
    	switch (type) {
		case "b":
			if (!getOther(file).contains("blacklist")){
				ssOther(file, "blacklist.exampleuuid", null);
			}
			break;
		case "i":
			if (!getOther(file).contains("ignorelist")){
				ssOther(file, "ignorelist.exampleuuid", null);
			}
			break;
		case "d":
			if (!getOther(file).contains("Dictionary")){
				ssOther(file, "Dictionary.exampleword", null);
			}
			break;
		default:
			break;
		}
    }
    public void saveDefaultOther(String file) {
    	
    	otherFiles = new File(getDataFolder(), file.replace("\\", File.separator));
        otherConfig = YamlConfiguration.loadConfiguration(otherFiles);
        if (otherFiles == null) {
        	otherFiles = new File(getDataFolder(), file);
        }
        if (file.contains("\\")) {
        	String temp_file = file.replace("\\", "/");
        	int i = 0;
        	String path = "";
        	for (String s : temp_file.split("/")) {
        		i++;
        		if (i != 1) { 
        			if (i != temp_file.split("/").length) {
            			if (path.equalsIgnoreCase("")) { path = File.separator + s; } else { path = path + File.separator + s; }
        			}	
        		}
        	}
        	File path_file = new File(getDataFolder() + path);
        	if (!path_file.exists()) {
        		path_file.mkdirs();
        	}
        }
        if (!otherFiles.exists()) {            
        	try {
				otherFiles.createNewFile();
			} catch (IOException e) {
				//e.printStackTrace();
			}
         }
    }
    
    public void delOther(String file) {
    	file = file.replace("\\", File.separator);
    	otherFiles = new File(getDataFolder(), file);
        otherConfig = YamlConfiguration.loadConfiguration(otherFiles);
        otherFiles.delete();
    }
    
    //--- End unlimited custom configs
    
    private FileConfiguration oldConfig = null; //customConfig 
    private File oldConfigFile = null; //customConfigFile
    
    public void reloadOld() {
        if (oldConfigFile == null) {
        	oldConfigFile = new File(getDataFolder(), "oldConfig.yml");
        }
        oldConfig = YamlConfiguration.loadConfiguration(oldConfigFile);
        // Look for defaults in the jar
        Reader defConfigStream;
		try {
			defConfigStream = new InputStreamReader(this.getResource("oldConfig.yml"), "UTF8");
		
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            oldConfig.setDefaults(defConfig);
        }
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    }
    
    public FileConfiguration getOldConfig() {
        if (oldConfig == null) {
        	reloadOld();
        }
        return oldConfig;
    }
    
    public void saveOldConfig() {
        if (oldConfig == null || oldConfig == null) {
            return;
        }
        try {
        	getOldConfig().save(oldConfigFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + oldConfigFile, ex);
        }
    }
    
}