package dev.rosewood.rosechat.message.tokenizer;

public class TokenizerResult {

    private final Token token;
    private final int consumed;
    private final MessageOutputs outputs;

    public TokenizerResult(Token token, int consumed) {
        this(token, consumed, new MessageOutputs());
    }

    public TokenizerResult(Token token, int consumed, MessageOutputs outputs) {
        this.token = token;
        this.consumed = consumed;
        this.outputs = outputs;
    }

    public Token getToken() {
        return this.token;
    }

    public int getConsumed() {
        return this.consumed;
    }

    public MessageOutputs getOutputs() {
        return this.outputs;
    }

}
