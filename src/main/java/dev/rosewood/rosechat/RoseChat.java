package dev.rosewood.rosechat;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.message.tokenizer.replacement.HeldItemTokenizer;
import dev.rosewood.rosechat.command.ChannelCommand;
import dev.rosewood.rosechat.command.ChatColorCommand;
import dev.rosewood.rosechat.command.DebugCommand;
import dev.rosewood.rosechat.command.DeleteMessageCommand;
import dev.rosewood.rosechat.command.HelpCommand;
import dev.rosewood.rosechat.command.IgnoreCommand;
import dev.rosewood.rosechat.command.MessageCommand;
import dev.rosewood.rosechat.command.MuteCommand;
import dev.rosewood.rosechat.command.NickColorCommand;
import dev.rosewood.rosechat.command.NicknameCommand;
import dev.rosewood.rosechat.command.RealnameCommand;
import dev.rosewood.rosechat.command.ReloadCommand;
import dev.rosewood.rosechat.command.ReplyCommand;
import dev.rosewood.rosechat.command.SocialSpyCommand;
import dev.rosewood.rosechat.command.ToggleEmojiCommand;
import dev.rosewood.rosechat.command.ToggleMessageCommand;
import dev.rosewood.rosechat.command.ToggleSoundCommand;
import dev.rosewood.rosechat.command.UnmuteCommand;
import dev.rosewood.rosechat.command.api.CommandManager;
import dev.rosewood.rosechat.command.api.SeniorCommandManager;
import dev.rosewood.rosechat.command.chat.ChatCommandManager;
import dev.rosewood.rosechat.command.chat.ClearChatCommand;
import dev.rosewood.rosechat.command.chat.InfoChatCommand;
import dev.rosewood.rosechat.command.chat.MoveChatCommand;
import dev.rosewood.rosechat.command.chat.MuteChatCommand;
import dev.rosewood.rosechat.command.chat.SudoChatCommand;
import dev.rosewood.rosechat.command.chat.ToggleChatCommand;
import dev.rosewood.rosechat.command.group.AcceptGroupCommand;
import dev.rosewood.rosechat.command.group.CreateGroupCommand;
import dev.rosewood.rosechat.command.group.DenyGroupCommand;
import dev.rosewood.rosechat.command.group.DisbandGroupCommand;
import dev.rosewood.rosechat.command.group.GroupCommandManager;
import dev.rosewood.rosechat.command.group.InfoGroupCommand;
import dev.rosewood.rosechat.command.group.InviteGroupCommand;
import dev.rosewood.rosechat.command.group.KickGroupCommand;
import dev.rosewood.rosechat.command.group.LeaveGroupCommand;
import dev.rosewood.rosechat.command.group.ListGroupCommand;
import dev.rosewood.rosechat.command.group.MembersGroupCommand;
import dev.rosewood.rosechat.command.group.MessageGroupCommand;
import dev.rosewood.rosechat.command.group.RenameGroupCommand;
import dev.rosewood.rosechat.hook.RoseChatPlaceholderExpansion;
import dev.rosewood.rosechat.hook.channel.bentobox.BentoBoxChannelProvider;
import dev.rosewood.rosechat.hook.channel.fabledskyblock.FabledSkyblockChannelProvider;
import dev.rosewood.rosechat.hook.channel.factionsuuid.FactionsUUIDChannelProvider;
//import dev.rosewood.rosechat.hook.channel.iridiumskyblock.IridiumSkyblockChannelProvider;
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
import dev.rosewood.rosechat.listener.MessageListener;
import dev.rosewood.rosechat.listener.PacketListener;
import dev.rosewood.rosechat.listener.PlayerListener;
import dev.rosewood.rosechat.manager.BungeeManager;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.manager.ConfigurationManager.*;
import dev.rosewood.rosechat.manager.DataManager;
import dev.rosewood.rosechat.manager.DiscordEmojiManager;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.manager.PlaceholderManager;
import dev.rosewood.rosechat.manager.PlayerDataManager;
import dev.rosewood.rosechat.manager.ReplacementManager;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosegarden.utils.NMSUtil;
import github.scarsz.discordsrv.DiscordSRV;
import java.util.concurrent.Executors;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class RoseChat extends RosePlugin {

    public static final ExecutorService MESSAGE_THREAD_POOL = Executors.newCachedThreadPool();
    private static RoseChat instance;
    private SeniorCommandManager commandManager;
    private Permission vault;
    private DiscordChatProvider discord;
    private NicknameProvider nicknameProvider;
    private ChatListener chatListener;
    private PacketListener packetListener;

    public RoseChat() {
        super(-1, 5608, ConfigurationManager.class, DataManager.class, LocaleManager.class, null);
        instance = this;
    }

    @Override
    public void enable() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        this.initHooks(pluginManager);

        // Register Commands
        CommandManager messageCommand = new CommandManager(new MessageCommand());
        CommandManager replyCommand = new CommandManager(new ReplyCommand());
        CommandManager socialSpyCommand = new CommandManager(new SocialSpyCommand());
        CommandManager toggleMessageCommand = new CommandManager(new ToggleMessageCommand());
        CommandManager toggleSoundCommand = new CommandManager(new ToggleSoundCommand());
        CommandManager toggleEmojiCommand = new CommandManager(new ToggleEmojiCommand());
        CommandManager channelCommand = new CommandManager(new ChannelCommand());
        CommandManager chatColorCommand = new CommandManager(new ChatColorCommand());
        CommandManager groupChatMessageCommand = new CommandManager(new MessageGroupCommand());
        CommandManager muteCommand = new CommandManager(new MuteCommand());
        CommandManager unmuteCommand = new CommandManager(new UnmuteCommand());
        CommandManager nicknameCommand = new CommandManager(new NicknameCommand(this));
        CommandManager nickColorCommand = new CommandManager(new NickColorCommand(this));
        CommandManager ignoreCommand = new CommandManager(new IgnoreCommand());
        CommandManager deleteMessageCommand = new CommandManager(new DeleteMessageCommand());
        CommandManager realnameCommand = new CommandManager(new RealnameCommand());

        GroupCommandManager groupCommand = (GroupCommandManager) new GroupCommandManager("gc", "/gc help")
                .addSubcommand(new CreateGroupCommand())
                .addSubcommand(new InviteGroupCommand())
                .addSubcommand(new KickGroupCommand())
                .addSubcommand(new AcceptGroupCommand())
                .addSubcommand(new DenyGroupCommand())
                .addSubcommand(new LeaveGroupCommand())
                .addSubcommand(new DisbandGroupCommand())
                .addSubcommand(new MembersGroupCommand())
                .addSubcommand(new RenameGroupCommand())
                .addSubcommand(new InfoGroupCommand())
                .addSubcommand(new ListGroupCommand());

        ChatCommandManager chatCommand = (ChatCommandManager) new ChatCommandManager("chat", "/chat help")
                .addSubcommand(new MuteChatCommand())
                .addSubcommand(new ClearChatCommand())
                .addSubcommand(new MoveChatCommand())
                .addSubcommand(new SudoChatCommand())
                .addSubcommand(new InfoChatCommand())
                .addSubcommand(new ToggleChatCommand());

        this.commandManager = (SeniorCommandManager) new SeniorCommandManager("rosechat", "/rosechat help")
                .addCommandManager(messageCommand)
                .addCommandManager(replyCommand)
                .addCommandManager(nicknameCommand)
                .addCommandManager(nickColorCommand)
                .addCommandManager(socialSpyCommand)
                .addCommandManager(toggleMessageCommand)
                .addCommandManager(toggleSoundCommand)
                .addCommandManager(toggleEmojiCommand)
                .addCommandManager(chatColorCommand)
                .addCommandManager(ignoreCommand)
                .addCommandManager(muteCommand)
                .addCommandManager(unmuteCommand)
                .addCommandManager(channelCommand)
                .addCommandManager(chatCommand)
                .addCommandManager(groupCommand)
                .addCommandManager(groupChatMessageCommand)
                .addCommandManager(deleteMessageCommand)
                .addCommandManager(realnameCommand)
                .addSubcommand(new DebugCommand(this))
                .addSubcommand(new ReloadCommand())
                .addSubcommand(new HelpCommand(this));

        // Register Listeners
        pluginManager.registerEvents(new PlayerListener(this), this);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeListener(this));

        new HeldItemTokenizer();
    }

    @Override
    public void reload() {
        super.reload();
        try {
            EventPriority priority = EventPriority.valueOf(Setting.CHAT_EVENT_PRIORITY.getString());
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

        // Remove and add to reset the priority.
        PluginManager pluginManager = Bukkit.getPluginManager();
        if (pluginManager.isPluginEnabled("ProtocolLib") && NMSUtil.getVersionNumber() >= 17) {
            this.packetListener = new PacketListener(this);
            this.packetListener.removeListeners();
            this.packetListener.addListener();
        }
    }

    @Override
    public void disable() {
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
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
            if (provider != null && provider.getProvider().hasGroupSupport()) this.vault = provider.getProvider();
        } else {
            localeManager.sendCustomMessage(Bukkit.getConsoleSender(), localeManager.getLocaleMessage("prefix") +
                    "&eVault was not found! Group placeholders will be disabled.");
        }

        if (!PlaceholderAPIHook.enabled())
            localeManager.sendCustomMessage(Bukkit.getConsoleSender(), localeManager.getLocaleMessage("prefix") +
                    "&ePlaceholderAPI was not found! Only RoseChat placeholders will work.");
        else new RoseChatPlaceholderExpansion().register();

        if (pluginManager.isPluginEnabled("ProtocolLib") && NMSUtil.getVersionNumber() >= 17) {
            pluginManager.registerEvents(new MessageListener(), this);
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (pluginManager.isPluginEnabled("DiscordSRV")) {
                this.discord = new DiscordSRVProvider();
                DiscordSRVListener discordListener = new DiscordSRVListener();
                DiscordSRV.api.subscribe(discordListener);
                DiscordSRV.getPlugin().getJda().addEventListener(discordListener);
            }
        }, 60L);

        if (pluginManager.isPluginEnabled("Essentials")) {
            this.nicknameProvider = new EssentialsHook();
        }

        // Channel Hooks
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

//        if (pluginManager.getPlugin("IridiumSkyblock") != null)
//            new IridiumSkyblockChannelProvider().register();

        if (pluginManager.getPlugin("FabledSkyblock") != null)
            new FabledSkyblockChannelProvider().register();

        if (pluginManager.getPlugin("MarriageMaster") != null)
            new MarriageMasterChannelProvider().register();
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

    public SeniorCommandManager getCommandManager() {
        return this.commandManager;
    }

    public static RoseChat getInstance() {
        return instance;
    }

}
