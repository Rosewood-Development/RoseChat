package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.CustomCommand;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ChannelManager extends Manager {

    private final LocaleManager localeManager;
    private final Map<String, ChannelProvider> channelProviders;
    private final Map<String, Channel> channels;
    private Channel defaultChannel;
    private CommentedFileConfiguration channelsConfig;

    public ChannelManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.localeManager = RoseChat.getInstance().getManager(LocaleManager.class);
        this.channelProviders = new HashMap<>();
        this.channels = new HashMap<>();
    }

    @Override
    public void reload() {
        File channelsFile = new File(this.rosePlugin.getDataFolder(), "channels.yml");
        if (!channelsFile.exists()) this.rosePlugin.saveResource("channels.yml", false);

        this.channelsConfig = CommentedFileConfiguration.loadConfiguration(channelsFile);

        // Delay generating channels until channel providers are registered.
        Bukkit.getScheduler().runTaskLater(this.rosePlugin, this::generateChannels, 40L);
    }

    @Override
    public void disable() {

    }

    /**
     * Registers a {@link ChannelProvider} and automatically creates the files.
     * @param channelProvider The {@link ChannelProvider} to register.
     */
    public void register(ChannelProvider channelProvider) {
        this.channelProviders.put(channelProvider.getSupportedPlugin().toLowerCase(), channelProvider);
        this.createProviderChannels(channelProvider);
    }

    private void createProviderChannels(ChannelProvider channelProvider) {
        try {
            if (channelProvider.getChannels() != null) {
                for (Class<? extends Channel> channelClass : channelProvider.getChannels()) {
                    Channel channel = channelClass.getDeclaredConstructor(ChannelProvider.class).newInstance(channelProvider);
                    channel.onLoad();
                    this.channels.put(channel.getId(), channel);
                    this.localeManager.sendCustomMessage(Bukkit.getConsoleSender(), this.localeManager.getLocaleMessage("prefix") +
                            "&eGenerated " + channelProvider.getSupportedPlugin() + " channel: " + channel.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates channels from the RoseChat channels.yml file.
     * * The {@link Channel#onLoad()} method is called when the channel loads.
     * Uses the registered {@link ChannelProvider}s to decide channel configuration.
     */
    public void generateChannels() {
        this.channels.clear();
        this.defaultChannel = null;

        // Create the channels that should be created by the providers.
        for (ChannelProvider channelProvider : this.channelProviders.values())
            this.createProviderChannels(channelProvider);

        for (String id : this.channelsConfig.getKeys(false)) {
            String plugin = this.channelsConfig.contains(id + ".plugin") ? this.channelsConfig.getString(id + ".plugin") : "RoseChat";

            if (!this.channelProviders.containsKey(plugin.toLowerCase())) {
                this.localeManager.sendCustomMessage(Bukkit.getConsoleSender(), this.localeManager.getLocaleMessage("prefix") +
                        "&eAttempted to load " + plugin + " channel '" + id + "' but " + plugin + " is not installed!");
                continue;
            }

            // If the plugin is installed, generate channels based off of the provider.
            ChannelProvider provider = this.channelProviders.get(plugin.toLowerCase());
            Class<? extends Channel> base = provider.getChannelGenerator();

            if (base != null) {
                try {
                    Channel channel = base.getDeclaredConstructor(ChannelProvider.class).newInstance(provider);
                    channel.onLoad(id, this.channelsConfig.getConfigurationSection(id));
                    this.channels.put(id, channel);
                    this.localeManager.sendCustomMessage(Bukkit.getConsoleSender(), this.localeManager.getLocaleMessage("prefix") +
                            "&eLoaded " + provider.getSupportedPlugin() + " channel: " + channel.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Registers a command alias for a channel.
     * @param command The command to register.
     */
    public void registerCommand(String command) {
        try {
            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap)bukkitCommandMap.get(Bukkit.getServer());
            commandMap.register(command, new CustomCommand(command));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public Map<String, ChannelProvider> getChannelProviders() {
        return this.channelProviders;
    }

    public Map<String, Channel> getChannels() {
        return this.channels;
    }

    public Channel getDefaultChannel() {
        return this.defaultChannel;
    }

    public void setDefaultChannel(Channel defaultChannel) {
        this.defaultChannel = defaultChannel;
    }

    public CommentedFileConfiguration getChannelsConfig() {
        return this.channelsConfig;
    }

}
