package dev.rosewood.rosechat.floralapi.root.storage;

import dev.rosewood.rosechat.floralapi.root.FloralPlugin;
import dev.rosewood.rosechat.floralapi.root.utils.LocalizedText;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Friendly wrapper for FileConfiguration.
 */
public class YMLFile {

    /**
     * An instance of the File.
     */
    private File file;

    /**
     * An instance of the FileConfiguration.
     */
    private FileConfiguration config;

    /**
     * Creates a new YMLFile object.
     * Creates a new .yml file with the given name.
     * @param name The name of the file.
     */
    public YMLFile(String name) {
        FloralPlugin plugin = FloralPlugin.getInstance();
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();

        file = new File(plugin.getDataFolder(), name + ".yml");
        if (!file.exists()) plugin.saveResource(name + ".yml", false);

        config = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Sets a value in the file.
     * @param path The path to be set.
     * @param value The value to be set.
     */
    public void set(String path, Object value) {
        getConfig().set(path, value);
    }

    /**
     * Gets the default configuration section.
     * @return The default configuration section.
     */
    public ConfigurationSection getDefaultSection() {
        return getConfig().getDefaultSection();
    }

    /**
     * Gets a section from the file.
     * @param path The path of the section.
     * @return A section of the file.
     */
    public ConfigurationSection getSection(String path) {
        return getConfig().getConfigurationSection(path);
    }

    /**
     * Checks if the file contains a path.
     * @param path The path to check against.
     * @return True if the file contains the path.
     */
    public boolean contains(String path) {
        return getConfig().contains(path);
    }

    /**
     * Checks if the value is a String.
     * @param path The path to check.
     * @return True if the value is a String.
     */
    public boolean isString(String path) {
        return getConfig().isString(path);
    }

    /**
     * Checks if the value is a String List.
     * @param path The path to check.
     * @return True if the value is a String List.
     */
    public boolean isList(String path) {
        return getConfig().isList(path);
    }

    /**
     * Gets the given path as a LocalizedText object.
     * @param path The path to get the value from.
     * @return A LocalizedText object created from the value.
     */
    public LocalizedText getLocalizedText(String path) {
        return new LocalizedText(path);
    }

    /**
     * Gets a String from the file.
     * @param path The path to get the value from.
     * @return A String value from the file.
     */
    public String getString(String path) {
        return getConfig().getString(path);
    }

    /**
     * Gets a Material from the file.
     * @param path The path to get the value from.
     * @return A Material value from the path.
     */
    public Material getMaterial(String path) {
        return Material.matchMaterial(getString(path));
    }

    /**
     * Gets an Integer from the file.
     * @param path The path to get the value from.
     * @return An Integer value from the path.
     */
    public int getInt(String path) {
        return getConfig().getInt(path);
    }

    /**
     * Gets a Boolean from the file.
     * @param path The path to get the value from.
     * @return A Boolean value from the path.
     */
    public boolean getBoolean(String path) {
        return getConfig().getBoolean(path);
    }

    /**
     * Gets a Float from the file.
     * @param path The path to get the value from.
     * @return A Float value from the path.
     */
    public float getFloat(String path) {
        return (float) getConfig().getDouble(path);
    }

    /**
     * Gets a Double from the file.
     * @param path The path to get the value from.
     * @return A Double value from the path.
     */
    public double getDouble(String path) {
        return getConfig().getDouble(path);
    }

    /**
     * Gets a Long from the file.
     * @param path The path to get the value from.
     * @return A Long value from the path.
     */
    public long getLong(String path) {
        return getConfig().getLong(path);
    }

    /**
     * Gets a String List value from the file.
     * @param path The path to get the value from.
     * @return A List<String> value from the path.
     */
    public List<String> getStringList(String path) {
        return getConfig().getStringList(path);
    }

    /**
     * Gets an Integer List value from the file.
     * @param path The path to get the value from.
     * @return A List<Integer> value from the path.
     */
    public List<Integer> getIntList(String path) {
        return getConfig().getIntegerList(path);
    }

    /**
     * Gets the FileConfiguration object.
     * @return The FileConfiguration object.
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Gets the File object.
     * @return The File object.
     */
    public File getFile() {
        return file;
    }

    /**
     * Saves this file.
     */
    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
