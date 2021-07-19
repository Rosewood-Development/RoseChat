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
        CAPS_CHECKING_ENABLED("moderation-settings.caps-checking-enabled", true, "Should the plugin check for messages that contain capital letters?"),
        MAXIMUM_CAPS_ALLOWED("moderation-settings.maximum-caps-allowed", 5, "The maximum amount of capital letters that are allowed in one message.", "Using this allows for players to use acronyms and words like 'LOL'."),
        LOWERCASE_CAPS_ENABLED("moderation-settings.lowercase-caps-enabled", true, "Should the plugin lowercase messages found to contain too many capital letters?", "If false, the message will not be sent."),
        WARN_ON_CAPS_SENT("moderation-settings.warn-on-caps-sent", true, "Should the plugin send a warning message (defined in language.yml) when a player sends a message that contains too many capital letters?"),
        SPAM_CHECKING_ENABLED("moderation-settings.spam-checking-enabled", true, "Should the plugin check for players sending multiple of the same message?"),
        SPAM_MESSAGE_COUNT("moderation-settings.spam-message-count", 5, "How many similar messages are allowed to be sent before it can be seen as spam?"),
        SPAM_FILTER_SENSITIVITY("moderation-settings.spam-filter-sensitivity", 0.3, "How similar are the messages that count as spam?"),
        WARN_ON_SPAM_SENT("moderation-settings.warn-on-spam-sent", true, "Should the plugin send a warning message (defined in language.yml) when a player spams?"),
        URL_CHECKING_ENABLED("moderation-settings.url-checking-enabled", true, "Should the plugin check for messages that contain URLs and IP addresses?"),
        URL_CENSORING_ENABLED("moderation-settings.url-censoring-enabled", true, "Should the plugin censor URLs and IP addresses?", "If true, messages will be censored - removing periods and click functionality.", "If false, the message will not be sent."),
        WARN_ON_URL_SENT("moderation-settings.warn-on-url-sent", true, "Should the plugin send a warning message (defined in language.yml) when a player sends a message containing a URL or IP address?"),
        SWEAR_CHECKING_ENABLED("moderation-settings.swear-checking-enabled", true, "Should the plugin check for swear words?"),
        SWEAR_FILTER_SENSITIVITY("moderation-settings.swear-filter-sensitivity", 0.2, "How similar are the messages that count as swears?"),
        BLOCKED_SWEARS("moderation-settings.blocked-swears", Collections.singletonList("faggot"), "If a player sends a message that contains one of these words, then the message will not be sent."),
        WARN_ON_BLOCKED_SWEAR_SENT("moderation-settings.warn-on-blocked-swear-sent", true, "Should the plugin send a warning message (defined in language.yml) when a player sends a message with a blocked swear in it?"),
        SWEAR_REPLACEMENTS("moderation-settings.swear-replacements", Arrays.asList("fuck:f***", "bitch:dog", "ass:butt"), "If a player sends a message that contains one of these words, then the word will be replaced.", "Format: 'contains:replaced'."),

        NICKNAME_SETTINGS("nickname-settings", null, "Nickname Settings"),
        MIN_NICKNAME_LENGTH("nickname-settings.min-nickname-length", 3, "The minimum length a nickname can be."),
        MAX_NICKNAME_LENGTH("nickname-settings.max-nickname-length", 32, "The maximum length a nickname can be."),
        ALLOW_SPACES_IN_NICKNAMES("nickname-settings.allow-spaces-in-nicknames", true, "Should spaces be allowed in nicknames?"),
        ALLOW_NONALPHANUMERIC_CHARACTERS_IN_NICKNAMES("nickname-settings.allow-nonalphanumeric-characters", true, "Should non-alphanumeric characters, such as brackets, be allowed in nicknames?"),

        CHAT_SETTINGS("chat-settings", null, "General Miscellaneous Settings"),
        OUTPUT_HOVER_EVENTS("chat-settings.output-hover-events", true, "Should hover events be output to the console?"),
        ALLOW_BUNGEECORD_MESSAGES("chat-settings.allow-bungeecord-messages", true, "Should players be allowed to message other players on connected servers?"),
        MESSAGE_SOUND("chat-settings.message-sound", Sound.BLOCK_NOTE_BLOCK_PLING.name(), "The sound that will be sent to a player when they receive a message.", "Players can individually disable this in-game with /togglesound.", "Valid sounds can be found at: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html", "Set to 'none' for no sound."),
        CHAT_REPLACEMENTS("chat-settings.chat-replacements", new RoseSettingSection(
                new RoseSettingValue("heart", new RoseSettingSection(
                        new RoseSettingValue("text", "<3"),
                        new RoseSettingValue("replacement", "❤"),
                        new RoseSettingValue("hover", "&b&o<3")
                ), "This replaces all <3's in a message with a ❤."),
                new RoseSettingValue("kawaii", new RoseSettingSection(
                        new RoseSettingValue("text", ":kawaii:"),
                        new RoseSettingValue("replacement", "(づ｡◕‿‿◕｡)づ"),
                        new RoseSettingValue("hover", "&b&o:kawaii:")
                )),
                new RoseSettingValue("rosechat", new RoseSettingSection(
                        new RoseSettingValue("text", "&RoseChat"),
                        new RoseSettingValue("replacement", "{rosechat}")
                )),
                new RoseSettingValue("coffee-color", new RoseSettingSection(
                        new RoseSettingValue("text", "&g"),
                        new RoseSettingValue("replacement", "#C0FFEE")
                )),
                new RoseSettingValue("pink-color", new RoseSettingSection(
                        new RoseSettingValue("text", "&h"),
                        new RoseSettingValue("replacement", "#C8A2C8")
                )),
                new RoseSettingValue("regex-example", new RoseSettingSection(
                        new RoseSettingValue("text", "[-]{0,1}[\\d]*[.]{0,1}[\\d]+"),
                        new RoseSettingValue("replacement", "&c%message%"),
                        new RoseSettingValue("hover", "&b&o<3")
                ), "The placeholder '%message%' can be used to get what was replaced.", "For example, this regex matches all numbers. The replacement adds &c in front of each number.")),
                "When a message containing the 'text' is sent in chat, it will be replaced with the 'replacment'.", "These can be used for custom emojis, color codes or general replacements.", "These replacements can be used with placeholders in placeholder.yml.", "These also support regular expressions."),

        TAGS("tags", new RoseSettingSection(
                new RoseSettingValue("player", new RoseSettingSection(
                        new RoseSettingValue("prefix", "@", "This is what the player will use to start a tag."),
                        new RoseSettingValue("tag-online-players", true, "This allows the tag to target online players."),
                        new RoseSettingValue("sound", Sound.BLOCK_NOTE_BLOCK_PLING.name(), "The sound that will be sent to the player who receives the tag.", "Players can individually disable this in-game with /togglesound.", "Valid sounds can be found at: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html", "Set to 'none' for no sound."),
                        new RoseSettingValue("format", "{player-tag}", "The format for this tag (see the 'formats' section for more info).")
                ), "The ID of the tag.", "This tag allows players to use @<player>, e.g. \"Hey @Lilac!\""),
                new RoseSettingValue("hashtag", new RoseSettingSection(
                        new RoseSettingValue("prefix", "#"),
                        new RoseSettingValue("format", "{hashtag}")
                ), "This tag allows players to use hashtags in-game.", "For example, \"#Rosewood\""),
                new RoseSettingValue("money", new RoseSettingSection(
                        new RoseSettingValue("prefix", "$"),
                        new RoseSettingValue("format", "{money-tag}")
                ), "This tag allows players to type money in-game, and make it formatted and hoverable.", "For example, \"$500\""),
                new RoseSettingValue("spoiler", new RoseSettingSection(
                        new RoseSettingValue("prefix", "<spoiler>"),
                        new RoseSettingValue("suffix", "</spoiler>"),
                        new RoseSettingValue("format", "{spoiler-tag}"),
                        new RoseSettingValue("match-length", true, "Match length allows the tag replacement to be the same length as the message sent.", "This is useful in tags with suffixes as it can replace a 3 letter long word with ⬛⬛⬛.")
                ), "This tag allows for a spoiler tag in-game.", "This makes the message black and players can hover over it to see the real message.", "Format: <spoiler>Your Message</spoiler>"),
                new RoseSettingValue("condition", new RoseSettingSection(
                        new RoseSettingValue("prefix", "?["),
                        new RoseSettingValue("suffix", "]"),
                        new RoseSettingValue("format", "{conditional-example}")
                ), "This tag is used a way to show how conditional placeholders work.", "See placeholders.yml for more information."),
                new RoseSettingValue("copy-paste", new RoseSettingSection(
                        new RoseSettingValue("prefix", "{"),
                        new RoseSettingValue("suffix", "}"),
                        new RoseSettingValue("format", "{copy-paste}")
                ), "This tag allows players to put the clicked message in their text bar.", "This can allow players to copy paste messages.")
        ), "Tags can be used to tag specific players or add extra detail to messages in chat.", "Tags use placeholders, defined in placeholders.yml, to allow hoverable and clickable tags."),

        CHAT_CHANNELS("chat-channels", new RoseSettingSection(
                new RoseSettingValue("global", new RoseSettingSection(
                        new RoseSettingValue("default", true, "This sets the channel as the default channel that players will be placed into when they first join the server."),
                        new RoseSettingValue("format", "{prefix}{player}{extra}{message}", "This is the format of the channel.", "The format uses placeholders defined in placeholders.yml"),
                        new RoseSettingValue("visible-anywhere", true, "Can messages from this channel be sent and read while in other channels?"),
                        new RoseSettingValue("discord", "global", "The DiscordSRV channel that messages can be sent and received from.", "These can be defined in DiscordSRV's config file.")
                ), "The ID of the channel, the player will use this to join the channel.", "For example, /c global"),
                new RoseSettingValue("staff", new RoseSettingSection(
                        new RoseSettingValue("format", "{staff-prefix}{player}{extra}{message}"),
                        new RoseSettingValue("visible-anywhere", true),
                        new RoseSettingValue("command", "staff", "Creates a command alias, /staff, for this channel.")
                ), "Staff Channel - /c staff"),
                new RoseSettingValue("local", new RoseSettingSection(
                        new RoseSettingValue("radius", 200, "If no format is given, the default channel format will be used.", "The radius is the distance between players that can read the messages sent.")
                ), "Local Channel - /c local"),
                new RoseSettingValue("skyblock", new RoseSettingSection(
                        new RoseSettingValue("world", "skyblock", "World channels only allow messages to be sent and received when the player is in the world."),
                        new RoseSettingValue("auto-join", true, "When auto-join is true and combined with a world channel, as soon as the player joins the world they will be added to the channel.")
                ), "Skyblock Channel - /c skyblock"),
                new RoseSettingValue("bungee", new RoseSettingSection(
                        new RoseSettingValue("servers", Arrays.asList("factions"), "Server channels allow messages to be sent and received across the listed servers.")
                ), "Bungee Channel - /c bungee")
        ), "Chat Channels are different chats that players can use.", "These can be accessed with /channel or /c in-game"),

        CHAT_FORMATS("chat-formats", null,"These are all of the miscellanous chat formats in the plugin."),
        MESSAGE_SENT_FORMAT("chat-formats.message-sent", "{left-bracket}{you}{arrow-sender}{msg-player}{right-bracket}{message}", "The format of a /msg sent to another player."),
        MESSAGE_RECEIVED_FORMAT("chat-formats.message-received", "{left-bracket}{msg-player}{arrow-receiver}{you}{right-bracket}{message}", "The format of a /msg received from another player."),
        MESSAGE_SPY_FORMAT("chat-formats.message-spy", "{spy-prefix}{spy-player}{spy-other}{message}", "The format of a spied /msg."),
        GROUP_FORMAT("chat-formats.group", "{group}{player}{extra}{message}", "The format of a group message."),
        GROUP_SPY_FORMAT("chat-formats.group-spy", "{spy-prefix}{group}{player}{extra}{message}", "The format of a spied group message."),
        CHANNEL_SPY_FORMAT("chat-formats.channel-spy", "{spy-prefix}{world}{player}{extra}{message}", "The format of a spied world message."),
        DELETE_CLIENT_MESSAGES_FORMAT("chat-formats.delete-client-messages", "{delete-client}", "WIP"),
        DELETE_OWN_MESSAGES_FORMAT("chat-formats.delete-own-messages", "{delete-own}", "WIP"),
        DELETE_OTHER_MESSAGES_FORMAT("chat-formats.delete-other-messages", "{delete-other}", "WIP"),
        DELETED_MESSAGE_FORMAT("chat-formats.deleted-message-format", "{deleted-message}", "WIP"),
        MINECRAFT_TO_DISCORD_FORMAT("chat-formats.minecraft-to-discord", "{}", "WIP"),
        DISCORD_TO_MINECRAFT_FORMAT("chat-formats.discord-to-minecraft", "{discord}{extra}{message}", "WIP");

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
