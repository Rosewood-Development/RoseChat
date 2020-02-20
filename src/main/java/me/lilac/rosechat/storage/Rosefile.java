package me.lilac.rosechat.storage;

import me.lilac.rosechat.Rosechat;
import me.lilac.rosechat.utils.Methods;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Rosefile {

    private Rosechat plugin;
    private File file;
    private FileConfiguration config;

    public Rosefile(String fileName) {
        plugin = Rosechat.getInstance();

        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();
        if (file == null) file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) plugin.saveResource(fileName, false);

        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            Methods.sendConsoleMessage("&cThere was an error while saving " + file.getName());
            e.printStackTrace();
        }
    }
}
