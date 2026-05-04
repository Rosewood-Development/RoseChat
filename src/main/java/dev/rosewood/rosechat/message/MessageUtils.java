package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.player.PlayerReceiveMessageEvent;
import dev.rosewood.rosechat.api.event.player.PlayerSendMessageEvent;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageRules.RuleOutputs;
import dev.rosewood.rosechat.message.contents.MessageContents;
import dev.rosewood.rosechat.message.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.tokenizer.Tokenizers;
import dev.rosewood.rosechat.message.tokenizer.composer.ChatComposer;
import dev.rosewood.rosechat.message.tokenizer.placeholder.RoseChatPlaceholderTokenizer;
import dev.rosewood.rosechat.message.tokenizer.shader.ShaderTokenizer;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.text.Normalizer;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ChatVersion;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.chat.VersionedComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

@SuppressWarnings("deprecation")
public class MessageUtils {

    public static final char ESCAPE_CHAR = '\\';
    public static final char SHADOW_PREFIX = '$';
    public static final Pattern PUNCTUATION_REGEX = Pattern.compile("[\\p{P}\\p{S}]");
    public static final Pattern URL_PATTERN = Pattern.compile("(http(s)?://)?[-a-zA-Z0-9@:%_+~#=]{2,32}(?<!\\.)\\.(?!\\.)[a-zA-Z0-9()]{2,6}\\b([-a-zA-Z0-9()@:%_+~#?&/=]*(?<!\\.)\\.?(?!\\.))*");
    public static final Pattern LEGACY_REGEX = Pattern.compile("&[0-9a-fA-F]");
    public static final Pattern LEGACY_REGEX_PARSED = Pattern.compile("§[0-9a-fA-F]");
    public static final Pattern LEGACY_REGEX_FORMATTING = Pattern.compile("&[k-oK-OrR]");
    public static final Pattern LEGACY_REGEX_FORMATTING_PARSED = Pattern.compile("§[k-oK-OrR]");
    public static final Pattern LEGACY_REGEX_COMBINED = Pattern.compile("([&§])[0-9a-fA-F]|([&§])[k-oK-OrR]");
    public static final Pattern HEX_REGEX = Pattern.compile("<#([A-Fa-f0-9]){6}>|\\{#([A-Fa-f0-9]){6}}|&#([A-Fa-f0-9]){6}|#([A-Fa-f0-9]){6}");
    public static final Pattern SPIGOT_HEX_REGEX = Pattern.compile("&x(&[A-Fa-f0-9]){6}");
    public static final Pattern SPIGOT_HEX_REGEX_PARSED = Pattern.compile("#(§[A-Fa-f0-9]){6}|§x(§[A-Fa-f0-9]){6}");
    public static final Pattern SPIGOT_HEX_REGEX_COMBINED = Pattern.compile("<#([A-Fa-f0-9]){6}>|\\{#([A-Fa-f0-9]){6}}|&#([A-Fa-f0-9]){6}|#([A-Fa-f0-9]){6}|&x(&[A-Fa-f0-9]){6}|#(§[A-Fa-f0-9]){6}|§x(§[A-Fa-f0-9]){6}");
    public static final Pattern RAINBOW_PATTERN = Pattern.compile("<(rainbow|r)(#(?<speed>\\d+))?(:(?<saturation>\\d*\\.?\\d+))?(:(?<brightness>\\d*\\.?\\d+))?>");
    public static final Pattern GRADIENT_PATTERN = Pattern.compile("<(gradient|g)(#(?<speed>\\d+))?(?<hex>(:#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})){2,})>");

    private static final boolean HAS_VERSIONED_SERIALIZER;
    static {
        boolean versioned = false;
        try {
            Class.forName("net.md_5.bungee.chat.VersionedComponentSerializer");
            versioned = true;
        } catch (ClassNotFoundException ignored) { }
        HAS_VERSIONED_SERIALIZER = versioned;
    }
    /**
     * Removes the accents from a string.
     * @param string The string to use.
     * @return A string without accents.
     */
    public static String stripAccents(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        string = Normalizer.normalize(string, Normalizer.Form.NFKD);

        for (char c : string.toCharArray()) {
            if (c <= '\u007F') sb.append(c);
        }

        return sb.toString();
    }

    /**
     * Gets the {@link LevenshteinDistance} between two given strings.
     * @param first The first string to use.
     * @param second The string to compare with.
     * @return A percentage of difference between the two strings.
     */
    public static double getLevenshteinDistancePercent(String first, String second) {
        int levDistance = LevenshteinDistance.getDefaultInstance()
                .apply(MessageUtils.stripAccents(first.toLowerCase()), MessageUtils.stripAccents(second.toLowerCase()));

        String longerMessage = second;

        if (second.length() < first.length())
            longerMessage = first;

        return (longerMessage.length() - levDistance) / (double) longerMessage.length();
    }

    /**
     * Checks if the given string is empty.
     * @param message The string to check.
     * @return True if the message is empty.
     */
    public static boolean isMessageEmpty(String message) {
        String colorized = HexUtils.colorify(message);
        return StringUtils.isBlank(ChatColor.stripColor(colorized));
    }

    /**
     * @param cs The {@link CharSequence} to check.
     * @return True if the {@link CharSequence} is alphanumeric or a space.
     */
    public static boolean isAlphanumericSpace(CharSequence cs) {
        if (cs == null)
            return false;

        int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isLetterOrDigit(cs.charAt(i)) && cs.charAt(i) != ' ')
                return false;
        }

        return true;
    }

    public static String stripShaderColors(String str) {
        if (!str.contains("#"))
            return str;

        Matcher matcher = HEX_REGEX.matcher(str);
        while (matcher.find()) {
            String match = str.substring(matcher.start(), matcher.end());
            if (Settings.CORE_SHADER_COLORS.get().contains(match)) {
                String freeHex = ShaderTokenizer.findFreeHex(match.substring(1));
                str = str.replace(match, "#" + freeHex);
            }
        }

        return str;
    }

    /**
     * Sends a private message from one player to another.
     * @param sender The {@link RosePlayer} who sent the message.
     * @param targetName The name of the player receiving the message.
     * @param message The message to send.
     */
    public static void sendPrivateMessage(RosePlayer sender, String targetName, String message) {
        RoseChatAPI api = RoseChatAPI.getInstance();
        String consoleName = api.getLocaleManager().getMessage("console-sender-name");
        boolean isConsoleName = targetName.equalsIgnoreCase("Console") ||
                targetName.equalsIgnoreCase(ChatColor.stripColor(HexUtils.colorify(consoleName)));

        Player target = MessageUtils.getPlayerExact(targetName);
        RosePlayer messageTarget = (target == null ?
                (isConsoleName ? new RosePlayer(consoleName, "default") : new RosePlayer(targetName, "default"))
                : new RosePlayer(target));

        // Quickly return if the player isn't online on any connected servers.
        if (!isConsoleName) {
            if (!api.isBungee() && target == null) {
                sender.sendLocaleMessage("invalid-argument",
                        StringPlaceholders.of("message",
                                RoseChatAPI.getInstance().getLocaleManager()
                                        .getLocaleMessage("argument-handler-player")));
                return;
            }

            if (api.isBungee()) {
                if (api.getBungeeManager().getAllPlayers().isEmpty()
                        || !api.getBungeeManager().getAllPlayers().contains(messageTarget.getRealName())) {
                    sender.sendLocaleMessage("invalid-argument",
                            StringPlaceholders.of("message",
                                    RoseChatAPI.getInstance().getLocaleManager()
                                            .getLocaleMessage("argument-handler-player")));
                    return;
                }
            }
        }

        RoseMessage roseMessage = RoseMessage.forLocation(sender, PermissionArea.MESSAGE);

        MessageRules rules = new MessageRules().applyAllFilters();
        RuleOutputs outputs = rules.apply(roseMessage, message);
        roseMessage.setPlayerInput(outputs.getFilteredMessage());

        // If the message is blocked, send a warning to the player.
        if (outputs.isBlocked()) {
            if (outputs.getWarningMessage() != null) {
                sender.send(outputs.getWarningMessage());
            } else if (outputs.getWarning() != null) {
                outputs.getWarning().send(sender);
            }

            if (Settings.SEND_BLOCKED_MESSAGES_TO_STAFF.get() && outputs.shouldNotifyStaff()) {
                for (Player staffPlayer : Bukkit.getOnlinePlayers()) {
                    if (staffPlayer.hasPermission("rosechat.seeblocked")) {
                        RosePlayer rosePlayer = new RosePlayer(staffPlayer);
                        rosePlayer.sendLocaleMessage("blocked-message",
                                StringPlaceholders.of("player", roseMessage.getSender().getName(),
                                        "message", message));
                    }
                }
            }

            return;
        }

        // If the message was sent by a player, check if the receiver is ignoring them.
        if (sender.isPlayer()) {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
            PlayerData targetData = RoseChatAPI.getInstance().getPlayerData(offlineTarget.getUniqueId());

            if (targetData != null && targetData.getIgnoringPlayers().contains(sender.getUUID())) {
                sender.sendLocaleMessage("command-togglemessage-cannot-message");
                return;
            }
        }

        // Parse the message for the console
        MessageContents parsedMessage = roseMessage.parse(messageTarget, Settings.CONSOLE_MESSAGE_FORMAT.get());

        // If the console is not the target of the message, send the console message format. Otherwise, send the received message format later.
        if (!isConsoleName && !sender.isConsole())
            new RosePlayer(Bukkit.getConsoleSender()).send(parsedMessage);

        // Parse for the spies.
        for (UUID uuid : RoseChatAPI.getInstance().getPlayerDataManager().getMessageSpies()) {
            // Don't send the spy message if the spy is the sender or receiver.
            if ((sender.isPlayer() && uuid.equals(sender.getUUID()))
                    || messageTarget.isPlayer() && uuid.equals(messageTarget.getUUID()))
                continue;

            // If the spy isn't valid, continue.
            Player spy = Bukkit.getPlayer(uuid);
            if (spy == null)
                continue;

            RosePlayer roseSpy = new RosePlayer(spy);
            roseSpy.validatePermissions();

            RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
                MessageContents parsedSpyMessage = roseMessage.parse(messageTarget, Settings.MESSAGE_SPY_FORMAT.get());
                parsedSpyMessage.sendMessage(spy);
            });
        }

        PlayerSendMessageEvent sendEvent = new PlayerSendMessageEvent(sender, messageTarget, roseMessage);
        Bukkit.getPluginManager().callEvent(sendEvent);
        if (sendEvent.isCancelled())
            return;

        // Parse the message for the sender and the receiver.
        RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
            MessageContents parsedSentMessage = roseMessage.parse(messageTarget, Settings.MESSAGE_SENT_FORMAT.get());

            MessageContents receivedMessageOutput = roseMessage.parse(messageTarget,
                    Settings.MESSAGE_RECEIVED_FORMAT.get());

            if (target == null) {
                // If the target is not valid and the name is "Console", then send the message to the console.
                if (isConsoleName) {
                    sender.send(parsedSentMessage);
                    receivedMessageOutput.sendMessage(Bukkit.getConsoleSender());
                } else {
                    boolean keepFormat = Settings.KEEP_MESSAGE_FORMAT.get();
                    String bungeeMessage = keepFormat ? receivedMessageOutput.build(ChatComposer.json()) : null;

                    if (!Settings.ALLOW_MESSAGING_VANISHED_PLAYERS.get() && !sender.hasPermission("rosechat.messagebypass")) {
                        RoseChatAPI.getInstance().getBungeeManager()
                                .sendMessageWithVanishCheck(sender, targetName, bungeeMessage, message, (success) -> {
                                    if (success) {
                                        sender.send(parsedSentMessage);
                                    } else {
                                        sender.sendLocaleMessage("invalid-argument",
                                                StringPlaceholders.of("message",
                                                        RoseChatAPI.getInstance().getLocaleManager()
                                                                .getLocaleMessage("argument-handler-player")));
                                    }
                                });
                    } else {
                        RoseChatAPI.getInstance().getBungeeManager()
                                .sendDirectMessage(sender, targetName, bungeeMessage, message, (success) -> {
                                    if (success) {
                                        // If the message was received successfully, send the sent message to the sender.
                                        sender.send(parsedSentMessage);
                                    } else {
                                        // If the message was not received successfully, then the player is assumed to not be online.
                                        sender.sendLocaleMessage("invalid-argument",
                                                StringPlaceholders.of("message",
                                                        RoseChatAPI.getInstance().getLocaleManager()
                                                                .getLocaleMessage("argument-handler-player")));
                                    }
                                });
                    }
                }
            } else {
                // The sender should receive the message first.
                sender.send(parsedSentMessage);

                PlayerReceiveMessageEvent receiveEvent = new PlayerReceiveMessageEvent(sender, messageTarget,
                        roseMessage, receivedMessageOutput);
                Bukkit.getPluginManager().callEvent(receiveEvent);
                if (receiveEvent.isCancelled())
                    return;

                // If the target is online, send the message.
                messageTarget.send(receiveEvent.getContents());

                if (messageTarget.isPlayer()) {
                    Player targetPlayer = messageTarget.asPlayer();
                    PlayerData targetData = messageTarget.getPlayerData();
                    if (targetData != null && targetData.hasMessageSounds() && Settings.MESSAGE_SOUND.get() != null) {
                        targetPlayer.playSound(targetPlayer.getLocation(), Settings.MESSAGE_SOUND.get(), 1.0f, 1.0f);
                    }
                }
            }
        });

        // Update the player's display name if the setting is enabled.
        if (sender.getPlayerData() == null || sender.getPlayerData().getNickname() == null)
            return;

        String nickname = sender.getPlayerData().getNickname();
        if (Settings.UPDATE_DISPLAY_NAMES.get() && nickname != null && !sender.getDisplayName().equals(sender.getPlayerData().getNickname())) {
            RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
                MessageContents components = RoseMessage.forLocation(sender, PermissionArea.NICKNAME)
                        .parse(sender, sender.getPlayerData().getNickname());
                sender.setDisplayName(components);

                if (RoseChat.getInstance().getNicknameProvider() != null) {
                    Player player = sender.asPlayer();
                    RoseChat.getInstance().getNicknameProvider().setNickname(player, player.getDisplayName());
                }
            });
        }
    }

    /**
     * Sends a private JSON message from one player to another.
     * This message originated from another server connected to a network.
     * @param sender The {@link RosePlayer} who sent the message.
     * @param targetName The name of the player receiving the message.
     * @param json The JSON message to send.
     * @param input The input of the message to send.
     */
    public static void sendPrivateJsonMessage(RosePlayer sender, String targetName, String json, String input) {
        RoseChatAPI api = RoseChatAPI.getInstance();

        Player target = MessageUtils.getPlayerExact(targetName);
        if (target == null)
            return;

        RosePlayer messageTarget = new RosePlayer(target);

        RosePlayer console = new RosePlayer(Bukkit.getConsoleSender());
        RoseMessage consoleMessage = RoseMessage.forLocation(sender, PermissionArea.MESSAGE);
        consoleMessage.setPlayerInput(input);
        console.send(consoleMessage.parse(messageTarget, Settings.CONSOLE_MESSAGE_FORMAT.get()));

        RoseMessage roseMessage = RoseMessage.forLocation(sender, PermissionArea.MESSAGE);
        roseMessage.setPlayerInput(json);

        for (UUID uuid : api.getPlayerDataManager().getMessageSpies()) {
            if ((sender.isPlayer() && uuid.equals(sender.getUUID())) || messageTarget.isPlayer() && uuid.equals(messageTarget.getUUID()))
                continue;

            Player spy = Bukkit.getPlayer(uuid);
            if (spy == null)
                continue;

            RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
                MessageContents parsedSpyMessage = consoleMessage.parse(messageTarget, Settings.MESSAGE_SPY_FORMAT.get());
                parsedSpyMessage.sendMessage(spy);
            });
        }

        String jsonMessage = applyJSONPlaceholders(roseMessage.getPlayerInput());
        MessageContents parsedMessage = parseJSONMessage(messageTarget, jsonMessage);

        PlayerReceiveMessageEvent receiveEvent = new PlayerReceiveMessageEvent(sender, messageTarget, roseMessage, parsedMessage);
        Bukkit.getPluginManager().callEvent(receiveEvent);
        if (receiveEvent.isCancelled())
            return;

        messageTarget.send(receiveEvent.getContents());
        PlayerData data = messageTarget.getPlayerData();
        if (data != null && data.hasMessageSounds() && Settings.MESSAGE_SOUND.get() != null)
            target.playSound(target.getLocation(), Settings.MESSAGE_SOUND.get(), 1.0f, 1.0f);
    }

    public static String applyJSONPlaceholders(String message) {
        String output = message;

        if (PlaceholderAPIHook.enabled()) {
            Matcher matcher = PlaceholderAPI.getPlaceholderPattern().matcher(message);
            while (matcher.find())
                output = output.replace(matcher.group(), matcher.group().replace("%other_", ""));
        }

        return output;
    }

    public static MessageContents parseJSONMessage(RosePlayer receiver, String json) {
        String parsedJson = receiver.isPlayer() ? PlaceholderAPIHook.applyPlaceholders(receiver.asPlayer(), json) : json;
        return MessageContents.fromJson(parsedJson);
    }

    /**
     * Gets the player whose name, display name or nickname contains the given name.
     * @param name The name, display name, or nickname of the player.
     * @return A {@link Player} retrieved from the given name.
     */
    public static Player getPlayer(String name) {
        if (name == null || name.isEmpty())
            return null;

        Player player = Bukkit.getPlayer(name);
        if (player != null)
            return player;

        for (PlayerData playerData : RoseChatAPI.getInstance().getPlayerDataManager().getPlayerData().values()) {
            if (playerData.getNickname() == null)
                continue;

            player = Bukkit.getPlayer(playerData.getUUID());
            if (player != null) {
                if (ChatColor.stripColor(player.getDisplayName()).startsWith(name.toLowerCase()))
                    return player;

                if (ChatColor.stripColor(HexUtils.colorify(playerData.getNickname().toLowerCase())).startsWith(name.toLowerCase()))
                    return player;
            }
        }

        return null;
    }

    /**
     * Gets the player whose name, display name or nickname is exactly given name.
     * @param name The name, display name, or nickname of the player.
     * @return A {@link Player} retrieved from the given name.
     */
    public static Player getPlayerExact(String name) {
        if (name == null || name.isEmpty())
            return null;

        Player player = Bukkit.getPlayerExact(name);
        if (player != null)
            return player;

        for (PlayerData playerData : RoseChatAPI.getInstance().getPlayerDataManager().getPlayerData().values()) {
            if (playerData.getNickname() == null)
                continue;

            player = Bukkit.getPlayer(playerData.getUUID());
            if (player == null)
                continue;

            if (ChatColor.stripColor(player.getDisplayName()).equalsIgnoreCase(name.toLowerCase()))
                return player;

            if (ChatColor.stripColor(HexUtils.colorify(playerData.getNickname().toLowerCase())).equalsIgnoreCase(name.toLowerCase()))
                return player;
        }

        return null;
    }

    public static boolean isPlayerVanished(Player player) {
        if (player == null)
            return false;

        for (MetadataValue value : player.getMetadata("vanished"))
            if (value.asBoolean())
                return true;

        return false;
    }

    /**
     * Checks if a message can be coloured by the given sender.
     * @param sender The {@link RosePlayer} who is sending the string.
     * @param str The string to check.
     * @param area The location, from a {@link PermissionArea} as a string.
     * @return True if the message can be colored.
     */
    public static boolean canColor(RosePlayer sender, String str, PermissionArea area) {
        if (str.isBlank())
            return true;

        RoseMessage message = RoseMessage.forLocation(sender, area);
        message.setPlayerInput(str);
        MessageContents components = MessageTokenizer.tokenize(message, sender, RoseChatPlaceholderTokenizer.MESSAGE_PLACEHOLDER, MessageDirection.PLAYER_TO_SERVER,
                Tokenizers.ROSECHAT_PLACEHOLDER.asBundle(),
                Tokenizers.FILTER.asBundle(),
                Tokenizers.COLORS_BUNDLE);
        Set<String> missingPermissions = components.outputs().getMissingPermissions();
        return missingPermissions.isEmpty();
    }

    public static String bungeeToJson(BaseComponent[] components) {
        if (HAS_VERSIONED_SERIALIZER) {
            return VersionedComponentSerializer.forVersion(ChatVersion.V1_21_5).toString(components);
        } else {
            return ComponentSerializer.toString(components);
        }
    }

    public static BaseComponent[] jsonToBungee(String json) {
        if (HAS_VERSIONED_SERIALIZER) {
            return VersionedComponentSerializer.forVersion(ChatVersion.V1_21_5).parse(json);
        } else {
            return ComponentSerializer.parse(json);
        }
    }

}
