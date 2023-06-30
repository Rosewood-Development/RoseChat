package dev.rosewood.rosechat.message.wrapper;

import dev.rosewood.rosechat.api.event.message.MessageBlockedEvent;
import dev.rosewood.rosechat.chat.FilterType;
import dev.rosewood.rosechat.manager.ConfigurationManager.Setting;
import dev.rosewood.rosechat.message.MessageUtils;
import org.bukkit.Bukkit;
import java.util.regex.Matcher;

public class MessageRules {

    // Rules
    private boolean filterCaps;
    private boolean filterSpam;
    private boolean filterURLs;
    private boolean filterLanguage;
    private boolean applySenderChatColor;
    private boolean ignoreMessageLogging;
    private boolean privateMessage;
    private PrivateMessageInfo privateMessageInfo;

    public MessageRules() {

    }

    /**
     * Copy constructor to prevent asynchronous editing issues.
     * @param other The {@link MessageRules} to copy.
     */
    public MessageRules(MessageRules other) {
        this.filterCaps = other.filterCaps;
        this.filterSpam = other.filterSpam;
        this.filterURLs = other.filterURLs;
        this.filterLanguage = other.filterLanguage;
        this.applySenderChatColor = other.applySenderChatColor;
        this.ignoreMessageLogging = other.ignoreMessageLogging;
        this.privateMessage = other.privateMessage;
        this.privateMessageInfo = other.privateMessageInfo;
    }

    /**
     * Checks if the sender has permission, and applies a capital letter filter if not.
     */
    public MessageRules applyCapsFilter() {
        this.filterCaps = true;
        return this;
    }

    /**
     * Checks if the sender has permission, and applies a spam filter if not.
     */
    public MessageRules applySpamFilter() {
        this.filterSpam = true;
        return this;
    }

    /**
     * Checks if the sender has permission, and applies a URL filter if not.
     */
    public MessageRules applyURLFilter() {
        this.filterURLs = true;
        return this;
    }

    /**
     * Checks if the sender has permission, and applies a language filter if not.
     */
    public MessageRules applyLanguageFilter() {
        this.filterLanguage = true;
        return this;
    }

    /**
     * Applies all the filters.
     * Recommended for chat messages.
     */
    public MessageRules applyAllFilters() {
        this.filterCaps = true;
        this.filterSpam = true;
        this.filterURLs = true;
        this.filterLanguage = true;
        return this;
    }

    /**
     * Prepends the sender's saved chat color to the message.
     */
    public MessageRules applySenderChatColor() {
        this.applySenderChatColor = true;
        return this;
    }

    /**
     * Disables message logging for the message.
     */
    public MessageRules ignoreMessageLogging() {
        this.ignoreMessageLogging = true;
        return this;
    }

    /**
     * Marks the message as a private message with the given information.
     * @param info The information to use, containing the sender and the receiver.
     */
    public MessageRules setPrivateMessageInfo(PrivateMessageInfo info) {
        this.privateMessage = true;
        this.privateMessageInfo = info;
        return this;
    }

    private boolean isCaps(RoseMessage message) {
        if (!Setting.CAPS_CHECKING_ENABLED.getBoolean()) return false;
        if (message.getSender().hasPermission("rosechat.caps." + message.getLocationPermission())) return false;

        int caps = 0;
        for (char c : message.getMessage().toCharArray()) {
            if (Character.isAlphabetic(c) && c == Character.toUpperCase(c)) caps++;
        }

        return caps > Setting.MAXIMUM_CAPS_ALLOWED.getInt();
    }

    private void filterCaps(RoseMessage message) {
        if (!Setting.CAPS_CHECKING_ENABLED.getBoolean()) return;
        if (!this.isCaps(message)) return;

        if (Setting.WARN_ON_CAPS_SENT.getBoolean()) message.setFilterType(FilterType.CAPS);
        if (Setting.LOWERCASE_CAPS_ENABLED.getBoolean()) {
            message.setMessage(message.getMessage().toLowerCase());
            return;
        }

        MessageBlockedEvent messageBlockedEvent = new MessageBlockedEvent(message);
        Bukkit.getPluginManager().callEvent(messageBlockedEvent);

        if (messageBlockedEvent.isCancelled())
            return;

        message.setBlocked(true);
    }

    private void filterSpam(RoseMessage message) {
        if (!Setting.SPAM_CHECKING_ENABLED.getBoolean()) return;
        if (message.getSender().hasPermission("rosechat.spam." + message.getLocationPermission())
                || message.getSender().getPlayerData() == null) return;

        if (!message.getSender().getPlayerData().getMessageLog().addMessageWithSpamCheck(message.getMessage())) return;
        if (Setting.WARN_ON_SPAM_SENT.getBoolean()) message.setFilterType(FilterType.SPAM);

        MessageBlockedEvent messageBlockedEvent = new MessageBlockedEvent(message);
        Bukkit.getPluginManager().callEvent(messageBlockedEvent);

        if (messageBlockedEvent.isCancelled())
            return;

        message.setBlocked(true);
    }

    private void filterURLs(RoseMessage message) {
        if (!Setting.URL_CHECKING_ENABLED.getBoolean()) return;
        if (message.getSender().hasPermission("rosechat.links." + message.getLocationPermission())) return;

        boolean hasURL = false;
        Matcher matcher = MessageUtils.URL_PATTERN.matcher(message.getMessage());
        while (matcher.find()) {
            String url = message.getMessage().substring(matcher.start(), matcher.end());
            message.setMessage(message.getMessage().replace(url, "&m" + url.replace(".", " ") + "&r"));
            hasURL = true;
        }

        if (!hasURL) return;
        if (Setting.WARN_ON_URL_SENT.getBoolean()) message.setFilterType(FilterType.URL);

        boolean isBlocked = !Setting.URL_CENSORING_ENABLED.getBoolean();

        if (!isBlocked)
            return;

        MessageBlockedEvent messageBlockedEvent = new MessageBlockedEvent(message);
        Bukkit.getPluginManager().callEvent(messageBlockedEvent);

        if (messageBlockedEvent.isCancelled())
            return;

        message.setBlocked(true);
    }

    private void filterLanguage(RoseMessage message) {
        if (!Setting.SWEAR_CHECKING_ENABLED.getBoolean()) return;
        if (message.getSender().hasPermission("rosechat.language." + message.getLocationPermission())) return;
        String strippedMessage = MessageUtils.stripAccents(message.getMessage().toLowerCase());

        for (String swear : Setting.BLOCKED_SWEARS.getStringList()) {
            for (String word : strippedMessage.split(" ")) {
                double difference = MessageUtils.getLevenshteinDistancePercent(swear, word);
                if ((1 - difference) <= (Setting.SWEAR_FILTER_SENSITIVITY.getDouble() / 100.0)) {
                    if (Setting.WARN_ON_BLOCKED_SWEAR_SENT.getBoolean()) message.setFilterType(FilterType.SWEAR);

                    MessageBlockedEvent messageBlockedEvent = new MessageBlockedEvent(message);
                    Bukkit.getPluginManager().callEvent(messageBlockedEvent);

                    if (messageBlockedEvent.isCancelled())
                        return;

                    message.setBlocked(true);
                    return;
                }
            }
        }

        for (String replacements : Setting.SWEAR_REPLACEMENTS.getStringList()) {
            String[] swearReplacement = replacements.split(":");
            String swear = swearReplacement[0];
            String replacement = swearReplacement[1];
            for (String word : message.getMessage().split(" ")) {
                double difference = MessageUtils.getLevenshteinDistancePercent(swear, word);
                if ((1 - difference) <= (Setting.SWEAR_FILTER_SENSITIVITY.getDouble() / 100.0)) {
                    message.setMessage(message.getMessage().replace(word, replacement));
                }
            }
        }
    }

    /**
     * Applies filters and settings based on the set rules.
     * @param message The message to edit and get information from.
     */
    protected void apply(RoseMessage message) {
        if (this.filterSpam) this.filterSpam(message);
        if (this.filterCaps) this.filterCaps(message);
        if (this.filterURLs) this.filterURLs(message);
        if (this.filterLanguage) this.filterLanguage(message);
        if (this.applySenderChatColor && message.getSenderData() != null) message.setMessage(message.getSenderData().getColor() + message.getMessage());
    }

    /**
     * @return True if the rules dictate ignoring message logging.
     */
    public boolean isIgnoringMessageLogging() {
        return this.ignoreMessageLogging;
    }

    /**
     * @return True if the rules dictate that it is a private message.
     */
    public boolean isPrivateMessage() {
        return this.privateMessage;
    }

    /**
     * @return The {@link PrivateMessageInfo} for the message that these rules should be applied to.
     */
    public PrivateMessageInfo getPrivateMessageInfo() {
        return this.privateMessageInfo;
    }

    /**
     * @param ignoreMessageLogging Whether to ignore message logging.
     */
    public void setIgnoreMessageLogging(boolean ignoreMessageLogging) {
        this.ignoreMessageLogging = ignoreMessageLogging;
    }

}
