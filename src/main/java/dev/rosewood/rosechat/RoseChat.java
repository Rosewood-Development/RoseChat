package dev.rosewood.rosechat;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.log.ConsoleMessageLog;
import dev.rosewood.rosechat.chat.task.ChatLogTask;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.hook.RoseChatPlaceholderExpansion;
import dev.rosewood.rosechat.hook.channel.bentobox.BentoBoxChannelProvider;
import dev.rosewood.rosechat.hook.channel.fabledskyblock.FabledSkyblockChannelProvider;
import dev.rosewood.rosechat.hook.channel.factionsuuid.FactionsUUIDChannelProvider;
import dev.rosewood.rosechat.hook.channel.husktowns.HuskTownsChannelProvider;
import dev.rosewood.rosechat.hook.channel.kingdomsx.KingdomsXChannelProvider;
import dev.rosewood.rosechat.hook.channel.marriagemaster.MarriageMasterChannelProvider;
import dev.rosewood.rosechat.hook.channel.mcmmo.McMMOChannelProvider;
import dev.rosewood.rosechat.hook.channel.rosechat.RoseChatChannelProvider;
import dev.rosewood.rosechat.hook.channel.simpleclans.SimpleClansChannelProvider;
import dev.rosewood.rosechat.hook.channel.superiorskyblock.SuperiorSkyblockChannelProvider;
import dev.rosewood.rosechat.hook.channel.towny.TownyChannelProvider;
import dev.rosewood.rosechat.hook.channel.worldguard.WorldGuardChannelProvider;
import dev.rosewood.rosechat.hook.discord.DiscordChatProvider;
import dev.rosewood.rosechat.hook.discord.DiscordSRVProvider;
import dev.rosewood.rosechat.hook.nickname.EssentialsHook;
import dev.rosewood.rosechat.hook.nickname.NicknameProvider;
import dev.rosewood.rosechat.listener.BungeeListener;
import dev.rosewood.rosechat.listener.ChatListener;
import dev.rosewood.rosechat.listener.DiscordSRVListener;
import dev.rosewood.rosechat.listener.PacketListener;
import dev.rosewood.rosechat.listener.PlayerListener;
import dev.rosewood.rosechat.listener.SidedSignListener;
import dev.rosewood.rosechat.listener.SignListener;
import dev.rosewood.rosechat.manager.BungeeManager;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.manager.CommandManager;
import dev.rosewood.rosechat.manager.DataManager;
import dev.rosewood.rosechat.manager.DiscordEmojiManager;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.manager.PlaceholderManager;
import dev.rosewood.rosechat.manager.PlayerDataManager;
import dev.rosewood.rosechat.manager.ReplacementManager;
import dev.rosewood.rosechat.message.tokenizer.replacement.HeldItemTokenizer;
import dev.rosewood.rosechat.nms.NMSAdapter;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosegarden.utils.NMSUtil;
import github.scarsz.discordsrv.DiscordSRV;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoseChat extends RosePlugin {

    public static final ExecutorService MESSAGE_THREAD_POOL = Executors.newCachedThreadPool();
    private static RoseChat instance;
    private Permission vault;
    private DiscordChatProvider discord;
    private NicknameProvider nicknameProvider;
    private ChatListener chatListener;
    private ConsoleMessageLog consoleLog;
    private ChatLogTask chatLogTask;

    public RoseChat() {
        super(-1, 5608,
                DataManager.class,
                LocaleManager.class,
                CommandManager.class);
        instance = this;
    }

    @Override
    public void enable() {
        if (!NMSAdapter.isValidVersion()) {
            LocaleManager localeManager = RoseChatAPI.getInstance().getLocaleManager();
            localeManager.sendCustomMessage(Bukkit.getConsoleSender(), localeManager.getLocaleMessage("prefix") +
                    "&eThe [item] placeholder is not supported on this Minecraft version.");
        }

        PluginManager pluginManager = Bukkit.getPluginManager();
        this.initHooks(pluginManager);

        // Register Listeners
        pluginManager.registerEvents(new PlayerListener(this), this);

        if (NMSUtil.getVersionNumber() >= 20) {
            pluginManager.registerEvents(new SidedSignListener(this), this);
        } else {
            pluginManager.registerEvents(new SignListener(this), this);
        }

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord",
                new BungeeListener(this));

        new HeldItemTokenizer();
    }

    @Override
    public void reload() {
        super.reload();

        // Unregister and register the chat event for a configurable priority.
        try {
            EventPriority priority = Settings.CHAT_EVENT_PRIORITY.get();
            if (this.chatListener != null) {
                HandlerList.unregisterAll(this.chatListener);
            } else {
                this.chatListener = new ChatListener();
            }

            Bukkit.getPluginManager().registerEvent(AsyncPlayerChatEvent.class, chatListener, priority, (listener, event) -> {
                if (event instanceof AsyncPlayerChatEvent chatEvent) {
                    this.chatListener = (ChatListener) listener;
                    this.chatListener.onChat(chatEvent);
                }
            }, this, true);
        } catch (IllegalArgumentException e) {
            LocaleManager localeManager = RoseChatAPI.getInstance().getLocaleManager();
            localeManager.sendCustomMessage(Bukkit.getConsoleSender(), localeManager.getLocaleMessage("prefix") +
                    "&eThe chat-event-priority is not a valid EventPriority");
        }

        // Unregister and register the packet event for a configurable priority.
        PluginManager pluginManager = Bukkit.getPluginManager();
        if (pluginManager.isPluginEnabled("ProtocolLib") && NMSUtil.getVersionNumber() >= 17) {
            PacketListener packetListener = new PacketListener(this);
            packetListener.removeListeners();
            packetListener.addListener();
        }

        this.registerChannelHooks(pluginManager);

        if (Settings.SAVE_CHAT_LOG_TO_FILE.get()) {
            if (this.consoleLog != null && this.chatLogTask != null)
                this.chatLogTask.save();

            try {
                this.consoleLog = new ConsoleMessageLog();
                this.chatLogTask = new ChatLogTask(this, this.consoleLog);
            } catch (IOException e) {
                e.printStackTrace();
                Bukkit.getLogger().warning("An error occurred while creating a chat log.");
            }
        }
    }

    @Override
    public void disable() {
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);

        if (this.chatLogTask != null)
            this.chatLogTask.save();
    }

    @Override
    protected List<Class<? extends Manager>> getManagerLoadPriority() {
        return Arrays.asList(
                ChannelManager.class,
                ReplacementManager.class,
                PlaceholderManager.class,
                PlayerDataManager.class,
                GroupManager.class,
                DiscordEmojiManager.class,
                BungeeManager.class
        );
    }

    private void initHooks(PluginManager pluginManager) {
        LocaleManager localeManager = this.getManager(LocaleManager.class);

        if (pluginManager.getPlugin("Vault") != null) {
            RegisteredServiceProvider<Permission> provider = this.getServer().getServicesManager().getRegistration(Permission.class);
            if (provider != null && provider.getProvider().hasGroupSupport())
                this.vault = provider.getProvider();
        } else {
            localeManager.sendCustomMessage(Bukkit.getConsoleSender(), localeManager.getLocaleMessage("prefix") +
                    "&eVault was not found! Group placeholders will be disabled.");
        }

        if (!PlaceholderAPIHook.enabled())
            localeManager.sendCustomMessage(Bukkit.getConsoleSender(), localeManager.getLocaleMessage("prefix") +
                    "&ePlaceholderAPI was not found! Only RoseChat placeholders will work.");
        else new RoseChatPlaceholderExpansion().register();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (pluginManager.isPluginEnabled("DiscordSRV")) {
                this.discord = new DiscordSRVProvider();
                DiscordSRVListener discordListener = new DiscordSRVListener();
                DiscordSRV.api.subscribe(discordListener);
                DiscordSRV.getPlugin().getJda().addEventListener(discordListener);
            }
        }, 60L);

        if (pluginManager.isPluginEnabled("Essentials"))
            this.nicknameProvider = new EssentialsHook();
    }

    public void registerChannelHooks(PluginManager pluginManager) {
        new RoseChatChannelProvider().register();

        if (pluginManager.getPlugin("Towny") != null)
            new TownyChannelProvider().register();

        if (pluginManager.getPlugin("mcMMO") != null)
            new McMMOChannelProvider().register();

        if (pluginManager.getPlugin("WorldGuard") != null)
            new WorldGuardChannelProvider().register();

        if (pluginManager.getPlugin("SimpleClans") != null)
            new SimpleClansChannelProvider().register();

        if (pluginManager.getPlugin("Factions") != null)
            new FactionsUUIDChannelProvider().register();

        if (pluginManager.getPlugin("Kingdoms") != null)
            new KingdomsXChannelProvider().register();

        if (pluginManager.getPlugin("BentoBox") != null)
            new BentoBoxChannelProvider().register();

        if (pluginManager.getPlugin("SuperiorSkyblock2") != null)
            new SuperiorSkyblockChannelProvider().register();

        if (pluginManager.getPlugin("FabledSkyblock") != null)
            new FabledSkyblockChannelProvider().register();

        if (pluginManager.getPlugin("MarriageMaster") != null)
            new MarriageMasterChannelProvider().register();

        if (pluginManager.getPlugin("HuskTowns") != null)
            new HuskTownsChannelProvider().register();
    }

    public Permission getVault() {
        return this.vault;
    }

    public DiscordChatProvider getDiscord() {
        return this.discord;
    }

    public NicknameProvider getNicknameProvider() {
        return this.nicknameProvider;
    }

    public ConsoleMessageLog getConsoleLog() {
        return this.consoleLog;
    }

    @Override
    protected List<RoseSetting<?>> getRoseConfigSettings() {
        return Settings.getKeys();
    }

    @Override
    protected String[] getRoseConfigHeader() {
        return new String[] {
                "     __________                    _________  __            __   ",
                "     \\______   \\ ____  ______ ____ \\_   ___ \\|  |__ _____ _/  |_ ",
                "      |       _//  _ \\/  ___// __ \\/    \\  \\/|  |  \\\\__  \\\\   __\\",
                "      |    |   (  <_> )___ \\\\  ___/\\     \\___|   Y  \\/ __ \\|  |  ",
                "      |____|_  /\\____/____  >\\___  >\\______  /___|  (____  /__|  ",
                "             \\/           \\/     \\/        \\/     \\/     \\/      ",
                ""
        };
    }

    public static RoseChat getInstance() {
        return instance;
    }

}
