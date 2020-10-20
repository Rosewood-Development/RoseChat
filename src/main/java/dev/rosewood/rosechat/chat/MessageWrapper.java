package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.managers.ConfigurationManager.Setting;
import dev.rosewood.rosechat.managers.DataManager;
import dev.rosewood.rosechat.managers.LocaleManager;
import dev.rosewood.rosechat.managers.PlaceholderSettingManager;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
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
import java.util.regex.Matcher;

public class MessageWrapper {

    private RoseChat plugin;
    private DataManager dataManager;

    private PlaceholderSettingManager placeholderManager;
    private Player player;
    private PlayerData playerData;
    private String message;
    private ComponentBuilder builder;

    private boolean replacements;
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
        this.dataManager = plugin.getManager(DataManager.class);
        this.playerData = dataManager.getPlayerData(sender.getUniqueId());
        this.placeholderManager = plugin.getManager(PlaceholderSettingManager.class);
        this.player = sender;
        this.message = message;
        this.builder = new ComponentBuilder();
        this.taggedPlayerNames = new ArrayList<>();
    }

    public MessageWrapper checkColours() {
        if (this.player.hasPermission("rosechat.chat.color")) return this;
        for (char colour : this.colourCharacters)
            if (this.message.contains("&" + colour))
                this.message = this.message.replace("&" + colour, "");
        return this;
    }

    public MessageWrapper checkFormatting() {
        if (this.player.hasPermission("rosechat.chat.format")) return this;
        for (char format : this.formattingCharacters)
            if (this.message.contains("&" + format))
                this.message = this.message.replace("&" + format, "");
        return this;
    }

    public MessageWrapper checkMagic() {
        if (this.player.hasPermission("rosechat.chat.magic")) return this;
        if (this.message.contains("&" + this.magicCharacter))
            this.message = this.message.replace("&" + this.magicCharacter, "");
        return this;
    }

    public MessageWrapper checkAll() {
        return this.checkColours().checkFormatting().checkMagic();
    }

    private boolean isCaps() {
        if (this.player.hasPermission("rosechat.bypass.caps")) return false;
        if (!Setting.CAPS_CHECKING_ENABLED.getBoolean()) return false;

        String alpha = message;//.replaceAll("[A-Za-z0-9]*]", "");
        int caps = 0;

        // Checks if the letter is the same as the letter capitalised.
        for (int i = 0; i < alpha.length(); i++)
            if (alpha.charAt(i) == Character.toUpperCase(alpha.charAt(i))) caps++;

        return caps > Setting.MAXIMUM_CAPS_ALLOWED.getInt();
    }

    public MessageWrapper filterCaps() {
        if (player.hasPermission("rosechat.bypass.caps")) return this;
        if (!Setting.CAPS_CHECKING_ENABLED.getBoolean()) return this;
        if (!isCaps()) return this;

        if (Setting.WARN_ON_CAPS_SENT.getBoolean()) filterType = FilterType.CAPS;

        if (Setting.LOWERCASE_CAPS_ENABLED.getBoolean()) {
            message = message.toLowerCase();
            return this;
        }
        isBlocked = true;

        return this;
    }

    public MessageWrapper filterSpam() {
        if (player.hasPermission("rosechat.bypass.spam")) return this;
        if (!Setting.SPAM_CHECKING_ENABLED.getBoolean()) return this;
        if (!playerData.getMessageLog().addMessageWithSpamCheck(message)) return this;
        if (Setting.WARN_ON_SPAM_SENT.getBoolean()) filterType = FilterType.SPAM;

        isBlocked = true;

        return this;
    }

    public MessageWrapper filterURLs() {
        if (player.hasPermission("rosechat.bypass.links")) return this;
        if (!Setting.URL_CHECKING_ENABLED.getBoolean()) return this;

        boolean hasUrl = false;

        Matcher matcher = MessageUtils.URL_PATTERN.matcher(message);
        while (matcher.find()) {
            String url = message.substring(matcher.start(0), matcher.end(0));
            message = message.replace(url, "&m" + url.replace(".", " ") + "&r");
            hasUrl = true;
        }

        if (!hasUrl) return this;

        if (Setting.WARN_ON_URL_SENT.getBoolean()) filterType = FilterType.URL;
        isBlocked = !Setting.URL_CENSORING_ENABLED.getBoolean();

        return this;
    }

    public MessageWrapper filterSwears() {
        if (player.hasPermission("rosechat.bypass.language")) return this;
        if (!Setting.SWEAR_CHECKING_ENABLED.getBoolean()) return this;
        String rawMessage = MessageUtils.stripAccents(message.toLowerCase());

        // how to solve the scunthorpe problem :sob:
        // Sorry server admins, your players can either say "aaaaassssssss" or not "grass"/"assassin"... :(
        for (String swear : Setting.BLOCKED_SWEARS.getStringList()) {
            for (String word : rawMessage.split(" ")) {
                double similarity = MessageUtils.getLevenshteinDistancePercent(swear, word);
                if (similarity >= Math.abs(Setting.SWEAR_FILTER_SENSITIVITY.getDouble() - 1)) {
                    if (Setting.WARN_ON_BLOCKED_SWEAR_SENT.getBoolean()) filterType = FilterType.SWEAR;
                    isBlocked = true;
                    return this;
                }
            }
        }

        // TODO: Merge, or make function for this duplicate code!!!
        for (String replacements : Setting.SWEAR_REPLACEMENTS.getStringList()) {
            String[] swearReplacement = replacements.split(":");
            String swear = swearReplacement[0];
            String replacement = swearReplacement[1];
            for (String word : message.split(" ")) {
                double similarity = MessageUtils.getLevenshteinDistancePercent(swear, word);
                if (similarity >= Math.abs(Setting.SWEAR_FILTER_SENSITIVITY.getDouble() - 1)) {
                    message = message.replace(word, replacement);
                }
            }
        }

        return this;
    }

    public MessageWrapper filterAll() {
        return this.filterCaps().filterSpam().filterURLs().filterSwears();
    }

    public MessageWrapper withReplacements() {
        this.replacements = true;
        return this;
    }

    public MessageWrapper withTags() {
        this.tags = true;
        return this;
    }

    public MessageWrapper parsePlaceholders(String format, Player other) {
        String group = plugin.getVault() == null ? "default" : plugin.getVault().getPrimaryGroup(player);

        List<String> unformattedChatFormat = placeholderManager.getParsedFormats().get(format);

        for (String placeholderId : unformattedChatFormat) {
            CustomPlaceholder placeholder = placeholderManager.getPlaceholder(placeholderId);
            if (placeholderId.equalsIgnoreCase("message")) {
                parseMessage(group, other);
                continue;
            }

            // Text can't be empty.
            if (placeholder.getText() == null) return this;
            StringPlaceholders placeholders = StringPlaceholders.builder("player_name", player.getName())
                    .addPlaceholder("player_displayname", player.getDisplayName())
                    .addPlaceholder("other_player_name", other == null ? "null" : other.getName())
                    .addPlaceholder("other_player_displayname", other == null ? "null" : other.getDisplayName())
                    .build();

            String text = PlaceholderAPIHook.applyPlaceholders(player, placeholders.apply(placeholder.getText().getTextFromGroup(group)));

            // Using this, as new TextComponent messes with hex colours.
            BaseComponent[] components = TextComponent.fromLegacyText(HexUtils.colorify(text));

            if (placeholder.getHover() != null && placeholder.getHover().getHoverStringFromGroup(group) != null) {
                String hover = PlaceholderAPIHook.applyPlaceholders(player, placeholders.apply(placeholder.getHover().getHoverStringFromGroup(group)));
                BaseComponent[] hoverComponents = TextComponent.fromLegacyText(HexUtils.colorify(hover));
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponents);

                for (BaseComponent component : components) component.setHoverEvent(hoverEvent);
            }

            if (placeholder.getClick() != null && placeholder.getClick().getClickFromGroup(group) != null) {
                String click = PlaceholderAPIHook.applyPlaceholders(player, placeholders.apply(placeholder.getClick().getValueFromGroup(group)));
                ClickEvent clickEvent = new ClickEvent(placeholder.getClick().getActionFromGroup(group), HexUtils.colorify(click));

                for (BaseComponent component : components) component.setClickEvent(clickEvent);
            }

            builder.append(components, ComponentBuilder.FormatRetention.FORMATTING);
        }

        return this;
    }

    public void parseMessage(String group, Player other) {
        boolean hasTagOrEmote;

        // Parse colours before.
        message = HexUtils.colorify(message);

        if (parseMessageTags(group, other)) return;

        int index = 0;
        String[] words = message.split(" ");
        for (String word : words) {
            // Ensures the final word does not have a space at the end.
            if (index != words.length - 1) word += " ";
            hasTagOrEmote = parseEmotes(word) || parseTags((index == 0 ? null : words[index - 1]), word, group);

            if (!hasTagOrEmote) {
                builder.append(TextComponent.fromLegacyText(HexUtils.colorify(word)), ComponentBuilder.FormatRetention.FORMATTING);
            }

            index++;
        }
    }

    private boolean parseMessageTags(String group, Player other) {
        if (!tags) return false;
        boolean hasTag = false;

        // It's nicer to parse the message tags, and then parse the word tags after.
        // But it's probably more efficient to parse both at once? :thinking:
        for (Tag tag : placeholderManager.getTags().values()) {
            if (!player.hasPermission("rosechat.tag." + tag.getId())) return false;
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

                builder.append(TextComponent.fromLegacyText(HexUtils.colorify(before)), ComponentBuilder.FormatRetention.FORMATTING);

                CustomPlaceholder placeholder = placeholderManager.getPlaceholder(tag.getFormat());
                StringPlaceholders placeholders = StringPlaceholders.builder("tag", tagMessage)
                        .addPlaceholder("player_name", this.player.getName())
                        .addPlaceholder("player_displayname", this.player.getDisplayName())
                        .addPlaceholder("other_player_name", other == null ? "null" : other.getName())
                        .addPlaceholder("other_player_displayname", other == null ? "null" : other.getDisplayName()).build();

                StringBuilder textSb = new StringBuilder(HexUtils.colorify(placeholder.getText().getTextFromGroup(group)));

                if (tag.shouldMatchLength()) {
                    String originalText = textSb.toString();
                    for (int i = 0; i < ChatColor.stripColor(tagMessage).length() - 1; i++) textSb.append(originalText);
                }

                // TODO: 'other' placeholders.
                String text = PlaceholderAPIHook.applyPlaceholders(player, placeholders.apply(textSb.toString()));

                BaseComponent[] components = TextComponent.fromLegacyText(HexUtils.colorify(text));

                if (placeholder.getHover() != null && placeholder.getHover().getHoverStringFromGroup(group) != null) {
                    String hover = PlaceholderAPIHook.applyPlaceholders(player, placeholders.apply(placeholder.getHover().getHoverStringFromGroup(group)));
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(HexUtils.colorify(hover)));
                    for (BaseComponent component : components) component.setHoverEvent(hoverEvent);
                    // If log hover to console,
                    Bukkit.getConsoleSender().sendMessage("[Hover] " + this.player.getDisplayName() + ": " + hover);
                }

                // TODO: Merge Duplicate
                if (placeholder.getClick() != null && placeholder.getClick().getClickFromGroup(group) != null) {
                    String click = PlaceholderAPIHook.applyPlaceholders(player, placeholders.apply(placeholder.getClick().getValueFromGroup(group)));
                    ClickEvent clickEvent = new ClickEvent(placeholder.getClick().getActionFromGroup(group), HexUtils.colorify(click));
                    for (BaseComponent component : components) component.setClickEvent(clickEvent);
                }

                builder.append(components, ComponentBuilder.FormatRetention.FORMATTING);

                if (after != null)
                    builder.append(TextComponent.fromLegacyText(HexUtils.colorify(after)), ComponentBuilder.FormatRetention.NONE);
            }

            hasTag = true;
        }

        return hasTag;
    }

    private boolean parseChatPlaceholders() {
        return false;
    }

    // TODO: Fix multiple emotes touching.
    private boolean parseEmotes(String word) {
        if (!replacements) return false;
        boolean hasEmote = false;
        
        for (ChatReplacement chatReplacement : placeholderManager.getReplacements().values()) {
            if (!player.hasPermission("rosechat.replacement." + chatReplacement.getId())) continue;
            String wordLower = word.toLowerCase();
            if (wordLower.contains(chatReplacement.getText().toLowerCase()) && player.hasPermission("rosechat.replacement." + chatReplacement.getId())) {
                int i = 0;
                String[] parts = wordLower.split(chatReplacement.getText());
                for (String s : parts) {
                    builder.append(TextComponent.fromLegacyText(HexUtils.colorify(s)), ComponentBuilder.FormatRetention.FORMATTING);

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
        PlaceholderSettingManager placeholderManager = plugin.getManager(PlaceholderSettingManager.class);
        boolean hasTag = false;

        for (Tag tag : placeholderManager.getTags().values()) {
            if (!player.hasPermission("rosechat.tag." + tag.getId())) return false;
            if (tag.getSuffix() != null) continue; // Ensures full message tags don't get parsed here.
            if (word.startsWith(tag.getPrefix()) && player.hasPermission("rosechat.chat.tag." + tag.getId())) {
                String lastColors = prevWord == null ? ChatColor.RESET + "": ChatColor.getLastColors(prevWord);
                String color = lastColors.isEmpty() ? ChatColor.RESET + "" : lastColors;
                word = word.replace(tag.getPrefix(), ""); // Removes the prefix from the message
                CustomPlaceholder placeholder = placeholderManager.getPlaceholder(tag.getFormat());

                // Okay, gotta trim here and later to get the player name.
                // But then gotta add the spaces back in the placeholder (wait how does this work with PlaceholderAPI??)
                word = word.trim();
                Player other = Bukkit.getPlayer(word);

                StringPlaceholders placeholders = StringPlaceholders.builder("tag", word + " " + color)
                        .addPlaceholder("player_name", this.player.getName() + " " + color)
                        .addPlaceholder("player_displayname", this.player.getDisplayName() + " " + color)
                        .addPlaceholder("other_player_name", (other == null ? word : other.getName()) + " " + color)
                        .addPlaceholder("other_player_displayname", (other == null ? word : other.getDisplayName()) + " " + color).build();

                // TODO: Other???? Somehow??
                String text = PlaceholderAPIHook.applyPlaceholders(this.player, placeholders.apply(placeholder.getText().getTextFromGroup(group)));

                BaseComponent[] components = TextComponent.fromLegacyText(HexUtils.colorify(text));

                if (placeholder.getHover() != null && placeholder.getHover().getHoverStringFromGroup(group) != null) {
                    String hover = PlaceholderAPIHook.applyPlaceholders(player, placeholders.apply(placeholder.getHover().getHoverStringFromGroup(group)));
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(HexUtils.colorify(hover)));
                    for (BaseComponent component : components) component.setHoverEvent(hoverEvent);
                }

                if (placeholder.getClick() != null && placeholder.getClick().getClickFromGroup(group) != null) {
                    String click = PlaceholderAPIHook.applyPlaceholders(player, placeholders.apply(placeholder.getClick().getValueFromGroup(group)));
                    ClickEvent clickEvent = new ClickEvent(placeholder.getClick().getActionFromGroup(group), HexUtils.colorify(click));
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
        if (chatReplacement.getHoverText() != null) {
            if (chatReplacement.getReplacement().startsWith("{") && chatReplacement.getReplacement().endsWith("}")) {
                CustomPlaceholder placeholder = placeholderManager.getPlaceholder(chatReplacement.getReplacement().replace("{", "").replace("}", ""));
                return;
            }

            BaseComponent[] components = TextComponent.fromLegacyText(chatReplacement.getReplacement());
            StringPlaceholders placeholders = StringPlaceholders.single("text", chatReplacement.getText());
            BaseComponent[] hover = TextComponent.fromLegacyText(HexUtils.colorify(placeholders.apply(chatReplacement.getHoverText())));
            for (BaseComponent component : components) component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
            builder.append(components, ComponentBuilder.FormatRetention.FORMATTING);
        } else {
            builder.append(TextComponent.fromLegacyText(HexUtils.colorify(chatReplacement.getReplacement())), ComponentBuilder.FormatRetention.FORMATTING);
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
