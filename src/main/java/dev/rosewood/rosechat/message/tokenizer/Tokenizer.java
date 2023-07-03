package dev.rosewood.rosechat.message.tokenizer;

public interface Tokenizer {

    /**
     * Tokenizes the input.
     * This method is called extremely frequently and should break out as early as possible if the input is not valid.
     *
     * @param params The {@link TokenizerParams} for this tokenization.
     * @return A {@link TokenizerResult} or null if the input is invalid.
     */
    TokenizerResult tokenize(TokenizerParams params);

}
