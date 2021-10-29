package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import org.bukkit.event.Listener;

public class DiscordListener implements Listener {

    private RoseChatAPI api;

    public DiscordListener() {
        this.api = RoseChatAPI.getInstance();
    }

    @Subscribe(priority = ListenerPriority.LOW)
    public void onDiscordMessagePostProcess(DiscordGuildMessagePostProcessEvent event) {
        event.setCancelled(true);

        for (ChatChannel channel : this.api.getChannels()) {
            if (channel.getDiscordChannel() == null) continue;
            if (!channel.getDiscordChannel().equalsIgnoreCase(this.api.getDiscord().getDestinationGameChannelNameForTextChannel(event.getChannel()))) continue;
            if (channel.isMuted()) return;

            Member member = event.getMember();
            String message = event.getMessage().getContentRaw();
            String color = "#" + Integer.toHexString(member.getColor().getRGB()).substring(2);
            RoseSender sender = new RoseSender(color + member.getNickname(), "");
            MessageWrapper messageWrapper = new MessageWrapper(sender, MessageLocation.CHANNEL, channel, message);
            channel.sendFromDiscord(messageWrapper);

            return;
        }
    }
}
