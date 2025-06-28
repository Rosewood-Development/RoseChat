package dev.rosewood.rosechat.message.wrapper;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.api.event.message.MessageBlockedEvent;
import dev.rosewood.rosechat.api.event.message.MessageFilteredEvent;
import dev.rosewood.rosechat.chat.FilterWarning;
import dev.rosewood.rosechat.chat.filter.Filter;
import dev.rosewood.rosechat.config.Settings;
import dev.rosewood.rosechat.message.MessageUtils;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class MessageRules {

    // Rules
    private boolean filterCaps;
    private boolean filterSpam;
    private boolean filterURLs;
    private boolean filterLanguage;
    private boolean ignoreMessageLogging;

    public MessageRules() {

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
     * Disables message logging for the message.
     */
    public MessageRules ignoreMessageLogging() {
        this.ignoreMessageLogging = true;
        return this;
    }

    private boolean isCaps(RoseMessage message, RuleOutputs outputs) {
        if (!Settings.CAPS_CHECKING_ENABLED.get())
            return false;

        if (message.getSender().hasPermission("rosechat.caps." + message.getLocationPermission()))
            return false;

        int caps = 0;
        for (char c : outputs.getFilteredMessage().toCharArray()) {
            if (Character.isAlphabetic(c) && c == Character.toUpperCase(c))
                caps++;
        }

        return caps > Settings.MAXIMUM_CAPS_ALLOWED.get();
    }

    private void filterCaps(RoseMessage message, RuleOutputs outputs) {
        if (!Settings.CAPS_CHECKING_ENABLED.get())
            return;

        if (!this.isCaps(message, outputs))
            return;

        if (Settings.WARN_ON_CAPS_SENT.get())
            outputs.setWarning(FilterWarning.CAPS);

        if (Settings.LOWERCASE_CAPS_ENABLED.get()) {
            outputs.transformMessage(String::toLowerCase);
            return;
        }

        outputs.setBlocked(true);
    }

    private void filterSpam(RoseMessage message, RuleOutputs outputs) {
        if (!Settings.SPAM_CHECKING_ENABLED.get() || this.ignoreMessageLogging)
            return;

        if (message.getSender().hasPermission("rosechat.spam." + message.getLocationPermission())
                || message.getSender().getPlayerData() == null)
            return;

        if (!message.getSender().getPlayerData().getMessageLog().addMessageWithSpamCheck(outputs.getFilteredMessage()))
            return;

        if (Settings.WARN_ON_SPAM_SENT.get())
            outputs.setWarning(FilterWarning.SPAM);

        outputs.setBlocked(true);
    }

    private void filterURLs(RoseMessage message, RuleOutputs outputs) {
        if (!Settings.URL_CHECKING_ENABLED.get())
            return;

        if (message.getSender().hasPermission("rosechat.links." + message.getLocationPermission()))
            return;

        boolean hasURL = false;
        Matcher matcher = MessageUtils.URL_PATTERN.matcher(outputs.getFilteredMessage());
        while (matcher.find()) {
            String url = outputs.getFilteredMessage().substring(matcher.start(), matcher.end());
            outputs.transformMessage(x -> x.replace(url, ChatColor.STRIKETHROUGH +
                    url.replace(".", " ") + ChatColor.RESET));
            hasURL = true;
        }

        if (!hasURL)
            return;

        if (Settings.WARN_ON_URL_SENT.get())
            outputs.setWarning(FilterWarning.URL);

        if (!Settings.URL_CENSORING_ENABLED.get())
            outputs.setBlocked(true);
    }

    private void filterLanguage(RoseMessage message, RuleOutputs outputs) {
        String strippedMessage = MessageUtils.stripAccents(outputs.getFilteredMessage().toLowerCase());

        for (Filter filter : RoseChatAPI.getInstance().getFilters()) {
            if (!filter.block() || !filter.hasPermission(message.getSender()))
                continue;

            // Exact match if sensitivity is 0.
            if (filter.sensitivity() == 0) {
                for (String word : outputs.getFilteredMessage().toLowerCase().split(" ")) {
                    for (String blocked : filter.matches()) {
                        if (word.equalsIgnoreCase(blocked)) {
                            if (filter.message() != null)
                                outputs.setWarningMessage(filter.message());

                            if (filter.notifyStaff())
                                outputs.setNotifyStaff(true);

                            outputs.setBlocked(true);

                            outputs.getServerCommands().addAll(filter.serverCommands());
                            outputs.getPlayerCommands().addAll(filter.playerCommands());
                            return;
                        }
                    }
                }
            } else {
                for (String word : strippedMessage.split(" ")) {
                    for (String blocked : filter.matches()) {
                        double difference = MessageUtils.getLevenshteinDistancePercent(blocked, word);

                        if ((1 - difference) <= filter.sensitivity() / 100.0) {
                            if (filter.message() != null)
                                outputs.setWarningMessage(filter.message());

                            if (filter.notifyStaff())
                                outputs.setNotifyStaff(true);

                            outputs.setBlocked(true);

                            outputs.getServerCommands().addAll(filter.serverCommands());
                            outputs.getPlayerCommands().addAll(filter.playerCommands());
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Applies filters and settings based on the set rules.
     * @param message The message to edit and get information from.
     * @param originalMessage The original message sent by the player.
     */
    public RuleOutputs apply(RoseMessage message, String originalMessage) {
        RuleOutputs outputs = new RuleOutputs(originalMessage);
        if (this.filterSpam)
            this.filterSpam(message, outputs);

        if (this.filterCaps)
            this.filterCaps(message, outputs);

        if (this.filterURLs)
            this.filterURLs(message, outputs);

        if (this.filterLanguage)
            this.filterLanguage(message, outputs);

        if (outputs.blocked) {
            MessageBlockedEvent messageBlockedEvent = new MessageBlockedEvent(message, originalMessage, outputs);
            Bukkit.getPluginManager().callEvent(messageBlockedEvent);
        }

        if (!outputs.getFilteredMessage().equals(originalMessage)) {
            MessageFilteredEvent messageFilteredEvent = new MessageFilteredEvent(message, originalMessage, outputs);
            Bukkit.getPluginManager().callEvent(messageFilteredEvent);
        }

        return outputs;
    }

    public RuleOutputs apply(RosePlayer rosePlayer, PermissionArea messageLocation, String originalMessage) {
        return this.apply(RoseMessage.forLocation(rosePlayer, messageLocation), originalMessage);
    }

    /**
     * @return True if the rules dictate ignoring message logging.
     */
    public boolean isIgnoringMessageLogging() {
        return this.ignoreMessageLogging;
    }

    public static class RuleOutputs {

        private boolean blocked;
        private FilterWarning warning;
        private String warningMessage;
        private String message;
        private boolean notifyStaff;
        private final List<String> serverCommands;
        private final List<String> playerCommands;

        public RuleOutputs(String message) {
            this.message = message;
            this.serverCommands = new ArrayList<>();
            this.playerCommands = new ArrayList<>();
        }

        public boolean isBlocked() {
            return this.blocked;
        }

        public void setBlocked(boolean blocked) {
            this.blocked = blocked;
        }

        public FilterWarning getWarning() {
            return this.warning;
        }

        public void setWarning(FilterWarning warning) {
            this.warning = warning;
        }

        public String getWarningMessage() {
            return this.warningMessage;
        }

        public void setWarningMessage(String warningMessage) {
            this.warningMessage = warningMessage;
        }

        public boolean shouldNotifyStaff() {
            return this.notifyStaff;
        }

        public void setNotifyStaff(boolean notifyStaff) {
            this.notifyStaff = notifyStaff;
        }

        public String getFilteredMessage() {
            return this.message;
        }

        public List<String> getServerCommands() {
            return this.serverCommands;
        }

        public List<String> getPlayerCommands() {
            return this.playerCommands;
        }

        public void transformMessage(Function<String, String> transformer) {
            this.message = transformer.apply(this.message);
        }

    }

}
