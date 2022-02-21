package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.event.PostParseMessageEvent;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MessageListener implements Listener {

    private final RoseChat plugin;

    public MessageListener(RoseChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPostParseMessage(PostParseMessageEvent event) {
        // If the sender is the same as the viewer (player looking at their own message)
        if (event.getMessage().getSender().isPlayer() && event.getMessage().getSender().getUUID() == event.getViewer().getUUID()) {
            if (!event.getViewer().hasPermission("rosechat.deletemessages.self")) return;

            BaseComponent[] components = event.getMessage().toComponents();
            if (components == null || components.length == 0) return;

            ComponentBuilder componentBuilder = new ComponentBuilder();
            componentBuilder.append(components, ComponentBuilder.FormatRetention.NONE);

            String placeholder = Setting.DELETE_OWN_MESSAGE_FORMAT.getString();
            BaseComponent[] deleteButton = this.getButton(event, placeholder);
            if (deleteButton != null) componentBuilder.append(deleteButton);
            event.getMessage().setComponents(componentBuilder.create());
        } else {
            if (!event.getViewer().hasPermission("rosechat.deletemessages.other")) return;

            BaseComponent[] components = event.getMessage().toComponents();
            if (components == null || components.length == 0) return;

            ComponentBuilder componentBuilder = new ComponentBuilder();
            componentBuilder.append(components, ComponentBuilder.FormatRetention.NONE);

            String placeholder = Setting.DELETE_OTHER_MESSAGE_FORMAT.getString();
            BaseComponent[] deleteButton = this.getButton(event, placeholder);
            if (deleteButton != null) componentBuilder.append(deleteButton);
            event.getMessage().setComponents(componentBuilder.create());
        }
    }

    private BaseComponent[] getButton(PostParseMessageEvent event, String placeholder) {
        return MessageUtils.parseCustomPlaceholder(event.getMessage().getSender(), event.getViewer(), placeholder.substring(1, placeholder.length() - 1),
                MessageUtils.getSenderViewerPlaceholders(event.getMessage().getSender(), event.getViewer())
                        .addPlaceholder("id", event.getMessage().getId())
                        .addPlaceholder("type", "server").build());
    }
}
