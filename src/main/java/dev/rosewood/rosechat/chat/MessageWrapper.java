package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.floralapi.root.utils.LocalizedText;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MessageWrapper {

    private RoseChat plugin;
    private Player player;
    private String message;
    private ComponentBuilder builder;

    private boolean emotes;
    private boolean tags;

    private char[] colourCharacters = { '1', '2', '3', '4',
            '5', '6', '7', '8',
            '9', '0', 'a', 'b',
            'c', 'd', 'e', 'f' };
    private char[] formattingCharacters = { 'l', 'm', 'n', 'o'};
    private char magicCharacter = 'k';

    private List<String> taggedPlayerNames;
    private boolean isBlocked;
    private FilterType filterType;
    private Sound tagSound;

    public MessageWrapper(Player sender, String message) {
        this.plugin = RoseChat.getInstance();
        this.player = sender;
        this.message = message;
        this.builder = new ComponentBuilder();
        taggedPlayerNames = new ArrayList<>();
    }

    public MessageWrapper checkColours() {
        if (player.hasPermission("rosechat.chat.color")) return this;
        for (char colour : colourCharacters)
            if (message.contains("&" + colour))
                message = message.replace("&" + colour, "");
        return this;
    }

    public MessageWrapper checkFormatting() {
        if (player.hasPermission("rosechat.chat.format")) return this;
        for (char format : formattingCharacters)
            if (message.contains("&" + format))
                message = message.replace("&" + format, "");
        return this;
    }

    public MessageWrapper checkMagic() {
        if (player.hasPermission("rosechat.chat.magic")) return this;
        if (message.contains("&" + magicCharacter))
            message = message.replace("&" + magicCharacter, "");
        return this;
    }

    public MessageWrapper checkAll() {
        checkColours();
        checkFormatting();
        checkMagic();
        return this;
    }

    private boolean isCaps() {
        if (player.hasPermission("rosechat.bypass.caps")) return false;
        if (!plugin.getConfigFile().getBoolean("moderation-settings.caps-checking-enabled")) return false;

        String alpha = message;//.replaceAll("[A-Za-z0-9]*]", "");
        int caps = 0;

        // Checks if the letter is the same as the letter capitalised.
        for (int i = 0; i < alpha.length(); i++)
            if (alpha.charAt(i) == Character.toUpperCase(alpha.charAt(i))) caps++;

        return caps > plugin.getConfigFile().getInt("moderation-settings.maximum-caps-allowed");
    }

    public MessageWrapper filterCaps() {
        if (player.hasPermission("rosechat.bypass.caps")) return this;
        if (!plugin.getConfigFile().getBoolean("moderation-settings.caps-checking-enabled")) return this;
        if (!isCaps()) return this;

        if (plugin.getConfigFile().getBoolean("moderation-settings.warn-on-caps-sent")) filterType = FilterType.CAPS;

        if (plugin.getConfigFile().getBoolean("moderation-settings.lowercase-capitals-enabled")) {
            message = message.toLowerCase();
            return this;
        }
        isBlocked = true;

        return this;
    }

    public MessageWrapper filterSpam() {
        if (player.hasPermission("rosechat.bypass.spam")) return this;
        if (!plugin.getConfigFile().getBoolean("moderation-settings.spam-checking-enabled")) return this;
        if (!plugin.getDataManager().getPlayerChatMessages(player).addMessageWithSpamCheck(message)) return this;
        if (plugin.getConfigFile().getBoolean("moderation-settings.warn-on-spam-sent")) filterType = FilterType.SPAM;

        isBlocked = true;

        return this;
    }

    public MessageWrapper filterURLs() {
        if (player.hasPermission("rosechat.bypass.links")) return this;
        if (!plugin.getConfigFile().getBoolean("moderation-settings.url-checking-enabled")) return this;

        if (!message.matches(MessageUtils.URL_PATTERN.pattern()))
            return this;

        if (plugin.getConfigFile().getBoolean("moderation-settings.warn-on-url-sent")) filterType = FilterType.URL;

        if (message.matches(MessageUtils.URL_PATTERN.pattern())) {
            if (plugin.getConfigFile().getBoolean("moderation-settings.url-censoring-enabled")) {
                // Maybe just replace within the URL/IP?
                message = message.replace(".", " ");
                // what else should go here idk
            } else {
                isBlocked = true;
            }
        }

        return this;
    }

    public MessageWrapper filterSwears() {
        if (player.hasPermission("rosechat.bypass.language")) return this;
        if (!plugin.getConfigFile().getBoolean("moderation-settings.swear-checking-enabled")) return this;
        String rawMessage = MessageUtils.stripAccents(message.toLowerCase());

        // how to solve the scunthorpe problem :sob:
        // Sorry server admins, your players can either say "aaaaassssssss" or not "grass"/"assassin"... :(
        for (String swear : plugin.getConfigFile().getStringList("moderation-settings.blocked-swears")) {
            for (String word : message.split(" ")) {
                double similarity = MessageUtils.getLevenshteinDistancePercent(swear, word);
                if (similarity >= plugin.getConfigFile().getDouble("moderation-settings.swear-filter-sensitivity")) {
                    if (plugin.getConfigFile().getBoolean("moderation-settings.warn-on-blocked-swear-sent")) filterType = FilterType.SWEAR;
                    isBlocked = true;
                    return this;
                }
            }
        }

        // TODO: Merge, or make function for this duplicate code!!!
        for (String replacements : plugin.getConfigFile().getStringList("moderation-settings.swear-replacements")) {
            String[] swearReplacement = replacements.split(":");
            String swear = swearReplacement[0];
            String replacement = swearReplacement[1];
            for (String word : message.split(" ")) {
                double similarity = MessageUtils.getLevenshteinDistancePercent(swear, word);
                if (similarity >= plugin.getConfigFile().getDouble("moderation-settings.swear-filter-sensitivity")) {
                    message = message.replace(word, replacement);

                    if (plugin.getConfigFile().getBoolean("moderation-settings.warn-on-replaced-swear-sent")) filterType = FilterType.SWEAR;
                }
            }
        }

        return this;
    }

    public MessageWrapper filterAll() {
        filterCaps();
        filterSpam();
        filterURLs();
        filterSwears();
        return this;
    }

    public MessageWrapper withEmotes() {
        this.emotes = true;
        return this;
    }

    public MessageWrapper withTags() {
        this.tags = true;
        return this;
    }

    public MessageWrapper parsePlaceholders(String format, Player other, Player viewer) {
        String group = plugin.getVault() == null ? "default" : plugin.getVault().getPrimaryGroup(player);

        List<String> unformattedChatFormat = plugin.getPlaceholderManager().getParsedFormats().get(format);

        for (String placeholderId : unformattedChatFormat) {
            CustomPlaceholder placeholder = plugin.getPlaceholderManager().getPlaceholder(placeholderId);
            if (placeholderId.equalsIgnoreCase("message")) {
                parseMessage(group, other, viewer);
                continue;
            }

            // Text can't be empty.
            if (placeholder.getText() == null) return this;
            String text = new LocalizedText(placeholder.getText().getTextFromGroup(group))
                    .withPlaceholder("player_name", player.getName())
                    .withPlaceholder("player_displayname", player.getDisplayName())
                    .withPlaceholder("other_player_name", other == null ? "null" : other.getName())
                    .withPlaceholder("other_player_displayname", other == null ? "null" : other.getDisplayName())
                    .withPlaceholderAPI(player).format();

            // Using this, as new TextComponent messes with hex colours.
            BaseComponent[] components = TextComponent.fromLegacyText(text);

            if (placeholder.getHover() != null) {
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new LocalizedText(placeholder.getHover().getHoverStringFromGroup(group))
                                .withPlaceholder("player_name", player.getName())
                                .withPlaceholder("player_displayname", player.getDisplayName())
                                .withPlaceholder("other_player_name", other == null ? "null" : other.getName())
                                .withPlaceholder("other_player_displayname", other == null ? "null" : other.getDisplayName())
                                .withPlaceholderAPI(player).toComponents());

                for (BaseComponent component : components) component.setHoverEvent(hoverEvent);
            }

            if (placeholder.getClick() != null) {
                ClickEvent clickEvent = new ClickEvent(placeholder.getClick().getActionFromGroup(group),
                        new LocalizedText(placeholder.getClick().getValueFromGroup(group))
                                .withPlaceholder("player_name", player.getName())
                                .withPlaceholder("player_displayname", player.getDisplayName())
                                .withPlaceholder("other_player_name", other == null ? "null" : other.getName())
                                .withPlaceholder("other_player_displayname", other == null ? "null" : other.getDisplayName())
                                .withPlaceholderAPI(player).format());

                for (BaseComponent component : components) component.setClickEvent(clickEvent);
            }

            builder.append(components, ComponentBuilder.FormatRetention.FORMATTING);
        }

        return this;
    }

    public void parseMessage(String group, Player other, Player viewer) {
        boolean hasTagOrEmote;

        // Parse colours before.
        message = new LocalizedText(message).format();

        if (parseMessageTags(group, other, viewer)) return;

        int index = 0;
        String[] words = message.split(" ");
        for (String word : words) {
            // Ensures the final word does not have a space at the end.
            if (index != words.length - 1) word += " ";
            hasTagOrEmote = parseEmotes(word) || parseTags((index == 0 ? null : words[index - 1]), word, group);

            if (!hasTagOrEmote) {
                builder.append(new LocalizedText(word).toComponents(), ComponentBuilder.FormatRetention.FORMATTING);
            }

            index++;
        }
    }

    private boolean parseMessageTags(String group, Player other, Player viewer) {
        boolean hasTag = false;

        // It's nicer to parse the message tags, and then parse the word tags after.
        // But it's probably more efficient to parse both at once? :thinking:
        for (Tag tag : plugin.getPlaceholderManager().getTags().values()) {
            if (!player.hasPermission("rosechat.chat.tag." + tag.getId())) return false;
            if (tag.getSuffix() == null) continue; // Ensures word tags don't get parsed here.
            if (!message.contains(tag.getPrefix()) && !message.contains(tag.getSuffix())) continue;
            String tempMessage = message;

            while (tempMessage.contains(tag.getPrefix()) && tempMessage.contains(tag.getSuffix())) {
                int firstPrefix = tempMessage.indexOf(tag.getPrefix()) + tag.getPrefix().length();
                int firstSuffix = tempMessage.indexOf(tag.getSuffix());
                String before = tempMessage.substring(0, firstPrefix - tag.getPrefix().length());
                String tagMessage = tempMessage.substring(firstPrefix, firstSuffix);
                String after = null;
                tempMessage = tempMessage.substring(firstSuffix + tag.getSuffix().length());


                if (!tempMessage.contains(tag.getSuffix())) {
                    after = tempMessage;
                }

                builder.append(new LocalizedText(before).toComponents(), ComponentBuilder.FormatRetention.FORMATTING);

                CustomPlaceholder placeholder = plugin.getPlaceholderManager().getPlaceholder(tag.getFormat());

                StringBuilder text = new StringBuilder(new LocalizedText(placeholder.getText().getTextFromGroup(group)).format());

                if (tag.shouldMatchLength()) {
                    String originalText = text.toString();
                    for (int i = 0; i < tagMessage.length() - 1; i++) text.append(originalText);
                }

                BaseComponent[] components = TextComponent.fromLegacyText(new LocalizedText(text.toString())
                        .withPlaceholder("tag", tagMessage)
                        .withPlaceholder("player_name", this.player.getName())
                        .withPlaceholder("player_displayname", this.player.getDisplayName())
                        .withPlaceholderAPI(player)
                        .format());

                if (placeholder.getHover() != null) {
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LocalizedText(placeholder.getHover()
                            .getHoverStringFromGroup(group))
                            .withPlaceholder("tag", tagMessage)
                            .withPlaceholder("player_name", this.player.getName())
                            .withPlaceholder("player_displayname", this.player.getDisplayName())
                            .withPlaceholderAPI(player)
                            .toComponents());
                    for (BaseComponent component : components) component.setHoverEvent(hoverEvent);
                }

                if (placeholder.getClick() != null) {
                    ClickEvent clickEvent = new ClickEvent(placeholder.getClick().getActionFromGroup(group), new LocalizedText(placeholder.getClick()
                            .getValueFromGroup(group))
                            .withPlaceholder("tag", tagMessage)
                            .withPlaceholder("player_name", this.player.getName())
                            .withPlaceholder("player_displayname", this.player.getDisplayName())
                            .withPlaceholderAPI(player)
                            .format());
                    for (BaseComponent component : components) component.setClickEvent(clickEvent);
                }

                builder.append(components, ComponentBuilder.FormatRetention.FORMATTING);

                if (after != null)
                    builder.append(new LocalizedText(after).toComponents(), ComponentBuilder.FormatRetention.FORMATTING);
            }

            hasTag = true;
        }

        return hasTag;
    }

    private boolean parseChatPlaceholders() {
        return false;
    }

    // TODO: Fix multiple emotes touching.
    // TODO: Merge all replacements here, allow placeholders in the emote replacement.
    private boolean parseEmotes(String word) {
        boolean hasEmote = false;
        
        for (ChatReplacement chatReplacement : plugin.getPlaceholderManager().getEmotes().values()) {
            if (!player.hasPermission("rosechat.chat.emote." + chatReplacement.getId())) return false;
            String wordLower = word.toLowerCase();
            if (wordLower.contains(chatReplacement.getText().toLowerCase()) && player.hasPermission("rosechat.chat.emote." + chatReplacement.getId())) {
                int i = 0;
                String[] parts = wordLower.split(chatReplacement.getText());
                for (String s : parts) {
                    builder.append(new LocalizedText(s).toComponents(), ComponentBuilder.FormatRetention.FORMATTING);

                    if (i != parts.length - 1) applyEmote(chatReplacement);
                    i++;
                }

                // Only the emote is sent.
                if (wordLower.endsWith(chatReplacement.getText().toLowerCase())) applyEmote(chatReplacement);
                hasEmote = true;
            }
        }
        
        return hasEmote;
    }

    // TODO: Fix colour parsing around the tag.
    private boolean parseTags(String prevWord, String word, String group) {
        boolean hasTag = false;

        for (Tag tag : plugin.getPlaceholderManager().getTags().values()) {
            if (!player.hasPermission("rosechat.chat.tag." + tag.getId())) return false;
            if (tag.getSuffix() != null) continue; // Ensures full message tags don't get parsed here.
            if (word.startsWith(tag.getPrefix()) && player.hasPermission("rosechat.chat.tag." + tag.getId())) {
                String lastColors = prevWord == null ? ChatColor.RESET + "": ChatColor.getLastColors(prevWord);
                String color = lastColors.isEmpty() ? ChatColor.RESET + "" : lastColors;
                word = word.replace(tag.getPrefix(), ""); // Removes the prefix from the message
                CustomPlaceholder placeholder = plugin.getPlaceholderManager().getPlaceholder(tag.getFormat());

                // Okay, gotta trim here and later to get the player name.
                // But then gotta add the spaces back in the placeholder (wait how does this work with PlaceholderAPI??)
                word = word.trim();
                Player other = Bukkit.getPlayer(word);

                BaseComponent[] components = TextComponent.fromLegacyText(new LocalizedText(placeholder.getText().getTextFromGroup(group))
                        .withPlaceholder("tag", word + " " + color)
                        .withPlaceholder("player_name", this.player.getName() + " " + color)
                        .withPlaceholder("player_displayname", this.player.getDisplayName() + " " + color)
                        .withPlaceholder("other_player_name", (other == null ? word : other.getName()) + " " + color)
                        .withPlaceholder("other_player_displayname", (other == null ? word : other.getDisplayName()) + " " + color)
                        .withPlaceholderAPI(player)
                        .format());

                if (placeholder.getHover() != null) {
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LocalizedText(placeholder.getHover()
                            .getHoverStringFromGroup(group))
                            .withPlaceholder("tag", word + color)
                            .withPlaceholder("player_name", this.player.getName() + " " + color)
                            .withPlaceholder("player_displayname", this.player.getDisplayName() + " " + color)
                            .withPlaceholder("other_player_name", (other == null ? word : other.getName()) + color)
                            .withPlaceholder("other_player_displayname", (other == null ? word : other.getDisplayName()) + color)
                            .withPlaceholderAPI(player)
                            .toComponents());
                    for (BaseComponent component : components) component.setHoverEvent(hoverEvent);
                }

                if (placeholder.getClick() != null) {
                    ClickEvent clickEvent = new ClickEvent(placeholder.getClick().getActionFromGroup(group), new LocalizedText(placeholder.getClick()
                            .getValueFromGroup(group))
                            .withPlaceholder("tag", word)
                            .withPlaceholder("player_name", this.player.getName() + " " + color)
                            .withPlaceholder("player_displayname", this.player.getDisplayName() + " " + color)
                            .withPlaceholder("other_player_name", (other == null ? word : other.getName()))
                            .withPlaceholder("other_player_displayname", (other == null ? word : other.getDisplayName()))
                            .withPlaceholderAPI(player)
                            .format());
                    for (BaseComponent component : components) component.setClickEvent(clickEvent);
                }

                builder.append(components, ComponentBuilder.FormatRetention.FORMATTING);
                if (tag.shouldTagOnlinePlayers()) taggedPlayerNames.add(word);
                tagSound = tag.getSound();

                hasTag = true;
            }
        }

        return hasTag;
    }

    private void applyEmote(ChatReplacement chatReplacement) {
        if (plugin.getConfigFile().getBoolean("show-emote-name-on-hover")) {
            BaseComponent[] components = TextComponent.fromLegacyText(chatReplacement.getReplacement());
            BaseComponent[] hover = new LocalizedText(plugin.getConfigFile().getString("show-emote-format"))
                    .withPlaceholder("text", chatReplacement.getText()).toComponents();
            for (BaseComponent component : components) component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
            builder.append(components, ComponentBuilder.FormatRetention.FORMATTING);
        } else {
            builder.append(new LocalizedText(chatReplacement.getReplacement()).toComponents(), ComponentBuilder.FormatRetention.FORMATTING);
        }
    }

    public BaseComponent[] build() {
        return builder.create();
    }

    public boolean isEmpty() {
        BaseComponent[] built = build();
        return built == null || built.length == 0;
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

    public void send(CommandSender sender) {
        sender.spigot().sendMessage(build());
    }
}
