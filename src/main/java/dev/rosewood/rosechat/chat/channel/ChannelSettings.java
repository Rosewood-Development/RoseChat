package dev.rosewood.rosechat.chat.channel;

import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.configuration.ConfigurationSection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelSettings {

    private final Map<String, String> formats;
    private final List<String> commands;
    private final List<String> overrideCommands;
    private final List<String> shoutCommands;
    private String discord;
    private boolean sendBungeeToDiscord;
    private boolean isDefault;

    public ChannelSettings() {
        this.formats = new HashMap<>();
        this.commands = new ArrayList<>();
        this.overrideCommands = new ArrayList<>();
        this.shoutCommands = new ArrayList<>();
    }

    public ChannelSettings(Map<String, String> formats, List<String> commands, List<String> overrideCommands,
                           List<String> shoutCommands, String discord, boolean sendBungeeToDiscord, boolean isDefault) {
        this.formats = formats;
        this.commands = commands;
        this.overrideCommands = overrideCommands;
        this.shoutCommands = shoutCommands;
        this.discord = discord;
        this.sendBungeeToDiscord = sendBungeeToDiscord;
        this.isDefault = isDefault;
    }

    public Map<String, String> getFormats() {
        return this.formats;
    }

    public List<String> getCommands() {
        return this.commands;
    }

    public List<String> getOverrideCommands() {
        return this.overrideCommands;
    }

    public List<String> getShoutCommands() {
        return this.shoutCommands;
    }

    public String getDiscord() {
        return this.discord;
    }

    public void setDiscord(String discord) {
        this.discord = discord;
    }

    public boolean shouldSendBungeeToDiscord() {
        return this.sendBungeeToDiscord;
    }

    public void setSendBungeeToDiscord(boolean sendBungeeToDiscord) {
        this.sendBungeeToDiscord = sendBungeeToDiscord;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public StringPlaceholders.Builder toPlaceholders(String nullValue) {
        return StringPlaceholders.builder()
                .add("discord", this.discord)
                .add("commands", this.commands.isEmpty() ?
                        nullValue : this.commands.toString())
                .add("shout-commands", this.shoutCommands.isEmpty() ?
                        nullValue : this.shoutCommands.toString())
                .add("override-commands", this.overrideCommands.isEmpty() ?
                        nullValue : this.overrideCommands.toString());
    }

    /**
     * Creates a new {@link ChannelSettings} from a given {@link ConfigurationSection}.
     * @param config The {@link ConfigurationSection} containing the settings.
     * @return A new {@link ChannelSettings}.
     */
    public static ChannelSettings fromConfig(ConfigurationSection config) {
        Map<String, String> formats = new HashMap<>();
        String discord = null;
        List<String> commands = new ArrayList<>();
        List<String> overrideCommands = new ArrayList<>();
        List<String> shoutCommands = new ArrayList<>();
        boolean sendBungeeToDiscord = false;
        boolean isDefault = false;

        if (config.contains("formats"))
            for (String key : config.getConfigurationSection("formats").getKeys(false))
                formats.put(key, config.getString("formats." + key));

        if (config.contains("discord"))
            discord = config.getString("discord");

        if (config.contains("commands"))
            commands = config.getStringList("commands");

        if (config.contains("override-commands"))
            overrideCommands = config.getStringList("override-commands");

        if (config.contains("shout-commands"))
            shoutCommands = config.getStringList("shout-commands");

        if (config.contains("send-bungee-messages-to-discord"))
            sendBungeeToDiscord = config.getBoolean("send-bungee-messages-to-discord");

        if (config.contains("default"))
            isDefault = config.getBoolean("default");

        return new ChannelSettings(formats, commands, overrideCommands, shoutCommands, discord, sendBungeeToDiscord, isDefault);
    }

}
