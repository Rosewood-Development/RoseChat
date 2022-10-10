package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EmojiManager extends Manager {

    private final Map<String, ChatReplacement> emojis;

    public EmojiManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.emojis = new HashMap<>();
    }

    @Override
    public void reload() {
        this.emojis.clear();

        File emojiFile = new File(this.rosePlugin.getDataFolder(), "emojis.yml");
        if (!emojiFile.exists()) this.rosePlugin.saveResource("emojis.yml", false);

        CommentedFileConfiguration emojiConfiguration = CommentedFileConfiguration.loadConfiguration(emojiFile);

        for (String id : emojiConfiguration.getKeys(false)) {
            String text = emojiConfiguration.getString(id + ".text");
            String replacement = emojiConfiguration.getString(id + ".replacement");
            String hover = emojiConfiguration.contains(id + ".hover") ? emojiConfiguration.getString(id + ".hover") : null;
            String font = emojiConfiguration.contains(id + ".font") ? emojiConfiguration.getString(id + ".font") : null;

            this.emojis.put(id, new ChatReplacement(id, text, replacement, hover, font, false));
        }
    }

    @Override
    public void disable() {

    }

    public void addEmoji(ChatReplacement emoji) {
        this.emojis.put(emoji.getId(), emoji);
    }

    public void removeEmoji(ChatReplacement emoji) {
        this.emojis.remove(emoji.getId());
    }

    public ChatReplacement getEmoji(String id) {
        return this.emojis.get(id);
    }

    public Map<String, ChatReplacement> getEmojis() {
        return this.emojis;
    }

}
