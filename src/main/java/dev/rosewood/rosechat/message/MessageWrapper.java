package dev.rosewood.rosechat.message;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.ChatChannel;
import dev.rosewood.rosechat.chat.ChatReplacement;
import dev.rosewood.rosechat.chat.FilterType;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.manager.DataManager;
import dev.rosewood.rosechat.manager.PlaceholderSettingManager;
import dev.rosewood.rosegarden.utils.HexUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class MessageWrapper {

    private RoseChat plugin;
    private DataManager dataManager;
    private PlaceholderSettingManager placeholderManager;
    private MessageSender sender;
    private String message;
    private String location;
    private ChatChannel channel;
    private PlayerData playerData;
    private List<BaseComponent[]> finalisedReplacements;
    private ComponentBuilder builder;
    private FilterType filterType;
    private boolean canBeSent = true;
    private boolean isEmpty;
    private List<String> taggedPlayerNames;
    private Sound tagSound;

    public MessageWrapper(MessageSender sender, String message, String location, ChatChannel channel) {
        this.plugin = RoseChat.getInstance();
        this.dataManager = this.plugin.getManager(DataManager.class);
        this.placeholderManager = this.plugin.getManager(PlaceholderSettingManager.class);
        this.sender = sender;
        this.message = message;
        this.location = location;
        this.channel = channel;
        this.builder = new ComponentBuilder();
        this.finalisedReplacements = new ArrayList<>();
        if (sender.isPlayer()) this.playerData = this.dataManager.getPlayerData(sender.asPlayer().getUniqueId());
        //parseMessage();

        // Parse Prefix & Suffix, then tokenize, so the prefix can receive tokens
    }

    public MessageWrapper(MessageSender sender, String message, String location) {
        this(sender, message, location, null);
    }

    public MessageWrapper(MessageSender sender, String message, ChatChannel channel) {
        this(sender, message, "channel." + channel.getId());
    }

    public MessageWrapper(Player player, String message, String location) {
        this(new MessageSender(player), message, location);
    }

    public MessageWrapper(Player player, String message, ChatChannel channel) {
        this(new MessageSender(player), message, channel);
    }

    public void parseMessage() {
        if (!this.parseFullMessage()) return;
        this.parseFormat();
        this.parseWords();
    }

    // Returns true if the message was successfully parsed and can be sent.
    public boolean parseFullMessage() {
        if (!this.sender.hasPermission("rosechat.rainbow." + location +  "\uE000")) {
            this.message = this.message.replaceAll(MessageUtils.RAINBOW_PATTERN.pattern(), "");
        }

        if (!this.sender.hasPermission("rosechat.gradient." + location)) {
            this.message = this.message.replaceAll(MessageUtils.GRADIENT_PATTERN.pattern(), "");
        }

        if (!this.sender.hasPermission("rosechat.color." + location)) {
            for (char color : MessageUtils.COLORS) this.message = this.message.replace("&" + color, "");
        }

        if (!this.sender.hasPermission("rosechat.hex." + location)) {
            this.message = this.message.replaceAll(MessageUtils.HEX_PATTERN.pattern(), "");
        }

        if (!this.sender.hasPermission("rosechat.format." + location)) {
            for (char format : MessageUtils.FORMATTING) this.message = this.message.replace("&" + format, "");
        }

        if (!this.sender.hasPermission("rosechat.magic." + location)) {
            this.message = this.message.replace("&" + MessageUtils.MAGIC, "");
        }

        if (Setting.CAPS_CHECKING_ENABLED.getBoolean() && !this.sender.hasPermission("rosechat.caps." + location)) {
            if (MessageUtils.isCaps(this.message)) {
                if (Setting.WARN_ON_CAPS_SENT.getBoolean()) this.filterType = FilterType.CAPS;
                if (Setting.LOWERCASE_CAPS_ENABLED.getBoolean()) {
                    this.message = this.message.toLowerCase();
                } else {
                    return this.canBeSent = false;
                }
            }
        }

        if (Setting.SPAM_CHECKING_ENABLED.getBoolean() && !this.sender.hasPermission("rosechat.spam." + location)) {
            if (this.playerData != null && this.playerData.getMessageLog().addMessageWithSpamCheck(this.message)) {
                if (Setting.WARN_ON_SPAM_SENT.getBoolean()) this.filterType = FilterType.SPAM;
                return this.canBeSent = false;
            }
        }

        return true;
    }

    public void parseFormat() {
        BaseComponent[] player = TextComponent.fromLegacyText(this.sender.getName() + ": ");
        this.builder.append(player, ComponentBuilder.FormatRetention.FORMATTING);
    }

    private String parseReplacement(ChatReplacement replacement, String messageToUse, String permission, boolean isEmoji) {
        String lowerMessage = this.message.toLowerCase();
        if (!this.sender.hasPermission(permission + replacement.getId())) return messageToUse;
        if (!lowerMessage.contains(replacement.getText().toLowerCase())) return messageToUse;
        String text = replacement.getText().toLowerCase();
        String emoji = lowerMessage.substring(lowerMessage.indexOf(text), lowerMessage.indexOf(text) + text.length());

        BaseComponent[] emojiComponent = TextComponent.fromLegacyText(HexUtils.colorify(replacement.getReplacement()));
        BaseComponent[] hoverComponent = replacement.getHoverText() != null ? TextComponent.fromLegacyText(HexUtils.colorify(replacement.getHoverText())) : null;
        BaseComponent[] removeFont = TextComponent.fromLegacyText("");
        for (BaseComponent component : emojiComponent) {
            if (hoverComponent != null) component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverComponent));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, text));
            if (isEmoji) component.setFont(replacement.getFont());
        }

        for (BaseComponent component : removeFont) component.setFont("default");

        while (messageToUse.toLowerCase().contains(emoji)) {
            messageToUse = messageToUse.replaceFirst("(?i)" + emoji, MessageUtils.REPLACEMENT_CHARACTER + MessageUtils.REPLACEMENT_CHARACTER);
            this.finalisedReplacements.add(emojiComponent);
            this.finalisedReplacements.add(removeFont);
        }

        return messageToUse;
    }

    public void parseWords() {
        String messageToUse = this.message;

        if (Setting.URL_CHECKING_ENABLED.getBoolean() && !this.sender.hasPermission("rosechat.links." + location)) {
            boolean hasUrl = false;

            Matcher urlMatcher = MessageUtils.URL_PATTERN.matcher(this.message);
            while (urlMatcher.find()) {
                String url = this.message.substring(urlMatcher.start(), urlMatcher.end());

                if (Setting.URL_CENSORING_ENABLED.getBoolean()) {
                    BaseComponent[] urlComponent = TextComponent.fromLegacyText(HexUtils.colorify("&m" + url.replace(".", " ")));
                    BaseComponent[] noStrikethrough = TextComponent.fromLegacyText("");
                    for (BaseComponent component : noStrikethrough) component.setStrikethrough(false);
                    messageToUse = messageToUse.replace(url, MessageUtils.REPLACEMENT_CHARACTER + MessageUtils.REPLACEMENT_CHARACTER);
                    this.finalisedReplacements.add(urlComponent);
                    this.finalisedReplacements.add(noStrikethrough);
                }

                hasUrl = true;
            }

            if (hasUrl) {
                if (Setting.WARN_ON_URL_SENT.getBoolean()) this.filterType = FilterType.URL;
                this.canBeSent = Setting.URL_CENSORING_ENABLED.getBoolean();
            }
        }

        if (this.sender.hasPermission("rosechat.emojis." + location)) {
            for (ChatReplacement replacement : this.placeholderManager.getEmojis().values()) {
                messageToUse = this.parseReplacement(replacement, messageToUse, "rosechat.emoji.", true);
            }
        }

        if (this.sender.hasPermission("rosechat.replacements." + location)) {
            for (ChatReplacement replacement : this.placeholderManager.getReplacements().values()) {
                messageToUse = this.parseReplacement(replacement, messageToUse, "rosechat.replacement.", false);
            }
        }

        // check language, block and replace
        // check chat placeholders
        // check emotes
        // check tags
        // check multi tags
        this.message = messageToUse;
        this.message = HexUtils.colorify(this.message);
    }

    public BaseComponent[] build() {
        this.isEmpty = this.message.trim().isEmpty();

        String[] messageSplit = this.message.split(MessageUtils.REPLACEMENT_PATTERN.pattern());
        for (String section : messageSplit) {
            if (section.startsWith(MessageUtils.REPLACEMENT_CHARACTER)) {
                if (!this.finalisedReplacements.isEmpty()) {
                    this.builder.append(this.finalisedReplacements.get(0));
                    this.finalisedReplacements.remove(0);
                    this.builder.append(TextComponent.fromLegacyText(section.substring(1)), ComponentBuilder.FormatRetention.FORMATTING).font("default");
                }
            } else {
                this.builder.append(TextComponent.fromLegacyText(section), ComponentBuilder.FormatRetention.FORMATTING).font("default");
            }
        }
        return this.builder.create();
    }

    public MessageSender getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getLocation() {
        return location;
    }

    public ChatChannel getChannel() {
        return channel;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public boolean canBeSent() {
        return canBeSent;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public List<String> getTaggedPlayerNames() {
        return taggedPlayerNames;
    }

    public Sound getTagSound() {
        return tagSound;
    }
}
