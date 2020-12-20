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
            this.put("base-command-color", "&e");
            this.put("base-command-help", "&eUse &b/rc help &efor command information.");

            this.put("#3", "Help Command");
            this.put("command-help-description", "&8 - &d/rc help &7- Displays the help menu... You have arrived.");
            this.put("command-help-title", "&eAvailable Commands:");

            this.put("#4", "Reload Command");
            this.put("command-reload-description", "&8 - &d/rc reload &7- Reloads the plugin.");
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
            this.put("command-reply-usage", "&c&e/reply");
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

            this.put("#11", "ToggleEmote Command");
            this.put("command-toggleemotes-description", "&8 - &d/toggleemotes &7- Toggles formatting emotes.");
            this.put("command-toggleemotes-usage", "&e/toggleemotes");
            this.put("command-toggleemotes-on", "&eYou have &aenabled &eformatting emotes.");
            this.put("command-toggleemotes-off", "&eYou have &cdisabled &eformatting emotes.");

            this.put("#12", "Channel Command");
            this.put("command-channel-description", "&8 - &d/channel &7- Send a message in a chat channel.");
            this.put("command-channel-usage", "&e/channel <channel> [message]");
            this.put("command-channel-not-found", "&cThis channel does not exist.");
            this.put("command-channel-joined", "&eYou are now using the &b%id% &echannel.");
        }};
    }
}
