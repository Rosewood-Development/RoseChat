package dev.rosewood.rosechat.managers;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.floralapi.root.storage.YMLFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelManager {

    private RoseChat plugin;
    private YMLFile config;
    private Map<String, ChatChannel> channels;

    public ChannelManager(RoseChat plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigFile();
        channels = new HashMap<>();
        load();
    }

    public void load() {
        for (String id : config.getSection("chat-channels").getKeys(false)) {
            boolean isDefault = config.contains("chat-channels." + id + ".default")
                    && config.getBoolean("chat-channels." + id + ".default");
            String format = config.contains("chat-channels." + id + ".format") ?
                    config.getString("chat-channels." + id + ".format") :
                    "default-format";
            boolean chatPlaceholders = config.contains("chat-channels." + id + ".chat-placeholders-enabled")
                    && config.getBoolean("chat-channels." + id + ".chat-placeholders-enabled");
            List<String> disabledTags = config.contains("chat-channels." + id + ".disabled-tags") ?
                    config.getStringList("chat-channels." + id + ".disabled-tags") : new ArrayList<>();
            List<String> disabledEmotes = config.contains("chat-channels." + id + ".disabled-emotes") ?
                    config.getStringList("chat-channels." + id + ".disabled-emotes") : new ArrayList<>();
            boolean capsCheck = config.contains("chat-channels." + id + ".caps-checking-enabled")
                    && config.getBoolean("chat-channels." + id + ".caps-checking-enabled");
            boolean urlCheck = config.contains("chat-channels." + id + ".url-checking-enabled")
                    && config.getBoolean("chat-channels." + id + ".url-checking-enabled");
            boolean spamCheck = config.contains("chat-channels." + id + ".spam-checking-enabled")
                    && config.getBoolean("chat-channels." + id + ".spam-checking-enabled");
            boolean swearCheck = config.contains("chat-channels." + id + ".swear-checking-enabled")
                    && config.getBoolean("chat-channels." + id + ".swear-checking-enabled");
            int radius = config.contains("chat-channels." + id + ".radius") ?
                    config.getInt("chat-channels." + id + ".radius") : -1;
            String world = config.contains("chat-channels." + id + ".world") ?
                    config.getString("chat-channels." + id + ".world") : null;
            boolean autoJoin  = config.contains("chat-channels." + id + ".auto-join")
                    && config.getBoolean("chat-channels." + id + ".auto-join");

            ChatChannel channel = new ChatChannel(id, format, isDefault);
            channel.setChatPlaceholders(chatPlaceholders);
            channel.setDisabledTags(disabledTags);
            channel.setDisabledEmotes(disabledEmotes);
            channel.setCheckCaps(capsCheck);
            channel.setCheckUrl(urlCheck);
            channel.setCheckSpam(spamCheck);
            channel.setCheckLanguage(swearCheck);
            channel.setRadius(radius);
            channel.setWorld(world);
            channel.setAutoJoin(autoJoin);

            channels.put(id, channel);

            plugin.getPlaceholderManager().parseFormat("channel-" + id, format);
        }
    }

    public ChatChannel getChannelById(String id) {
        return this.channels.get(id);
    }

    public Map<String, ChatChannel> getChannels() {
        return channels;
    }
}
