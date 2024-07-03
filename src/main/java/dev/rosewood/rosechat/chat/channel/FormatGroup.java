package dev.rosewood.rosechat.chat.channel;

import org.bukkit.configuration.ConfigurationSection;
import java.util.HashMap;
import java.util.Map;

public class FormatGroup {

    private final Map<String, String> formats;

    public FormatGroup() {
        this.formats = new HashMap<>();
    }

    public String get(String format) {
        return this.formats.get(format);
    }

    public void add(String key, String value) {
        this.formats.put(key, value);
    }

    public String getMinecraft() {
        return this.formats.get("minecraft");
    }

    public String getMinecraftToDiscord() {
        return this.formats.get("minecraft-to-discord");
    }

    public String getDiscordToMinecraft() {
        return this.formats.get("discord-to-minecraft");
    }

    public String getShout() {
        return this.formats.get("shout");
    }

    public String getBroadcast() {
        return this.formats.get("broadcast");
    }

    public static FormatGroup fromConfig(ConfigurationSection section) {
        FormatGroup formatGroup = new FormatGroup();

        for (String key : section.getKeys(false)) {
            formatGroup.formats.put(key, section.getString(key));
        }

        return formatGroup;
    }

}
