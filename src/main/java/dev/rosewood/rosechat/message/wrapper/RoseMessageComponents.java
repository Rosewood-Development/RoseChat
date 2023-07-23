package dev.rosewood.rosechat.message.wrapper;

import dev.rosewood.rosechat.message.tokenizer.MessageOutputs;
import net.md_5.bungee.api.chat.BaseComponent;

public record RoseMessageComponents(BaseComponent[] components,
                                    MessageOutputs outputs) { }
