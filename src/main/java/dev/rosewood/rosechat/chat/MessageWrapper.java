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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class MessageWrapper {

    private RoseChat plugin;
    private Player player;
    private String message;
    private ComponentBuilder builder;

    private char[] colourCharacters = { '1', '2', '3', '4',
            '5', '6', '7', '8',
            '9', '0', 'a', 'b',
            'c', 'd', 'e', 'f' };
    private char[] formattingCharacters = { 'l', 'm', 'n', 'o'};
    private char magicCharacter = 'k';

    private List<String> taggedPlayerNames;
    private boolean isBlocked;
    private FilterType filterType;

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

        if (plugin.getConfigFile().getBoolean("moderation-settings.url-censoring-enabled")) {
            Matcher matcher = MessageUtils.URL_PATTERN.matcher(message);
            while (matcher.find()) {
                // bruh idk
            }
            return this;
        }

        isBlocked = true;

        return this;
    }

    public MessageWrapper filterSwears() {
        if (player.hasPermission("rosechat.bypass.language")) return this;
        if (!plugin.getConfigFile().getBoolean("moderation-settings.swear-checking-enabled")) return this;
        String rawMessage = MessageUtils.stripAccents(message.toLowerCase());

        // how to solve the scunthorpe problem :sob:
        // Sorry server admins, your players can either say "aaaaassssssss" or not "grass", or "assassin"... :(
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

            TextComponent component = new TextComponent(text);

            if (placeholder.getHover() != null) {
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new LocalizedText(placeholder.getHover().getHoverStringFromGroup(group))
                                .withPlaceholder("player_name", player.getName())
                                .withPlaceholder("player_displayname", player.getDisplayName())
                                .withPlaceholder("other_player_name", other == null ? "null" : other.getName())
                                .withPlaceholder("other_player_displayname", other == null ? "null" : other.getDisplayName())
                                .withPlaceholderAPI(player).toComponents());
                component.setHoverEvent(hoverEvent);
            }

            if (placeholder.getClick() != null) {
                ClickEvent clickEvent = new ClickEvent(placeholder.getClick().getActionFromGroup(group),
                        new LocalizedText(placeholder.getClick().getValueFromGroup(group))
                                .withPlaceholder("player_name", player.getName())
                                .withPlaceholder("player_displayname", player.getDisplayName())
                                .withPlaceholder("other_player_name", other == null ? "null" : other.getName())
                                .withPlaceholder("other_player_displayname", other == null ? "null" : other.getDisplayName())
                                .withPlaceholderAPI(player).format());
                component.setClickEvent(clickEvent);
            }

            builder.append(component, ComponentBuilder.FormatRetention.FORMATTING);
        }

        return this;
    }

    public void parseMessage(String group, Player other, Player viewer) {
        for (String word : this.message.split(" ")) {
            String tagPrefix = plugin.getConfigFile().getString("tags.player.prefix");

            if (word.startsWith(tagPrefix) && player.hasPermission("rosechat.chat.tag") && word.length() > 1) {
                word = word.replace(tagPrefix, "");
                CustomPlaceholder placeholder = plugin.getPlaceholderManager().getPlaceholder("tag");

                TextComponent component = new TextComponent(new LocalizedText(placeholder.getText().getTextFromGroup(group))
                        .withPlaceholder("tag", word + " ").format());

                if (placeholder.getHover() != null) {
                    HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LocalizedText(placeholder.getHover()
                            .getHoverStringFromGroup(group)).withPlaceholderAPI(Bukkit.getOfflinePlayer(word)).toComponents());
                    component.setHoverEvent(hoverEvent);
                }

                if (placeholder.getClick() != null) {
                    ClickEvent clickEvent = new ClickEvent(placeholder.getClick().getActionFromGroup(group),
                            new LocalizedText(placeholder.getClick().getValueFromGroup(group))
                                    .withPlaceholderAPI(Bukkit.getOfflinePlayer(word)).format());
                    component.setClickEvent(clickEvent);
                }

                builder.append(component, ComponentBuilder.FormatRetention.FORMATTING);

                taggedPlayerNames.add(word);
            }
            else {
                if (player.hasPermission("rosechat.chat.color")) {
                    builder.append(new LocalizedText(word + " ").toComponents(), ComponentBuilder.FormatRetention.FORMATTING);
                } else {
                    builder.append(word, ComponentBuilder.FormatRetention.FORMATTING);
                }
            }
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

    public void send(CommandSender sender) {
        sender.spigot().sendMessage(build());
    }
}
