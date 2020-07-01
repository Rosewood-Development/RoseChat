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
        String alpha = message.replaceAll("[^A-Za-z0-9*À-ÖØ-öø-ÿ/]*", "X");
        //String alpha = message;//.replaceAll("[A-Za-z0-9]*]", "");
        int caps = 0;

        // Checks if the letter is the same as the letter capitalised.
        for (int i = 0; i < alpha.length(); i++)
            if (alpha.charAt(i) == Character.toUpperCase(alpha.charAt(i))) caps++;

        return caps > plugin.getConfigFile().getInt("max-amount-of-caps")
                && !player.hasPermission("rosechat.bypass.caps");
    }

    public MessageWrapper filterCaps() {
        if (!isCaps() || plugin.getConfigFile().getBoolean("caps-check")) return this;
        isBlocked = true;
        filterType = FilterType.CAPS;
        return this;
    }

    public MessageWrapper filterSpam() {
        return this;
    }

    public MessageWrapper filterURLs() {
        return this;
    }

    public MessageWrapper filterSwears() {
        return this;
    }

    public MessageWrapper filterAll() {
        filterCaps();
        filterSpam();
        filterURLs();
        filterSwears();
        return this;
    }

    public MessageWrapper parsePlaceholders(String format) {
        String group = plugin.getVault() == null ? "default" : plugin.getVault().getPrimaryGroup(player);

        List<String> unformattedChatFormat = plugin.getPlaceholderManager().getParsedFormats().get(format);

        for (String placeholderId : unformattedChatFormat) {
            CustomPlaceholder placeholder = plugin.getPlaceholderManager().getPlaceholder(placeholderId);
            Bukkit.broadcastMessage(placeholder.getText().getTextFromGroup(group));
            if (placeholderId.equalsIgnoreCase("message")) {
                parseMessage(group);
                continue;
            }

            // Text can't be empty.
            if (placeholder.getText() == null) return this;
            String text = new LocalizedText(placeholder.getText().getTextFromGroup(group))
                    .withPlaceholderAPI(player).format();

            TextComponent component = new TextComponent(text);

            if (placeholder.getHover() != null) {
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new LocalizedText(placeholder.getHover().getHoverStringFromGroup(group))
                        .withPlaceholderAPI(player).toComponents());
                component.setHoverEvent(hoverEvent);
            }

            if (placeholder.getClick() != null) {
                ClickEvent clickEvent = new ClickEvent(placeholder.getClick().getActionFromGroup(group),
                        new LocalizedText(placeholder.getClick().getValueFromGroup(group))
                                .withPlaceholderAPI(player).format());
                component.setClickEvent(clickEvent);
            }

            builder.append(component, ComponentBuilder.FormatRetention.FORMATTING);
        }

        return this;
    }

    public void parseMessage(String group) {
        for (String word : this.message.split(" ")) {
            String tagPrefix = plugin.getConfigFile().getString("tag-prefix");

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
