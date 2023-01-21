package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.chat.Tag;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
import org.bukkit.Sound;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TagManager extends Manager {

    private final Map<String, Tag> tags;

    public TagManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.tags = new HashMap<>();
    }

    @Override
    public void reload() {
        this.tags.clear();

        File tagsFile = new File(this.rosePlugin.getDataFolder(), "tags.yml");
        if (!tagsFile.exists()) this.rosePlugin.saveResource("tags.yml", false);

        CommentedFileConfiguration tagsConfiguration = CommentedFileConfiguration.loadConfiguration(tagsFile);

        for (String id : tagsConfiguration.getKeys(false)) {
            String prefix = tagsConfiguration.getString(id + ".prefix");
            String suffix = tagsConfiguration.getString(id + ".suffix");
            boolean tagOnlinePlayers = tagsConfiguration.getBoolean(id + ".tag-online-players");
            boolean matchLength = tagsConfiguration.getBoolean(id + ".match-length");
            String format = tagsConfiguration.getString(id + ".format").replace("{", "").replace("}", "");
            Sound sound;

            try {
                sound = Sound.valueOf(tagsConfiguration.getString(id + ".sound"));
            } catch (Exception e) {
                sound = null;
            }

            this.tags.put(id, new Tag(id, prefix, suffix, tagOnlinePlayers, matchLength, format, sound));
            this.rosePlugin.getManager(PlaceholderManager.class).parseFormat("tag-" + id, format);
        }
    }

    @Override
    public void disable() {

    }

    public void addTag(Tag tag) {
        this.tags.put(tag.getId(), tag);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag.getId());
    }

    public Tag getTag(String id) {
        return this.tags.get(id);
    }

    public Map<String, Tag> getTags() {
        return this.tags;
    }

}
