package dev.rosewood.rosechat.managers;

import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.managers.ConfigurationManager.Setting;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import dev.rosewood.rosegarden.manager.Manager;
import org.bukkit.Bukkit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelManager extends Manager {

    private Map<String, ChatChannel> channels;
    private ChatChannel defaultChannel;

    public ChannelManager(RosePlugin rosePlugin) {
        super(rosePlugin);
        this.channels = new HashMap<>();
    }

    @Override
    public void reload() {
        this.defaultChannel = null;

        for (String id : Setting.CHAT_CHANNELS.getSection().getKeys(false)) {
            CommentedConfigurationSection section = Setting.CHAT_CHANNELS.getSection();

            boolean isDefault = section.contains(id + ".default") && section.getBoolean(id + ".default");
            String format = section.contains(id + ".format") ? section.getString(id + ".format") : "default-format";
            String command = section.contains(id + ".command") ? section.getString(id + ".command") : null;
            boolean placeholders = section.contains(id + ".chat-placeholders-enabled") && section.getBoolean(id + ".chat-placeholders-enabled");
            List<String> disabledTags = section.contains(id + ".disabled-tags") ? section.getStringList(id + ".disabled-tags") : new ArrayList<>();
            List<String> disabledReplacements = section.contains(id + ".disabled-replacements") ? section.getStringList(id + ".disabled-replacements") : new ArrayList<>();
            boolean capsCheck = section.contains(id + ".caps-checking-enabled") && section.getBoolean(id + ".caps-checking-enabled");
            boolean urlCheck = section.contains(id + ".url-checking-enabled") && section.getBoolean(id + ".url-checking-enabled");
            boolean spamCheck = section.contains(id + ".spam-checking-enabled") && section.getBoolean(id + ".spam-checking-enabled");
            boolean swearCheck = section.contains(id + ".swear-checking-enabled") && section.getBoolean(id + ".swear-checking-enabled");
            boolean visible = section.contains(id + ".visible") && section.getBoolean(id + ".visible");
            int radius = section.contains(id + ".radius") ? section.getInt(id + ".radius") : -1;
            String world = section.contains(id + ".world") ? section.getString(id + ".world") : null;
            boolean autoJoin = section.contains(id + ".auto-join") && section.getBoolean("auto-join");
            List<String> servers = section.contains(id + ".servers") ? section.getStringList(id + ".servers") : new ArrayList<>();

            ChatChannel channel = new ChatChannel(id, format, isDefault)
                    .setChatPlaceholders(placeholders).setDisabledTags(disabledTags).setDisabledReplacements(disabledReplacements)
                    .setCheckCaps(capsCheck).setCheckUrl(urlCheck).setCheckSpam(spamCheck).setCheckLanguage(swearCheck)
                    .setRadius(radius).setWorld(world).setAutoJoin(autoJoin).setServers(servers).setVisible(visible).setCommand(command);

            channels.put(id, channel);

            if (isDefault && defaultChannel == null)
                defaultChannel = channel;

            rosePlugin.getManager(PlaceholderSettingManager.class).parseFormat("channel-" + id, format);
        }

        if (defaultChannel == null) {
            LocaleManager localeManager = rosePlugin.getManager(LocaleManager.class);
            defaultChannel = (ChatChannel) this.channels.values().toArray()[this.channels.size() - 1];
            localeManager.sendCustomMessage(Bukkit.getConsoleSender(), localeManager.getLocaleMessage("prefix") +
                    "&eNo default chat channel was found. Using &b" + defaultChannel.getId() + " &eas default.");
        }
    }

    @Override
    public void disable() {

    }

    public ChatChannel getChannel(String id) {
        return channels.get(id);
    }

    public Map<String, ChatChannel> getChannels() {
        return channels;
    }

    public ChatChannel getDefaultChannel() {
        return defaultChannel;
    }
}
