package dev.rosewood.rosechat.config;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.RoseSettingSerializer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.event.EventPriority;
import static dev.rosewood.rosechat.config.SettingSerializers.EVENT_PRIORITY;
import static dev.rosewood.rosegarden.config.RoseSettingSerializers.BOOLEAN;
import static dev.rosewood.rosegarden.config.RoseSettingSerializers.DOUBLE;
import static dev.rosewood.rosegarden.config.RoseSettingSerializers.INTEGER;
import static dev.rosewood.rosegarden.config.RoseSettingSerializers.STRING;
import static dev.rosewood.rosegarden.config.RoseSettingSerializers.STRING_LIST;

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
    public static final RoseSetting<Boolean> ENABLE_DELETING_MESSAGES = create(MODERATION_SETTINGS, "enable-deleting-messages", BOOLEAN, true,
            "Should deleting messages be enabled?",
                        "Requires ProtocolLib");
    public static final RoseSetting<Boolean> SAVE_CHAT_LOG_TO_FILE = create(MODERATION_SETTINGS, "save-chat-log-to-file", BOOLEAN, false,
            "Should the plugin keep track of every message sent by players and store them in a file?");
    public static final RoseSetting<Boolean> SEND_BLOCKED_MESSAGES_TO_STAFF = create(MODERATION_SETTINGS, "send-blocked-messages-to-staff", BOOLEAN, false,
            "Should blocked messages be sent to those with the 'rosechat.seeblocked' permission.");

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
    public static final RoseSetting<Boolean> KEEP_MESSAGE_FORMAT = create(CHAT_SETTINGS, "keep-message-format", BOOLEAN, false,
            "Should the message format be kept when sending a message to another server?",
                        "Requires BungeeCord");
    public static final RoseSetting<Integer> BUNGEECORD_MESSAGE_TIMEOUT = create(CHAT_SETTINGS, "bungeecord-message-timeout", INTEGER, 500,
            "How long should the server wait when sending a message to another server?",
                        "Requires BungeeCord");
    public static final RoseSetting<String> MESSAGE_SOUND = create(CHAT_SETTINGS, "message-sound", STRING, "minecraft:block.note_block.pling",
            "The sound that will be sent to a player when they receive a message.",
                        "Players can individually disable this in-game with /togglesound.",
                        "Sounds are namespaced to allow custom sounds from resource packs.",
                        "A list of vanilla sounds can be found at: https://www.digminecraft.com/lists/sound_list_pc.php",
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
    public static final RoseSetting<Boolean> ADD_GROUP_CHANNELS_TO_CHANNEL_LIST = create(CHAT_SETTINGS, "add-group-channels-to-channel-list", BOOLEAN, false,
            "Should group channels be accessible using /channel instead of /gcm?",
                        "The can-join-group-channels setting will not take affect.");
    public static final RoseSetting<Boolean> SEND_GROUP_LEAVE_TO_ALL_MEMBERS = create(CHAT_SETTINGS, "send-group-leave-to-all-members", BOOLEAN, true,
            "Should the message, sent when a player leaves a group, be sent to all members of the group?");
    public static final RoseSetting<Boolean> DISBAND_GROUP_ON_OWNER_DISCONNECT = create(CHAT_SETTINGS, "disband-group-on-owner-disconnect", BOOLEAN, false,
            "Should group channels be disbanded when the owner disconnects?",
                        "Enabling this allows for temporary group chats. Owners can still use /group promote to select a new owner.");
    public static final RoseSetting<Boolean> REMOVE_COLOR_CODES = create(CHAT_SETTINGS, "remove-color-codes", BOOLEAN, false,
            "Should color codes be removed if a player attempts to send colors without having permission?");
    public static final RoseSetting<Boolean> REMOVE_REPLACEMENTS = create(CHAT_SETTINGS, "remove-replacements", BOOLEAN, false,
            "Should replacements be removed if a player attempts to send a replacement without having permission?");
    public static final RoseSetting<Boolean> ALLOW_NO_HELD_ITEM = create(CHAT_SETTINGS, "allow-no-held-item", BOOLEAN, true,
            "Should players be allowed to use the held item replacement if they are not holding an item?",
                        "If false, the 'no-held-item' locale message will be sent.");
    public static final RoseSetting<Boolean> ALLOW_CHAT_SUGGESTIONS = create(CHAT_SETTINGS, "allow-chat-suggestions", BOOLEAN, true,
            "Can players use tab to display emoji, replacement, and placeholder suggestions in chat?",
                        "This allows players to see their available replacements when sending a message in chat. Requires 1.19+");
    public static final RoseSetting<Boolean> ENABLE_ON_SIGNS = create(CHAT_SETTINGS, "enable-on-signs", BOOLEAN, false,
            "Can players use RoseChat features on signs?",
                        "Players will require the sign permissions. For example, 'rosechat.replacements.sign', to use replacements on signs.",
                        "Players will also need the individual permissions, such as 'rosechat.replacement.heart'.");

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

    public static final RoseSetting<CommentedConfigurationSection> MARKDOWN_FORMATS = create("markdown-formats", "Markdown Format Settings", "Use %input_1% to substitute the player message");
    public static final RoseSetting<String> MARKDOWN_FORMAT_BOLD = create(MARKDOWN_FORMATS, "bold", STRING, "&l%input_1%&L");
    public static final RoseSetting<String> MARKDOWN_FORMAT_UNDERLINE = create(MARKDOWN_FORMATS, "underline", STRING, "&n%input_1%&N");
    public static final RoseSetting<String> MARKDOWN_FORMAT_STRIKETHROUGH = create(MARKDOWN_FORMATS, "strikethrough", STRING, "&m%input_1%&M");
    public static final RoseSetting<String> MARKDOWN_FORMAT_ITALIC = create(MARKDOWN_FORMATS, "italic", STRING, "&o%input_1%&O");
    public static final RoseSetting<String> MARKDOWN_FORMAT_SPOILER = create(MARKDOWN_FORMATS, "spoiler", STRING, "<spoiler>%input_1%</spoiler>");
    public static final RoseSetting<String> MARKDOWN_FORMAT_CODE_BLOCK_ONE = create(MARKDOWN_FORMATS, "code-block-one", STRING, "`%input_1%`");
    public static final RoseSetting<String> MARKDOWN_FORMAT_CODE_BLOCK_MULTIPLE = create(MARKDOWN_FORMATS, "code-block-multiple", STRING, "```%input_1%```");
    public static final RoseSetting<String> MARKDOWN_FORMAT_BLOCK_QUOTES = create(MARKDOWN_FORMATS, "block-quotes", STRING, "&2> &o");
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
