package dev.rosewood.rosechat;

import dev.rosewood.rosechat.commands.CommandChannel;
import dev.rosewood.rosechat.commands.CommandHelp;
import dev.rosewood.rosechat.commands.CommandMessage;
import dev.rosewood.rosechat.commands.CommandReply;
import dev.rosewood.rosechat.commands.CommandSocialSpy;
import dev.rosewood.rosechat.commands.CommandToggleEmotes;
import dev.rosewood.rosechat.commands.CommandToggleMessages;
import dev.rosewood.rosechat.commands.CommandToggleSound;
import dev.rosewood.rosechat.database.migrations._1_Create_Tables_Data;
import dev.rosewood.rosechat.floralapi.CommandManager;
import dev.rosewood.rosechat.floralapi.CommandReload;
import dev.rosewood.rosechat.floralapi.SeniorCommandManager;
import dev.rosewood.rosechat.listeners.ChatListener;
import dev.rosewood.rosechat.listeners.PlayerListener;
import dev.rosewood.rosechat.managers.ChannelManager;
import dev.rosewood.rosechat.managers.ConfigurationManager;
import dev.rosewood.rosechat.managers.DataManager;
import dev.rosewood.rosechat.managers.LocaleManager;
import dev.rosewood.rosechat.managers.PlaceholderSettingManager;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.database.DataMigration;
import dev.rosewood.rosegarden.hook.PlaceholderAPIHook;
import dev.rosewood.rosegarden.manager.Manager;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import java.util.Arrays;
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

        CommandManager messageCommand = new CommandManager(new CommandMessage(this));
        CommandManager replyCommand = new CommandManager(new CommandReply(this));
        CommandManager socialSpyCommand = new CommandManager(new CommandSocialSpy(this));
        CommandManager toggleMessageCommand = new CommandManager(new CommandToggleMessages(this));
        CommandManager toggleSoundCommand = new CommandManager(new CommandToggleSound(this));
        CommandManager toggleEmotesCommand = new CommandManager(new CommandToggleEmotes(this));
        CommandManager channelCommand = new CommandManager(new CommandChannel(this));

        this.commandManager = (SeniorCommandManager) new SeniorCommandManager("rosechat", "/rosechat help")
                .addCommandManager(messageCommand)
                .addCommandManager(replyCommand)
                .addCommandManager(socialSpyCommand)
                .addCommandManager(toggleMessageCommand)
                .addCommandManager(toggleSoundCommand)
                .addCommandManager(toggleEmotesCommand)
                .addCommandManager(channelCommand)
                .addSubcommand(new CommandHelp(this))
                .addSubcommand(new CommandReload());

        new ChatListener(this);
        new PlayerListener(this);

        // Load data for all players currently online
        DataManager dataManager = this.getManager(DataManager.class);
        Bukkit.getOnlinePlayers().forEach(x -> dataManager.getPlayerData(x.getUniqueId(), data -> {}));
    }

    @Override
    public void disable() {

    }

    @Override
    protected List<Class<? extends Manager>> getManagerLoadPriority() {
        return Arrays.asList(
                PlaceholderSettingManager.class,
                ChannelManager.class,
                DataManager.class
        );
    }

    @Override
    public List<Class<? extends DataMigration>> getDataMigrations() {
        return Arrays.asList(
                _1_Create_Tables_Data.class
        );
    }

    private void initHooks() {
        LocaleManager localeManager = this.getManager(LocaleManager.class);

        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Permission> provider = getServer().getServicesManager().getRegistration(Permission.class);
            this.vault = provider.getProvider();
        } else {
            localeManager.sendCustomMessage(Bukkit.getConsoleSender(), localeManager.getLocaleMessage("prefix") +
                    "&eVault was not found! Group placeholders will be disabled.");
        }

        if (!PlaceholderAPIHook.enabled())
            localeManager.sendCustomMessage(Bukkit.getConsoleSender(), localeManager.getLocaleMessage("prefix") +
                    "&ePlaceholderAPI was not found! Only RoseChat placeholders will work.");
    }

    public Permission getVault() {
        return vault;
    }

    public SeniorCommandManager getCommandManager() {
        return commandManager;
    }

    public static RoseChat getInstance() {
        return instance;
    }
}
