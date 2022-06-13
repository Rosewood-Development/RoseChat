package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.PostParseMessageEvent;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.placeholders.CustomPlaceholder;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MessageListener implements Listener {

    private final RoseChatAPI api;

    public MessageListener(RoseChat plugin) {
        this.api = RoseChatAPI.getInstance();;
    }

    @EventHandler
    public void onPostParseMessage(PostParseMessageEvent event) {
        if (event.isToDiscord()) return;

        // If the sender is the same as the viewer (player looking at their own message)
        if (event.getMessage().getSender().isPlayer() && event.getMessage().getSender().getUUID() == event.getViewer().getUUID()) {
            if (!event.getViewer().hasPermission("rosechat.deletemessages.self")) return;

            BaseComponent[] components = event.getMessage().toComponents();
            if (components == null || components.length == 0) return;

            ComponentBuilder componentBuilder = new ComponentBuilder();
            String placeholder = Setting.DELETE_OWN_MESSAGE_FORMAT.getString();
            BaseComponent[] deleteButton = this.getButton(event, placeholder);

            if (this.shouldSuffixButton(event, placeholder)) {
                componentBuilder.append(components, ComponentBuilder.FormatRetention.NONE);
                if (deleteButton != null) componentBuilder.append(deleteButton);
            } else {
                if (deleteButton != null) componentBuilder.append(deleteButton);
                componentBuilder.append(components, ComponentBuilder.FormatRetention.NONE);
            }
            event.getMessage().setComponents(componentBuilder.create());
        } else {
            if (!event.getViewer().hasPermission("rosechat.deletemessages.other")) return;

            BaseComponent[] components = event.getMessage().toComponents();
            if (components == null || components.length == 0) return;

            ComponentBuilder componentBuilder = new ComponentBuilder();
            String placeholder = Setting.DELETE_OTHER_MESSAGE_FORMAT.getString();
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
    }

    private boolean shouldSuffixButton(PostParseMessageEvent event, String placeholderId) {
        CustomPlaceholder placeholder = this.api.getPlaceholderManager().getPlaceholder(placeholderId.substring(1, placeholderId.length() - 1));
        if (placeholder == null) return false;

        String text = placeholder.getText().parse(event.getMessage().getSender(), event.getViewer(),
                MessageUtils.getSenderViewerPlaceholders(event.getMessage().getSender(), event.getViewer())
                        .addPlaceholder("id", event.getMessage().getId())
                        .addPlaceholder("type", "server").build());
        return text.trim().startsWith("%message%");
    }

    private BaseComponent[] getButton(PostParseMessageEvent event, String placeholder) {
        placeholder = placeholder.replace("%message%", "");
        return MessageUtils.parseCustomPlaceholder(event.getMessage().getSender(), event.getViewer(), placeholder.substring(1, placeholder.length() - 1),
                MessageUtils.getSenderViewerPlaceholders(event.getMessage().getSender(), event.getViewer())
                        .addPlaceholder("id", event.getMessage().getId())
                        .addPlaceholder("type", "server")
                        .addPlaceholder("message", "").build(), false);
    }

}
