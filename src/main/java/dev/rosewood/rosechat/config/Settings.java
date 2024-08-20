package dev.rosewood.rosechat.config;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.RoseSettingSerializer;
import org.bukkit.Sound;
import org.bukkit.event.EventPriority;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static dev.rosewood.rosechat.config.SettingSerializers.*;
import static dev.rosewood.rosegarden.config.RoseSettingSerializers.*;

public final class Settings {

    private static final List<RoseSetting<?>> KEYS = new ArrayList<>();

    public static final RoseSetting<CommentedConfigurationSection> MODERATION_SETTINGS = create("moderation-settings", "Moderation Settings");
    public static final RoseSetting<Boolean> CAPS_CHECKING_ENABLED = create(MODERATION_SETTINGS, "caps-checking-enabled", BOOLEAN, true,
            "Should the plugin check for messages that contain too many capital letters?");
    public static final RoseSetting<Integer> MAXIMUM_CAPS_ALLOWED = create(MODERATION_SETTINGS, "maximum-caps-allowed", INTEGER, 5,
            "The maximum amount of capital letters that are allowed in one message.",
                        "Using this allows players to use words like 'LOL'.");
    public static final RoseSetting<Boolean> LOWERCASE_CAPS_ENABLED = create(MODERATION_SETTINGS, "lowercase-caps-enabled", BOOLEAN, true,
            "Should the plugin lowercase messages found to contain too many capital letters?",
                        "If false, the message will not be sent.");
    public static final RoseSetting<Boolean> WARN_ON_CAPS_SENT = create(MODERATION_SETTINGS, "warn-on-caps-sent", BOOLEAN, true,
            "Should the plugin send a warning message (defined in the locale file) when a player sends a message that contains too many capital letters?",
                        "This requires lowercase-caps-enabled to be false, as the message will not be sent.");
    public static final RoseSetting<Boolean> SPAM_CHECKING_ENABLED = create(MODERATION_SETTINGS, "spam-checking-enabled", BOOLEAN, true,
            "Should the plugin check for players send multiple of the same, or similar, message?");
    public static final RoseSetting<Integer> SPAM_MESSAGE_COUNT = create(MODERATION_SETTINGS, "spam-message-count", INTEGER, 5,
            "How many similar messages are allowed to be sent before it is seen as spam?",
                        "Using this allows players to correct themselves if they have a typo.");
    public static final RoseSetting<Double> SPAM_FILTER_SENSITIVITY = create(MODERATION_SETTINGS, "spam-filter-sensitivity", DOUBLE, 30.0,
            "The sensitivity of the spam filter.",
                        "A higher number will catch words that are more different.",
                        "For example, a low sensitivity will catch bitch and bítch, but a high sensitivity may catch batch.",
                        "A lower value may be preferred to prevent catching real words.");
    public static final RoseSetting<Boolean> WARN_ON_SPAM_SENT = create(MODERATION_SETTINGS, "warn-on-spam-sent", BOOLEAN, true,
            "Should the plugin send a warning message (defined in the locale file) when a player spams?");
    public static final RoseSetting<Boolean> URL_CHECKING_ENABLED = create(MODERATION_SETTINGS, "url-checking-enabled", BOOLEAN, true,
            "Should the plugin check for messages that contain URLs and IP addresses?",
                        "If false, a player will still need permission to send URLs that can be clicked on.");
    public static final RoseSetting<Boolean> URL_CENSORING_ENABLED = create(MODERATION_SETTINGS, "url-censoring-enabled", BOOLEAN, true,
            "Should the plugin censor URLs and IP addresses?",
                        "If true, messages will be censored; periods and click functionality will be removed.",
                        "If false, the message will not be sent.");
    public static final RoseSetting<Boolean> WARN_ON_URL_SENT = create(MODERATION_SETTINGS, "warn-on-url-sent", BOOLEAN, true,
            "Should the plugin send a warning message (defined in the locale file) when a player sends a message that contains a URL or IP address?",
                        "This requires url-censoring-enabled to be false, as the message will not be sent.");
    public static final RoseSetting<Boolean> SWEAR_CHECKING_ENABLED = create(MODERATION_SETTINGS, "swear-checking-enabled", BOOLEAN, true,
            "Should the plugin check for swear words?");
    public static final RoseSetting<Double> SWEAR_FILTER_SENSITIVITY = create(MODERATION_SETTINGS, "swear-filter-sensitivity", DOUBLE, 25.0,
            "The sensitivity of the swear filter.",
                        "A higher number will catch words that are more different.",
                        "For example, a low sensitivity will catch bitch and bítch, but a high sensitivity may catch batch.",
                        "A lower value may be preferred to prevent catching real words.");
    public static final RoseSetting<List<String>> BLOCKED_SWEARS = create(MODERATION_SETTINGS, "blocked-swears", STRING_LIST, Collections.singletonList("bitch"),
            "If a player sends a message that contains one of these words, then the message will not be sent.");
    public static final RoseSetting<Boolean> WARN_ON_BLOCKED_SWEAR_SENT = create(MODERATION_SETTINGS, "warn-on-blocked-swear-sent", BOOLEAN, true,
            "Should the plugin send a warning message (defined in the locale file) when a player sends a message with a blocked wear word?");
    public static final RoseSetting<List<String>> SWEAR_REPLACEMENTS = create(MODERATION_SETTINGS, "swear-replacements", STRING_LIST, Arrays.asList("fuck:f***", "ass:butt"),
            "If a player sends a message that contains one of these words, then the word will be replaced.",
                        "Note: This does not affect words like 'assassin'.",
                        "Format: 'contains:replaced'");
    public static final RoseSetting<Boolean> ENABLE_DELETING_MESSAGES = create(MODERATION_SETTINGS, "enable-deleting-messages", BOOLEAN, true,
            "Should deleting messages be enabled?",
                        "Requires ProtocolLib");

    public static final RoseSetting<CommentedConfigurationSection> NICKNAME_SETTINGS = create("nickname-settings", "Nickname Settings");
    public static final RoseSetting<Integer> MINIMUM_NICKNAME_LENGTH = create(NICKNAME_SETTINGS, "minimum-nickname-length", INTEGER, 3,
            "The minimum length a nickname can be.");
    public static final RoseSetting<Integer> MAXIMUM_NICKNAME_LENGTH = create(NICKNAME_SETTINGS, "maximum-nickname-length", INTEGER, 32,
            "The maximum length a nickname can be.",
                        "Note: This does not include color codes.");
    public static final RoseSetting<Boolean> ALLOW_SPACES_IN_NICKNAMES = create(NICKNAME_SETTINGS, "allow-spaces-in-nicknames", BOOLEAN, true,
            "Should spaces be allowed in nicknames?");
    public static final RoseSetting<Boolean> ALLOW_NONALPHANUMERIC_CHARACTERS = create(NICKNAME_SETTINGS, "allow-nonalphanumeric-characters", BOOLEAN, true,
            "Should non-alphanumeric characters, such as brackets, be allowed in nicknames?");
    public static final RoseSetting<Boolean> UPDATE_DISPLAY_NAMES = create(NICKNAME_SETTINGS, "update-display-names-on-chat", BOOLEAN, true,
            "Should player display names be updated every time the player sends a message?",
                        "When disabled, display names will only be updated when the player uses /nick.",
                        "This allows other plugins to get the display name at the time that the player last sent a message, rather than when they set their nickname");
    public static final RoseSetting<Boolean> UPDATE_PLAYER_LIST = create(NICKNAME_SETTINGS, "update-player-list", BOOLEAN, true,
            "Should the player list (tab) be updated when a player changes their nickname?");
    public static final RoseSetting<Boolean> ALLOW_DUPLICATE_NAMES = create(NICKNAME_SETTINGS, "allow-duplicate-names", BOOLEAN, true,
            "Should multiple players be allowed to use the same nickname?");

    public static final RoseSetting<CommentedConfigurationSection> CHAT_SETTINGS = create("chat-settings", "General Miscellaneous Settings");
    public static final RoseSetting<EventPriority> CHAT_EVENT_PRIORITY = create(CHAT_SETTINGS, "chat-event-priority", EVENT_PRIORITY, EventPriority.LOW,
            "The event priority for the chat listener.",
                        "This may need to be changed if another plugin does something with chat.",
                        "Valid Options: LOWEST, LOW, NORMAL, HIGH, HIGHEST");
    public static final RoseSetting<String> PACKET_EVENT_PRIORITY = create(CHAT_SETTINGS, "packet-event-priority", STRING, "NORMAL",
            "The event priority for adding the delete button to messages.",
                        "This may need to be changed if another plugin also uses ProtocolLib to edit messages.",
                        "Valid Options: LOWEST, LOW, NORMAL, HIGH, HIGHEST");
    public static final RoseSetting<Boolean> ALLOW_BUNGEECORD_MESSAGES = create(CHAT_SETTINGS, "allow-bungeecord-messages", BOOLEAN, true,
            "Should players be allowed to message other players on connected servers?",
                        "Requires BungeeCord");
    public static final RoseSetting<Integer> BUNGEECORD_MESSAGE_TIMEOUT = create(CHAT_SETTINGS, "bungeecord-message-timeout", INTEGER, 500,
            "How long should the server wait when sending a message to another server?",
                        "Requires BungeeCord");
    public static final RoseSetting<Sound> MESSAGE_SOUND = create(CHAT_SETTINGS, "message-sound", SOUND, Sound.BLOCK_NOTE_BLOCK_PLING,
            "The sound that will be sent to a player when they receive a message.",
                        "Players can individually disable this in-game with /togglesound.",
                        "Valid sounds can be found at: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html",
                        "Set to 'none' to disable.");
    public static final RoseSetting<Boolean> USE_MARKDOWN_FORMATTING = create(CHAT_SETTINGS, "use-markdown-formatting-in-game", BOOLEAN, true,
            "Should players be allowed to use markdown formatting in-game?",
                        "For example, using **text** to make text bold.");
    public static final RoseSetting<List<String>> CORE_SHADER_COLORS = create(CHAT_SETTINGS, "core-shader-colors", STRING_LIST, Collections.singletonList("#FFFFFE"),
            "When a player uses one of these colors, RoseChat will shift the color slightly (#FFFFFE -> #FFFFFD) to avoid the shader being used without permission");
    public static final RoseSetting<Boolean> USE_PER_COLOR_PERMISSIONS = create(CHAT_SETTINGS, "use-per-color-permissions", BOOLEAN, false,
            "Should there be a permission to use each chat color?",
                        "For example, using 'rosechat.red.<location>' to use &c.",
                        "More information can be found on the wiki: https://github.com/Rosewood-Development/RoseChat/wiki/Commands-%26-Permissions/",
                        "A full list of colors can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/ChatColor.html");
    public static final RoseSetting<Integer> WORLDGUARD_CHECK_INTERVAL = create(CHAT_SETTINGS, "worldguard-check-interval", INTEGER, 20,
            "How often, in ticks, should the plugin check if a player is in a region associated with a WorldGuard channel?",
                        "Requires WorldGuard");
    public static final RoseSetting<Boolean> CAN_JOIN_GROUP_CHANNELS = create(CHAT_SETTINGS, "can-join-group-channels", BOOLEAN, true,
            "Should players be allowed to join group channels like normal channels?",
                        "A player can use '/gcmsg <channel>' without a message to join the channel.");
    public static final RoseSetting<Boolean> ADD_GROUP_CHANNELS_TO_CHANNEL_LIST = create(CHAT_SETTINGS, "add-group-channels-to-channel-list", BOOLEAN, true,
            "Should group channels be accessible using /channel instead of /gcm?",
                        "The can-join-group-channels setting will not take affect.");
    public static final RoseSetting<Boolean> REMOVE_COLOR_CODES = create(CHAT_SETTINGS, "remove-color-codes", BOOLEAN, false,
            "Should color codes be removed if a player attempts to send colors without having permission?");
    public static final RoseSetting<Boolean> ALLOW_NO_HELD_ITEM = create(CHAT_SETTINGS, "allow-no-held-item", BOOLEAN, true,
            "Should players be allowed to use the held item replacement if they are not holding an item?",
                        "If false, the 'no-held-item' locale message will be sent.");

    public static final RoseSetting<CommentedConfigurationSection> DISCORD_SETTINGS = create("discord-settings", "Discord Settings", "Requires DiscordSRV");
    public static final RoseSetting<Boolean> USE_DISCORD = create(DISCORD_SETTINGS, "use-discord", BOOLEAN, true,
            "Should DiscordSRV support be enabled?");
    public static final RoseSetting<Boolean> USE_IGN_WITH_DISCORD = create(DISCORD_SETTINGS, "use-minecraft-ign-with-discord", BOOLEAN, true,
            "Should a player's in-game name (nickname, display name, or username) be used instead of the Discord name?",
                        "This will only work if a player has linked their accounts.");
    public static final RoseSetting<Boolean> DELETE_DISCORD_MESSAGES = create(DISCORD_SETTINGS, "delete-discord-messages", BOOLEAN, true,
            "Should messages sent in Discord be deleted when the same message is deleted in-game, and vice versa?",
                        "Requires ProtocolLib");
    public static final RoseSetting<Boolean> EDIT_DISCORD_MESSAGES = create(DISCORD_SETTINGS, "edit-messages", BOOLEAN, true,
            "Should messages in-game be edited when the same message is edited in Discord?");
    public static final RoseSetting<Boolean> REQUIRE_PERMISSIONS = create(DISCORD_SETTINGS, "require-permissions", BOOLEAN, true,
            "Should messages sent in Discord require in-game permissions?",
                        "For example, when sending a message with color.");
    public static final RoseSetting<Boolean> DELETE_BLOCKED_MESSAGES = create(DISCORD_SETTINGS, "delete-blocked-messages", BOOLEAN, true,
            "Should messages that are blocked by moderation settings (e.g. swears) be deleted when sent from Discord?",
                        "The require-permissions setting needs to be enabled for this to work.");
    public static final RoseSetting<Integer> DISCORD_MESSAGE_LIMIT = create(DISCORD_SETTINGS, "message-limit", INTEGER, 3,
            "Players can send multiple lines when using DiscordSRV. This may spam the chat.",
                        "How many lines can a player send from Discord? The rest will not be sent.");
    public static final RoseSetting<Boolean> CAN_TAG_MEMBERS = create(DISCORD_SETTINGS, "can-tag-members", BOOLEAN, true,
            "Can players tag Discord members?",
                        "Players can use @<username> in messages to tag a member.");
    public static final RoseSetting<Boolean> SUPPORT_THIRD_PARTY_PLUGINS = create(DISCORD_SETTINGS, "support-third-party-plugins", BOOLEAN, false,
            "Should third-party plugins be supported?",
                        "This should be enabled when using plugins that edit Discord messages.",
                        "For example, InteractiveChatDiscordAddon.",
                        "This must be enabled if you want to use Webhooks with DiscordSRV.",
                        "Enabling this will disable the ability to delete Discord messages from in-game.");

    public static final RoseSetting<CommentedConfigurationSection> CHAT_FORMATS = create("chat-formats", "These are the other chat formats in the plugin.");
    public static final RoseSetting<String> MESSAGE_SENT_FORMAT = create(CHAT_FORMATS, "message-sent", STRING, "{message-sent}{message}",
            "The format of a /message sent to another player.");
    public static final RoseSetting<String> MESSAGE_RECEIVED_FORMAT = create(CHAT_FORMATS, "message-received", STRING, "{message-received}{message}",
            "The format of a /message received from another player.");
    public static final RoseSetting<String> CONSOLE_MESSAGE_FORMAT = create(CHAT_FORMATS, "console-message", STRING, "{console-message}{message}",
            "The format of a /message which will be displayed in the console.");
    public static final RoseSetting<String> MESSAGE_SPY_FORMAT = create(CHAT_FORMATS, "message-spy", STRING, "{spy-prefix}{spy-players}{message}",
            "The format of a spied /message.");
    public static final RoseSetting<String> GROUP_FORMAT = create(CHAT_FORMATS, "group", STRING, "{group-prefix}{group-member-prefix}{player}{separator}{message}",
            "The format of a group message.");
    public static final RoseSetting<String> GROUP_SPY_FORMAT = create(CHAT_FORMATS, "group-spy", STRING, "{spy-prefix}{group-prefix}{group-member-prefix}{player}{separator}{message}",
            "The format of a spied group message.");
    public static final RoseSetting<String> CHANNEL_SPY_FORMAT = create(CHAT_FORMATS, "channel-spy", STRING, "{spy-prefix}{channel-prefix}{player}{separator}{message}",
            "The format of a spied channel message.");
    public static final RoseSetting<Boolean> DELETE_MESSAGE_SUFFIX = create(CHAT_FORMATS, "delete-message-suffix", BOOLEAN, false,
            "Should the delete button be added to the end of the client message?");
    public static final RoseSetting<String> DELETE_CLIENT_MESSAGE_FORMAT = create(CHAT_FORMATS, "delete-client-message", STRING, "{delete-message}",
            "The format of a button to delete messages sent from the server to a player.",
                        "Requires ProtocolLib");
    public static final RoseSetting<String> DELETE_OWN_MESSAGE_FORMAT = create(CHAT_FORMATS, "delete-own-message", STRING, "{delete-message}",
            "The format of a button to delete a player's own message.",
                        "Requires ProtocolLib");
    public static final RoseSetting<String> DELETE_OTHER_MESSAGE_FORMAT = create(CHAT_FORMATS, "delete-other-message", STRING, "{delete-message}",
            "The format of a button to delete other players' messages.",
                        "Requires ProtocolLib");
    public static final RoseSetting<String> DELETED_MESSAGE_FORMAT = create(CHAT_FORMATS, "deleted-message", STRING, "{deleted-message}",
            "The format of a previously deleted message.",
                        "Requires ProtocolLib");
    public static final RoseSetting<String> HELD_ITEM_REPLACEMENT = create(CHAT_FORMATS, "held-item-replacement", STRING, "item",
            "The ID of the replacement, found in replacements.yml, for held items in chat.");
    public static final RoseSetting<String> EDITED_DISCORD_MESSAGE_FORMAT = create(CHAT_FORMATS, "edited-discord-message", STRING, "{edited}",
            "The format of an edited discord message.");

    public static final RoseSetting<CommentedConfigurationSection> MARKDOWN_FORMATS = create("markdown-formats", "Markdown Format Settings");
    public static final RoseSetting<String> MARKDOWN_FORMAT_BOLD = create(MARKDOWN_FORMATS, "bold", STRING, "&l%message%&L");
    public static final RoseSetting<String> MARKDOWN_FORMAT_UNDERLINE = create(MARKDOWN_FORMATS, "underline", STRING, "&n%message&N");
    public static final RoseSetting<String> MARKDOWN_FORMAT_STRIKETHROUGH = create(MARKDOWN_FORMATS, "strikethrough", STRING, "&m%message%&M");
    public static final RoseSetting<String> MARKDOWN_FORMAT_ITALIC = create(MARKDOWN_FORMATS, "italic", STRING, "&o%message%&O");
    public static final RoseSetting<String> MARKDOWN_FORMAT_SPOILER = create(MARKDOWN_FORMATS, "spoiler", STRING, "<spoiler>%message%</spoiler>");
    public static final RoseSetting<String> MARKDOWN_FORMAT_CODE_BLOCK_ONE = create(MARKDOWN_FORMATS, "code-block-one", STRING, "`%message%`");
    public static final RoseSetting<String> MARKDOWN_FORMAT_CODE_BLOCK_MULTIPLE = create(MARKDOWN_FORMATS, "code-block-multiple", STRING, "```%message%```");
    public static final RoseSetting<String> MARKDOWN_FORMAT_BLOCK_QUOTES = create(MARKDOWN_FORMATS, "block-quotes", STRING, "&2> &o");
    public static final RoseSetting<String> MARKDOWN_FORMAT_URL = create(MARKDOWN_FORMATS, "url", STRING, "{url}");
    public static final RoseSetting<String> DISCORD_FORMAT_CHANNEL = create(MARKDOWN_FORMATS, "channel-link", STRING, "{discord-channel-link}");

    private static <T> RoseSetting<T> create(RoseSetting<CommentedConfigurationSection> section, String key, RoseSettingSerializer<T> serializer, T defaultValue, String... comments) {
        RoseSetting<T> setting = RoseSetting.backed(RoseChat.getInstance(), section.getKey() + "." + key,
                serializer, defaultValue, comments);
        KEYS.add(setting);
        return setting;
    }

    private static <T> RoseSetting<T> create(String key, RoseSettingSerializer<T> serializer, T defaultValue, String... comments) {
        RoseSetting<T> setting = RoseSetting.backed(RoseChat.getInstance(), key, serializer, defaultValue, comments);
        KEYS.add(setting);
        return setting;
    }

    private static RoseSetting<CommentedConfigurationSection> create(String key, String... comments) {
        RoseSetting<CommentedConfigurationSection> setting = RoseSetting.backedSection(RoseChat.getInstance(), key, comments);
        KEYS.add(setting);
        return setting;
    }

    public static List<RoseSetting<?>> getKeys() {
        return Collections.unmodifiableList(KEYS);
    }

}
