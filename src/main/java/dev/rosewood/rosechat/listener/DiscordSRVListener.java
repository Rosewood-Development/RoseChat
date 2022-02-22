package dev.rosewood.rosechat.listener;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class DiscordSRVListener implements Listener {

    private RoseChatAPI api;

    public DiscordSRVListener() {
        this.api = RoseChatAPI.getInstance();
    }

    @Subscribe(priority = ListenerPriority.LOW)
    public void onDiscordMessagePostProcess(DiscordGuildMessagePostProcessEvent event) {
        event.setCancelled(true);

        for (ChatChannel channel : this.api.getChannels()) {
            if (channel.getDiscordChannel() == null) continue;
            if (!channel.getDiscordChannel().equalsIgnoreCase(DiscordSRV.getPlugin().getDestinationGameChannelNameForTextChannel(event.getChannel()))) continue;
            if (channel.isMuted()) return;

            Member member = event.getMember();

            StringPlaceholders placeholders = StringPlaceholders.builder()
                    .addPlaceholder("user_nickname", member.getNickname() == null ? member.getUser().getName() : member.getNickname())
                    .addPlaceholder("user_name", member.getUser().getName())
                    .addPlaceholder("user_role", member.getRoles().isEmpty() ? "" : member.getRoles().get(0).getName())
                    .addPlaceholder("user_color", "#" + this.getColor(member))
                    .addPlaceholder("user_tag", member.getUser().getDiscriminator()).build();

            String message = event.getMessage().getContentRaw();
            Bukkit.broadcastMessage(event.getMessage().getContentStripped() + " / ");
            RoseSender sender = new RoseSender(member.getEffectiveName(), "default");
            sender.setDisplayName(member.getNickname());

            MessageWrapper messageWrapper = new MessageWrapper(sender, MessageLocation.CHANNEL, channel, message, placeholders);
            channel.sendFromDiscord(event.getMessage().getId(), messageWrapper);

            return;
        }
    }

    private String getColor(Member member) {
        if (member.getColor() != null) return Integer.toHexString(member.getColorRaw());
        if (member.getRoles().isEmpty()) return "FFFFFF";

        for (Role role : member.getRoles()) {
            if (role.getColor() != null) return Integer.toHexString(role.getColorRaw());
        }

        return "FFFFFF";
    }
}
