package dev.rosewood.rosechat.listeners;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.MessageBuilder;
import dev.rosewood.rosechat.floralapi.petal.chat.ChatMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private RoseChat plugin;

    public ChatListener() {
        plugin = RoseChat.getInstance();
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().hasPermission("rosechat.chat")) return;

        Player player = event.getPlayer();
        String oldMessage = event.getMessage();
        event.setCancelled(true);

        MessageBuilder messageBuilder = new MessageBuilder(player, oldMessage)
                .checkColours()
                .checkFormatting()
                .checkMagic()
                .parsePlaceholders("chat-format");

        ChatMessage message = messageBuilder.build();

        // Don't cancel the message if it is null? Would use default format?
        if (message == null) return;

        if (messageBuilder.isBlocked()) {
            messageBuilder.getFilterType().getWarning().sendMessage(player);
            return;
        }

        // Sends to all players and logs it to console.
        for (Player receiver : event.getRecipients()) message.send(receiver);
        message.send(Bukkit.getConsoleSender());

        if (plugin.getConfigFile().getString("tag-sound") == null) return;
        String soundStr = plugin.getConfigFile().getString("tag-sound");

        if (Sound.valueOf(soundStr) == null) return;

        for (String playerStr : messageBuilder.getTaggedPlayerNames()) {
            if (Bukkit.getPlayer(playerStr) == null) continue;
            Player tagged = Bukkit.getPlayer(playerStr);
            tagged.playSound(tagged.getLocation(), Sound.valueOf(soundStr), 1, 1);
        }
    }

    /*

        // Apply tags.
        List<String> taggedPlayerNames = new ArrayList<>();
        oldMessage = applyTagging(player, oldMessage, taggedPlayerNames);

        // apply filters & colour codes, etc
        ChatMessage message = applyFormatting(player, oldMessage, "chat-format");

        if (plugin.getConfigFile().getString("tag-sound") == null) return;
        if (Sound.valueOf(plugin.getConfigFile().getString("tag-sound")) == null) return;
        Sound tagSound = Sound.valueOf(plugin.getConfigFile().getString("tag-sound"));
        for (String playerStr : taggedPlayerNames) {
            if (Bukkit.getPlayer(playerStr) == null) continue;
            Player tagged = Bukkit.getPlayer(playerStr);
            tagged.playSound(tagged.getLocation(), tagSound, 1, 1);
        }

    public String applyTagging(Player player, String message, List<String> taggedPlayerNames) {
        String tagPrefix = plugin.getConfigFile().getString("tag-prefix");
        String[] words = message.split(" ");
        for (String word : words) {
            if (!word.startsWith(tagPrefix)) continue;
            if (Bukkit.getPlayer(word.replace(tagPrefix, "")) == null
                    && !plugin.getConfigFile().getBoolean("allow-offline-tags")) continue;
            taggedPlayerNames.add(word);
        }

        ChatComponent tagComponent = applyFormatting(player, message, "tagging-format").getComponents().get(0);

        return message;
    }

    public ChatMessage applyFormatting(Player sender, String oldMessage, String format) {
        ChatMessage message = new ChatMessage();
        String group = plugin.getVault() == null ? "default" : plugin.getVault().getPrimaryGroup(sender);
        List<String> unformattedChatFormat = plugin.getPlaceholderManager().getParsedFormats().get(format);
        for (String placeholderId : unformattedChatFormat) {
            CustomPlaceholder placeholder = plugin.getPlaceholderManager().getPlaceholder(placeholderId);

            // Text can't be empty.
            if (placeholder.getText() == null) return null;
            String text = new LocalizedText(placeholder.getText().getTextFromGroup(group))
                    .withPlaceholder("message", oldMessage)
                    .withPlaceholderAPI(sender).format();

            ChatComponent component = new ChatComponent(text);

            if (placeholder.getHover() != null) {
                component.setHoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new LocalizedText(placeholder.getHover().getHoverStringFromGroup(group))
                                .withPlaceholder("message", oldMessage)
                                .withPlaceholderAPI(sender).format());
            }

            if (placeholder.getClick() != null) {
                component.setClickEvent(placeholder.getClick().getActionFromGroup(group),
                        new LocalizedText(placeholder.getClick().getValueFromGroup(group))
                                .withPlaceholder("message", oldMessage)
                                .withPlaceholderAPI(sender).format());
            }

            message.addComponent(component);
        }

        return message;
    }*/
}
