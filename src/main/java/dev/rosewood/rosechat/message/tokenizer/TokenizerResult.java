package dev.rosewood.rosechat.message.tokenizer;

public record TokenizerResult(Token token,
                              int index,
                              int consumed) { }
