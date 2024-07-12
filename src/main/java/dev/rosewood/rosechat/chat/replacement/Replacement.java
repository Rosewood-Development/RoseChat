package dev.rosewood.rosechat.chat.replacement;

public class Replacement {

    private final String id;
    private ReplacementInput input;
    private ReplacementOutput output;

    /**
     * Creates a new replacement with the given ID.
     * @param id The ID to use.
     */
    public Replacement(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public ReplacementInput getInput() {
        return this.input;
    }

    public void setInput(ReplacementInput input) {
        this.input = input;
        if (input.getPermission() == null)
            input.setPermission("rosechat.replacement." + this.id);
    }

    public ReplacementOutput getOutput() {
        return this.output;
    }

    public void setOutput(ReplacementOutput output) {
        this.output = output;
    }

}
