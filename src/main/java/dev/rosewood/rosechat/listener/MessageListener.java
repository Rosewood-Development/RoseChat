package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.events.PostParseMessageEvent;
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
        if (event.getMessage().getSender().isPlayer() && (event.getMessage().getSender().getUUID() == event.getViewer().getUUID())) {
            if (!event.getViewer().hasPermission("rosechat.deletemessages.self")) return;
            // TODO: this, with actual format
            BaseComponent[] components = event.getMessage().toComponents();
            if (components == null || components.length == 0) return;
            ComponentBuilder componentBuilder = new ComponentBuilder();
            componentBuilder.append(components, ComponentBuilder.FormatRetention.NONE);
            componentBuilder.append(" ", ComponentBuilder.FormatRetention.NONE);
            BaseComponent[] deleteButton = MessageUtils.parseCustomPlaceholder(event.getMessage().getSender(), event.getViewer(), Setting.DELETE_OWN_MESSAGES_FORMAT.getString(),
                    MessageUtils.getSenderViewerPlaceholders(event.getMessage().getSender(), event.getViewer()).build());
            if (deleteButton != null) componentBuilder.append(deleteButton);
            event.getMessage().setComponents(componentBuilder.create());
        } else {
            if (!event.getViewer().hasPermission("rosechat.deletemessages.other")) return;
            BaseComponent[] components = event.getMessage().toComponents();
            if (components == null || components.length == 0) return;
            ComponentBuilder componentBuilder = new ComponentBuilder();
            componentBuilder.append(components, ComponentBuilder.FormatRetention.NONE);
            componentBuilder.append(" ", ComponentBuilder.FormatRetention.NONE);
            BaseComponent[] deleteButton = MessageUtils.parseCustomPlaceholder(event.getMessage().getSender(), event.getViewer(), Setting.DELETE_OTHER_MESSAGES_FORMAT.getString(),
                    MessageUtils.getSenderViewerPlaceholders(event.getMessage().getSender(), event.getViewer()).build());
            if (deleteButton != null) componentBuilder.append(deleteButton);
            event.getMessage().setComponents(componentBuilder.create());
        }
    }
}
