package dev.rosewood.rosechat;

import dev.rosewood.rosechat.command.ChannelCommand;
import dev.rosewood.rosechat.command.ChatColorCommand;
import dev.rosewood.rosechat.command.HelpCommand;
import dev.rosewood.rosechat.command.MessageCommand;
import dev.rosewood.rosechat.command.MuteCommand;
import dev.rosewood.rosechat.command.ReloadCommand;
import dev.rosewood.rosechat.command.ReplyCommand;
import dev.rosewood.rosechat.command.SocialSpyCommand;
import dev.rosewood.rosechat.command.ToggleEmojiCommand;
import dev.rosewood.rosechat.command.ToggleMessageCommand;
import dev.rosewood.rosechat.command.ToggleSoundCommand;
import dev.rosewood.rosechat.command.api.CommandManager;
import dev.rosewood.rosechat.command.api.SeniorCommandManager;
import dev.rosewood.rosechat.command.chat.ChatCommandManager;
import dev.rosewood.rosechat.command.chat.ClearChatCommand;
import dev.rosewood.rosechat.command.chat.MoveChatCommand;
import dev.rosewood.rosechat.command.chat.MuteChatCommand;
import dev.rosewood.rosechat.command.chat.SudoChatCommand;
import dev.rosewood.rosechat.command.group.AcceptGroupCommand;
import dev.rosewood.rosechat.command.group.CreateGroupCommand;
import dev.rosewood.rosechat.command.group.DenyGroupCommand;
import dev.rosewood.rosechat.command.group.DisbandGroupCommand;
import dev.rosewood.rosechat.command.group.GroupCommandManager;
import dev.rosewood.rosechat.command.group.InviteGroupCommand;
import dev.rosewood.rosechat.command.group.KickGroupCommand;
import dev.rosewood.rosechat.command.group.LeaveGroupCommand;
import dev.rosewood.rosechat.command.group.MembersGroupCommand;
import dev.rosewood.rosechat.command.group.MessageGroupCommand;
import dev.rosewood.rosechat.command.group.RenameGroupCommand;
import dev.rosewood.rosechat.database.migrations._1_Create_Tables_Data;
import dev.rosewood.rosechat.listener.ChatListener;
import dev.rosewood.rosechat.listener.PlayerListener;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosechat.manager.ConfigurationManager;
import dev.rosewood.rosechat.manager.DataManager;
import dev.rosewood.rosechat.manager.GroupManager;
import dev.rosewood.rosechat.manager.LocaleManager;
import dev.rosewood.rosechat.manager.PlaceholderSettingManager;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.database.DataMigration;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.manager.Manager;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RoseChat extends RosePlugin {

    private static RoseChat instance;
    private SeniorCommandManager commandManager;
    private Permission vault;

    public RoseChat() {
        super(-1, 5608, ConfigurationManager.class, DataManager.class, LocaleManager.class);
        instance = this;
    }

    @Override
    public void enable() {
        initHooks();

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

        GroupCommandManager groupCommand = (GroupCommandManager) new GroupCommandManager("gc", "/gc help")
                .addSubcommand(new CreateGroupCommand())
                .addSubcommand(new InviteGroupCommand())
                .addSubcommand(new KickGroupCommand())
                .addSubcommand(new AcceptGroupCommand())
                .addSubcommand(new DenyGroupCommand())
                .addSubcommand(new LeaveGroupCommand())
                .addSubcommand(new DisbandGroupCommand())
                .addSubcommand(new MembersGroupCommand())
                .addSubcommand(new RenameGroupCommand());

        ChatCommandManager chatCommand = (ChatCommandManager) new ChatCommandManager("chat", "/chat help")
                .addSubcommand(new MuteChatCommand())
                .addSubcommand(new ClearChatCommand())
                .addSubcommand(new MoveChatCommand())
                .addSubcommand(new SudoChatCommand());

        this.commandManager = (SeniorCommandManager) new SeniorCommandManager("rosechat", "/rosechat help")
                .addCommandManager(messageCommand)
                .addCommandManager(replyCommand)
                .addCommandManager(socialSpyCommand)
                .addCommandManager(toggleMessageCommand)
                .addCommandManager(toggleSoundCommand)
                .addCommandManager(toggleEmojiCommand)
                .addCommandManager(chatColorCommand)
                .addCommandManager(muteCommand)
                .addCommandManager(channelCommand)
                .addCommandManager(chatCommand)
                .addCommandManager(groupCommand)
                .addCommandManager(groupChatMessageCommand)
                .addSubcommand(new HelpCommand(this))
                .addSubcommand(new ReloadCommand());

        // Register Listeners
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new ChatListener(this), this);
        pluginManager.registerEvents(new PlayerListener(this), this);

        // Load data for all players currently online
        DataManager dataManager = this.getManager(DataManager.class);
        Bukkit.getOnlinePlayers().forEach(player -> {
            dataManager.getPlayerData(player.getUniqueId(), data -> {});
        });

        // Load all group chats.
        GroupManager groupManager = this.getManager(GroupManager.class);
        //
    }

    @Override
    public void disable() {

    }

    @Override
    protected List<Class<? extends Manager>> getManagerLoadPriority() {
        return Arrays.asList(
                PlaceholderSettingManager.class,
                ChannelManager.class,
                DataManager.class,
                GroupManager.class
        );
    }

    @Override
    public List<Class<? extends DataMigration>> getDataMigrations() {
        return Collections.singletonList(
                _1_Create_Tables_Data.class
        );
    }

    private void initHooks() {
        LocaleManager localeManager = this.getManager(LocaleManager.class);

        if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Permission> provider = getServer().getServicesManager().getRegistration(Permission.class);
            if (provider != null) this.vault = provider.getProvider();
        } else {
            localeManager.sendCustomMessage(Bukkit.getConsoleSender(), localeManager.getLocaleMessage("prefix") +
                    "&eVault was not found! Group placeholders will be disabled.");
        }

        if (!PlaceholderAPIHook.enabled())
            localeManager.sendCustomMessage(Bukkit.getConsoleSender(), localeManager.getLocaleMessage("prefix") +
                    "&ePlaceholderAPI was not found! Only RoseChat placeholders will work.");
    }

    public Permission getVault() {
        return this.vault;
    }

    public SeniorCommandManager getCommandManager() {
        return this.commandManager;
    }

    public static RoseChat getInstance() {
        return instance;
    }
}
