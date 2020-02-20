package me.lilac.rosechat.storage;

import me.lilac.rosechat.Rosechat;
import me.lilac.rosechat.placeholder.ClickPlaceholder;
import me.lilac.rosechat.placeholder.CustomPlaceholder;
import me.lilac.rosechat.placeholder.HoverPlaceholder;
import me.lilac.rosechat.placeholder.TextPlaceholder;
import me.lilac.rosechat.utils.Methods;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ConfigManager {

    private Rosechat plugin;
    private FileConfiguration config;

    public ConfigManager() {
        plugin = Rosechat.getInstance();
        config = plugin.getConfig();
        load();
    }

    public void load() {
        loadData();
        loadMessages();
        loadConfig();
    }

    private void loadData() {
        Methods.sendConsoleMessage("&eLoading Player Data...");

        for (String uuid : plugin.getData().getConfig().getStringList("StaffChat"))
            PlayerData.getPlayersUsingStaffchat().add(UUID.fromString(uuid));

        for (String uuid : plugin.getData().getConfig().getStringList("Sounds-Off"))
            PlayerData.getPlayersWithoutSounds().add(UUID.fromString(uuid));

        for (String uuid : plugin.getData().getConfig().getStringList("Social-Spy"))
            PlayerData.getPlayersUsingSocialSpy().add(UUID.fromString(uuid));

        for (String uuid : plugin.getData().getConfig().getStringList("Messages-Off"))
            PlayerData.getPlayersWithoutMessages().add(UUID.fromString(uuid));

        for (String uuid : plugin.getData().getConfig().getStringList("Muted-Players"))
            PlayerData.getMutedPlayers().add(UUID.fromString(uuid));

        Methods.sendConsoleMessage("&eLoaded Player Data.");
    }

    public void saveData() {
        List<String> staffchat = new ArrayList<>();
        List<String> sounds = new ArrayList<>();
        List<String> socialspy = new ArrayList<>();
        List<String> msgs = new ArrayList<>();
        List<String> muted = new ArrayList<>();

        for (UUID uuid : PlayerData.getPlayersUsingStaffchat()) staffchat.add(uuid.toString());
        for (UUID uuid : PlayerData.getPlayersWithoutSounds()) staffchat.add(uuid.toString());
        for (UUID uuid : PlayerData.getPlayersUsingSocialSpy()) socialspy.add(uuid.toString());
        for (UUID uuid : PlayerData.getPlayersWithoutMessages()) msgs.add(uuid.toString());
        for (UUID uuid : PlayerData.getMutedPlayers()) muted.add(uuid.toString());

        plugin.getData().getConfig().set("StaffChat", staffchat);
        plugin.getData().getConfig().set("Sounds-Off", sounds);
        plugin.getData().getConfig().set("Social-Spy", socialspy);
        plugin.getData().getConfig().set("Messages-Off", msgs);
        plugin.getData().save();
    }

    private void loadMessages() {
        Methods.sendConsoleMessage("&eLoading Messages...");
        FileConfiguration messages = plugin.getMessages().getConfig();
        Messages.setPrefix(messages.getString("Prefix"));
        Messages.setBroadcastPrefix(messages.getString("Broadcast-Message"));
        Messages.setNoPermission(messages.getString("No-Permission"));
        Messages.setChoosePlayer(messages.getString("Choose-Player"));
        Messages.setEnterMessage(messages.getString("Enter-Message"));
        Messages.setPlayerOffline(messages.getString("Player-not-Online"));
        Messages.setInvalidArgument(messages.getString("Invalid-Argument"));
        Messages.setBlockedCaps(messages.getString("Blocked-Caps-Message"));
        Messages.setBlockedSpam(messages.getString("Blocked-Spam-Message"));
        Messages.setBlockedLanguage(messages.getString("Blocked-Language-Message"));
        Messages.setToggleSoundOn(messages.getString("Toggle-Sound-On"));
        Messages.setToggleSoundOff(messages.getString("Toggle-Sound-Off"));
        Messages.setToggleStaffChatOn(messages.getString("Toggle-StaffChat-On"));
        Messages.setToggleStaffChatOff(messages.getString("Toggle-StaffChat-Off"));
        Messages.setToggleSocialSpyOn(messages.getString("Toggle-SocialSpy-On"));
        Messages.setToggleSocialSpyOff(messages.getString("Toggle-SocialSpy-Off"));
        Messages.setSocialSpyPrefix(messages.getString("SocialSpy-Message"));
        Messages.setNoReply(messages.getString("No-Reply"));
        Messages.setCannotMessage(messages.getString("Cannot-Message"));
        Messages.setToggleMessagesOn(messages.getString("Toggle-Messages-On"));
        Messages.setToggleMessagesOff(messages.getString("Toggle-Messages-Off"));
        Methods.sendConsoleMessage("&eLoaded Messages.");
    }

    private void loadConfig() {
        Methods.sendConsoleMessage("&eLoading Placeholders...");
        Settings.setCapsCheck(config.getBoolean("Caps-Check"));
        Settings.setLowercaseCaps(config.getBoolean("Lowercase-Caps"));
        Settings.setSpamCheck(config.getBoolean("Spam-Check"));

        Settings.setBlockedMessages(new HashMap<>());
        for (String string : config.getStringList("Blocked-Messages")) {
            if (string.contains(":")) {
                String[] split = string.split(":");
                Settings.getBlockedMessages().put(split[0], split[1]);
                continue;
            }
            Settings.getBlockedMessages().put(string, null);
        }

        Settings.setMessageSound(Sound.valueOf(config.getString("Message-Sound")));
        Settings.setBroadcastSound(Sound.valueOf(config.getString("Broadcast-Sound")));

        Settings.setChatFormat(config.getString("Chat-Format"));
        Settings.setMessageSentFormat(config.getString("Message-Sent-Format"));
        Settings.setMessageReceivedFormat(config.getString("Message-Received-Format"));
        Settings.setStaffChatFormat(config.getString("StaffChat-Format"));

        for (String placeholder : config.getConfigurationSection("Custom-Placeholders").getKeys(false)) {
            CustomPlaceholder customPlaceholder = new CustomPlaceholder();
            customPlaceholder.setText(loadTextPlaceholder(placeholder));

            HoverPlaceholder hover = loadHoverPlaceholder(placeholder);
            ClickPlaceholder click = loadClickPlaceholder(placeholder);
            if (hover != null) customPlaceholder.setHover(hover);
            if (click != null) customPlaceholder.setClick(click);

            plugin.getPlaceholderManager().getPlaceholders().put(placeholder, customPlaceholder);
        }

        plugin.getPlaceholderManager().init();
        Methods.sendConsoleMessage("&eLoaded Placeholders.");
    }

    private TextPlaceholder loadTextPlaceholder(String placeholder) {
        String path = "Custom-Placeholders." + placeholder + ".text.";
        TextPlaceholder text = new TextPlaceholder();
        text.setUseGroups(config.getBoolean(path + ".use-groups"));
        text.setDefaultText(config.getString(path + ".default"));

        if (!config.contains(path + "groups")) return text;

        text.setGroups(new HashMap<>());
        for (String string : config.getConfigurationSection(path + "groups").getKeys(false))
            text.getGroups().put(string, config.getString(path + "groups." + string));

        return text;
    }

    private HoverPlaceholder loadHoverPlaceholder(String placeholder) {
        if (!config.contains("Custom-Placeholders." + placeholder + ".hover")) return null;
        String path = "Custom-Placeholders." + placeholder + ".hover.";
        HoverPlaceholder hover = new HoverPlaceholder();
        hover.setUseGroups(config.getBoolean(path + ".use-groups"));

        StringBuilder builder = new StringBuilder("");
        int i = 0;
        for (String string : config.getStringList(path + "default")) {
            if (i == config.getStringList(path + "default").size() - 1) {
                builder.append(string);
                break;
            }

            builder.append(string + "\n");
            i++;
        }

        hover.setDefaultHoverEvent(builder.toString());

        if (!config.contains(path + "groups")) return hover;

        hover.setGroups(new HashMap<>());
        for (String string : config.getConfigurationSection(path + "groups").getKeys(false)) {
            StringBuilder builder1 = new StringBuilder("");
            int j = 0;
            for (String string1 : config.getStringList(path + "groups." + string)) {
                if (j == config.getStringList(path + "groups." + string).size() - 1) {
                    builder1.append(string1);
                    break;
                }

                builder1.append(string1 + "\n");
                j++;
            }

            hover.getGroups().put(string, builder1.toString());
        }

        return hover;
    }

    private ClickPlaceholder loadClickPlaceholder(String placeholder) {
        if (!config.contains("Custom-Placeholders." + placeholder + ".click")) return null;
        String path = "Custom-Placeholders." + placeholder + ".click.";
        ClickPlaceholder click = new ClickPlaceholder();
        click.setUseGroups(config.getBoolean(path + "use-groups"));
        click.setDefaultClick(new ClickPlaceholder.RoseClickEvent(config.getString(path + "default-action"),
                config.getString(path + "default-extra")));

        if (!config.contains(path + "groups")) return click;

        click.setGroups(new HashMap<>());
        for (String string : config.getConfigurationSection(path + "groups").getKeys(false)) {
            click.getGroups().put(string,
                    new ClickPlaceholder.RoseClickEvent(config.getString(path + "groups." + string + ".action"),
                            config.getString(path + "groups." + string + ".extra")));
        }

        return click;
    }
}
