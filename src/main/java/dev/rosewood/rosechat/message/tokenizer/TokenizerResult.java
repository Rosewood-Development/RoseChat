package dev.rosewood.rosechat.message.tokenizer;

public record TokenizerResult(Token token,
                              int index,
                              int consumed) {

    @Deprecated(forRemoval = true)
    public TokenizerResult(Token token, int consumed) {
        this(token, 0, consumed);
    }

}
