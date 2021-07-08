package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.command.CustomCommand;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.rosegarden.manager.Manager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelManager extends Manager {

    private final LocaleManager localeManager;
    private final Map<String, ChatChannel> channels;
    private ChatChannel defaultChannel;

    public ChannelManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.channels = new HashMap<>();
        this.localeManager = this.rosePlugin.getManager(LocaleManager.class);
    }

    @Override
    public void reload() {
        this.defaultChannel = null;

        for (String id : Setting.CHAT_CHANNELS.getSection().getKeys(false)) {
            CommentedConfigurationSection section = Setting.CHAT_CHANNELS.getSection();

            boolean isDefault = section.contains(id + ".default") && section.getBoolean(id + ".default");
            String format = section.contains(id + ".format") ? section.getString(id + ".format") : "default-format";
            String command = section.contains(id + ".command") ? section.getString(id + ".command") : null;
            boolean visible = section.contains(id + ".visible-anywhere") && section.getBoolean(id + ".visible-anywhere");
            int radius = section.contains(id + ".radius") ? section.getInt(id + ".radius") : -1;
            String world = section.contains(id + ".world") ? section.getString(id + ".world") : null;
            boolean autoJoin = section.contains(id + ".auto-join") && section.getBoolean("auto-join");
            List<String> servers = section.contains(id + ".servers") ? section.getStringList(id + ".servers") : new ArrayList<>();
            String discord = section.contains(id + ".discord") ? section.getString(id + ".discord") : null;

            if (id.length() > 255) id = id.substring(255);

            ChatChannel channel = new ChatChannel(id, format, isDefault);
            channel.setCommand(command);
            channel.setRadius(radius);
            channel.setWorld(world);
            channel.setAutoJoin(autoJoin);
            channel.setVisibleAnywhere(visible);
            channel.setServers(servers);
            channel.setDiscordChannel(discord);

            this.channels.put(id, channel);

            if (isDefault && this.defaultChannel == null)
                this.defaultChannel = channel;

            if (command != null) this.registerCommand(command);
            this.rosePlugin.getManager(PlaceholderSettingManager.class).parseFormat("channel-" + id, format);
        }

        if (this.defaultChannel == null) {
            this.defaultChannel = (ChatChannel) this.channels.values().toArray()[this.channels.size() - 1];
            this.localeManager.sendCustomMessage(Bukkit.getConsoleSender(), this.localeManager.getLocaleMessage("prefix") +
                    "&eNo default chat channel was found. Using &b" + this.defaultChannel.getId() + " &eas default.");
        }
    }

    @Override
    public void disable() {

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
