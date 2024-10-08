package dev.rosewood.rosechat.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.chat.channel.ChannelMessageOptions;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.DeletableMessage;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.MessageRules;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosechat.placeholder.DefaultPlaceholders;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageDeleteEvent;
import github.scarsz.discordsrv.dependencies.jda.api.events.message.guild.GuildMessageUpdateEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
        if (!Settings.USE_DISCORD.get() || !Settings.EDIT_DISCORD_MESSAGES.get() || Settings.SUPPORT_THIRD_PARTY_PLUGINS.get())
            return;

        RoseChatAPI api = RoseChatAPI.getInstance();
        List<PlayerData> updatePlayers = new ArrayList<>();
        api.getPlayerDataManager().getPlayerData().forEach(((uuid, playerData) -> {
            for (DeletableMessage deletableMessage : playerData.getMessageLog().getDeletableMessages()) {
                if (deletableMessage.getDiscordId() != null && !deletableMessage.getDiscordId().equals(event.getMessageId()))
                    continue;

                updatePlayers.add(playerData);
                return;
            }
        }));

        this.processMessage(event.getChannel(), event.getMember(), event.getMessage(), true, updatePlayers);
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
        if (!Settings.ENABLE_DELETING_MESSAGES.get() || !Settings.USE_DISCORD.get() || Settings.SUPPORT_THIRD_PARTY_PLUGINS.get())
            return;

        RoseChatAPI api = RoseChatAPI.getInstance();
        List<PlayerData> updatePlayers = new ArrayList<>();
        AtomicReference<UUID> deletableMessageUUID = new AtomicReference<>();
        api.getPlayerDataManager().getPlayerData().forEach(((uuid, playerData) -> {
            for (DeletableMessage deletableMessage : playerData.getMessageLog().getDeletableMessages()) {
                if (deletableMessage.isClient())
                    continue;

                if (deletableMessage.getDiscordId() != null && !deletableMessage.getDiscordId().equals(event.getMessageId()))
                    continue;

                updatePlayers.add(playerData);
                deletableMessageUUID.set(deletableMessage.getUUID());
            }
        }));

        if (deletableMessageUUID.get() == null)
            return;

        for (PlayerData data : updatePlayers) {
            Player player = Bukkit.getPlayer(data.getUUID());
            if (player == null)
                continue;

            api.deleteMessage(new RosePlayer(player), deletableMessageUUID.get());
        }
    }

    @Subscribe(priority = ListenerPriority.LOW)
    public void onDiscordMessagePostProcess(DiscordGuildMessagePostProcessEvent event) {
        if (!Settings.USE_DISCORD.get())
            return;

        event.setCancelled(true);
        Bukkit.getScheduler().runTaskAsynchronously(RoseChat.getInstance(), () -> {
            this.processMessage(event.getChannel(), event.getMember(), event.getMessage(), false, null);
        });
    }

    public void processMessage(TextChannel discordChannel, Member member, Message message, boolean update, List<PlayerData> updateFor) {
        if (member == null)
            return;

        for (Channel channel : this.api.getChannels()) {
            if (channel.getSettings().getDiscord() == null)
                continue;

            if (!channel.getSettings().getDiscord().equals(this.discord.getDestinationGameChannelNameForTextChannel(discordChannel)))
                continue;

            if (channel.isMuted())
                return;

            UUID uuid = this.discord.getAccountLinkManager().getUuid(member.getId());
            String color = getColor(member);
            StringPlaceholders.Builder placeholders = StringPlaceholders.builder()
                    .add("user_name", member.getUser().getName())
                    .add("user_role", member.getRoles().isEmpty() ? "" : member.getRoles().get(0).getName())
                    .add("user_color", "#" + (color.length() == 5 ? "0" + color : color))
                    .add("user_tag", member.getUser().getDiscriminator());

            // If not using the setting, or the player has never joined, use their discord name.
            if (uuid == null) {
                this.createMessage(message, null, member.getEffectiveName(), channel, placeholders, update, updateFor);
                return;
            }

            // Use the player's nickname if they're online.
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                PlayerData data = this.api.getPlayerData(player.getUniqueId());

                String name = data.getNickname() == null ? player.getDisplayName() : data.getNickname();
                name = Settings.USE_IGN_WITH_DISCORD.get() ? name : member.getEffectiveName();

                this.createMessage(message, player, name, channel, placeholders, update, updateFor);
                return;
            }

            // If the cache contains a name, use it.
            String cachedNickname = this.cachedNicknames.getIfPresent(uuid);
            if (cachedNickname != null) {
                this.createMessage(message, Bukkit.getOfflinePlayer(uuid), cachedNickname, channel, placeholders, update, updateFor);
                return;
            }

            // If the cache doesn't contain a nickname, get it from the PlayerData.
            this.api.getPlayerDataManager().getPlayerData(uuid, (data) -> {
                String name;

                try {
                    name = this.cachedNicknames.get(uuid, () -> {
                       if (data.getNickname() != null)
                           return data.getNickname();

                       return Bukkit.getOfflinePlayer(uuid).getName();
                    });
                } catch (Exception e) {
                    name = member.getEffectiveName();
                }

                this.createMessage(message, Bukkit.getOfflinePlayer(uuid), name, channel, placeholders, update, updateFor);
            });

            return;
        }
    }

    private void createMessage(Message message, OfflinePlayer offlinePlayer, String name, Channel channel, StringPlaceholders.Builder placeholders, boolean update, List<PlayerData> updateFor) {
        StringBuilder messageBuilder = new StringBuilder(this.api.getDiscordEmojiManager().unformatUnicode(message.getContentRaw()));
        RosePlayer sender = (offlinePlayer == null ? new RosePlayer(name, true) : new RosePlayer(offlinePlayer));
        sender.setDiscordProxy(true);

        // Add all attachments.
        for (Message.Attachment attachment : message.getAttachments())
            messageBuilder.append("\n").append(attachment.getUrl());

        String[] lines = messageBuilder.toString().split("\n");
        int index = 0;
        for (String line : lines) {
            index++;

            if (index > Settings.DISCORD_MESSAGE_LIMIT.get())
                return;

            if (!MessageUtils.isMessageEmpty(line)) {
                RoseMessage messageWrapper = RoseMessage.forChannel(sender, channel);
                messageWrapper.setPlaceholders(placeholders.add("user_nickname", name).build());

                MessageRules rules = new MessageRules();
                if (Settings.REQUIRE_PERMISSIONS.get()) {
                    // Don't count the message as spam if the message is being edited.
                    if (update)
                        rules.applyCapsFilter().applyURLFilter().applyLanguageFilter();

                    else rules.applyAllFilters();
                }

                MessageRules.RuleOutputs outputs = rules.apply(messageWrapper, line);
                if (outputs.isBlocked() && Settings.DELETE_BLOCKED_MESSAGES.get()) {
                    message.delete().queue();
                    return;
                }

                messageWrapper.setPlayerInput(outputs.getFilteredMessage());

                if (update) {
                    BaseComponent[] consoleComponents = null;

                    for (PlayerData playerData : updateFor) {
                        Player player = Bukkit.getPlayer(playerData.getUUID());
                        if (player == null)
                            continue;
                        
                        rules.ignoreMessageLogging();

                        for (DeletableMessage deletableMessage : playerData.getMessageLog().getDeletableMessages()) {
                            if (deletableMessage.getDiscordId() == null || !deletableMessage.getDiscordId().equals(message.getId()))
                                continue;

                            messageWrapper.setUUID(deletableMessage.getUUID());
                            BaseComponent[] components = messageWrapper.parseMessageFromDiscord(new RosePlayer(player),
                                    channel.getSettings().getFormats().get("discord-to-minecraft"), message.getId()).content();
                            components = this.appendEdited(components, deletableMessage, new RosePlayer(player));
                            deletableMessage.setJson(ComponentSerializer.toString(components));

                            // Set original to null for the new delete button.
                            deletableMessage.setOriginal(null);

                            if (consoleComponents == null)
                                consoleComponents = components;
                            break;
                        }

                        for (int i = 0; i < 100; i++)
                            player.sendMessage("\n");

                        for (DeletableMessage deletableMessage : playerData.getMessageLog().getDeletableMessages())
                            player.spigot().sendMessage(ComponentSerializer.parse(deletableMessage.getJson()));
                    }

                    if (consoleComponents != null)
                        Bukkit.getConsoleSender().spigot().sendMessage(consoleComponents);
                } else {
                    String discordFormat = channel.getSettings().getFormats().get("discord-to-minecraft");
                    String format = discordFormat != null ? discordFormat : channel.getSettings().getFormats().get("chat");

                    ChannelMessageOptions options = new ChannelMessageOptions.Builder()
                            .wrapper(messageWrapper)
                            .discordId(message.getId())
                            .format(format)
                            .build();
                    channel.send(options);
                }
            }
        }
    }

    private BaseComponent[] appendEdited(BaseComponent[] components, DeletableMessage message, RosePlayer viewer) {
        RosePlayer sender = new RosePlayer(message.getSender() == null ? Bukkit.getConsoleSender() : Bukkit.getPlayer(message.getSender()));
        BaseComponent[] edited = RoseChatAPI.getInstance().parse(viewer, viewer, Settings.EDITED_DISCORD_MESSAGE_FORMAT.get(),
                DefaultPlaceholders.getFor(sender, viewer)
                        .add("type", message.isClient() ? "client" : "server")
                        .add("channel", message.getChannel())
                        .add("original", message.getOriginal() == null ?
                                null : TextComponent.toLegacyText(ComponentSerializer.parse(message.getOriginal()))).build());

        ComponentBuilder builder = new ComponentBuilder();
        builder.append(components, ComponentBuilder.FormatRetention.NONE);
        builder.append(edited);

        return builder.create();
    }

    public static String getColor(Member member) {
        if (member.getColor() != null)
            return Integer.toHexString(member.getColorRaw());

        if (member.getRoles().isEmpty())
            return "FFFFFF";

        for (Role role : member.getRoles()) {
            if (role.getColor() != null)
                return Integer.toHexString(role.getColorRaw());
        }

        return "FFFFFF";
    }

}
