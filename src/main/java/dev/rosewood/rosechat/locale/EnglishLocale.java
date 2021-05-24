package dev.rosewood.rosechat.locale;

import dev.rosewood.rosegarden.locale.Locale;
import java.util.LinkedHashMap;
import java.util.Map;

public class EnglishLocale implements Locale {

    @Override
    public String getLocaleName() {
        return "en_US";
    }

    @Override
    public String getTranslatorName() {
        return "Lilac";
    }

    @Override
    public Map<String, Object> getDefaultLocaleValues() {
        return new LinkedHashMap<String, Object>() {{
            this.put("#0", "Plugin Message Prefix");
            this.put("prefix", "&7[<g:#8A2387:#E94057:#F27121>RoseChat&7] ");

            this.put("#1", "Command Messages");
            this.put("no-permission", "&cYou do not have permission to do this!");
            this.put("player-not-found", "&cThis player is not online!");
            this.put("player-only", "&cThe console cannot use this command!");
            this.put("invalid-arguments", "&cUsage: &b%syntax%&c.");

            this.put("#2", "Base Command Message");
            this.put("base-command-help", "&eUse &b/rc help &efor command information.");

            this.put("#3", "Help Command");
            this.put("command-help-description", "&8 - &d/rc help &7- Displays the help menu... You have arrived.");
            this.put("command-help-title", "&eAvailable Commands:");
            this.put("command-help-usage", "&e/rc help");

            this.put("#4", "Reload Command");
            this.put("command-reload-description", "&8 - &d/rc reload &7- Reloads the plugin.");
            this.put("command-reload-usage", "&e/rc reload");
            this.put("command-reload-reloaded", "&ePlugin data, configuration, and locale files were reloaded.");

            this.put("#5", "Moderation Messages");
            this.put("blocked-caps", "&cYour message could not be sent as it contains too many capital letters!");
            this.put("blocked-spam", "&cPlease do not spam!");
            this.put("blocked-language", "&cYou used a bad word. :(");
            this.put("blocked-url", "&cPlease do not send URLs!");

            this.put("#6", "Message Command");
            this.put("command-message-description", "&8 - &d/msg &7- Message a player.");
            this.put("command-message-usage", "&e/msg <player> <message>");
            this.put("command-message-enter-message", "&cPlease enter a message!");

            this.put("#7", "Reply Command");
            this.put("command-reply-description", "&8 - &d/reply &7- Replies to a message from another player.");
            this.put("command-reply-usage", "&e/reply");
            this.put("command-reply-enter-message", "&cPlease enter a message!");
            this.put("command-reply-no-one", "&cThere is no one to reply to...");

            this.put("#8", "SocialSpy Command");
            this.put("command-socialspy-description", "&8 - &d/socialspy &7- Toggles the ability to see private messages.");
            this.put("command-socialspy-usage", "&e/socialspy");
            this.put("command-socialspy-enabled", "&eYou have &aenabled &esocial spy.");
            this.put("command-socialspy-disabled", "&eYou have &cdisabled &esocial spy.");

            this.put("#9", "ToggleMessage Command");
            this.put("command-togglemessage-description", "&8 - &d/togglemessage &7- Toggles the ability to receive messages.");
            this.put("command-togglemessage-usage", "&e/togglemessage");
            this.put("command-togglemessage-on", "&eYou have &aenabled &ereceiving messages.");
            this.put("command-togglemessage-off", "&eYou have &cdisabled &ereceiving messages.");
            this.put("command-togglemessage-cannot-message", "&cYou cannot message this player!");

            this.put("#10", "ToggleSound Command");
            this.put("command-togglesound-description", "&8 - &d/togglesound &7- Toggles message and tag sounds.");
            this.put("command-togglesound-usage", "&e/togglesound <message/tag/all>");
            this.put("command-togglesound-on", "&eYou have &aenabled &esounds when receiving &b%type%&e.");
            this.put("command-togglesound-off", "&eYou have &cdisabled &esounds when receiving &b%type%&e.");
            this.put("command-togglesound-messages", "messages");
            this.put("command-togglesound-tags", "tags");

            this.put("#11", "ToggleEmoji Command");
            this.put("command-toggleemoji-description", "&8 - &d/toggleemoji &7- Toggles formatting emojis.");
            this.put("command-toggleemoji-usage", "&e/toggleemoji");
            this.put("command-toggleemoji-on", "&eYou have &aenabled &eformatting emoji.");
            this.put("command-toggleemoji-off", "&eYou have &cdisabled &eformatting emoji.");

            this.put("#12", "Channel Command");
            this.put("command-channel-description", "&8 - &d/channel &7- Send a message in a chat channel.");
            this.put("command-channel-usage", "&e/channel <channel> [message]");
            this.put("command-channel-not-found", "&cThis channel does not exist.");
            this.put("command-channel-joined", "&eYou are now using the &b%id% &echannel.");

            this.put("#13", "Chat Command");
            this.put("command-chat-description", "&8 - &d/chat &7- Displays the admin help menu.");
            this.put("command-chat-usage", "&e/chat help");

            this.put("#14", "Chat Help Command");
            this.put("command-chat-help-description", "&8 - &c/chat help &7- Displays the admin help menu... You have arrived.");
            this.put("command-chat-help-usage", "&8 - &e/chat help");
            this.put("command-chat-help-title", "&eAvailable Commands");

            this.put("#15", "Chat Clear Command");
            this.put("command-chat-clear-description", "&8 - &c/chat clear &7- Clears the chat.");
            this.put("command-chat-clear-usage", "&e/chat clear [channel]");
            this.put("command-chat-clear-cleared", "&eThe &b%channel% &echannel has been cleared.");

            this.put("#16", "Chat Move Command");
            this.put("command-chat-move-description", "&8 - &c/chat move &7- Moves a player to the selected channel.");
            this.put("command-chat-move-usage", "&e/chat move <player> <channel>");
            this.put("command-chat-move-success", "&b%player% &ehas been moved to &b%channel%&e.");
            this.put("command-chat-move-moved", "&eYou have been moved to the &b%channel% &echannel.");

            this.put("#17", "Chat Mute Command");
            this.put("command-chat-mute-description", "&8 - &c/chat mute &7- Mutes a chat channel.");
            this.put("command-chat-mute-usage", "&e/chat mute <channel>");
            this.put("command-chat-mute-muted", "&eThe &b%channel% &echannel has been muted.");
            this.put("command-chat-mute-unmuted", "&eThe &b%channel% &echannel has been unmuted.");
            this.put("channel-muted", "&cYou can not send a message while this channel is muted.");

            this.put("#18", "Chat Sudo Command");
            this.put("command-chat-sudo-description", "&8 - &c/chat sudo &7- Send a chat message as another player.");
            this.put("command-chat-sudo-usage", "&e/chat sudo <player> <channel> <message>");

            this.put("#20", "Group Chat Command");
            this.put("command-gc-description", "&8 - &d/gc &7- DIsplays the group chat help menu.");
            this.put("command-gc-usage", "&e/gc help");

            this.put("#21", "Group Chat Help Command");
            this.put("command-gc-help-description", "&8 - &b/gc help &7- Displays the group chat help menu... You have arrived.");
            this.put("command-gc-help-usage", "&e/gc help");

            this.put("#22", "Group Chat Create Command");
            this.put("command-gc-create-description", "&8 - &b/gc create &7- Creates a new group chat.");
            this.put("command-gc-create-usage", "&e/gc create <name>");
            this.put("command-gc-create-success", "&eYou have created a new group chat. Use &b/gc invite &eto invite a player.");

            this.put("#23", "Group Chat Invite Command");
            this.put("command-gc-invite-description", "&8 - &b/gc invite &7- Invites a player to the group chat.");
            this.put("command-gc-invite-usage", "&e/gc invite <player>");
            this.put("command-gc-invite-success", "&eYou have invited &b%player% &eto the group chat.");
            this.put("command-gc-invite-invited", "&b%player% &ehas invited you to their group chat, &b%name%&e.");

            this.put("#24", "Group Chat Kick Command");
            this.put("command-gc-kick-description", "&8 - &b/gc kick &7- Kicks a player from the group chat.");
            this.put("command-gc-kick-usage", "&e/gc kick <player>");
            this.put("command-gc-kick-success", "&eYou have kicked &b%player% &efrom the group chat.");
            this.put("command-gc-kick-kicked", "&eYou have been kicked from the group chat.");

            this.put("#25", "Group Chat Accept Command");
            this.put("command-gc-accept-description", "&8 - &b/gc accept &7- Accepts a group chat invite.");
            this.put("command-gc-accept-usage", "&e/gc accept");
            this.put("command-gc-accept-success", "&eYou have joined &b%player%&e's group chat.");
            this.put("command-gc-accept-accepted", "&b%player% &ehas joined the group chat.");

            this.put("#26", "Group Chat Leave Command");
            this.put("command-gc-leave-description", "&8 - &b/gc leave &7- Removes you from the group chat.");
            this.put("command-gc-leave-usage", "&e/gc leave");
            this.put("command-gc-leave-success", "&eYou have left the group chat.");
            this.put("command-gc-leave-left", "&b%player% &ehas left the group chat.");

            this.put("#27", "Group Chat Disband Command");
            this.put("command-gc-disband-description", "&8 - &b/gc disband &7- Deletes your group chat.");
            this.put("command-gc-disband-usage", "&e/gc disband");
            this.put("command-gc-disband-success", "&eThe &b%name% &egroup chat you were in has been disbanded...");

            this.put("#28", "Group Chat Members Command");
            this.put("command-gc-members-description", "&8 - &b/gc members &7- Lists the members of the group chat.");
            this.put("command-gc-members-usage", "&e/gc members");
            this.put("command-gc-members-title", "&8[&bGroup Members&8]");
            this.put("command-gc-members-member", "&8- %player%");

            this.put("#29", "Color Command");
            this.put("command-color-description", "&8 - &d/color &7- Change your default chat color.");
            this.put("command-color-usage", "&e/color <color>");
            this.put("command-color-success", "&eYour new chat color is %color%&e.");
            this.put("command-color-invalid", "&eThis is not a valid color!");

            this.put("#30", "Mute Command");
            this.put("command-mute-description", "&8- &d/mute &7- Mute a player.");
            this.put("command-mute-usage", "&e/mute <player> [time]");
            this.put("command-mute-success", "&eYou have muted &b%player% &efor &b%time%&e.");
            this.put("command-mute-indefinite", "an indefinite amount of time");
            this.put("mute-muted", "&cYou can not send a message while you are muted!");
        }};
    }
}
