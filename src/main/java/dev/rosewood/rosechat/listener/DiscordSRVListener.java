package dev.rosewood.rosechat.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.MessageWrapper;
import dev.rosewood.rosechat.message.RoseSender;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DiscordSRVListener implements Listener {

    private final RoseChatAPI api;
    private final Cache<UUID, String> cachedNicknames;

    public DiscordSRVListener() {
        this.api = RoseChatAPI.getInstance();
        this.cachedNicknames = CacheBuilder.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).build();
    }

    @Subscribe(priority = ListenerPriority.LOW)
    public void onDiscordMessagePostProcess(DiscordGuildMessagePostProcessEvent event) {
        event.setCancelled(true);

        for (ChatChannel channel : this.api.getChannels()) {
            if (channel.getDiscordChannel() == null) continue;
            if (!channel.getDiscordChannel().equalsIgnoreCase(DiscordSRV.getPlugin().getDestinationGameChannelNameForTextChannel(event.getChannel()))) continue;
            if (channel.isMuted()) return;

            Member member = event.getMember();
            UUID uuid = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(member.getId());

            StringPlaceholders.Builder placeholders = StringPlaceholders.builder()
                    .addPlaceholder("user_name", member.getUser().getName())
                    .addPlaceholder("user_role", member.getRoles().isEmpty() ? "" : member.getRoles().get(0).getName())
                    .addPlaceholder("user_color", "#" + this.getColor(member))
                    .addPlaceholder("user_tag", member.getUser().getDiscriminator());

            // If not using the setting, or the player has never joined, use their discord name.
            if (!Setting.USE_IGN_WITH_DISCORD.getBoolean() || uuid == null) {
                processMessage(null, event, member.getEffectiveName(), channel, placeholders);
                return;
            }

            // Use the player's nickname if they're online.
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                processMessage(player, event, player.getDisplayName(), channel, placeholders);
                return;
            }

            // If the cache contains a name, use it.
            String cachedNickname = this.cachedNicknames.getIfPresent(uuid);
            if (cachedNickname != null) {
                 processMessage(Bukkit.getOfflinePlayer(uuid), event, cachedNickname, channel, placeholders);
                 return;
            }

            // If the cache doesn't contain a nickname, get it from the PlayerData.
            this.api.getDataManager().getPlayerData(uuid, (data) -> {
                String name;

                try {
                    name = this.cachedNicknames.get(uuid, () -> {
                        if (data.getNickname() != null) return data.getNickname();
                        return Bukkit.getOfflinePlayer(uuid).getName();
                    });
                } catch (ExecutionException ignored) {
                    name = member.getEffectiveName();
                }

                processMessage(Bukkit.getOfflinePlayer(uuid), event, name, channel, placeholders);
            });

            return;
        }
    }

    private void processMessage(OfflinePlayer player, DiscordGuildMessagePostProcessEvent event, String name, ChatChannel channel, StringPlaceholders.Builder placeholders) {
        StringBuilder message = new StringBuilder(this.api.getDiscordEmojiManager().unformatUnicode(event.getMessage().getContentRaw()));
        RoseSender sender = player == null ? new RoseSender(name, "default") : new RoseSender(player);

        // Also send all attachments.
        for (Message.Attachment attachment : event.getMessage().getAttachments())
            message.append("\n").append(attachment.getUrl());


        String[] lines = message.toString().split("\n");
        int index = 0;
        for (String line : lines) {
            index++;
            if (index > Setting.DISCORD_MESSAGE_LIMIT.getInt()) return;
            if (!MessageUtils.isMessageEmpty(line)) {
                MessageWrapper messageWrapper = new MessageWrapper(sender, MessageLocation.CHANNEL, channel, line,
                        placeholders.addPlaceholder("user_nickname", name).build());

                if (Setting.REQUIRE_PERMISSIONS.getBoolean()) messageWrapper.validate().filter();

                if (!messageWrapper.canBeSent() && Setting.DELETE_BLOCKED_MESSAGES.getBoolean()) {
                    event.getMessage().delete().queue();
                    return;
                }

                channel.sendFromDiscord(event.getMessage().getId(), messageWrapper);
                BaseComponent[] messageComponents = messageWrapper.toComponents();
                if (messageComponents != null) Bukkit.getConsoleSender().spigot().sendMessage(messageComponents);
            }
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
