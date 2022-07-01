package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.command.CustomCommand;
import dev.rosewood.rosechat.message.ChatPreviewHandler;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosegarden.utils.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelManager extends Manager {

    private final LocaleManager localeManager;
    private final Map<String, ChatChannel> channels;
    private ChatChannel defaultChannel;
    private ChatPreviewHandler chatPreviewHandler;

    public ChannelManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.channels = new HashMap<>();
        this.localeManager = this.rosePlugin.getManager(LocaleManager.class);
    }

    @Override
    public void reload() {
        this.defaultChannel = null;

        File channelsFile = new File(this.rosePlugin.getDataFolder(), "channels.yml");
        if (!channelsFile.exists()) this.rosePlugin.saveResource("channels.yml", false);

        CommentedFileConfiguration channelsConfig = CommentedFileConfiguration.loadConfiguration(channelsFile);

        for (String id : channelsConfig.getKeys(false)) {
            boolean isDefault = channelsConfig.contains(id + ".default") && channelsConfig.getBoolean(id + ".default");
            String format = channelsConfig.contains(id + ".format") ? channelsConfig.getString(id + ".format") : this.defaultChannel.getFormat();
            String command = channelsConfig.contains(id + ".command") ? channelsConfig.getString(id + ".command") : null;
            boolean visible = channelsConfig.contains(id + ".visible-anywhere") && channelsConfig.getBoolean(id + ".visible-anywhere");
            boolean joinable = !channelsConfig.contains(id + ".joinable") || channelsConfig.getBoolean(id + ".joinable");
            int radius = channelsConfig.contains(id + ".radius") ? channelsConfig.getInt(id + ".radius") : -1;
            String world = channelsConfig.contains(id + ".world") ? channelsConfig.getString(id + ".world") : null;
            boolean autoJoin = channelsConfig.contains(id + ".auto-join") && channelsConfig.getBoolean("auto-join");
            List<String> servers = channelsConfig.contains(id + ".servers") ? channelsConfig.getStringList(id + ".servers") : new ArrayList<>();
            String discord = channelsConfig.contains(id + ".discord") ? channelsConfig.getString(id + ".discord") : null;

            if (id.length() > 255) id = id.substring(255);

            ChatChannel channel = new ChatChannel(id, format, isDefault);
            channel.setCommand(command);
            channel.setRadius(radius);
            channel.setWorld(world);
            channel.setAutoJoin(autoJoin);
            channel.setVisibleAnywhere(visible);
            channel.setServers(servers);
            channel.setDiscordChannel(discord);
            channel.setJoinable(joinable);

            this.channels.put(id, channel);

            if (isDefault && this.defaultChannel == null)
                this.defaultChannel = channel;

            if (command != null) this.registerCommand(command);
            this.rosePlugin.getManager(PlaceholderManager.class).parseFormat("channel-" + id, format);
        }

        if (this.defaultChannel == null) {
            this.defaultChannel = (ChatChannel) this.channels.values().toArray()[this.channels.size() - 1];
            this.localeManager.sendCustomMessage(Bukkit.getConsoleSender(), this.localeManager.getLocaleMessage("prefix") +
                    "&eNo default chat channel was found. Using &b" + this.defaultChannel.getId() + " &eas default.");
        }

        if (NMSUtil.getVersionNumber() >= 19 && ConfigurationManager.Setting.CHAT_PREVIEW.getBoolean()) {
            this.chatPreviewHandler = new ChatPreviewHandler(this.rosePlugin);
            this.chatPreviewHandler.enable();
        }
    }

    @Override
    public void disable() {
        if (this.chatPreviewHandler != null)
            this.chatPreviewHandler.disable();
    }

    private void registerCommand(String command) {
        try {
            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap)bukkitCommandMap.get(Bukkit.getServer());
            commandMap.register(command, new CustomCommand(command));
        } catch (ReflectiveOperationException e) {
            this.localeManager.sendCustomMessage(Bukkit.getConsoleSender(), this.localeManager.getLocaleMessage("prefix") +
                    "&eThere was an issue while creating the &b" + command + " &echannel command.");
        }
    }

    public void addChannel(ChatChannel channel) {
        this.channels.put(channel.getId(), channel);
    }

    public void removeChannel(ChatChannel channel) {
        this.channels.remove(channel.getId());
    }

    public ChatChannel getChannel(String id) {
        return this.channels.get(id);
    }

    public Map<String, ChatChannel> getChannels() {
        return this.channels;
    }

    public ChatChannel getDefaultChannel() {
        return this.defaultChannel;
    }

}
