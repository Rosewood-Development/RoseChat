package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.PostParseMessageEvent;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.placeholders.RoseChatPlaceholder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MessageListener implements Listener {

    private final RoseChatAPI api;

    public MessageListener() {
        this.api = RoseChatAPI.getInstance();
    }

    @EventHandler
    public void onPostParseMessage(PostParseMessageEvent event) {
        if (event.getMessageDirection() == MessageDirection.TO_DISCORD || event.getMessageDirection() == MessageDirection.TO_BUNGEE_SERVER) return;

        // If the sender is the same as the viewer (player looking at their own message)
        if (event.getMessage().getSender().isPlayer() && event.getMessage().getSender().getUUID() == event.getViewer().getUUID()) {
            if (!event.getViewer().hasPermission("rosechat.deletemessages.self")) return;
            BaseComponent[] components = event.getMessage().toComponents();
            if (components == null || components.length == 0) return;

            ComponentBuilder componentBuilder = new ComponentBuilder();
            String placeholder = Setting.DELETE_OWN_MESSAGE_FORMAT.getString();
            this.appendButton(event, components, componentBuilder, placeholder);
        } else {
            if (!event.getViewer().hasPermission("rosechat.deletemessages.others")) return;

            BaseComponent[] components = event.getMessage().toComponents();
            if (components == null || components.length == 0) return;

            ComponentBuilder componentBuilder = new ComponentBuilder();
            String placeholder = Setting.DELETE_OTHER_MESSAGE_FORMAT.getString();
            this.appendButton(event, components, componentBuilder, placeholder);
        }
    }

    private void appendButton(PostParseMessageEvent event, BaseComponent[] components, ComponentBuilder componentBuilder, String placeholder) {
        BaseComponent[] deleteButton = this.getButton(event, placeholder);

        if (this.shouldSuffixButton(event, placeholder)) {
            componentBuilder.append(components, ComponentBuilder.FormatRetention.NONE);
            if (deleteButton != null) componentBuilder.append(deleteButton);
        } else {
            if (deleteButton != null) componentBuilder.append(deleteButton);
            componentBuilder.append(components, ComponentBuilder.FormatRetention.NONE);
        }

        event.getMessage().setComponents(componentBuilder.create());
    }

    private boolean shouldSuffixButton(PostParseMessageEvent event, String placeholderId) {
        if (placeholderId == null || placeholderId.isEmpty()) return false;
        RoseChatPlaceholder placeholder = this.api.getPlaceholderManager().getPlaceholder(placeholderId.substring(1, placeholderId.length() - 1));
        if (placeholder == null) return false;

       String text = placeholder.getText().parseToString(new RosePlayer(Bukkit.getConsoleSender()), event.getViewer(),
                MessageUtils.getSenderViewerPlaceholders(event.getMessage().getSender(), event.getViewer())
                        .add("id", event.getMessage().getUUID())
                        .add("type", "server")
                        .add("channel", event.getMessage().getLocationPermission().replace("channel.", "")).build());
        return text.trim().startsWith("%message%");
    }

    private BaseComponent[] getButton(PostParseMessageEvent event, String placeholder) {
        placeholder = placeholder.replace("%message%", "");

        return RoseChatAPI.getInstance().parse(new RosePlayer(Bukkit.getConsoleSender()), event.getViewer(), placeholder,
                MessageUtils.getSenderViewerPlaceholders(event.getMessage().getSender(), event.getViewer())
                        .add("id", event.getMessage().getUUID())
                        .add("type", "server")
                        .add("channel", event.getMessage().getLocationPermission().replace("channel.", ""))
                        .add("message", "").build());
    }

}
