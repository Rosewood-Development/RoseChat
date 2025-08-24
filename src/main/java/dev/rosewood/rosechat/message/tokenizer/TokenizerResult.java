package dev.rosewood.rosechat.message.tokenizer;

import java.util.List;

public record TokenizerResult(Token token,
                              int index,
                              int consumed) implements Comparable<TokenizerResult> {

    @Deprecated(forRemoval = true)
    public TokenizerResult(Token token, int consumed) {
        this(token, 0, consumed);
    }

    @Override
    public int compareTo(TokenizerResult other) {
        return Integer.compare(this.index, other.index());
    }

    public boolean overlaps(int start, int end) {
        int resultStart = this.index;
        int resultEnd = resultStart + this.consumed;
        return end > resultStart && start < resultEnd;
    }

    public static boolean overlaps(List<TokenizerResult> results, int start, int end) {
        for (TokenizerResult result : results)
            if (result.overlaps(start, end))
                return true;
        return false;
    }

}
