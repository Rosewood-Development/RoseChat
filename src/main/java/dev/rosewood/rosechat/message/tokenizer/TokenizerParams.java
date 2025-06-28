package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import java.util.Set;

public class TokenizerParams {

    private final MessageOutputs outputs;
    private final RoseMessage message;
    private final RosePlayer receiver;
    private final String input;
    private final Token parentToken;
    private final boolean usePlayerChatColor;
    private final MessageDirection direction;
    private final Set<String> ignoredFilters;

    public TokenizerParams(RoseMessage message, RosePlayer receiver, String input, Token parentToken,
                           boolean usePlayerChatColor, MessageDirection direction, MessageOutputs outputs, Set<String> ignoredFilters) {
        this.outputs = outputs;
        this.message = message;
        this.receiver = receiver;
        this.input = input;
        this.parentToken = parentToken;
        this.usePlayerChatColor = usePlayerChatColor;
        this.direction = direction;
        this.ignoredFilters = ignoredFilters;
    }

    public MessageOutputs getOutputs() {
        return this.outputs;
    }

    public RosePlayer getSender() {
        return this.message.getSender();
    }

    public RosePlayer getReceiver() {
        return this.receiver;
    }

    public String getInput() {
        return this.input;
    }

    public boolean containsPlayerInput() {
        return this.parentToken.containsPlayerInput();
    }

    public boolean shouldUsePlayerChatColor() {
        return this.usePlayerChatColor;
    }

    public String getPlayerInput() {
        return this.message.getPlayerInput();
    }

    public PermissionArea getLocation() {
        return this.message.getLocation();
    }

    public String getLocationPermission() {
        return this.message.getLocationPermission();
    }

    public Channel getChannel() {
        return this.message.getChannel();
    }

    public StringPlaceholders getPlaceholders() {
        StringPlaceholders.Builder builder = StringPlaceholders.builder();
        builder.addAll(this.message.getPlaceholders());
        builder.addAll(this.parentToken.getPlaceholders());
        return builder.build();
    }

    public MessageDirection getDirection() {
        return this.direction;
    }

    public Set<String> getIgnoredFilters() {
        return this.ignoredFilters;
    }

}
