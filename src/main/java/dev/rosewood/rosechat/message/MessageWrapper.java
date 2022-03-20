package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.PostParseMessageEvent;
import dev.rosewood.rosechat.api.event.PreParseMessageEvent;
import dev.rosewood.rosechat.chat.FilterType;
import dev.rosewood.rosechat.chat.Group;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.wrapper.ComponentColorizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.MessageTokenizer;
import dev.rosewood.rosechat.message.wrapper.tokenizer.Tokenizers;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;

/**
 * Wrapper to turn regular messages into BaseComponent[]'s with colour, emoji, tags, etc.
 */
public class MessageWrapper {

    private final RoseSender sender;
    private final Group group;
    private final MessageLocation location;
    private final String locationPermission;
    private String message;
    private PlayerData senderData;
    private StringPlaceholders placeholders;
    private boolean logMessages;

    private final List<UUID> taggedPlayers;
    private Sound tagSound;
    private boolean canBeSent;
    private FilterType filterType;
    private BaseComponent[] tokenized;
    private UUID id;
    private DeletableMessage deletableMessage;

    public MessageWrapper(RoseSender sender, MessageLocation messageLocation, Group group, String message) {
        this.sender = sender;
        this.group = group;
        this.location = messageLocation;
        this.locationPermission = this.location.toString().toLowerCase() + (group == null ? "" : "." + group.getLocationPermission());
        this.message = message;
        this.canBeSent = true;
        if (sender.isPlayer()) this.senderData = RoseChatAPI.getInstance().getPlayerData(sender.getUUID());
        this.taggedPlayers = new ArrayList<>();
        this.id = UUID.randomUUID();
        this.logMessages = true;
    }

    public MessageWrapper(RoseSender sender, MessageLocation location, Group group, String message, StringPlaceholders placeholders) {
        this(sender, location, group, message);
        this.placeholders = placeholders;
    }

    public MessageWrapper(Player sender, MessageLocation messageLocation, Group group, String message) {
        this(new RoseSender(sender), messageLocation, group, message);
    }

    /**
     * Removes color codes instead of showing '&c' if the player doesn't have permission.
     * @return The MessageWrapper.
     */
    public MessageWrapper validateColors() {
        if (this.sender.hasPermission("rosechat.color." + this.locationPermission)) return this;
        Matcher matcher = ComponentColorizer.VALID_LEGACY_REGEX.matcher(this.message);
        List<String> toRemove = new ArrayList<>();
        while (matcher.find()) toRemove.add(this.message.substring(matcher.start(), matcher.end()));
        for (String s : toRemove) this.message = this.message.replace(s, "");

        return this;
    }

    /**
     * Removes formatting codes instead of showing '&l' if the player doesn't have permission.
     * @return The MessageWrapper.
     */
    public MessageWrapper validateFormatting() {
        Matcher matcher = ComponentColorizer.VALID_LEGACY_REGEX_FORMATTING.matcher(this.message);
        List<String> toRemove = new ArrayList<>();
        while (matcher.find()) {
            if (!this.sender.hasPermission("rosechat.format." + this.locationPermission)) {
                String code = this.message.substring(matcher.start(), matcher.end());
                if (!code.endsWith("k")) toRemove.add(code);
            }

            if (!this.sender.hasPermission("rosechat.magic." + this.locationPermission)) {
                String code = this.message.substring(matcher.start(), matcher.end());
                if (code.endsWith("k")) toRemove.add(code);
            }
        }
        for (String s : toRemove) this.message = this.message.replace(s, "");

        return this;
    }

    /**
     * Removes codes instead of showing them if the player doesn't have permission.
     * @return The MessageWrapper.
     */
    public MessageWrapper validateHex() {
        Matcher matcher = ComponentColorizer.HEX_REGEX.matcher(this.message);
        List<String> toRemove = new ArrayList<>();
        while (matcher.find()) {
            String color = this.message.substring(matcher.start(), matcher.end());
            if (this.sender.hasPermission("rosechat.hex." + this.locationPermission)) this.message = this.message.replace(color, color.toLowerCase());
            else toRemove.add(color);
        }
        for (String s : toRemove) this.message = this.message.replace(s, "");

        return this;
    }

    /**
     * Removes codes instead of showing them if the player doesn't have permission.
     * @return The MessageWrapper.
     */
    public MessageWrapper validateRainbow() {
        Matcher matcher = ComponentColorizer.RAINBOW_PATTERN.matcher(this.message);
        List<String> toRemove = new ArrayList<>();
        while (matcher.find()) {
            String color = this.message.substring(matcher.start(), matcher.end());
            if (this.sender.hasPermission("rosechat.rainbow." + this.locationPermission)) this.message = this.message.replace(color, color.toLowerCase());
            else toRemove.add(color);
        }
        for (String s : toRemove) this.message = this.message.replace(s, "");

        return this;
    }

    /**
     * Removes codes instead of showing them if the player doesn't have permission.
     * @return The MessageWrapper.
     */
    public MessageWrapper validateGradient() {
        Matcher matcher = ComponentColorizer.GRADIENT_PATTERN.matcher(this.message);
        List<String> toRemove = new ArrayList<>();
        while (matcher.find()) {
            String color = this.message.substring(matcher.start(), matcher.end());
            if (this.sender.hasPermission("rosechat.gradient." + this.locationPermission)) this.message = this.message.replace(color, color.toLowerCase());
            else toRemove.add(color);
        }
        for (String s : toRemove) this.message = this.message.replace(s, "");

        return this;
    }

    /**
     * Removes codes instead of showing them if the player doesn't have permission.
     * @return The MessageWrapper.
     */
    public MessageWrapper validate() {
        return this.validateRainbow().validateGradient().validateColors().validateFormatting().validateHex();
    }

    private boolean isCaps() {
        if (this.sender.hasPermission("rosechat.caps." + this.locationPermission)) return false;
        if (!Setting.CAPS_CHECKING_ENABLED.getBoolean()) return false;
        int caps = 0;
        for (char c : this.message.toCharArray()) {
            if (Character.isAlphabetic(c) && c == Character.toUpperCase(c)) caps++;
        }

        return caps > Setting.MAXIMUM_CAPS_ALLOWED.getInt();
    }

    /**
     * Decides to filter based on permissions, filters based on configuration settings.
     * @return The MessageWrapper.
     */
    public MessageWrapper filterCaps() {
        if (this.sender.hasPermission("rosechat.caps." + this.locationPermission)) return this;
        if (!Setting.CAPS_CHECKING_ENABLED.getBoolean()) return this;
        if (!this.isCaps()) return this;

        if (Setting.WARN_ON_CAPS_SENT.getBoolean()) this.filterType = FilterType.CAPS;
        if (Setting.LOWERCASE_CAPS_ENABLED.getBoolean()) {
            this.message = this.message.toLowerCase();
            return this;
        }

        this.canBeSent = false;
        return this;
    }

    /**
     * Decides to filter based on permissions, filters based on configuration settings.
     * @return The MessageWrapper.
     */
    public MessageWrapper filterSpam() {
        if (this.sender.hasPermission("rosechat.spam." + this.locationPermission) || this.senderData == null) return this;
        if (!Setting.SPAM_CHECKING_ENABLED.getBoolean()) return this;
        if (this.senderData != null && !this.senderData.getMessageLog().addMessageWithSpamCheck(this.message)) return this;
        if (Setting.WARN_ON_SPAM_SENT.getBoolean()) this.filterType = FilterType.SPAM;
        this.canBeSent = false;
        return this;
    }

    /**
     * Decides to filter based on permissions, filters based on configuration settings.
     * @return The MessageWrapper.
     */
    public MessageWrapper filterURLs() {
        if (this.sender.hasPermission("rosechat.links." + this.locationPermission)) return this;
        if (!Setting.URL_CHECKING_ENABLED.getBoolean()) return this;

        boolean hasURL = false;
        Matcher matcher = MessageUtils.URL_PATTERN.matcher(this.message);
        while (matcher.find()) {
            String url = this.message.substring(matcher.start(), matcher.end());
            this.message = this.message.replace(url, "&m" + url.replace(".", " ") + "&r");
            hasURL = true;
        }

        if (!hasURL) return this;
        if (Setting.WARN_ON_URL_SENT.getBoolean()) this.filterType = FilterType.URL;

        this.canBeSent = Setting.URL_CENSORING_ENABLED.getBoolean();
        return this;
    }

    /**
     * Decides to filter based on permissions, filters based on configuration settings.
     * @return The MessageWrapper.
     */
    public MessageWrapper filterLanguage() {
        if (this.sender.hasPermission("rosechat.language." + this.locationPermission)) return this;
        if (!Setting.SWEAR_CHECKING_ENABLED.getBoolean()) return this;
        String rawMessage = MessageUtils.stripAccents(this.message.toLowerCase());

        // Split by space to avoid things like 'grass' being caught.
        // This is an impossible problem to fix.
        for (String swear : Setting.BLOCKED_SWEARS.getStringList()) {
            for (String word : rawMessage.split(" ")) {
                double similarity = MessageUtils.getLevenshteinDistancePercent(swear, word);
                if (similarity >= (Setting.SWEAR_FILTER_SENSITIVITY.getDouble() / 100)) {
                    if (Setting.WARN_ON_BLOCKED_SWEAR_SENT.getBoolean()) this.filterType = FilterType.SWEAR;
                    this.canBeSent = false;
                    return this;
                }
            }
        }

        for (String replacements : Setting.SWEAR_REPLACEMENTS.getStringList()) {
            String[] swearReplacement = replacements.split(":");
            String swear = swearReplacement[0];
            String replacement = swearReplacement[1];
            for (String word : this.message.split(" ")) {
                double similarity = MessageUtils.getLevenshteinDistancePercent(swear, word);
                if (similarity >= (Setting.SWEAR_FILTER_SENSITIVITY.getDouble() / 100)) {
                    this.message = this.message.replace(word, replacement);
                }
            }
        }

        return this;
    }

    /**
     * Filter the messaged based on permissions and configuration settings.
     * @return The MessageWrapper.
     */
    public MessageWrapper filter() {
        return this.filterCaps().filterSpam().filterLanguage().filterURLs();
    }

    /**
     * Applys the sender's default chat colour.
     * This should be used after validating and filtering as it may cause issues.
     * @return The MessageWrapper.
     */
    public MessageWrapper applyDefaultColor() {
        if (this.senderData != null) this.message = this.senderData.getColor() + this.message;
        return this;
    }

    public String parseToString() {
        return this.message;
    }

    private void logMessage(MessageLog log, String discordId) {
        if (!this.logMessages) return;
        this.deletableMessage = new DeletableMessage(this.id, ComponentSerializer.toString(this.tokenized), false, discordId);
        log.addDeletableMessage(this.deletableMessage);
    }

    public BaseComponent[] parse(String format, RoseSender viewer) {
        PreParseMessageEvent preParseMessageEvent = new PreParseMessageEvent(this, viewer);
        Bukkit.getPluginManager().callEvent(preParseMessageEvent);

        if (!preParseMessageEvent.isCancelled()) {
            ComponentBuilder componentBuilder = new ComponentBuilder();

            if (format == null || !format.contains("{message}")) {
                MessageTokenizer tokenizer = new MessageTokenizer.Builder()
                        .message(this).group(this.group).sender(this.sender)
                        .viewer(viewer).location(this.location)
                        .tokenizers(Setting.USE_DISCORD_FORMATTING.getBoolean() ? Tokenizers.DEFAULT_WITH_DISCORD_TOKENIZERS : Tokenizers.DEFAULT_TOKENIZERS)
                        .tokenize(this.message);

                componentBuilder.append(tokenizer.toComponents(), ComponentBuilder.FormatRetention.FORMATTING);
                return this.tokenized = componentBuilder.create();
            }

            String[] formatSplit = format.split("\\{message\\}");
            String before = formatSplit[0];
            String after = formatSplit.length > 1 ? formatSplit[1] : null;

            if (before != null && !before.isEmpty()) {
                MessageTokenizer tokenizer = new MessageTokenizer.Builder()
                        .message(this).group(this.group).sender(this.sender)
                        .viewer(viewer).location(this.location)
                        .tokenizers(Tokenizers.FORMATTING_TOKENIZERS)
                        .tokenize(before);

                componentBuilder.append(tokenizer.toComponents(), ComponentBuilder.FormatRetention.FORMATTING);
            }

            if (format.contains("{message}")) {
                MessageTokenizer tokenizer = new MessageTokenizer.Builder()
                        .message(this).group(this.group).sender(this.sender)
                        .viewer(viewer).location(this.location)
                        .tokenizers(Tokenizers.DEFAULT_TOKENIZERS)
                        .tokenize(this.message);

                componentBuilder.append(tokenizer.toComponents(), ComponentBuilder.FormatRetention.FORMATTING);
            }

            if (after != null && !after.isEmpty()) {
                MessageTokenizer tokenizer = new MessageTokenizer.Builder()
                        .message(this).group(this.group).sender(this.sender)
                        .viewer(viewer).location(this.location)
                        .tokenizers(Tokenizers.FORMATTING_TOKENIZERS)
                        .tokenize(after);

                componentBuilder.append(tokenizer.toComponents(), ComponentBuilder.FormatRetention.FORMATTING);
            }

            this.tokenized = componentBuilder.create();

            PostParseMessageEvent postParseMessageEvent = new PostParseMessageEvent(this, viewer);
            Bukkit.getPluginManager().callEvent(postParseMessageEvent);

            if (viewer != null) {
                PlayerData viewerData = RoseChatAPI.getInstance().getPlayerData(viewer.getUUID());
                if (viewerData != null) this.logMessage(viewerData.getMessageLog(), null);
            }

            return this.tokenized;
        }

        return this.toComponents();
    }

    public BaseComponent[] parseFromDiscord(String id, String format, RoseSender viewer) {
        PreParseMessageEvent preParseMessageEvent = new PreParseMessageEvent(this, viewer);
        Bukkit.getPluginManager().callEvent(preParseMessageEvent);

        if (!preParseMessageEvent.isCancelled()) {
            ComponentBuilder componentBuilder = new ComponentBuilder();

            if (format == null || !format.contains("{message}")) {
                MessageTokenizer tokenizer = new MessageTokenizer.Builder()
                        .message(this).group(this.group).sender(this.sender)
                        .viewer(viewer).location(this.location)
                        .tokenizers(Tokenizers.DEFAULT_WITH_DISCORD_TOKENIZERS)
                        .tokenize(this.message);

                componentBuilder.append(tokenizer.toComponents(), ComponentBuilder.FormatRetention.FORMATTING);
                return this.tokenized = componentBuilder.create();
            }

            String[] formatSplit = format.split("\\{message\\}");
            String before = formatSplit[0];
            String after = formatSplit.length > 1 ? formatSplit[1] : null;

            if (before != null && !before.isEmpty()) {
                MessageTokenizer tokenizer = new MessageTokenizer.Builder()
                        .message(this).group(this.group).sender(this.sender)
                        .viewer(viewer).location(this.location)
                        .tokenizers(Tokenizers.FROM_DISCORD_FORMATTING_TOKENIZERS)
                        .tokenize(before);

                componentBuilder.append(tokenizer.toComponents(), ComponentBuilder.FormatRetention.FORMATTING);
            }

            if (format.contains("{message}")) {
                MessageTokenizer tokenizer = new MessageTokenizer.Builder()
                        .message(this).group(this.group).sender(this.sender)
                        .viewer(viewer).location(this.location)
                        .tokenizers(Tokenizers.DEFAULT_WITH_DISCORD_TOKENIZERS)
                        .tokenize(this.message);

                componentBuilder.append(tokenizer.toComponents(), ComponentBuilder.FormatRetention.FORMATTING);
            }

            if (after != null && !after.isEmpty()) {
                MessageTokenizer tokenizer = new MessageTokenizer.Builder()
                        .message(this).group(this.group).sender(this.sender)
                        .viewer(viewer).location(this.location)
                        .tokenizers(Tokenizers.FROM_DISCORD_FORMATTING_TOKENIZERS)
                        .tokenize(after);

                componentBuilder.append(tokenizer.toComponents(), ComponentBuilder.FormatRetention.FORMATTING);
            }

            this.tokenized = componentBuilder.create();

            PostParseMessageEvent postParseMessageEvent = new PostParseMessageEvent(this, viewer);
            Bukkit.getPluginManager().callEvent(postParseMessageEvent);

            if (viewer != null) {
                PlayerData viewerData = RoseChatAPI.getInstance().getPlayerData(viewer.getUUID());
                if (viewerData != null) this.logMessage(viewerData.getMessageLog(), id);
            }

            return this.tokenized;
        }
        return this.toComponents();
    }

    public BaseComponent[] parseToDiscord(String format, RoseSender viewer) {
        PreParseMessageEvent preParseMessageEvent = new PreParseMessageEvent(this, viewer);
        Bukkit.getPluginManager().callEvent(preParseMessageEvent);

        if (!preParseMessageEvent.isCancelled()) {
            ComponentBuilder componentBuilder = new ComponentBuilder();

            if (format == null || !format.contains("{message}")) {
                MessageTokenizer tokenizer = new MessageTokenizer.Builder()
                        .message(this).group(this.group).sender(this.sender)
                        .viewer(viewer).location(this.location)
                        .tokenizers(Tokenizers.TO_DISCORD_TOKENIZERS)
                        .tokenize(this.message);

                componentBuilder.append(tokenizer.toComponents(), ComponentBuilder.FormatRetention.FORMATTING);
                return this.tokenized = componentBuilder.create();
            }

            String[] formatSplit = format.split("\\{message\\}");
            String before = formatSplit[0];
            String after = formatSplit.length > 1 ? formatSplit[1] : null;

            if (before != null && !before.isEmpty()) {
                MessageTokenizer tokenizer = new MessageTokenizer.Builder()
                        .message(this).group(this.group).sender(this.sender)
                        .viewer(viewer).location(this.location)
                        .tokenizers(Tokenizers.TO_DISCORD_TOKENIZERS)
                        .tokenize(before);

                componentBuilder.append(tokenizer.toComponents(), ComponentBuilder.FormatRetention.FORMATTING);
            }

            if (format.contains("{message}")) {
                MessageTokenizer tokenizer = new MessageTokenizer.Builder()
                        .message(this).group(this.group).sender(this.sender)
                        .viewer(viewer).location(this.location)
                        .tokenizers(Tokenizers.TO_DISCORD_TOKENIZERS)
                        .tokenize(this.message);

                componentBuilder.append(tokenizer.toComponents(), ComponentBuilder.FormatRetention.FORMATTING);
            }

            if (after != null && !after.isEmpty()) {
                MessageTokenizer tokenizer = new MessageTokenizer.Builder()
                        .message(this).group(this.group).sender(this.sender)
                        .viewer(viewer).location(this.location)
                        .tokenizers(Tokenizers.TO_DISCORD_TOKENIZERS)
                        .tokenize(after);

                componentBuilder.append(tokenizer.toComponents(), ComponentBuilder.FormatRetention.FORMATTING);
            }

            this.tokenized = componentBuilder.create();

            PostParseMessageEvent postParseMessageEvent = new PostParseMessageEvent(this, viewer, true);
            Bukkit.getPluginManager().callEvent(postParseMessageEvent);
            return this.tokenized;
        }

        return this.toComponents();
    }

    public BaseComponent[] toComponents() {
        return this.tokenized;
    }

    public void setComponents(BaseComponent[] components) {
        this.tokenized = components;
    }

    public List<UUID> getTaggedPlayers() {
        return this.taggedPlayers;
    }

    public void setTagSound(Sound tagSound) {
        this.tagSound = tagSound;
    }

    public Sound getTagSound() {
        return this.tagSound;
    }

    public boolean canBeSent() {
        return this.canBeSent;
    }

    public FilterType getFilterType() {
        return this.filterType;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RoseSender getSender() {
        return this.sender;
    }

    public MessageLocation getLocation() {
        return this.location;
    }

    public StringPlaceholders getPlaceholders() {
        return this.placeholders;
    }

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public DeletableMessage getDeletableMessage() {
        return this.deletableMessage;
    }

    public boolean shouldLogMessages() {
        return this.logMessages;
    }

    public void setShouldLogMessages(boolean logMessages) {
        this.logMessages = logMessages;
    }
}
