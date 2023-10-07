package dev.rosewood.rosechat.message.tokenizer;

public class TokenizerResult {

    private final Token token;
    private final int consumed;
    private final MessageOutputs outputs;

    public TokenizerResult(Token token, int consumed) {
        this.token = token;
        this.consumed = consumed;
        this.outputs = new MessageOutputs();
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
