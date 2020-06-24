package dev.rosewood.rosechat.chat;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.floralapi.petal.chat.ChatComponent;
import dev.rosewood.rosechat.floralapi.petal.chat.ChatMessage;
import dev.rosewood.rosechat.floralapi.root.utils.LocalizedText;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MessageBuilder {

    private RoseChat plugin;
    private Player player;
    private String message;
    private ChatMessage outMessage;

    private char[] colourCharacters = { '1', '2', '3', '4',
            '5', '6', '7', '8',
            '9', '0', 'a', 'b',
            'c', 'd', 'e', 'f' };
    private char[] formattingCharacters = { 'l', 'm', 'n', 'o'};
    private char magicCharacter = 'k';

    private List<String> taggedPlayerNames;
    private boolean isBlocked;
    private FilterType filterType;

    public MessageBuilder(Player sender, String message) {
        this.plugin = RoseChat.getInstance();
        this.player = sender;
        this.message = message;
        this.outMessage = new ChatMessage();
        taggedPlayerNames = new ArrayList<>();
    }

    public MessageBuilder checkColours() {
        if (player.hasPermission("rosechat.chat.color")) return this;
        for (char colour : colourCharacters)
            if (message.contains("&" + colour))
                message = message.replace("&" + colour, "");
        return this;
    }

    public MessageBuilder checkFormatting() {
        if (player.hasPermission("rosechat.chat.format")) return this;
        for (char format : formattingCharacters)
            if (message.contains("&" + format))
                message = message.replace("&" + format, "");
        return this;
    }

    public MessageBuilder checkMagic() {
        if (player.hasPermission("rosechat.chat.magic")) return this;
        if (message.contains("&" + magicCharacter))
            message = message.replace("&" + magicCharacter, "");
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

    public MessageBuilder filterCaps() {
        if (!isCaps() || plugin.getConfigFile().getBoolean("caps-check")) return this;
        isBlocked = true;
        filterType = FilterType.CAPS;
        return this;
    }

    public MessageBuilder filterSpam() {
        return this;
    }

    public MessageBuilder filterURLs() {
        return this;
    }

    public MessageBuilder filterSwears() {
        return this;
    }

    public MessageBuilder filterAll() {
        filterCaps();
        filterSpam();
        filterURLs();
        filterSwears();
        return this;
    }

    // god please help me
    public MessageBuilder applyTags() {
        String tagPrefix = plugin.getConfigFile().getString("tag-prefix");
        String[] words = message.split(" ");

        for (String word : words) {
            if (!word.startsWith(tagPrefix)) continue;
            if (Bukkit.getPlayer(word.replace(tagPrefix, "")) == null
                    && !plugin.getConfigFile().getBoolean("allow-offline-tags")) return this;
            taggedPlayerNames.add(word);
            message = message.replace(word, "{tag}");
        }

        return this;
    }

    public MessageBuilder parsePlaceholders(String format) {
        ChatMessage message = new ChatMessage();
        String group = plugin.getVault() == null ? "default" : plugin.getVault().getPrimaryGroup(player);

        List<String> unformattedChatFormat = plugin.getPlaceholderManager().getParsedFormats().get(format);

        for (String placeholderId : unformattedChatFormat) {
            CustomPlaceholder placeholder = plugin.getPlaceholderManager().getPlaceholder(placeholderId);

            // Text can't be empty.
            if (placeholder.getText() == null) return this;
            String text = new LocalizedText(placeholder.getText().getTextFromGroup(group))
                    .withPlaceholderAPI(player).format();

            ChatComponent component = new ChatComponent(text);

            if (placeholder.getHover() != null) {
                component.setHoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new LocalizedText(placeholder.getHover().getHoverStringFromGroup(group))
                                .withPlaceholderAPI(player).format());
            }

            if (placeholder.getClick() != null) {
                component.setClickEvent(placeholder.getClick().getActionFromGroup(group),
                        new LocalizedText(placeholder.getClick().getValueFromGroup(group))
                                .withPlaceholderAPI(player).format());
            }

            message.addComponent(component);
        }

        // Parse the message after. To do: Some way to detect stuff after the message too.
        for (String word : this.message.split(" ")) {
            ChatComponent component;

            if (word.startsWith("@")) {
                component = new ChatComponent("&6@tag ")
                        .setHoverEvent(HoverEvent.Action.SHOW_TEXT, "&cThis is a tag.");
            } else {
                component = new ChatComponent(word + " ");
            }

            message.addComponent(component);
        }

        outMessage = message;
        return this;
    }

    public ChatMessage build() {
        return outMessage;
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
}
