package dev.rosewood.rosechat.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.DeletableMessage;
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
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageUpdateEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class DiscordSRVListener extends ListenerAdapter implements Listener {

    private final RoseChatAPI api;
    private final DiscordSRV discord;
    private final Cache<UUID, String> cachedNicknames;

    public DiscordSRVListener() {
        this.api = RoseChatAPI.getInstance();
        this.discord = DiscordSRV.getPlugin();
        this.cachedNicknames = CacheBuilder.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).build();
    }

    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        RoseChatAPI api = RoseChatAPI.getInstance();
        List<PlayerData> updatePlayers = new ArrayList<>();
        api.getDataManager().getPlayerData().forEach(((uuid, playerData) -> {
            for (DeletableMessage deletableMessage : playerData.getMessageLog().getDeletableMessages()) {
                if (!deletableMessage.getDiscordId().equals(event.getMessageId())) continue;
                updatePlayers.add(playerData);
                return;
            }
        }));

        processMessage(event.getChannel(), event.getMember(), event.getMessage(), true, updatePlayers);
    }

    @Subscribe(priority = ListenerPriority.LOW)
    public void onDiscordMessagePostProcess(DiscordGuildMessagePostProcessEvent event) {
        event.setCancelled(true);
        processMessage(event.getChannel(), event.getMember(), event.getMessage(), false, null);
    }

    public void processMessage(TextChannel discordChannel, Member member, Message message, boolean update, List<PlayerData> updateFor) {
        for (ChatChannel channel : this.api.getChannels()) {
            if (channel.getDiscordChannel() == null) continue;
            if (!channel.getDiscordChannel().equals(this.discord.getDestinationGameChannelNameForTextChannel(discordChannel))) continue;
            if (channel.isMuted()) return;

            UUID uuid = this.discord.getAccountLinkManager().getUuid(member.getId());
            StringPlaceholders.Builder placeholders = StringPlaceholders.builder()
                    .addPlaceholder("user_name", member.getUser().getName())
                    .addPlaceholder("user_role", member.getRoles().isEmpty() ? "" : member.getRoles().get(0).getName())
                    .addPlaceholder("user_color", "#" + getColor(member))
                    .addPlaceholder("user_tag", member.getUser().getDiscriminator());

            // If not using the setting, or the player has never joined, use their discord name.
            if (!Setting.USE_IGN_WITH_DISCORD.getBoolean() || uuid == null) {
                createMessage(message, null, member.getEffectiveName(), channel, placeholders, update, updateFor);
                return;
            }

            // Use the player's nickname if they're online.
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                PlayerData data = this.api.getPlayerData(player.getUniqueId());
                createMessage(message, player, data.getNickname(), channel, placeholders, update, updateFor);
                return;
            }

            // If the cache contains a name, use it.
            String cachedNickname = this.cachedNicknames.getIfPresent(uuid);
            if (cachedNickname != null) {
                createMessage(message, Bukkit.getOfflinePlayer(uuid), cachedNickname, channel, placeholders, update, updateFor);
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
                } catch (ExecutionException e) {
                    name = member.getEffectiveName();
                }

                createMessage(message, Bukkit.getOfflinePlayer(uuid), name, channel, placeholders, update, updateFor);
            });

            return;
        }
    }

    private void createMessage(Message message, OfflinePlayer offlinePlayer, String name, ChatChannel channel, StringPlaceholders.Builder placeholders, boolean update, List<PlayerData> updateFor) {
        // TODO: Parse Discord Formatting
        //String parsedMessage = ComponentColorizer.parseDiscordFormatting(message.getContentRaw());
        StringBuilder messageBuilder = new StringBuilder(this.api.getDiscordEmojiManager().unformatUnicode(message.getContentRaw()));
        RoseSender sender = (offlinePlayer == null ? new RoseSender(name, "default") : new RoseSender(offlinePlayer));

        // Add all attachments.
        for (Message.Attachment attachment : message.getAttachments())
            messageBuilder.append("\n").append(attachment.getUrl());

        String[] lines = messageBuilder.toString().split("\n");
        int index = 0;
        for (String line : lines) {
            index++;

            if (index > Setting.DISCORD_MESSAGE_LIMIT.getInt()) return;
            if (!MessageUtils.isMessageEmpty(line)) {
                MessageWrapper messageWrapper = new MessageWrapper(sender, MessageLocation.CHANNEL, channel, line, placeholders
                        .addPlaceholder("user_nickname", name).build());

                if (Setting.REQUIRE_PERMISSIONS.getBoolean()) messageWrapper.filter().applyDefaultColor();

                if (!messageWrapper.canBeSent() && Setting.DELETE_BLOCKED_MESSAGES.getBoolean()) {
                    message.delete().queue();
                    return;
                }

                if (update) {
                    for (PlayerData playerData : updateFor) {
                        Player player = Bukkit.getPlayer(playerData.getUUID());
                        if (player == null) continue;

                        messageWrapper.setShouldLogMessages(false);
                        for (DeletableMessage deletableMessage : playerData.getMessageLog().getDeletableMessages()) {
                            if (!deletableMessage.getDiscordId().equals(message.getId())) continue;
                            messageWrapper.setId(deletableMessage.getUUID());
                            BaseComponent[] components = messageWrapper.parseFromDiscord(message.getId(), Setting.DISCORD_TO_MINECRAFT_FORMAT.getString(), new RoseSender(player));
                            deletableMessage.setJson(ComponentSerializer.toString(components));
                            break;
                        }

                        for (int i = 0; i < 100; i++) player.sendMessage("\n");
                        for (DeletableMessage deletableMessage : playerData.getMessageLog().getDeletableMessages())
                            player.spigot().sendMessage(ComponentSerializer.parse(deletableMessage.getJson()));
                    }
                } else {
                    channel.sendFromDiscord(message.getId(), messageWrapper);
                    BaseComponent[] messageComponents = messageWrapper.toComponents();
                    if (messageComponents != null) Bukkit.getConsoleSender().spigot().sendMessage(messageComponents);
                }
            }
        }
    }

    public static String getColor(Member member) {
        if (member.getColor() != null) return Integer.toHexString(member.getColorRaw());
        if (member.getRoles().isEmpty()) return "FFFFFF";

        for (Role role : member.getRoles()) {
            if (role.getColor() != null) return Integer.toHexString(role.getColorRaw());
        }

        return "FFFFFF";
    }

}
