package dev.rosewood.rosechat.floralapi.root.utils;

import dev.rosewood.rosechat.floralapi.root.FloralPlugin;
import dev.rosewood.rosechat.floralapi.root.storage.YMLFile;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A friendly way to get localized and formatted text.
 */
public class LocalizedText {

    /**
     * An instance of the main class.
     */
    private FloralPlugin plugin;

    /**
     * The text to be localized and formatted.
     */
    private String text;

    public final static Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]){6}");

    /**
     * Creates a new LocalizedText object with the given parameter.
     * If the language file contains the string, gets the string from the language file.
     * @param text The text to be localized and formatted.
     */
    public LocalizedText(String text) {
        plugin = FloralPlugin.getInstance();
        if (plugin.getLanguageFile().contains(text) && plugin.getLanguageFile().isString(text)) this.text = plugin.getLanguageFile().getString(text);
        else this.text = text;
    }

    /**
     * Creates a new LocalizedText object with the given parameter.
     * @param text The text to be localized and formatted.
     * @param file The file to read to find the text.
     */
    public LocalizedText(String text, YMLFile file) {
        if (file.contains(text) && file.isString(text)) this.text = file.getString(text);
        else this.text = text;
    }

    /**
     * Creates a new LocalizedText object from an list.
     * @param array The string list to be localized and formatted.
     */
    public LocalizedText(List<String> array) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < array.size(); i++) {
            if (i != 0) builder.append("\n").append(array.get(i));
            else builder.append(array.get(i));
        }

        this.text = builder.toString();
    }

    /**
     * Creates a new LocalizedText object from a string list in the language file.
     * @return An instance of this class.
     */
    public LocalizedText fromArray() {
        return fromArray(plugin.getLanguageFile());
    }

    /**
     * Creates a new LocalizedText object from a string list in the language file.
     * @param file The file to read to find the text.
     * @return An instance of this class.
     */
    public LocalizedText fromArray(YMLFile file) {
        List<String> array = file.getStringList(text);
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < array.size(); i++) {
            if (i != 0) builder.append("\n").append(array.get(i));
            else builder.append(array.get(i));
        }

        this.text = builder.toString();

        return this;
    }

    /**
     * Sets PlaceholderAPI placeholders in this text.
     * @param player The player for the placeholders.
     * @return An instance of this class.
     */
    public LocalizedText withPlaceholderAPI(Player player) {
        if (plugin.hasPlaceholderAPI()) text = PlaceholderAPI.setPlaceholders(player, text);
        return this;
    }

    /**
     * Sets PlaceholderAPI placeholders in this text.
     * @param player The player for the placeholders.
     * @return An instance of this class.
     */
    public LocalizedText withPlaceholderAPI(OfflinePlayer player) {
        if (plugin.hasPlaceholderAPI()) text = PlaceholderAPI.setPlaceholders(player, text);
        return this;
    }

    /**
     * Sets PlaceholderAPI placeholders in this text.
     * @param sender The player for the placeholders.
     * @return An instance of this class.
     */
    public LocalizedText withPlaceholderAPI(CommandSender sender) {
        if (!(sender instanceof Player)) return this;
        Player player = (Player) sender;
        return withPlaceholderAPI(player);
    }

    /**
     * Sets a local placeholder in this text.
     * @param placeholder The placeholder to replace, without the %.
     * @param replaceWith The string to replace the placeholder.
     * @return An instance of this class.
     */
    public LocalizedText withPlaceholder(String placeholder, String replaceWith) {
        text = text.replace("%" + placeholder + "%", replaceWith);
        return this;
    }

    /**
     * Sets a local placeholder in this text.
     * @param placeholder The placeholder to replace, without the %.
     * @param replaceWith The string to replace the placeholder.
     * @return An instance of this class.
     */
    public LocalizedText withPlaceholder(String placeholder, double replaceWith) {
        text = text.replace("%" + placeholder + "%", replaceWith + "");
        return this;
    }

    /**
     * Sets a local placeholder in this text.
     * @param placeholder The placeholder to replace, without the %.
     * @param replaceWith The string to replace the placeholder.
     * @return An instance of this class.
     */
    public LocalizedText withPlaceholder(String placeholder, int replaceWith) {
        text = text.replace("%" + placeholder + "%", replaceWith + "");
        return this;
    }

    /**
     * Sets the %prefix% placeholder.
     * @return An instance of this class.
     */
    public LocalizedText withPrefixPlaceholder() {
        text = text.replace("%prefix%", plugin.getLanguageFile().getString("prefix"));
        return this;
    }

    /**
     * Adds the plugin prefix to the text.
     * @return An instance of this class.
     */
    public LocalizedText withPrefix() {
        text = "%prefix% " + text;
        return withPrefixPlaceholder();
    }

    /**
     * Formats the text with colour and formatting codes.
     * @return The text formatted with colour codes.
     */
    public String format() {
        String formatted = text;

        if (NMSUtil.getVersionNumber() >= 16) {
            Matcher matcher = HEX_PATTERN.matcher(formatted);

            while (matcher.find()) {
                ChatColor hexColour = ChatColor.of(matcher.group());
                String before = formatted.substring(0, matcher.start());
                String after = formatted.substring(matcher.end());
                formatted = before + hexColour + after;
                matcher = HEX_PATTERN.matcher(formatted);
            }
        }

        return ChatColor.translateAlternateColorCodes('&', formatted);
    }

    /**
     * Sends the message to a command sender (console or player).
     * @param sender The person who receives the message.
     */
    public void sendMessage(CommandSender sender) {
        if (text != null || !text.isEmpty()) sender.sendMessage(format());
    }

    /**
     * Makes the messagea debug message.
     */
    public LocalizedText asDebugMessage() {
        this.text = "&b&l[&c&lDebug&b&l]&r " + this.text;
        return this;
    }

    /**
     * Sends a message to the console, without specifying sender.
     */
    public void sendConsoleMessage() {
        if (text != null || !text.isEmpty()) Bukkit.getConsoleSender().sendMessage(format());
    }

    /**
     * Broadcasts a message to the entire server.
     */
    public void broadcast() {
        Bukkit.broadcastMessage(format());
    }

    /**
     * Converts a string to be config writable.
     * @param text The text to convert.
     * @return The config ready string.
     */
    public static String serialize(String text) {
        // TODO: Serialize hex codes properly, they use the following format:
        // TODO: §x§#§f§f§f§f§f§f
        return text.replace(ChatColor.COLOR_CHAR, '&');
    }

    /**
     * Converts a list to be config writable.
     * @param list The list to convert.
     * @return The config ready list.
     */
    public static List<String> serialize(List<String> list) {
        List<String> serialized = new ArrayList<>();
        for (String string : list) serialized.add(serialize(string));
        return serialized;
    }

    /**
     * Converts the text to a list.
     * @return The converted text as a list.
     */
    public List<String> toArray() {
        return new ArrayList<>(Arrays.asList(format().split("\n")));
    }

    /**
     * Converts the text to BaseComponents
     * @return The converted text as a BaseComponent array.
     */
    public BaseComponent[] toComponents() {
        return TextComponent.fromLegacyText(format());
    }

    /**
     * @return The unformatted text.
     */
    @Override
    public String toString() {
        return text;
    }
}