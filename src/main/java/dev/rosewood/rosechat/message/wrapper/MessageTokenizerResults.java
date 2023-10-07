package dev.rosewood.rosechat.message.wrapper;

import dev.rosewood.rosechat.message.tokenizer.MessageOutputs;

public record MessageTokenizerResults<T>(T content,
                                         MessageOutputs outputs) { }
