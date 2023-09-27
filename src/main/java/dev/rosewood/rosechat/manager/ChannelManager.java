package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.command.CustomCommand;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosechat.hook.channel.worldguard.WorldGuardChannel;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChannelManager extends Manager {

    private final LocaleManager localeManager;
    private final Map<String, ChannelProvider> channelProviders;
    private final Map<String, Channel> channels;
    private final List<WorldGuardChannel> worldGuardChannels;
    private Channel defaultChannel;
    private CommentedFileConfiguration channelsConfig;
    private BukkitTask worldGuardTask;

    public ChannelManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.localeManager = RoseChat.getInstance().getManager(LocaleManager.class);
        this.channelProviders = new HashMap<>();
        this.channels = new HashMap<>();
        this.worldGuardChannels = new ArrayList<>();
    }

    @Override
    public void reload() {
        File channelsFile = new File(this.rosePlugin.getDataFolder(), "channels.yml");
        if (!channelsFile.exists()) this.rosePlugin.saveResource("channels.yml", false);

        this.channelsConfig = CommentedFileConfiguration.loadConfiguration(channelsFile);
        this.registerCommands(this.channelsConfig);

        // Delay generating channels until channel providers are registered.
        Bukkit.getScheduler().runTaskLater(this.rosePlugin, () -> {
            this.generateChannels();
            if (this.channelProviders.containsKey("worldguard")) {
                long interval = Setting.WORLDGUARD_CHECK_INTERVAL.getLong();
                if (interval != 0) this.worldGuardTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.rosePlugin,
                        this::updatePlayerRegions, 0, interval);
            }
        }, 0L);
    }

    @Override
    public void disable() {
        if (this.worldGuardTask != null)
            this.worldGuardTask.cancel();
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
                    if (channel.isDefaultChannel()) this.defaultChannel = channel;
                    this.channels.put(channel.getId(), channel);

                    if (channelProvider.getSupportedPlugin().equalsIgnoreCase("WorldGuard"))
                        this.worldGuardChannels.add((WorldGuardChannel) channel);
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
            this.generateChannel(provider, id);
        }

        Map<String, List<String>> loadedChannels = new HashMap<>();

        // If a channel does not have a format, set the format to the same as the default channel.
        for (Channel channel : this.channels.values()) {
            if (channel.getFormat() == null) channel.setFormat(this.defaultChannel.getFormat());

            if (!loadedChannels.containsKey(channel.getProvider().getSupportedPlugin()))
                loadedChannels.put(channel.getProvider().getSupportedPlugin(), new ArrayList<>());

            loadedChannels.get(channel.getProvider().getSupportedPlugin()).add(channel.getId());
        }

        // Output the loaded channels
        for (String provider : loadedChannels.keySet()) {
            String channels = loadedChannels.get(provider).toString();

            this.localeManager.sendCustomMessage(Bukkit.getConsoleSender(), this.localeManager.getLocaleMessage("prefix") +
                    "&eLoaded " + provider + " channels: " + channels.substring(1, channels.length() - 1));
        }
    }

    /**
     * Generates a single channel from a provider, with the given ID.
     * @param provider The {@link ChannelProvider} to generate the channel from.
     * @param id The ID of the channel.
     */
    public void generateChannel(ChannelProvider provider, String id) {
        Class<? extends Channel> base = provider.getChannelGenerator();

        if (base != null) {
            try {
                Channel channel = base.getDeclaredConstructor(ChannelProvider.class).newInstance(provider);
                channel.onLoad(id, this.channelsConfig.getConfigurationSection(id));
                if (channel.isDefaultChannel()) this.defaultChannel = channel;
                this.channels.put(id, channel);

                if (provider.getSupportedPlugin().equalsIgnoreCase("WorldGuard"))
                    this.worldGuardChannels.add((WorldGuardChannel) channel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteChannel(String id) {
        this.channels.remove(id);
    }

    /**
     * Searches through a config file for 'commands', and registers those commands.
     * @param config The {@link CommentedFileConfiguration} to search through.
     */
    public void registerCommands(CommentedFileConfiguration config) {
        for (String id : config.getKeys(false)) {
            if (config.contains(id + ".commands")) {
                for (String command : config.getStringList(id + ".commands")) {
                    this.registerCommand(command);
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
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            commandMap.register(command, new CustomCommand(command));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the region that players are located in.
     */
    public void updatePlayerRegions() {
        for (Player player : Bukkit.getOnlinePlayers()) {

            for (WorldGuardChannel channel : this.getWorldGuardChannels()) {
                // If the player is in the channel and not in the region, then kick them from the channel.
                if (channel.getMembers().contains(player.getUniqueId()) && !channel.isInWhitelistedRegion(player)) {
                    channel.kick(player.getUniqueId());
                    break;
                }

                if (!channel.getMembers().contains(player.getUniqueId()) && channel.isInWhitelistedRegion(player)) {
                    channel.forceJoin(player.getUniqueId());
                    this.localeManager.sendMessage(player,
                            "command-channel-joined", StringPlaceholders.of("id", channel.getId()));
                    break;
                }
            }
        }
    }

    public Map<String, ChannelProvider> getChannelProviders() {
        return this.channelProviders;
    }

    public Map<String, Channel> getChannels() {
        return this.channels;
    }

    public Channel getChannel(String id) {
        return this.channels.get(id);
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

    public List<WorldGuardChannel> getWorldGuardChannels() {
        return this.worldGuardChannels;
    }

}
