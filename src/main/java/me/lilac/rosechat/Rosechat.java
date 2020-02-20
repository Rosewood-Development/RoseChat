package me.lilac.rosechat;

import me.lilac.rosechat.commands.*;
import me.lilac.rosechat.listeners.EventPlayerChat;
import me.lilac.rosechat.placeholder.PlaceholderManager;
import me.lilac.rosechat.storage.ConfigManager;
import me.lilac.rosechat.storage.PlayerData;
import me.lilac.rosechat.storage.Rosefile;
import me.lilac.rosechat.storage.Settings;
import me.lilac.rosechat.utils.Methods;
import me.lilac.rosechat.chat.ChatManager;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Rosechat extends JavaPlugin {

    private static Rosechat instance;
    private Rosefile data, messages;
    private ChatManager chatManager;
    private PlaceholderManager placeholderManager;
    private ConfigManager configManager;
    private Chat chat;

    @Override
    public void onEnable() {
        instance = this;

        if (!initChat()) Methods.sendConsoleMessage("&cVault was not found!");
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
            Methods.sendConsoleMessage("&cPlaceholderAPI was not found!");

        saveDefaultConfig();
        reload();

        getServer().getPluginManager().registerEvents(new EventPlayerChat(), this);
        getCommand("broadcast").setExecutor(new CommandBroadcast());
        getCommand("message").setExecutor(new CommandMessage());
        getCommand("reply").setExecutor(new CommandReply());
        getCommand("rosechat").setExecutor(new CommandRosechat());
        getCommand("staffchat").setExecutor(new CommandStaffChat());
        getCommand("togglesound").setExecutor(new CommandToggleSound());
        getCommand("socialspy").setExecutor(new CommandSocialSpy());
        getCommand("messagetoggle").setExecutor(new CommandMessageToggle());
    }

    public void reload() {
        chatManager = new ChatManager();
        placeholderManager = new PlaceholderManager();
        PlayerData.getPlayersUsingStaffchat().clear();
        PlayerData.getPlayersWithoutSounds().clear();
        PlayerData.getPlayersWithoutMessages().clear();
        PlayerData.getPlayersUsingSocialSpy().clear();

        reloadConfig();
        data = new Rosefile("data.yml");
        messages = new Rosefile("messages.yml");
        chatManager = new ChatManager();

        configManager = new ConfigManager();
    }

    private boolean initChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    public static Rosechat getInstance() {
        return instance;
    }

    public Chat getVault() {
        return chat;
    }

    public Rosefile getData() {
        return data;
    }

    public Rosefile getMessages() {
        return messages;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
