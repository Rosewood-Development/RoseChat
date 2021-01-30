package dev.rosewood.rosechat.listeners;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.MessageUtils;
import dev.rosewood.rosechat.chat.MessageWrapper;
import dev.rosewood.rosechat.managers.ConfigurationManager.Setting;
import dev.rosewood.rosechat.managers.DataManager;
import dev.rosewood.rosechat.managers.LocaleManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private RoseChat plugin;
    private DataManager dataManager;

    public ChatListener(RoseChat plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.dataManager = plugin.getManager(DataManager.class);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            Player player = event.getPlayer();
            if (!player.hasPermission("rosechat.chat")) {
                this.plugin.getManager(LocaleManager.class).sendMessage(player, "no-permission");
                return;
            }

            String message = event.getMessage();
            ChatChannel channel = this.dataManager.getPlayerData(player.getUniqueId()).getCurrentChannel();

            MessageWrapper messageWrapper = new MessageWrapper(player, message)
                    .checkAll("rosechat.chat")
                    .filterAll("rosechat.chat")
                    .withReplacements()
                    .withTags()
                    .inChannel(channel)
                    .parse(channel.getFormatId(), null);

            MessageUtils.sendStandardMessage(player, messageWrapper, channel);

            // Outputs the message to the console, with information about the hover events.
            if (Setting.OUTPUT_HOVER_EVENTS.getBoolean()) {
                String hoverOutput = messageWrapper.getHoverAsString();
                if (hoverOutput != null && !hoverOutput.isEmpty())
                    Bukkit.getConsoleSender().sendMessage(hoverOutput);
            }
        });
    }
}
