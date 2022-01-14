package dev.rosewood.rosechat.manager;

import com.google.common.base.Charsets;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ReplacementManager extends Manager {

    private Map<String, ChatReplacement> replacements;

    public ReplacementManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.replacements = new HashMap<>();
    }

    @Override
    public void reload() {
        File replacementFile = new File(this.rosePlugin.getDataFolder(), "replacements.yml");
        if (!replacementFile.exists()) this.rosePlugin.saveResource("replacements.yml", false);

        YamlConfiguration replacementConfiguration = new YamlConfiguration();

        try {
            FileInputStream stream = new FileInputStream(replacementFile);
            replacementConfiguration.load(new InputStreamReader(stream, Charsets.UTF_8));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        for (String id : replacementConfiguration.getKeys(false)) {
            String text = replacementConfiguration.getString(id + ".text");
            String replacement = replacementConfiguration.getString(id + ".replacement");
            boolean regex = replacementConfiguration.contains(id + ".regex") && replacementConfiguration.getBoolean(id + ".regex");

            if (replacement.startsWith("{") && replacement.endsWith("}"))
                MessageUtils.parseFormat("replacement-" + id, replacement);

            this.replacements.put(id, new ChatReplacement(id, text, replacement, regex));
        }
    }

    @Override
    public void disable() {

    }

    public void addReplacement(ChatReplacement replacement) {
        this.replacements.put(replacement.getId(), replacement);
    }

    public void removeReplacement(ChatReplacement replacement) {
        this.replacements.remove(replacement.getId());
    }

    public ChatReplacement getReplacement(String id) {
        return this.replacements.get(id);
    }

    public Map<String, ChatReplacement> getReplacements() {
        return this.replacements;
    }
}
