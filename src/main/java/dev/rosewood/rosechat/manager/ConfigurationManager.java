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
        MAXIMUM_CAPS_ALLOWED("moderation-settings.maximum-caps-allowed", 5, "The maximum amount of capital letters that are allowed in one message.", "Using this allows for players to use words like 'LOL', and other acronyms."),
        LOWERCASE_CAPS_ENABLED("moderation-settings.lowercase-caps-enabled", true, "Should the plugin lowercase messages found to contain too many capital letters?", "If false, the message will not be sent."),
        WARN_ON_CAPS_SENT("moderation-settings.warn-on-caps-sent", true, "Should the plugin send a warning message (defined in the locale file) when a player sends a message that contains too many capital letters?"),
        SPAM_CHECKING_ENABLED("moderation-settings.spam-checking-enabled", true, "Should the plugin check for players sending multiple of the same, or similar, messages?"),
        SPAM_MESSAGE_COUNT("moderation-settings.spam-message-count", 5, "How many similar messages are allowed to be sent before it can be seen as spam?", "Using this allows for players to correct themselves if they have a typo."),
        SPAM_FILTER_SENSITIVITY("moderation-settings.spam-filter-sensitivity", 0.3, "How similar are the messages that count as spam?"),
        WARN_ON_SPAM_SENT("moderation-settings.warn-on-spam-sent", true, "Should the plugin send a warning message (defined in the locale file) when a player spams?"),
        URL_CHECKING_ENABLED("moderation-settings.url-checking-enabled", true, "Should the plugin check for messages that contain URLs and IP addresses?"),
        URL_CENSORING_ENABLED("moderation-settings.url-censoring-enabled", true, "Should the plugin censor URLs and IP addresses?", "If true, messages will be censored; periods and click functionality will be removed.", "If false, the message will not be sent."),
        WARN_ON_URL_SENT("moderation-settings.warn-on-url-sent", true, "Should the plugin send a warning message (defined in the locale file) when a player sends a message containing a URL or IP address?"),
        SWEAR_CHECKING_ENABLED("moderation-settings.swear-checking-enabled", true, "Should the plugin check for swear words?"),
        SWEAR_FILTER_SENSITIVITY("moderation-settings.swear-filter-sensitivity", 0.2, "How similar are the messages that count as swears?"),
        BLOCKED_SWEARS("moderation-settings.blocked-swears", Collections.singletonList("bitch"), "If a player sends a message that contains one of these words, then the message will not be sent."),
        WARN_ON_BLOCKED_SWEAR_SENT("moderation-settings.warn-on-blocked-swear-sent", true, "Should the plugin send a warning message (defined in the locale file) when a player sends a message with a blocked swear in it?"),
        SWEAR_REPLACEMENTS("moderation-settings.swear-replacements", Arrays.asList("fuck:f***", "ass:butt"), "If a player sends a message that contains one of these words, then the word will be replaced.", "Format: 'contains:replaced'"),

        NICKNAME_SETTINGS("nickname-settings", null, "Nickname Settings"),
        MIN_NICKNAME_LENGTH("nickname-settings.min-nickname-length", 3, "The minimum length a nickname can be."),
        MAX_NICKNAME_LENGTH("nickname-settings.max-nickname-length", 32, "The maximum length a nickname can be."),
        ALLOW_SPACES_IN_NICKNAMES("nickname-settings.allow-spaces-in-nicknames", true, "Should spaces be allowed in nicknames?"),
        ALLOW_NONALPHANUMERIC_CHARACTERS_IN_NICKNAMES("nickname-settings.allow-nonalphanumeric-characters", true, "Should non-alphanumeric characters, such as brackets, be allowed in nicknames?"),

        CHAT_SETTINGS("chat-settings", null, "General Miscellaneous Settings"),
        OUTPUT_HOVER_EVENTS("chat-settings.output-hover-events", true, "Should hover events be output to the console?"),
        ALLOW_BUNGEECORD_MESSAGES("chat-settings.allow-bungeecord-messages", true, "Should players be allowed to message other players on connected servers?", "Requires BungeeCord"),
        MESSAGE_SOUND("chat-settings.message-sound", Sound.BLOCK_NOTE_BLOCK_PLING.name(), "The sound that will be sent to a player when they receive a message.", "Players can individually disable this in-game with /togglesound.", "Valid sounds can be found at: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html", "Set to 'none' for no sound."),
        CHAT_REPLACEMENTS("chat-settings.chat-replacements", new RoseSettingSection(
                new RoseSettingValue("rainbow", new RoseSettingSection(
                        new RoseSettingValue("text", "&h"),
                        new RoseSettingValue("replacement", "<r:0.3>")
                )),
                new RoseSettingValue("regex-example", new RoseSettingSection(
                        new RoseSettingValue("text", "[-]{0,1}[\\\\d]*[.]{0,1}[\\\\d]+"),
                        new RoseSettingValue("replacement", "&c%message%"),
                        new RoseSettingValue("hover", "&b&o<3"),
                        new RoseSettingValue("regex", true)
                ), "The '%message%' placeholder can be used to get what was replaced.", "For example, this regex matches URLS. The replacement changes the colour and adds an underline.")),
                "When a message containing the 'text' is sent in chat, it will be replaced with the 'replacment'.", "These can be used for custom color codes, general replacements or regular expressions.", "These replacements can be used with placeholders in placeholder.yml.", "Emoji's can be defined in the emojis.yml file, these are parsed separately from replacements."),

        TAGS("tags", new RoseSettingSection(
                new RoseSettingValue("player", new RoseSettingSection(
                        new RoseSettingValue("prefix", "@", "This is what the player will use to start a tag."),
                        new RoseSettingValue("tag-online-players", true, "This allows the tag to target online players."),
                        new RoseSettingValue("sound", Sound.BLOCK_NOTE_BLOCK_PLING.name(), "The sound that will be sent to the player who receives the tag.", "Players can individually disable this in-game with /togglesound.", "Valid sounds can be found at: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html", "Set to 'none' for no sound."),
                        new RoseSettingValue("format", "{player-tag}", "The format for this tag. These are placeholders defined in 'placeholders.yml'.")
                ), "This tag allows players to use @<player>, e.g. \"Hey @Lilac!\"", "The ID of the tag."),
                new RoseSettingValue("hashtag", new RoseSettingSection(
                        new RoseSettingValue("prefix", "#"),
                        new RoseSettingValue("format", "{hashtag}")
                ), "This tag allows players to use hashtags in-game.", "For example, '#RoseWood'"),
                new RoseSettingValue("money", new RoseSettingSection(
                        new RoseSettingValue("prefix", "$"),
                        new RoseSettingValue("format", "{money-tag}")
                ), "This tag allows players to type money in-game, and make it formatted and hoverable.", "For example, '$500'"),
                new RoseSettingValue("spoiler", new RoseSettingSection(
                        new RoseSettingValue("prefix", "<spoiler>"),
                        new RoseSettingValue("suffix", "</spoiler>"),
                        new RoseSettingValue("format", "{spoiler-tag}"),
                        new RoseSettingValue("match-length", true, "Match length allows the tag replacement to be the same length as the message sent.", "This is useful in tags with suffixes as it can replace a 3 letter long word with 3 of the same character.")
                ), "This tag allows for a spoiler tag in-game.", "This makes the message black and players can hover over it to see the real message.", "Format: <spoiler>Your Message</spoiler>"),
                new RoseSettingValue("copy-paste", new RoseSettingSection(
                        new RoseSettingValue("prefix", "{"),
                        new RoseSettingValue("suffix", "}"),
                        new RoseSettingValue("format", "{copy-paste}")
                ), "This tag allows players to copy a message."),
                new RoseSettingValue("staff-only", new RoseSettingSection(
                        new RoseSettingValue("prefix", "<staff>"),
                        new RoseSettingValue("suffix", "</staff>>"),
                        new RoseSettingValue("format", "{staff-only}"),
                        new RoseSettingValue("match-length", true)
                ), "This tag is like the spoiler tag, but only players with a certain permission can view it.")
        ), "Tag Settings", "Tags can be used to tag specific players or add extra detail to messages.", "Tags use placeholders, defined in placeholders.yml, to allow hoverable and clickable tags."),

        CHAT_CHANNELS("chat-channels", new RoseSettingSection(
                new RoseSettingValue("global", new RoseSettingSection(
                        new RoseSettingValue("default", true, "This sets the channel as the default channel that players will be placed into when they first join the server."),
                        new RoseSettingValue("format", "{prefix}{player}{separator}{message}", "This is the format of the channel.", "The format uses placeholders defined in placeholders.yml"),
                        new RoseSettingValue("visible-anywhere", true, "Can messages from this channel be sent and read while in other channels?"),
                        new RoseSettingValue("discord", "global", "The DiscordSRV channel that messages can be sent and received from.", "These can be defined in DiscordSRV's config file.", "Requires DiscordSRV"),
                        new RoseSettingValue("servers", Collections.singletonList("factions"), "This allows messages to be sent to the listed servers.", "These are the same server names as defined in bungeecord.yml", "Requires BungeeCord")
                ), "The ID of the channel, the player can use this to join the channel.", "For example, /c global"),
                new RoseSettingValue("staff", new RoseSettingSection(
                        new RoseSettingValue("format", "{channel-prefix}{player}{separator}{message}"),
                        new RoseSettingValue("visible-anywhere", true),
                        new RoseSettingValue("command", "staff", "Creates a command alias, /staff, for this channel.")
                ), "Staff Channel - /c staff"),
                new RoseSettingValue("local", new RoseSettingSection(
                        new RoseSettingValue("radius", 200, "The radius is the distance between players that can read the messages that are sent.")
                ), "Local Channel - /c local", "If no format is given, then the format of the default channel will be used"),
                new RoseSettingValue("skyblock", new RoseSettingSection(
                        new RoseSettingValue("format", "{channel-prefix}{player}{separator}{message}"),
                        new RoseSettingValue("world", "skyblock", "This option makes messages only able to be sent and received when the player is in this world."),
                        new RoseSettingValue("joinable", false, "This stops players from joining the channel with a command."),
                        new RoseSettingValue("auto-join", true, "When auto-join is true and combined with a world channel, as soon as the player joins the world they will be added to the channel.")
                ), "Skyblock Channel - /c skyblock")
        ), "Chat Channel Settings", "Chat Channels are different chats that players can use.", "These can be accessed with /channel or /c in-game"),

        CHAT_FORMATS("chat-formats", null,"These are all of the miscellanous chat formats in the plugin."),
        MESSAGE_SENT_FORMAT("chat-formats.message-sent", "{message-sent-sender}{message-sent-arrow}{message-sent-receiver}{message}", "The format of a /message sent to another player."),
        MESSAGE_RECEIVED_FORMAT("chat-formats.message-received", "{message-received-sender}{message-received-arrow}{message-received-receiver}{message}", "The format of a /message recieved from another player."),
        MESSAGE_SPY_FORMAT("chat-formats.message-spy", "{spy-prefix}{spy-message}", "The format of a spied /message."),
        GROUP_FORMAT("chat-formats.group", "{group-prefix}{group-member-prefix}{player}{separator}{message}", "The format of a group message."),
        GROUP_SPY_FORMAT("chat-formats.group-spy", "{spy-prefix}{group-prefix}{group-member-prefix}{player}{separator}{message}", "The format of a spied group message."),
        CHANNEL_SPY_FORMAT("chat-formats.channel-spy", "{spy-prefix}{channel-prefix}{player}{separator}{message}", "The format of a spied channel message."),
        DELETE_CLIENT_MESSAGES_FORMAT("chat-formats.delete-client-messages", "{delete-message}", "The format of a delete client message button."),
        DELETE_OWN_MESSAGES_FORMAT("chat-formats.delete-own-messages", "{delete-message}", "The format of a delete message button."),
        DELETE_OTHER_MESSAGES_FORMAT("chat-formats.delete-other-messages", "{delete-message}", "The format of a delete other message button."),
        DELETED_MESSAGE_FORMAT("chat-formats.deleted-message-format", "{delete-message}", "The format of a previously deleted message."),
        MINECRAFT_TO_DISCORD_FORMAT("chat-formats.minecraft-to-discord", "{discord}", "The format of a message that was sent to discord.", "Requires DiscordSRV"),
        DISCORD_TO_MINECRAFT_FORMAT("chat-formats.discord-to-minecraft", "{from-discord}", "The format of a message that was sent from discord.", "Requires DiscordSRV"),

        DISCORD_FORMATS("discord-formats", null, "Discord Format Settings", "Requires DiscordSRV"),
        DISCORD_FORMAT_BOLD("discord-formats.bold", "&l"),
        DISCORD_FORMAT_UNDERLINE("discord-formats.underline", "&m"),
        DISCORD_FORMAT_STRIKETHROUGH("discord-formats.strikethrough", "&n"),
        DISCORD_FORMAT_ITALIC("discord-formats.italic", "&o"),
        DISCORD_FORMAT_SPOILER("discord-formats.spoiler", "<spoiler>%message%</spoiler>", "This can be changed to '&k' if the spoiler tag is not enabled."),
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
