package dev.rosewood.rosechat;

import dev.rosewood.rosechat.commands.CommandMessage;
import dev.rosewood.rosechat.commands.CommandReply;
import dev.rosewood.rosechat.floralapi.root.FloralPlugin;
import dev.rosewood.rosechat.floralapi.root.command.CommandManager;
import dev.rosewood.rosechat.floralapi.root.command.CommandReload;
import dev.rosewood.rosechat.floralapi.root.command.SeniorCommandManager;
import dev.rosewood.rosechat.floralapi.root.storage.YMLFile;
import dev.rosewood.rosechat.floralapi.root.utils.LocalizedText;
import dev.rosewood.rosechat.listeners.ChatListener;
import dev.rosewood.rosechat.listeners.PlayerListener;
import dev.rosewood.rosechat.managers.ChannelManager;
import dev.rosewood.rosechat.managers.DataManager;
import dev.rosewood.rosechat.managers.PlaceholderManager;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.RegisteredServiceProvider;

public class RoseChat extends FloralPlugin {

    private static RoseChat instance;
    private Permission vault;
    private YMLFile dataFile;
    private PlaceholderManager placeholderManager;
    private ChannelManager channelManager;
    private DataManager dataManager;

    @Override
    public void onStartUp() {
        initHooks();

        CommandManager messageCommand = new CommandManager(new CommandMessage(this));
        CommandManager replyCommand = new CommandManager(new CommandReply(this));

        new SeniorCommandManager("rosechat", "/rosechat help")
                .addCommandManager(messageCommand)
                .addCommandManager(replyCommand)
                .addSubcommand(new CommandReload());

        this.dataFile = new YMLFile("data");

        new ChatListener(this);
        new PlayerListener(this);
        this.dataManager = new DataManager();
    }

    @Override
    public void onShutDown() {

    }

    @Override
    public void onReload() {
        instance = this;
        this.placeholderManager = new PlaceholderManager(this);
        this.channelManager = new ChannelManager(this);
    }

    @Override
    public String getPluginName() {
        return "&8[&cRosechat&8]";
    }

    private void initHooks() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Permission> provider = getServer().getServicesManager().getRegistration(Permission.class);
            this.vault = provider.getProvider();
        } else {
            new LocalizedText(getPluginName() + " &cVault was not found! Group placeholders will be disabled.").sendConsoleMessage();
        }

        if (!hasPlaceholderAPI())
            new LocalizedText(getPluginName() + " &ePlaceholderAPI was not found! Only RoseChat placeholders will work.").sendConsoleMessage();
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public YMLFile getDataFile() {
        return dataFile;
    }

    public Permission getVault() {
        return vault;
    }

    public static RoseChat getInstance() {
        return instance;
    }
}
