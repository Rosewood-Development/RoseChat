package dev.rosewood.rosechat.chat.replacement;

public class ReplacementInput {

    private String text;
    private String prefix;
    private String suffix;
    private String stop;
    private String inlinePrefix;
    private String inlineSuffix;
    private boolean isRegex;
    private boolean isContentRegex;
    private boolean isInlineRegex;
    private boolean canToggle;

    public ReplacementInput() {

    }

    public ReplacementInput(ReplacementInput input) {
        this.text = input.text;
        this.prefix = input.prefix;
        this.suffix = input.suffix;
        this.stop = input.stop;
        this.inlinePrefix = input.inlinePrefix;
        this.inlineSuffix = input.inlineSuffix;
        this.isRegex = input.isRegex;
        this.isContentRegex = input.isContentRegex;
        this.isInlineRegex = input.isInlineRegex;
        this.canToggle = input.canToggle;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getStop() {
        return this.stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public String getInlinePrefix() {
        return this.inlinePrefix;
    }

    public void setInlinePrefix(String inlinePrefix) {
        this.inlinePrefix = inlinePrefix;
    }

    public String getInlineSuffix() {
        return this.inlineSuffix;
    }

    public void setInlineSuffix(String inlineSuffix) {
        this.inlineSuffix = inlineSuffix;
    }

    public boolean isRegex() {
        return this.isRegex;
    }

    public void setIsRegex(boolean isRegex) {
        this.isRegex = isRegex;
    }

    public boolean isContentRegex() {
        return this.isContentRegex;
    }

    public void setIsContentRegex(boolean isContentRegex) {
        this.isContentRegex = isContentRegex;
    }

    public boolean isInlineRegex() {
        return this.isInlineRegex;
    }

    public void setIsInlineRegex(boolean isInlineRegex) {
        this.isInlineRegex = isInlineRegex;
    }

    public boolean canToggle() {
        return this.canToggle;
    }

    public void setCanToggle(boolean canToggle) {
        this.canToggle = canToggle;
    }

}
