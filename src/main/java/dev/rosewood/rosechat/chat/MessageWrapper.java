package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.managers.ConfigurationManager.Setting;
import dev.rosewood.rosechat.managers.DataManager;
import dev.rosewood.rosechat.managers.PlaceholderSettingManager;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;

public class MessageWrapper {

    private RoseChat plugin;
    private DataManager dataManager;
    private PlaceholderSettingManager placeholderManager;
    private CommandSender sender;
    private Player player;
    private MessageSender messageSender;
    private PlayerData playerData;
    private String message;
    private ComponentBuilder builder;
    private ChatChannel channel;
    private boolean replacements;
    private boolean tags;

    private List<String> taggedPlayerNames;
    private boolean isBlocked;
    private FilterType filterType;
    private Sound tagSound;
    private StringBuilder hoverBuilder;

    // Creates a new message wrapper with all required variables.
    private MessageWrapper() {
        this.plugin = RoseChat.getInstance();
        this.dataManager = this.plugin.getManager(DataManager.class);
        this.placeholderManager = this.plugin.getManager(PlaceholderSettingManager.class);
        this.builder = new ComponentBuilder();
        this.taggedPlayerNames = new ArrayList<>();
        this.hoverBuilder = new StringBuilder();
    }

    // Creates a new message wrapper, specifically for players.
    public MessageWrapper(Player player, String message) {
        this();
        this.sender = player;
        this.player = player;
        this.playerData = dataManager.getPlayerData(this.player.getUniqueId());
        this.message = message;
    }

    // Creates a new message wrapper, specifically for MessageSenders.
    public MessageWrapper(MessageSender sender, String message) {
        this();
        this.sender = sender;
        this.messageSender = sender;
        this.message = message;
    }

    // Checks if the sender is able to send colour, if not, removes all colour from the message.
    public MessageWrapper checkColors(String basePermission) {
        if (this.sender.hasPermission(basePermission + ".color")) return this;
        for (char colour : MessageUtils.COLORS) {
            if (this.message.contains("&" + colour)) {
                this.message = this.message.replace("&" + colour, "");
            }
        }

        return this;
    }

    // Checks if the sender is able to send formatting, if not, removes all formatting from the message.
    public MessageWrapper checkFormatting(String basePermission) {
        if (this.sender.hasPermission(basePermission + ".format")) return this;
        for (char format : MessageUtils.FORMATTING) {
            if (this.message.contains("&" + format)) {
                this.message = this.message.replace("&" + format, "");
            }
        }

        return this;
    }

    // Checks if the sender is able to send magic characters, if not, removes all magic characters from the message.
    public MessageWrapper checkMagic(String basePermission) {
        if (this.sender.hasPermission(basePermission + ".sender")) return this;
        if (this.message.contains("&" + MessageUtils.MAGIC)) {
            this.message = this.message.replace("&" + MessageUtils.MAGIC, "");
        }

        return this;
    }

    // Checks if the player can send colours, formatting and magic characters.
    public MessageWrapper checkAll(String basePermission) {
        return this.checkColors(basePermission).checkFormatting(basePermission).checkMagic(basePermission);
    }

    // Checks if the message breaks the configured rules about capital letters.
    private boolean isCaps() {
        // Checks if the letter is the same but capitalised.
        int caps = 0;
        for (int i = 0; i < this.message.length(); i++) {
            char ch = this.message.charAt(i);
            if (ch == Character.toUpperCase(ch)) caps++;
        }

        return caps > Setting.MAXIMUM_CAPS_ALLOWED.getInt();
    }

    // Removes the capital letters from the message in accordance to the configuration.
    public MessageWrapper filterCaps(String basePermission) {
        if (this.sender.hasPermission(basePermission + ".caps") || !Setting.CAPS_CHECKING_ENABLED.getBoolean()) return this;
        if (!isCaps()) return this;

        if (Setting.WARN_ON_CAPS_SENT.getBoolean()) this.filterType = FilterType.CAPS;

        // If the setting is enabled, lowercase the caps and return.
        // If not, the message should be blocked.
        if (Setting.LOWERCASE_CAPS_ENABLED.getBoolean()) {
            message = message.toLowerCase();
            return this;
        }

        this.isBlocked = true;
        return this;
    }

    // Removes the spam from the message in accordance to the configuration.
    public MessageWrapper filterSpam(String basePermission) {
        if (this.sender.hasPermission(basePermission + ".spam") || !Setting.SPAM_CHECKING_ENABLED.getBoolean()) return this;
        if (this.player != null && !this.playerData.getMessageLog().addMessageWithSpamCheck(message)) return this;

        if (Setting.WARN_ON_CAPS_SENT.getBoolean()) this.filterType = FilterType.SPAM;

        this.isBlocked = true;
        return this;
    }

    // Removes the URL from the message in accordance to the configuration.
    public MessageWrapper filterURLs(String basePermission) {
        if (this.sender.hasPermission(basePermission + ".links") || !Setting.URL_CHECKING_ENABLED.getBoolean()) return this;

        boolean hasURL = false;

        Matcher matcher = MessageUtils.URL_PATTERN.matcher(this.message);
        while (matcher.find()) {
            String url = message.substring(matcher.start(0), matcher.end(0));
            this.message = message.replace(url, "&m" + url.replace(".", " ") + "&r");
            hasURL = true;
        }

        if (!hasURL) return this;
        if (Setting.WARN_ON_URL_SENT.getBoolean()) this.filterType = FilterType.URL;

        isBlocked = !Setting.URL_CHECKING_ENABLED.getBoolean();
        return this;
    }

    // Removes the swears from the message in accordance to the configuration.
    // Swear filters are never 100% perfect and people will *always* get around them.
    public MessageWrapper filterSwears(String basePermission) {
        if (this.sender.hasPermission(basePermission + ".language") || !Setting.SWEAR_CHECKING_ENABLED.getBoolean()) return this;
        String rawMessage = MessageUtils.stripAccents(this.message.toLowerCase());

        for (String swear : Setting.BLOCKED_SWEARS.getStringList()) {
            for (String word : rawMessage.split(" ")) {
                double similarity = MessageUtils.getLevenshteinDistancePercent(swear, word);

                if (similarity >= Math.abs(Setting.SWEAR_FILTER_SENSITIVITY.getDouble() - 1)) {
                    if (Setting.WARN_ON_BLOCKED_SWEAR_SENT.getBoolean()) this.filterType = FilterType.SWEAR;
                    this.isBlocked = true;
                    return this;
                }
            }
        }

        for (String replacements : Setting.SWEAR_REPLACEMENTS.getStringList()) {
            String[] replacementSplit = replacements.split(":");
            String swear = replacementSplit[0];
            String replacement = replacementSplit[1];

            // TODO: Should probably compare this with the raw message, but will need to manage the replacement better.
            // TODO: Possibly getting the index of the word and replacing that?
            for (String word : this.message.split(" ")) {
                double similarity = MessageUtils.getLevenshteinDistancePercent(swear, word);

                if (similarity >= Math.abs(Setting.SWEAR_FILTER_SENSITIVITY.getDouble() - 1)) {
                    this.message = this.message.replace(word, replacement);
                }
            }
        }

        return this;
    }

    // Checks the message against all filters and filters accordingly.
    public MessageWrapper filterAll(String basePermission) {
        return this.filterCaps(basePermission).filterSpam(basePermission).filterURLs(basePermission).filterSwears(basePermission);
    }

    // Marks this MessageWrapper as being able to use replacements.
    public MessageWrapper withReplacements() {
        this.replacements = true;
        return this;
    }

    // Marks this MessageWrapper as being able to use tags.
    public MessageWrapper withTags() {
        this.tags = true;
        return this;
    }

    // Marks this MessageWrapper as being in a specific channel, useful for retreiving channel members.
    public MessageWrapper inChannel(ChatChannel channel) {
        this.channel = channel;
        return this;
    }

    public BaseComponent[] getComponentFromPlaceholder(String baseText, CustomPlaceholder placeholder, Player otherPlayer, String group, StringPlaceholders.Builder placeholders) {
        if (placeholder.getText() == null) return null;
        placeholders.addPlaceholder("player_name", this.player.getName())
                .addPlaceholder("player_displayname", this.player.getDisplayName())
                .addPlaceholder("other_player_name", otherPlayer == null ? "null" : otherPlayer.getName())
                .addPlaceholder("other_player_displayname", otherPlayer == null ? "null" : otherPlayer.getDisplayName())
                .build();

        String text = baseText == null ? PlaceholderAPIHook.applyPlaceholders(this.player, placeholders
                .apply(placeholder.getText().getTextFromGroup(group))) : baseText;
        BaseComponent[] components = TextComponent.fromLegacyText(HexUtils.colorify(text));

        if (placeholder.getHover() != null && placeholder.getHover().getHoverStringFromGroup(group) != null) {
            String hover = PlaceholderAPIHook.applyPlaceholders(this.player, placeholders
                    .apply(placeholder.getHover().getHoverStringFromGroup(group)));
            BaseComponent[] hoverComponents = TextComponent.fromLegacyText(HexUtils.colorify(hover));
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents);

            for (BaseComponent component : components) component.setHoverEvent(hoverEvent);
        }

        if (placeholder.getClick() != null && placeholder.getClick().getClickFromGroup(group) != null) {
            String click = PlaceholderAPIHook.applyPlaceholders(this.player, placeholders
                    .apply(placeholder.getClick().getValueFromGroup(group)));
            ClickEvent clickEvent = new ClickEvent(placeholder.getClick().getActionFromGroup(group), HexUtils.colorify(click));

            for (BaseComponent component : components) component.setClickEvent(clickEvent);
        }

        return components;
    }

    public MessageWrapper parse(String format, Player otherPlayer) {
        this.builder = new ComponentBuilder();
        String group = this.plugin.getVault() == null ? "default" : this.plugin.getVault().getPrimaryGroup(player);

        List<String> unformattedChatFormat = this.placeholderManager.getParsedFormat(format);

        for (String placeholderId : unformattedChatFormat) {
            if (placeholderId.equalsIgnoreCase("message")) {
                this.parseMessage(group, otherPlayer);
                continue;
            }

            CustomPlaceholder placeholder = this.placeholderManager.getPlaceholder(placeholderId);
            this.builder.append(this.getComponentFromPlaceholder(null, placeholder, otherPlayer, group, StringPlaceholders.builder()));
        }

        return this;
    }

    public MessageWrapper parseMessage(String group, Player otherPlayer) {
        boolean hasTagOrEmote;

        this.message = HexUtils.colorify(message);

        if (this.parseMultiTags(group, otherPlayer)) return this;

        int index = 0;
        String[] words = this.message.split(" ");
        for (String word : words) {
            if (index != words.length - 1) word += " ";
            hasTagOrEmote = this.parseReplacement(word, group) || parseTags((index == 0 ? null : words[index - 1]), word, group);

            if (!hasTagOrEmote) {
                // I think here is where PlaceholderAPI would be allowed to be used in chat.
                this.builder.append(TextComponent.fromLegacyText(HexUtils.colorify(word)), ComponentBuilder.FormatRetention.FORMATTING)
                        .font("default");
            }

            index++;
        }
        return this;
    }

    private boolean parseMultiTags(String group, Player otherPlayer) {
        if (!this.tags) return false;
        boolean hasTag = false;

        for (Tag tag : this.placeholderManager.getTags().values()) {
            if (tag.getSuffix() == null) continue; // Ensures only multitags get parsed here.
            // If the message doesn't contain the prefix or suffix, ignore it.
            if (!this.message.contains(tag.getPrefix()) && this.message.contains(tag.getSuffix())) continue;
            if (!this.player.hasPermission("rosechat.tag." + tag.getId())) continue;

            String tempMessage = this.message;
            while (tempMessage.contains(tag.getPrefix()) && tempMessage.contains(tag.getSuffix())) {
                int firstPrefix = tempMessage.indexOf(tag.getPrefix()) + tag.getPrefix().length();
                int firstSuffix = tempMessage.indexOf(tag.getSuffix());
                String beforeTag = tempMessage.substring(0, firstPrefix - tag.getPrefix().length());
                String duringTag = tempMessage.substring(firstPrefix, firstSuffix); // Gets the message between the tags.
                String afterTag = null;

                // Make sure there's no more tags
                tempMessage = tempMessage.substring(firstSuffix + tag.getSuffix().length());
                if (!tempMessage.contains(tag.getSuffix())) {
                    afterTag = tempMessage;
                }

                this.builder.append(TextComponent.fromLegacyText(HexUtils.colorify(beforeTag)), ComponentBuilder.FormatRetention.FORMATTING).font("default");


                // Can't use getComponentFromPlaceholder, acts differently.
                CustomPlaceholder placeholder = this.placeholderManager.getPlaceholder(tag.getFormat());

                StringPlaceholders placeholders = StringPlaceholders.builder("tag", duringTag)
                        .addPlaceholder("player_name", this.player.getName())
                        .addPlaceholder("player_displayname", this.player.getDisplayName())
                        .addPlaceholder("other_player_name", otherPlayer == null ? "null" : otherPlayer.getName())
                        .addPlaceholder("other_player_displayname", otherPlayer == null ? "null" : otherPlayer.getDisplayName())
                        .build();

                StringBuilder textSb = new StringBuilder(HexUtils.colorify(placeholder.getText().getTextFromGroup(group)));

                // Allows multitags to match the length of the message.
                if (tag.shouldMatchLength()) {
                    String originalText = textSb.toString();
                    for (int i = 0; i < ChatColor.stripColor(duringTag).length() - 1; i++) textSb.append(originalText);
                }

                String text = PlaceholderAPIHook.applyPlaceholders(this.player, placeholders.apply(textSb.toString()));
                BaseComponent[] components = this.getComponentFromPlaceholder(text, placeholder, otherPlayer, group, StringPlaceholders.builder("tag", duringTag));

                this.builder.append(components, ComponentBuilder.FormatRetention.FORMATTING).font("default");

                if (afterTag != null)
                    this.builder.append(TextComponent.fromLegacyText(HexUtils.colorify(afterTag)), ComponentBuilder.FormatRetention.NONE)
                            .font("default");

                hasTag = true;
            }
        }

        return hasTag;
    }

    private boolean parseTags(String previousWord, String word, String group) {
        boolean hasTag = false;

        for (Tag tag : this.placeholderManager.getTags().values()) {
            if (tag.getSuffix() != null) continue; // Ensures multitags don't get parsed here.
            if (!word.startsWith(tag.getPrefix())) continue;
            if (!this.player.hasPermission("rosechat.tag." + tag.getId())) continue;

            String lastColor = previousWord == null ? ChatColor.RESET + "" : org.bukkit.ChatColor.getLastColors(previousWord);
            String color = lastColor.isEmpty() ? ChatColor.RED + "" : lastColor;
            word = word.replace(tag.getPrefix(), ""); // Removes the prefix from the tag.

            CustomPlaceholder placeholder = this.placeholderManager.getPlaceholder(tag.getFormat());
            word = word.trim();
            Player otherPlayer = Bukkit.getPlayer(word);

            BaseComponent[] components = this.getComponentFromPlaceholder(null, placeholder, otherPlayer, group,
                    StringPlaceholders.builder("tag", word + " " + color));
            this.builder.append(components, ComponentBuilder.FormatRetention.FORMATTING).font("default");

            if (tag.shouldTagOnlinePlayers()) taggedPlayerNames.add(word);
            tagSound = tag.getSound();

            hasTag = true;
        }

        return hasTag;
    }

    private boolean parseChatPlaceholders() {
        return false;
    }

    private boolean parseReplacement(String word, String group) {
        if (!this.replacements) return false;
        if (this.playerData != null && !this.playerData.hasEmotes()) return false;
        boolean hasReplacement = false;

        for (ChatReplacement replacement : this.placeholderManager.getReplacements().values()) {
            if (!this.player.hasPermission("rosechat.replacement." + replacement.getId())) continue;
            String wordLower = word.toLowerCase();

            if (wordLower.contains(replacement.getText().toLowerCase()) && this.player.hasPermission("rosechat.replacement." + replacement.getId())) {
                int i = 0;
                String[] parts = wordLower.split(replacement.getText());

                for (String s : parts) {
                    this.builder.append(TextComponent.fromLegacyText(HexUtils.colorify(s)), ComponentBuilder.FormatRetention.FORMATTING)
                            .font("emotes");

                    if (i != parts.length - 1) this.applyReplacement(replacement, group);
                    i++;
                }

                if (wordLower.endsWith(replacement.getText().toLowerCase())) applyReplacement(replacement, group);
                hasReplacement = true;
            }
        }

        return hasReplacement;
    }

    // Applies the given chat replacement.
    // TODO: Regex replacements
    private void applyReplacement(ChatReplacement chatReplacement, String group) {
        if (!this.player.hasPermission("rosechat.replacement." + chatReplacement.getId())) return;
        if (chatReplacement.getHoverText() != null) {
            String text = chatReplacement.getReplacement();

            if (chatReplacement.getReplacement().startsWith("{") && chatReplacement.getReplacement().endsWith("}")) {
                CustomPlaceholder placeholder = this.placeholderManager.getPlaceholder(chatReplacement.getReplacement()
                        .replace("{", "").replace("}", ""));
                this.builder.append(this.getComponentFromPlaceholder(null, placeholder, null, group, StringPlaceholders.builder()));
                return;
            }

            if (chatReplacement.getReplacement().startsWith("%") && chatReplacement.getReplacement().endsWith("%")) {
                text = PlaceholderAPIHook.applyPlaceholders(this.player, chatReplacement.getReplacement());
            }

            StringPlaceholders placeholders = StringPlaceholders.single("text", chatReplacement.getText());
            BaseComponent[] components = TextComponent.fromLegacyText(HexUtils.colorify(placeholders.apply(text)));
            BaseComponent[] hover = TextComponent.fromLegacyText(HexUtils.colorify(placeholders.apply(chatReplacement.getHoverText())));
            for (BaseComponent component : components) component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
            this.builder.append(components, ComponentBuilder.FormatRetention.FORMATTING).font(chatReplacement.getFont());
        } else {
            this.builder.append(TextComponent.fromLegacyText(HexUtils.colorify(chatReplacement.getReplacement())), ComponentBuilder.FormatRetention.FORMATTING)
                    .font(chatReplacement.getFont());
        }
    }

    // Builds the message.
    public BaseComponent[] build() {
        return this.builder.create();
    }

    // Checks if the message is empty.
    public boolean isEmpty() {
        BaseComponent[] built = this.build();
        return built == null || build().length == 0;
    }

    // Gets all the players in the channel.
    public List<Player> getChannelMembers() {
        List<Player> players = new ArrayList<>();

        for (UUID uuid : this.channel.getPlayers()) {
            if (uuid.equals(player.getUniqueId())) continue;
            if (Bukkit.getPlayer(uuid) != null) players.add(Bukkit.getPlayer(uuid));
        }

        return players;
    }

    // Tags the players in the given channel, or out of all players if no channel is specified.
    public void tagPlayers() {
        if (this.tagSound == null) return;
        if (this.channel != null) {
            for (UUID taggedUuid : this.channel.getPlayers()) {
                Player tagged = Bukkit.getPlayer(taggedUuid);
                if (tagged == null || !this.taggedPlayerNames.contains(tagged.getName())) continue;

                PlayerData taggedData = dataManager.getPlayerData(tagged.getUniqueId());
                if (taggedData.hasTagSounds()) tagged.playSound(tagged.getLocation(), this.getTagSound(), 1, 1);
            }

            return;
        }

        for (String playerStr : this.taggedPlayerNames) {
            Player tagged = Bukkit.getPlayer(playerStr);
            if (tagged == null) continue;

            PlayerData taggedData = dataManager.getPlayerData(tagged.getUniqueId());
            if (taggedData.hasTagSounds()) tagged.playSound(tagged.getLocation(), this.getTagSound(), 1, 1);
        }
    }

    // Sends the message to a specific command sender.
    public void send(CommandSender sender) {
        sender.spigot().sendMessage(this.build());
    }

    // Sends the message to a specific player.
    public void send(Player player) {
        player.spigot().sendMessage(this.build());
    }

    // Sends the message to a specific channel.
    public void send(ChatChannel channel) {
        BaseComponent[] message = this.build();

        for (Player player : this.getChannelMembers()) {
            player.spigot().sendMessage(message);
        }
    }

    public List<String> getTaggedPlayerNames() {
        return taggedPlayerNames;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public Sound getTagSound() {
        return tagSound;
    }

    public ChatChannel getChannel() {
        return channel;
    }

    public String getHoverAsString() {
        return this.hoverBuilder.toString();
    }
}
