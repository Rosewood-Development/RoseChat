package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.RoseSettingSection;
import dev.rosewood.rosegarden.config.RoseSettingValue;
import dev.rosewood.rosegarden.manager.AbstractConfigurationManager;
import org.bukkit.Sound;
import java.util.Arrays;
import java.util.Collections;

public class ConfigurationManager extends AbstractConfigurationManager {

    public enum Setting implements RoseSetting {
        MODERATION_SETTINGS("moderation-settings", null, "Moderation Settings"),
        CAPS_CHECKING_ENABLED("moderation-settings.caps-checking-enabled", true,
                "Should the plugin check for messages that contain too many capital letters?"),
        MAXIMUM_CAPS_ALLOWED("moderation-settings.maximum-caps-allowed", 5,
                "The maximum amount of capital letters that are allowed in one message.", "Using this allows for players to use words like 'LOL'."),
        LOWERCASE_CAPS_ENABLED("moderation-settings.lowercase-caps-enabled", true,
                "Should the plugin lowercase messages found to contain too many capital letters?", "If false, the message will not be sent."),
        WARN_ON_CAPS_SENT("moderation-settings.warn-on-caps-sent", true,
                "Should the plugin send a warning message (defined in the locale file) when a player sends a message that contains too many capital letters?"),
        SPAM_CHECKING_ENABLED("moderation-settings.spam-checking-enabled", true,
                "Should the plugin check for players sending multiple of the same, or similar, messages?"),
        SPAM_MESSAGE_COUNT("moderation-settings.spam-message-count", 5,
                "How many similar messages are allowed to be sent before it can be seen as spam?",
                "Using this allows for players to correct themselves if they have a typo."),
        SPAM_FILTER_SENSITIVITY("moderation-settings.spam-filter-sensitivity", 70.0,
                "How similar are the messages that count as spam?", "For example, messages that are more than 70% similar will not be sent."),
        WARN_ON_SPAM_SENT("moderation-settings.warn-on-spam-sent", true,
                "Should the plugin send a warning message (defined in the locale file) when a player spams?"),
        URL_CHECKING_ENABLED("moderation-settings.url-checking-enabled", true,
                "Should the plugin check for messages that contain URLs and IP addresseS?"),
        URL_CENSORING_ENABLED("moderation-settings.url-censoring-enabled", true,
                "Should the plugin censor URLs and IP addresses?",
                "If true, messages will be censored; periods and click functionality will be removed.",
                "If false, the message will not be sent."),
        WARN_ON_URL_SENT("moderation-settings.warn-on-url-sent", true,
                "Should the plugin send a warning message (defined in the locale file) when a player sends a message contain a URL or IP address?"),
        SWEAR_CHECKING_ENABLED("moderation-settings.swear-checking-enabled", true,
                "Should the plugin check for swear words?"),
        SWEAR_FILTER_SENSITIVITY("moderation-settings.swear-filter-sensitivity", 80.0,
                "How similar are the messages that count as swears?", "For example, words that are more than 80% similar will not be sent."),
        BLOCKED_SWEARS("moderation-settings.blocked-swears", Collections.singletonList("bitch"),
                "If a player sends a message that contains one of these words, then the message will not be sent."),
        WARN_ON_BLOCKED_SWEAR_SENT("moderation-settings.warn-on-blocked-swear-sent", true,
                "Should the plugin send a warning message (defined in the locale file) when a player sends a message with  a blocked swear word."),
        SWEAR_REPLACEMENTS("moderation-settings.swear-replacements", Arrays.asList("fuck:f***", "ass:butt"),
                "If a player sends a message that contains one of these words, then the word will be replaced.",
                "Note: This does not affect words like 'assassin'.", "Format: 'contains:replaced'"),

        NICKNAME_SETTINGS("nickname-settings", null, "Nickname Settings"),
        MINIMUM_NICKNAME_LENGTH("nickname-settings.minimum-nickname-length", 3,
                "The minimum length a nickname can be."),
        MAXIMUM_NICKNAME_LENGTH("nickname-settings.maximum-nickname-length", 32,
                "The maximum length a nickname can be.",
                "Note: This does not include color codes."),
        ALLOW_SPACES_IN_NICKNAMES("nickname-settings.allow-spaces-in-nicknames", true,
                "Should spaces be allowed in nicknames?"),
        ALLOW_NONALPHANUMERIC_CHARACTERS("nickname-settings.allow-nonalphanumeric-characters", true,
                "Should non-alphanumeric characters, such as brackets, be allowed in nicknames?"),

        CHAT_SETTINGS("chat-settings", null, "General Miscellaneous Settings"),
        ALLOW_BUNGEECORD_MESSAGES("chat-settings.allow-bungeecord-messages", true,
                "Should players be allowed to message other players on connected servers?", "Requires BungeeCord"),
        MESSAGE_SOUND("chat-settings.message-sound", "BLOCK_NOTE_BLOCK_PLING",
                "The sound that will be sent to a player when they receive a message.",
                "Players can individually disable this in-game with /togglesound.",
                "Valid sounds can be found at: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html", "Set to 'none' for no sound."),

        DISCORD_SETTINGS("discord-settings", null, "Discord Settings"),
        USE_IGN_WITH_DISCORD("discord-settings.use-minecraft-ign-with-discord", true,
                "Should a player's in-game name (nickname, display name or username) be used instead of the Discord name?",
                "Will only work if a player links their accounts.",
                "Requires DiscordSRV"),
        DELETE_DISCORD_MESSAGES("discord-settings.delete-discord-messages", true,
                "Should messages sent in Discord be deleted when the same message is deleted in-game, and vice versa?",
                "Requires DiscordSRV, ProtocolLib"),
        REQUIRE_PERMISSIONS("discord-settings.require-permissions", true,
                "Should messages sent in Discord require in-game permissions?",
                "For example, when sending a message with colour.",
                "Requires DiscordSRV"),
        DELETE_BLOCKED_MESSAGES("discord-settings.delete-blocked-messages", true,
                "Should messages that are blocked by moderation settings (e.g. swears) be deleted when sent from Discord?",
                "The require-permissions setting needs to be enabled for this to work.",
                "Requires DiscordSRV"),

        CHAT_FORMATS("chat-formats", null, "These are the the other chat formats in the plugin."),
        MESSAGE_SENT_FORMAT("chat-formats.message-sent", "{message-sent-sender}{message-sent-arrow}{message-sent-receiver}{message}",
                "The format of a /message sent to another player."),
        MESSAGE_RECEIVED_FORMAT("chat-formats.message-received", "{message-received-sender}{message-received-arrow}{message-received-receiver}{message}",
                "The format of a /message received from another player."),
        MESSAGE_SPY_FORMAT("chat-formats.message-spy", "{spy-prefix}{spy-players}{message}",
                "The format of a spied /message."),
        GROUP_FORMAT("chat-formats.group", "{group-prefix}{group-member-prefix}{player}{separator}{message}",
                "The format of a group message."),
        GROUP_SPY_FORMAT("chat-formats.group-spy", "{spy-prefix}{group-prefix}{group-member-prefix}{player}{separator}{message}",
                "The format of a spied group message."),
        CHANNEL_SPY_FORMAT("chat-formats.channel-spy", "{spy-prefix}{channel-prefix}{player}{separator}{message}",
                "The format of a spied channel message."),
        DELETE_CLIENT_MESSAGE_FORMAT("chat-formats.delete-client-message", "{delete-message}",
                "The format of a button to delete messages sent from the server to a player.",
                "Requires ProtocolLib"),
        DELETE_OWN_MESSAGE_FORMAT("chat-formats.delete-own-message", "{delete-message}",
                "The format of a button to delete a player's own messages.",
                "Requires ProtocolLib"),
        DELETE_OTHER_MESSAGE_FORMAT("chat-formats.delete-other-message", "{delete-message}",
                "The format of a the button to delete other players' messages.",
                "Requires ProtocolLib"),
        DELETED_MESSAGE_FORMAT("chat-formats.deleted-message", "{deleted-message}",
                "The format of a previously deleted message.",
                "Requires ProtocolLib"),
        MINECRAFT_TO_DISCORD_FORMAT("chat-formats.minecraft-to-discord", "{discord}",
                "The format of a message that was sent to discord.", "Requires DiscordSRV"),
        DISCORD_TO_MINECRAFT_FORMAT("chat-formats.discord-to-minecraft", "{from-discord}{discord-player}{separator}{message}",
                "The format of a message that was sent from discord.", "Requires DiscordSRV"),

        DISCORD_FORMATS("discord-formats", null, "Discord Format Settings", "Requires DiscordSRV"),
        DISCORD_FORMAT_BOLD("discord-formats.bold", "&l"),
        DISCORD_FORMAT_UNDERLINE("discord-formats.underline", "&n"),
        DISCORD_FORMAT_STRIKETHROUGH("discord-formats.strikethrough", "&m"),
        DISCORD_FORMAT_ITALIC("discord-formats.italic", "&o"),
        DISCORD_FORMAT_SPOILER("discord-formats.spoiler", "<spoiler>%message%</spoiler>"),
        DISCORD_FORMAT_CODE_BLOCK_ONE("discord-formats.code-block-one", ""),
        DISCORD_FORMAT_CODE_BLOCK_MULTIPLE("discord-formats.code-block-multiple", ""),
        DISCORD_FORMAT_BLOCK_QUOTES("discord-formats.block-quotes", "&2> &o");

        private final String key;
        private final Object defaultValue;
        private final String[] comments;
        private Object value = null;

        Setting(String key, Object defaultValue, String... comments) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.comments = comments != null ? comments : new String[0];
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public Object getDefaultValue() {
            return this.defaultValue;
        }

        @Override
        public String[] getComments() {
            return this.comments;
        }

        @Override
        public Object getCachedValue() {
            return this.value;
        }

        @Override
        public CommentedFileConfiguration getBaseConfig() {
            return RoseChat.getInstance().getManager(ConfigurationManager.class).getConfig();
        }

        @Override
        public void setCachedValue(Object value) {
            this.value = value;
        }
    }

    public ConfigurationManager(RosePlugin rosePlugin) {
        super(rosePlugin, Setting.class);
    }

    @Override
    protected String[] getHeader() {
        return new String[] {
                "  _____                 _____ _           _",
                " |  __ \\               / ____| |         | |",
                " | |__) |___  ___  ___| |    | |__   __ _| |_",
                " |  _  // _ \\/ __|/ _ \\ |    | '_ \\ / _` | __|",
                " | | \\ \\ (_) \\__ \\  __/ |____| | | | (_| | |_",
                " |_|  \\_\\___/|___/\\___|\\_____|_| |_|\\__,_|\\__|"
        };
    }
}
