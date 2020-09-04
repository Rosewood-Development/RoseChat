package dev.rosewood.rosechat;

import dev.rosewood.rosechat.commands.CommandMessage;
import dev.rosewood.rosechat.commands.CommandReply;
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
import java.util.Collections;
import java.util.List;

public class RoseChat extends RosePlugin {

    private static RoseChat instance;
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

        new SeniorCommandManager("rosechat", "/rosechat help")
                .addCommandManager(messageCommand)
                .addCommandManager(replyCommand)
                .addSubcommand(new CommandReload());

        new ChatListener(this);
        new PlayerListener(this);
    }

    @Override
    public void disable() {

    }

    @Override
    protected List<Class<? extends Manager>> getManagerLoadPriority() {
        return Arrays.asList(
                DataManager.class,
                PlaceholderSettingManager.class,
                ChannelManager.class
        );
    }

    @Override
    public List<DataMigration> getDataMigrations() {
        return Collections.emptyList();
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

    public static RoseChat getInstance() {
        return instance;
    }
}
