package dev.rosewood.rosechat.message.parser;

import dev.rosewood.rosechat.message.RosePlayer;
import dev.rosewood.rosechat.message.wrapper.RoseMessage;
import net.md_5.bungee.api.chat.BaseComponent;

public class FromDiscordParser implements MessageParser {

    @Override
    public BaseComponent[] parse(RoseMessage message, RosePlayer sender, RosePlayer viewer, String format) {
        return null;
    }

}
