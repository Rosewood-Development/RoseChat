package dev.rosewood.rosechat.listener.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class PaperChatListener implements ChatListener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        event.setCancelled(true);
        String legacyText = LegacyComponentSerializer.legacySection().serialize(event.message());
        this.handleChat(event.getPlayer(), legacyText);
    }

}
