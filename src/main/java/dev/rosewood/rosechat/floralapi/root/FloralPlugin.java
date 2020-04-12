package dev.rosewood.rosechat.floralapi.root;

import dev.rosewood.rosechat.floralapi.root.storage.YMLFile;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for FloralAPI.
 * Every plugin utilising FloralAPI should extend this class.
 * It's nice to implement each module, but not necessary.
 */
public abstract class FloralPlugin extends JavaPlugin implements RootPlugin {

    public static boolean UNPAID_MODE = false;

    /**
     * An instance of this class.
     */
    private static FloralPlugin instance;

    /**
     * The files that should be used by every floral plugin.
     */
    private YMLFile configFile, languageFile;

    /**
     * Whether or not the server has PlaceholderAPI installed.
     */
    private boolean hasPlaceholderAPI;

    /**
     * Called when the plugin starts up.
     */
    public abstract void onStartUp();

    /**
     * Called when the plugin shuts down.
     */
    public abstract void onShutDown();

    /**
     * Called when the plugin is reloaded via the reload subcommand.
     */
    public abstract void onReload();

    /**
     * Called when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        instance = this;

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) hasPlaceholderAPI = true;

        reload();
        onStartUp();
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        onShutDown();
    }

    /**
     * Typically reloads the plugin files.
     * Called when the plugin is reloaded via the reload subcommand.
     */
    public void reload() {
        configFile = new YMLFile("config");
        languageFile = new YMLFile("language");
        onReload();
    }

    /**
     * Gets the title of the plugin directly from the plugin.yml file.
     * @return The title of the plugin.
     */
    public String getPluginTitle() {
        return getDescription().getName();
    }

    /**
     * Gets the display name of the plugin. Typically used for internal prefixes.
     * @return The display name of the plugin.
     */
    public abstract String getPluginName();

    /**
     * Gets whether or not the server has the PlaceholderAPI plugin installed.
     * @return True if the server has PlaceholderAPI installed.
     */
    public boolean hasPlaceholderAPI() {
        return hasPlaceholderAPI;
    }

    /**
     * Gets an instance of this class.
     * @return An instance of this class.
     */
    public static FloralPlugin getInstance() {
        return instance;
    }

    /**
     * Gets the general configuration file.
     * @return An instance of the general configuration file (config.yml).
     */
    public YMLFile getConfigFile() {
        return configFile;
    }

    /**
     * Gets the language file.
     * @return An instance of the language file (language.yml).
     */
    public YMLFile getLanguageFile() {
        return languageFile;
    }
}