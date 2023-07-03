package dev.rosewood.rosechat.message.tokenizer;

import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.message.MessageLocation;
import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import dev.rosewood.rosegarden.utils.StringPlaceholders;

public class TokenizerParams {

    private final MessageOutputs outputs;
    private final RosePlayer sender;
    private final RosePlayer receiver;
    private final String input;
    private final String playerInput;
    private final MessageLocation location;
    private final String locationPermission;
    private final Channel channel;
    private final StringPlaceholders placeholders;

    public TokenizerParams(RoseMessage message, RosePlayer receiver, String input) {
        this.outputs = new MessageOutputs();
        this.sender = message.getSender();
        this.receiver = receiver;
        this.input = input;
        this.playerInput = message.getMessage();
        this.location = message.getLocation();
        this.locationPermission = message.getLocationPermission();
        this.channel = message.getChannel();
        this.placeholders = message.getPlaceholders();
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

    public String getPlayerInput() {
        return this.playerInput;
    }

    public MessageLocation getLocation() {
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

}
