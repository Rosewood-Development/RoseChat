package dev.rosewood.rosechat;

import dev.rosewood.rosechat.floralapi.root.FloralPlugin;
import dev.rosewood.rosechat.floralapi.root.command.CommandManager;
import dev.rosewood.rosechat.floralapi.root.command.CommandReload;
import dev.rosewood.rosechat.floralapi.root.storage.YMLFile;
import dev.rosewood.rosechat.floralapi.root.utils.LocalizedText;
import dev.rosewood.rosechat.listeners.ChatListener;
import dev.rosewood.rosechat.placeholders.PlaceholderManager;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.RegisteredServiceProvider;

public class RoseChat extends FloralPlugin {

    private static RoseChat instance;
    private Permission vault;
    private YMLFile dataFile;
    private PlaceholderManager placeholderManager;

    @Override
    public void onStartUp() {
        initHooks();

        new CommandManager("rosechat", "rosechat help")
                .addSubcommand(new CommandReload());

        dataFile = new YMLFile("data");

        new ChatListener();
    }

    @Override
    public void onShutDown() {

    }


    @Override
    public void onReload() {
        instance = this;
        placeholderManager = new PlaceholderManager();
    }

    @Override
    public String getPluginName() {
        return "&8[&cRosechat&8]";
    }

    private void initHooks() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Permission> provider = getServer().getServicesManager().getRegistration(Permission.class);
            vault = provider.getProvider();
        } else {
            new LocalizedText(getPluginName() + " &cVault was not found! Group placeholders will be disabled.").sendConsoleMessage();
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null)
            new LocalizedText(getPluginName() + " &ePlaceholderAPI was not found! Only RoseChat placeholders will work.").sendConsoleMessage();
    }

    public YMLFile getDataFile() {
        return dataFile;
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    public Permission getVault() {
        return vault;
    }

    public static RoseChat getInstance() {
        return instance;
    }
}
