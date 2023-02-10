package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.message.RosePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatPreviewEvent;

public class ChatPreviewListener implements Listener {

    private final RoseChatAPI api;

    public ChatPreviewListener() {
        this.api = RoseChatAPI.getInstance();
    }

    @EventHandler
    public void onChatPreview(AsyncPlayerChatPreviewEvent event) {
        Player player = event.getPlayer();
        PlayerData data = this.api.getPlayerData(player.getUniqueId());
        //ChatChannel channel = data.getCurrentChannel();
        RosePlayer sender = new RosePlayer(player);

       // MessageWrapper message = new MessageWrapper(sender, MessageLocation.CHANNEL, channel, event.getMessage()).filterCaps().filterLanguage().filterURLs().applyDefaultColor().ignoreMessageLogging();
       // event.setMessage(TextComponent.toLegacyText(message.parse(channel.getFormat(), sender)));
    }

}
