package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.player.PlayerReceiveMessageEvent;
import dev.rosewood.rosechat.api.event.player.PlayerSendMessageEvent;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.message.parser.RoseChatParser;
import dev.rosewood.rosechat.message.tokenizer.MessageOutputs;
import dev.rosewood.rosechat.message.tokenizer.TokenizerParams;
import dev.rosewood.rosechat.message.tokenizer.placeholder.RoseChatPlaceholderTokenizer;
import dev.rosewood.rosechat.message.tokenizer.shader.ShaderTokenizer;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.MessageTokenizerResults;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import java.text.Normalizer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {

    public static final String PUNCTUATION_REGEX = "[\\p{P}\\p{S}]";
    public static final Pattern URL_PATTERN = Pattern.compile("(http(s)?://)?[-a-zA-Z0-9@:%._+~#=]{2,32}\\.[a-zA-Z0-9()]{2,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)");
    public static final Pattern VALID_LEGACY_REGEX = Pattern.compile("&[0-9a-fA-F]");
    public static final Pattern VALID_LEGACY_REGEX_PARSED = Pattern.compile("§[0-9a-fA-F]");
    public static final Pattern VALID_LEGACY_REGEX_FORMATTING = Pattern.compile("&[k-oK-OrR]");
    public static final Pattern VALID_LEGACY_REGEX_FORMATTING_PARSED = Pattern.compile("§[k-oK-OrR]");
    public static final Pattern VALID_LEGACY_REGEX_COMBINED = Pattern.compile("(&|§)[0-9a-fA-F]|(&|§)[k-oK-OrR]");
    public static final Pattern HEX_REGEX = Pattern.compile("<#([A-Fa-f0-9]){6}>|\\{#([A-Fa-f0-9]){6}}|&#([A-Fa-f0-9]){6}|#([A-Fa-f0-9]){6}");
    public static final Pattern SPIGOT_HEX_REGEX = Pattern.compile("&x(&[A-Fa-f0-9]){6}");
    public static final Pattern SPIGOT_HEX_REGEX_PARSED = Pattern.compile("#(§[A-Fa-f0-9]){6}|§x(§[A-Fa-f0-9]){6}");
    public static final Pattern SPIGOT_HEX_REGEX_COMBINED = Pattern.compile("<#([A-Fa-f0-9]){6}>|\\{#([A-Fa-f0-9]){6}}|&#([A-Fa-f0-9]){6}|#([A-Fa-f0-9]){6}|&x(&[A-Fa-f0-9]){6}|#(§[A-Fa-f0-9]){6}|§x(§[A-Fa-f0-9]){6}");
    public static final Pattern RAINBOW_PATTERN = Pattern.compile("<(?<type>rainbow|r)(#(?<speed>\\d+))?(:(?<saturation>\\d*\\.?\\d+))?(:(?<brightness>\\d*\\.?\\d+))?(:(?<loop>l|L|loop))?>");
    public static final Pattern GRADIENT_PATTERN = Pattern.compile("<(?<type>gradient|g)(#(?<speed>\\d+))?(?<hex>(:#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})){2,})(:(?<loop>l|L|loop))?>");

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
    public static boolean isAlphanumericSpace(final CharSequence cs) {
        if (cs == null)
            return false;

        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isLetterOrDigit(cs.charAt(i)) && cs.charAt(i) != ' ')
                return false;
        }

        return true;
    }

    /**
     * @param sender The {@link RosePlayer} who will send these placeholders.
     * @param viewer The {@link RosePlayer} who will view these placeholders.
     * @return A {@link StringPlaceholders.Builder} containing default placeholders for a sender and viewer.
     */
    public static StringPlaceholders.Builder getSenderViewerPlaceholders(RosePlayer sender, RosePlayer viewer) {
        StringPlaceholders.Builder builder = StringPlaceholders.builder()
                .add("player_name", sender.getRealName())
                .add("player_displayname", sender.getDisplayName())
                .add("player_nickname", sender.getName());

        if (viewer != null) {
            builder.add("other_player_name", viewer.getRealName())
                    .add("other_player_displayname", viewer.getDisplayName())
                    .add("other_player_nickname", viewer.getName());
        }

        Permission vault = RoseChatAPI.getInstance().getVault();
        if (vault != null)
            builder.add("vault_rank", sender.getPermissionGroup());

        return builder;
    }

    /**
     * @param sender The {@link RosePlayer} who will send these placeholders.
     * @param viewer The {@link RosePlayer} who will view these placeholders.
     * @param channel The {@link Channel} that these placeholders will be sent in.
     * @return A {@link StringPlaceholders.Builder} containing default placeholders for a sender and viewer.
     */
    public static StringPlaceholders.Builder getSenderViewerPlaceholders(RosePlayer sender, RosePlayer viewer, Channel channel) {
        if (channel == null)
            return getSenderViewerPlaceholders(sender, viewer);

        else if (channel.getId().equalsIgnoreCase("group"))
            return getSenderViewerPlaceholders(sender, viewer, (GroupChannel) channel);

        StringPlaceholders.Builder builder = getSenderViewerPlaceholders(sender, viewer);
        builder.add("channel", channel.getId());

        return builder;
    }

    /**
     * @param sender The {@link RosePlayer} who will send these placeholders.
     * @param viewer The {@link RosePlayer} who will view these placeholders.
     * @param channel The {@link Channel} that these placeholders will be sent in.
     * @param extra More {@link StringPlaceholders} to use.
     * @return A {@link StringPlaceholders.Builder} containing default placeholders for a sender and viewer.
     */
    public static StringPlaceholders.Builder getSenderViewerPlaceholders(RosePlayer sender, RosePlayer viewer, Channel channel, StringPlaceholders extra) {
        StringPlaceholders.Builder builder;
        if (channel == null)
            builder =  getSenderViewerPlaceholders(sender, viewer);
        else if (channel.getId().equalsIgnoreCase("group"))
            return getSenderViewerPlaceholders(sender, viewer, (GroupChannel) channel);
        else
            builder = getSenderViewerPlaceholders(sender, viewer, channel);

        return extra == null ? builder : builder.addAll(extra);
    }

    /**
     * @param sender The {@link RosePlayer} who will send these placeholders.
     * @param viewer The {@link RosePlayer} who will view these placeholders.
     * @param group The {@link GroupChannel} that these placeholders will be sent in.
     * @return A {@link StringPlaceholders.Builder} containing default placeholders for a sender and viewer.
     */
    public static StringPlaceholders.Builder getSenderViewerPlaceholders(RosePlayer sender, RosePlayer viewer, GroupChannel group) {
        StringPlaceholders.Builder builder = getSenderViewerPlaceholders(sender, viewer);
        builder.add("group", group.getId())
                .add("group_name", group.getName())
                .add("group_owner", Bukkit.getOfflinePlayer(group.getOwner()).getName());

        return builder;
    }

    /**
     * Sends a message to a Discord channel.
     * @param message The message to send.
     * @param channel The {@link Channel} that the message was sent from.
     * @param discordChannel The channel to send the message to.
     */
    public static void sendDiscordMessage(RoseMessage message, Channel channel, String discordChannel) {
        RoseChatAPI.getInstance().getDiscord().sendMessage(message, channel, discordChannel);
    }

    /**
     * Sends a private message from one player to another.
     * @param sender The {@link RosePlayer} who sent the message.
     * @param targetName The name of the player receiving the message.
     * @param message The message to send.
     */
    public static void sendPrivateMessage(RosePlayer sender, String targetName, String message) {
        RoseChatAPI api = RoseChatAPI.getInstance();

        Player target = MessageUtils.getPlayerExact(targetName);
        RosePlayer messageTarget = target == null ? new RosePlayer(targetName, "default") : new RosePlayer(target);

        // todo bungee test
        // Quickly return if the player isn't online on any connected servers.
        if (!targetName.equalsIgnoreCase("Console") && (!api.isBungee() && target == null) ||
                (!api.getBungeeManager().getAllPlayers().isEmpty() && api.getBungeeManager().getAllPlayers().contains(messageTarget.getRealName()))) {
            sender.sendLocaleMessage("invalid-argument",
                    StringPlaceholders.of("message",
                            RoseChatAPI.getInstance().getLocaleManager().getLocaleMessage("argument-handler-player")));
            return;
        }

        RoseMessage roseMessage = RoseMessage.forLocation(sender, PermissionArea.MESSAGE);

        MessageRules rules = new MessageRules().applyAllFilters();
        MessageRules.RuleOutputs outputs = rules.apply(roseMessage, message);
        roseMessage.setPlayerInput(outputs.getFilteredMessage());

        // If the message is blocked, send a warning to the player.
        if (outputs.isBlocked()) {
            if (outputs.getWarning() != null)
                outputs.getWarning().send(sender);
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

        // Parse the message for the console to generate the tokens
        BaseComponent[] parsedMessage = roseMessage.parse(messageTarget, Setting.CONSOLE_MESSAGE_FORMAT.getString()).content();

        // If the console is not the target of the message, send the console message format. Otherwise, send the received message format later.
        if (!targetName.equalsIgnoreCase("Console") && !sender.isConsole())
            Bukkit.getConsoleSender().spigot().sendMessage(parsedMessage);

        // Parse for the channel spies.
        for (UUID uuid : RoseChatAPI.getInstance().getPlayerDataManager().getMessageSpies()) {
            // Don't send the spy message if the spy is the sender or receiver.
            if ((sender.isPlayer() && uuid.equals(sender.getUUID())) || messageTarget.isPlayer() && uuid.equals(messageTarget.getUUID()))
                continue;

            // If the spy isn't valid, continue.
            Player spy = Bukkit.getPlayer(uuid);
            if (spy == null)
                continue;

            RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
                BaseComponent[] parsedSpyMessage = roseMessage.parse(messageTarget, Setting.MESSAGE_SPY_FORMAT.getString()).content();
                spy.spigot().sendMessage(parsedSpyMessage);
            });
        }

        PlayerSendMessageEvent sendEvent = new PlayerSendMessageEvent(sender, messageTarget, roseMessage);
        Bukkit.getPluginManager().callEvent(sendEvent);
        if (sendEvent.isCancelled())
            return;

        // Parse the message for the sender and the receiver.
        RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
            BaseComponent[] parsedSentMessage = roseMessage.parse(messageTarget, Setting.MESSAGE_SENT_FORMAT.getString()).content();

            MessageTokenizerResults<BaseComponent[]> receivedMessageOutput = roseMessage.parse(messageTarget, Setting.MESSAGE_RECEIVED_FORMAT.getString());
            BaseComponent[] parsedReceivedMessage = receivedMessageOutput.content();

            if (target == null) {
                // If the target is not valid and the name is "Console", then send the message to the console.
                if (targetName.equalsIgnoreCase("Console")) {
                    sender.send(parsedSentMessage);
                    Bukkit.getConsoleSender().spigot().sendMessage(parsedReceivedMessage);
                } else {
                    // If the target is not valid, but the name isn't console, we should see if it is a bungee player.
                    RoseChatAPI.getInstance().getBungeeManager().sendDirectMessage(sender, targetName, message, (success) -> {
                        if (success) {
                            // If the message was received successfully, send the sent message to the sender.
                            sender.send(parsedSentMessage);
                        } else {
                            // If the message was not received successfully, then the player is assumed to not be online.
                            sender.sendLocaleMessage("invalid-argument",
                                    StringPlaceholders.of("message",
                                            RoseChatAPI.getInstance().getLocaleManager().getLocaleMessage("argument-handler-player")));
                        }
                    });
                }
            } else {
                // The sender should receive the message first.
                sender.send(parsedSentMessage);

                PlayerReceiveMessageEvent receiveEvent = new PlayerReceiveMessageEvent(sender, messageTarget, roseMessage, receivedMessageOutput);
                Bukkit.getPluginManager().callEvent(receiveEvent);
                if (receiveEvent.isCancelled())
                    return;

                parsedReceivedMessage = receiveEvent.getComponents().content();

                // If the target is online, send the message.
                messageTarget.send(parsedReceivedMessage);

                if (messageTarget.isPlayer()) {
                    Player targetPlayer = messageTarget.asPlayer();
                    PlayerData targetData = messageTarget.getPlayerData();
                    if (targetData != null && targetData.hasMessageSounds() && !Setting.MESSAGE_SOUND.getString().equalsIgnoreCase("none")) {
                        targetPlayer.playSound(targetPlayer.getLocation(), Sound.valueOf(Setting.MESSAGE_SOUND.getString()), 1.0f, 1.0f);
                    }
                }
            }
        });

        // Update the player's display name if the setting is enabled.
        if (sender.getPlayerData() == null || sender.getPlayerData().getNickname() == null)
            return;

        String nickname = sender.getPlayerData().getNickname();
        if (Setting.UPDATE_DISPLAY_NAMES.getBoolean() && nickname != null && !sender.getDisplayName().equals(sender.getPlayerData().getNickname())) {
            RoseChat.MESSAGE_THREAD_POOL.execute(() -> {
                MessageTokenizerResults<BaseComponent[]> components = RoseMessage.forLocation(sender, PermissionArea.NICKNAME).parse(sender, sender.getPlayerData().getNickname());
                sender.setDisplayName(TextComponent.toLegacyText(components.content()));

                if (RoseChat.getInstance().getNicknameProvider() != null) {
                    Player player = sender.asPlayer();
                    RoseChat.getInstance().getNicknameProvider().setNickname(player, player.getDisplayName());
                }
            });
        }
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
            if (ChatColor.stripColor(player.getDisplayName()).startsWith(name.toLowerCase()))
                return player;

            if (ChatColor.stripColor(HexUtils.colorify(playerData.getNickname().toLowerCase())).startsWith(name.toLowerCase()))
                return player;
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
            if (ChatColor.stripColor(player.getDisplayName()).equalsIgnoreCase(name.toLowerCase()))
                return player;

            if (ChatColor.stripColor(HexUtils.colorify(playerData.getNickname().toLowerCase())).equalsIgnoreCase(name.toLowerCase()))
                return player;
        }

        return null;
    }

    /**
     * Checks if a message can be coloured by the given sender.
     * @param sender The {@link RosePlayer} who is sending the string.
     * @param str The string to check.
     * @param area The location, from a {@link PermissionArea} as a string.
     * @return True if the message can be colored.
     */
    public static boolean canColor(RosePlayer sender, String str, PermissionArea area) {
        Matcher colorMatcher = VALID_LEGACY_REGEX.matcher(str);
        Matcher formatMatcher = VALID_LEGACY_REGEX_FORMATTING.matcher(str);
        Matcher hexMatcher = HEX_REGEX.matcher(str);
        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(str);
        Matcher rainbowMatcher = RAINBOW_PATTERN.matcher(str);

        String location = area.toString().toLowerCase();
        boolean hasColor = colorMatcher.find();
        boolean usePerColorPerms = Setting.USE_PER_COLOR_PERMISSIONS.getBoolean();
        boolean hasLocationPermission = sender.hasPermission("rosechat.color." + location);
        boolean hasColorPermission = hasColor && sender.hasPermission("rosechat." + ChatColor.getByChar(Character.toLowerCase(colorMatcher.group().charAt(1))).getName().toLowerCase() + "." + location);
        boolean canColor = !hasColor || (usePerColorPerms ? hasColorPermission && hasLocationPermission : hasLocationPermission);
        boolean canMagic = !str.contains("&k") || sender.hasPermission("rosechat.magic." + location);
        boolean canFormat = !formatMatcher.find() || sender.hasPermission("rosechat.format." + location);
        boolean canHex = !hexMatcher.find() || sender.hasPermission("rosechat.hex." + location);
        boolean canGradient = !gradientMatcher.find() || sender.hasPermission("rosechat.gradient." + location);
        boolean canRainbow = !rainbowMatcher.find() || sender.hasPermission("rosechat.rainbow." + location);

        return canColor && canMagic && canFormat && canHex && canGradient && canRainbow;
    }

    public static String getCaptureGroup(Matcher matcher, String group) {
        try {
            return matcher.group(group);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Checks if the sender of a message has the specified permission.
     * @param params The {@link TokenizerParams} to get information from, such as the sender and message location.
     * @param permission The permission to check, should not contain the location information. For example, 'rosechat.emojis'.
     * @return True if the sender has the permission
     */
    public static boolean hasTokenPermission(TokenizerParams params, String permission) {
        // If the message doesn't exist, sent from the console, or has a location of 'NONE', then the sender should have permission.
        if (params == null || params.getSender() == null
                || params.getLocation() == PermissionArea.NONE || (params.getSender().isConsole())
                || !params.containsPlayerInput())
            return true;

        // Gets the full permission, e.g. rosechat.emoji.channel.global
        String fullPermission = permission + "." + params.getLocationPermission();

        return params.getSender().getIgnoredPermissions().contains(fullPermission.replace("rosechat.", ""))
                || params.getSender().getIgnoredPermissions().contains("*")
                || checkAndLogPermission(params, fullPermission);
    }

    /**
     * Checks if the sender of a message has the specified permission.
     * Checks against the first permission, for example, 'rosechat.emojis', and extended permissions such as 'rosechat.emoji.smile'.
     * @param params The {@link TokenizerParams} to get information from, such as the sender and message location.
     * @param permission The permission to check, should not contain the location information. For example, 'rosechat.emojis'
     * @param extendedPermission The extended permission, should not contain the location information. For example, 'rosechat.emoji.smile'.
     * @return True if the sender has permission.
     */
    public static boolean hasExtendedTokenPermission(TokenizerParams params, String permission, String extendedPermission) {
        // If the message doesn't exist, sent from the console, or has a location of 'NONE', then the sender should have permission.
        if (params == null || params.getSender() == null
                || params.getLocation() == PermissionArea.NONE || (params.getSender().isConsole())
                || !params.containsPlayerInput())
            return true;

        // The sender will not have an extended permission if they do not have the base permission.
        if (!hasTokenPermission(params, permission))
            return false;

        return params.getSender().getIgnoredPermissions().contains(extendedPermission.replace("rosechat.", ""))
                || params.getSender().getIgnoredPermissions().contains("*")
                || checkAndLogPermission(params, extendedPermission);
    }

    private static boolean checkAndLogPermission(TokenizerParams params, String permission) {
        boolean hasPermission = params.getSender().hasPermission(permission);
        if (!hasPermission)
            params.getOutputs().getMissingPermissions().add(permission);

        return hasPermission;
    }

    public static BaseComponent[] appendDeleteButton(RosePlayer sender, PlayerData playerData, String messageId, String messageJson) {
        ComponentBuilder builder = new ComponentBuilder();
        String placeholder = Setting.DELETE_CLIENT_MESSAGE_FORMAT.getString();

        BaseComponent[] deleteClientButton = RoseChatAPI.getInstance().parse(new RosePlayer(Bukkit.getConsoleSender()), sender, placeholder,
                MessageUtils.getSenderViewerPlaceholders(sender, sender)
                        .add("id", messageId)
                        .add("type", "client").build());

        if (deleteClientButton == null || deleteClientButton.length == 0) {
            playerData.getMessageLog().addDeletableMessage(new DeletableMessage(UUID.randomUUID(), messageJson, true));
            return null;
        }

        if (Setting.DELETE_MESSAGE_FORMAT_APPEND_SUFFIX.getBoolean()) {
            builder.append(ComponentSerializer.parse(messageJson), ComponentBuilder.FormatRetention.NONE);
            builder.append(deleteClientButton, ComponentBuilder.FormatRetention.NONE);
        } else {
            builder.append(deleteClientButton, ComponentBuilder.FormatRetention.NONE);
            builder.append(ComponentSerializer.parse(messageJson), ComponentBuilder.FormatRetention.NONE);
        }

        return builder.create();
    }

    public static boolean isPlayerVanished(Player player) {
        for (MetadataValue value : player.getMetadata("vanished"))
            if (value.asBoolean())
                return true;

        return false;
    }

    public static String stripShaderColors(String str) {
        if (!str.contains("#"))
            return str;

        Matcher matcher = HEX_REGEX.matcher(str);
        while (matcher.find()) {
            String match = str.substring(matcher.start(), matcher.end());
            if (Setting.CORE_SHADER_COLORS.getStringList().contains(match)) {
                String freeHex = ShaderTokenizer.findFreeHex(match.substring(1));
                str = str.replace(match, "#" + freeHex);
            }
        }

        return str;
    }

    /**
     * Checks if a nickname is allowed based on the configurable rules, length, spaces, and non alpha chars.
     * @param player The player who is setting this nickname.
     * @param target The player who is receiving this nickname.
     * @param message A {@link RoseMessage} containing the nickname.
     * @return True, if the nickname is allowed.
     */
    public static boolean isNicknameAllowed(RosePlayer player, RosePlayer target, RoseMessage message) {
        String nickname = message.getPlayerInput();
        String strippedNickname = ChatColor.stripColor(HexUtils.colorify(nickname));

        if (strippedNickname.length() < Math.max(1, Setting.MINIMUM_NICKNAME_LENGTH.getInt())) {
            player.sendLocaleMessage("command-nickname-too-short");
            return false;
        }

        if (strippedNickname.length() > Setting.MAXIMUM_NICKNAME_LENGTH.getInt()) {
            player.sendLocaleMessage("command-nickname-too-long");
            return false;
        }

        if (!Setting.ALLOW_SPACES_IN_NICKNAMES.getBoolean() && strippedNickname.contains(" ")) {
            player.sendLocaleMessage("command-nickname-not-allowed");
            return false;
        }

        if (!Setting.ALLOW_NONALPHANUMERIC_CHARACTERS.getBoolean() && !isAlphanumericSpace(strippedNickname)) {
            player.sendLocaleMessage("command-nickname-not-allowed");
            return false;
        }

        // Parse the nickname to make sure the player isn't missing any permissions.
        MessageTokenizerResults<BaseComponent[]> results = new RoseChatParser().parse(message, target, RoseChatPlaceholderTokenizer.MESSAGE_PLACEHOLDER);
        MessageOutputs outputs = results.outputs();
        if (!outputs.getMissingPermissions().isEmpty()) {
            player.sendLocaleMessage("no-permission");
            return false;
        }

        return true;
    }

}
