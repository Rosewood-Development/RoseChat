package dev.rosewood.rosechat.message.tokenizer;

public abstract class Tokenizer {

    private final String name;

    protected Tokenizer(String name) {
        this.name = name;
    }

    /**
     * Tokenizes the input.
     * This method is called extremely frequently and should break out as early as possible if the input is not valid.
     *
     * @param params The {@link TokenizerParams} for this tokenization.
     * @return A {@link TokenizerResult} or null if the input is invalid.
     */
    public abstract TokenizerResult tokenize(TokenizerParams params);

    /**
     * @return true if this tokenizer is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return true;
    }

    /**
     * @return The name of this tokenizer.
     */
    public final String getName() {
        return this.name;
    }



}
