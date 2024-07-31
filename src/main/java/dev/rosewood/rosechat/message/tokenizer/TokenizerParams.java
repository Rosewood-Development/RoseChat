package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.message.MessageDirection;
import dev.rosewood.rosechat.message.PermissionArea;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.utils.StringPlaceholders;

public class TokenizerParams {

    private final MessageOutputs outputs;
    private final RosePlayer sender;
    private final RosePlayer receiver;
    private final String input;
    private final boolean containsPlayerInput;
    private final String playerInput;
    private final PermissionArea location;
    private final String locationPermission;
    private final Channel channel;
    private final StringPlaceholders placeholders;
    private final MessageDirection direction;

    public TokenizerParams(RoseMessage message, RosePlayer receiver, String input, boolean containsPlayerInput, MessageDirection direction) {
        this.outputs = new MessageOutputs();
        this.sender = message.getSender();
        this.receiver = receiver;
        this.input = input;
        this.containsPlayerInput = containsPlayerInput;
        this.playerInput = message.getPlayerInput();
        this.location = message.getLocation();
        this.locationPermission = message.getLocationPermission();
        this.channel = message.getChannel();
        this.placeholders = message.getPlaceholders();
        this.direction = direction;
    }

    public MessageOutputs getOutputs() {
        return this.outputs;
    }

    public RosePlayer getSender() {
        return this.sender;
    }

    public RosePlayer getReceiver() {
        return this.receiver;
    }

    public String getInput() {
        return this.input;
    }

    public boolean containsPlayerInput() {
        return this.containsPlayerInput;
    }

    public String getPlayerInput() {
        return this.playerInput;
    }

    public PermissionArea getLocation() {
        return this.location;
    }

    public String getLocationPermission() {
        return this.locationPermission;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public StringPlaceholders getPlaceholders() {
        return this.placeholders;
    }

    public MessageDirection getDirection() {
        return this.direction;
    }

}
