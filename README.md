# VIP+ Chat
One of the best and customizable vip/staff chat plugins on spigot!

### [Stable Release](https://github.com/agentsix1/VIP-Plus-Chat/raw/refs/heads/main/target/staffchat-0.8.6.jar) - Download v0.8.6 (Support for 1.13+)
### [DEV Build](https://github.com/agentsix1/VIP-Plus-Chat/raw/refs/heads/main/target/staffchat-0.8.7.jar) - Download v0.8.7 (Support for 1.13+)
### [Previous Release](https://www.spigotmc.org/resources/vip-chat-advanced.3308/) - Download v0.8.5 (Support for 1.6.4+)

### Change Log v0.8.7 - Changed July 7th 2025 (Dev Build)
**Updates**
- Recoded how chats are handled
- Removed requirement for chatcount
- Removed requirement for indexing your chat
- Tab complete will show usernames instead of display names
-- This would error and lead to you disconnecting if username contained an illegal character
- Added a message to Messages for no-args
- Added TAG for %VERSION% for the message no-args
- Broadcasting blacklist messages has been disabled by default
- Added Chat Names to swear toggle tab complete
- Added Chat Names to swear add/rem/del/etc.... tab complete
- You are no longer allowed to add ':' as a swear word or replacement word
- Checks were added for additional :'s in the swear dictionary
- Added some additional anti dupe swear word protection
- Added a new system to convert your config from v0.8.6 -> v0.8.7 with out losing data


**Bug Fixes**
- [BUG FIX] Fixed the an error when typing /vpc with out arguments
- [BUG FIX] Fixed an error when using /vpc blacklist with the incorrect amount of arguments
- [BUG FIX] Fixed an error occurring when showing the blacklist when it was empty
- [BUG FIX] You will no longer get 2 no permission messages when typing /vpc swear toggle {chat}
- [BUG FIX] Tab complete for fast focus no longer shows usernames
- [BUG FIX] Invalid/Broken swear word layouts will be disabled and not shown in the swear list
- [BUG FIX] Fixed an error showing when Invalid/Broken swear words were in the swear list

### Change Log v0.8.6 - Changed Oct 29th 2024 (Release July 1st 2025)
- Added tab complete to all custom chats
- Added tab complete to all commands
- Fixed checkver to use proper updated url (https)
- Added ability to add custom command for quickly focusing chat (fastfocus-command)
- Complete recode of the entire custom chat system to properly register commands
- You can now use symbols defined in chat.yml to quickly type in chat
- /vpc info now displays your information such as blacklist state, focused chats, ignored players, ignored chats
- /vpc info {player} now displays the players information such as blacklist state, focused chats, ignored players, ignored chats
- /vpc focused will now display what channel you are currently focused on
- Added basic follia support
