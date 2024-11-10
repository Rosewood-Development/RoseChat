package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.chat.replacement.Replacement;
import dev.rosewood.rosechat.chat.replacement.ReplacementInput;
import dev.rosewood.rosechat.chat.replacement.ReplacementOutput;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.rosegarden.utils.NMSUtil;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ReplacementManager extends Manager {

    private final Map<String, Replacement> replacements;
    private final Map<String, Pattern> compiledPatterns;

    public ReplacementManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.replacements = new HashMap<>();
        this.compiledPatterns = new HashMap<>();
    }

    @Override
    public void reload() {
        this.replacements.clear();

        File replacementsFile = new File(this.rosePlugin.getDataFolder(), "replacements.yml");
        if (!replacementsFile.exists())
            this.rosePlugin.saveResource("replacements.yml", false);

        CommentedFileConfiguration replacementsConfiguration = CommentedFileConfiguration.loadConfiguration(replacementsFile);

        for (String id : replacementsConfiguration.getKeys(false)) {
            if (!replacementsConfiguration.contains(id + ".input") || !replacementsConfiguration.contains(id + ".output"))
                continue;

            ReplacementInput input = this.parseReplacementInput(replacementsConfiguration.getConfigurationSection(id + ".input"));
            ReplacementOutput output = this.parseReplacementOutput(replacementsConfiguration.getConfigurationSection(id + ".output"), false);

            Replacement replacement = this.createReplacement(id, input, output);
            if (replacementsConfiguration.contains(id + ".input.has-closing-tag")
                    && replacementsConfiguration.getBoolean(id + ".input.has-closing-tag"))
                this.generateClosingTag(replacement);
        }

        File colorsFile = new File(this.rosePlugin.getDataFolder(), "colors.yml");
        if (!colorsFile.exists())
            this.rosePlugin.saveResource("colors.yml", false);

        CommentedFileConfiguration colorsConfiguration = CommentedFileConfiguration.loadConfiguration(colorsFile);

        for (String id : colorsConfiguration.getKeys(false)) {
            if (!colorsConfiguration.contains(id + ".input") || !colorsConfiguration.contains(id + ".output"))
                continue;

            ReplacementInput input = this.parseReplacementInput(colorsConfiguration.getConfigurationSection(id + ".input"));
            ReplacementOutput output = this.parseReplacementOutput(colorsConfiguration.getConfigurationSection(id + ".output"),
                    input.getPrefix() == null);

            Replacement replacement = this.createReplacement(id, input, output);
            if (colorsConfiguration.contains(id + ".input.has-closing-tag")
                    && colorsConfiguration.getBoolean(id + ".input.has-closing-tag"))
                this.generateClosingTag(replacement);
        }
    }

    private Replacement createReplacement(String id, ReplacementInput input, ReplacementOutput output) {
        // Precompile regex strings.
        if (input.isRegex() || input.isContentRegex() || input.isInlineRegex()) {
            if (input.getText() != null) {
                this.compiledPatterns.put(id + "-text", Pattern.compile(input.getText()));
            }
        }

        if (input.isRegex()) {
            if (input.getPrefix() != null) {
                this.compiledPatterns.put(id + "-prefix", Pattern.compile(input.getPrefix()));
            }

            if (input.getSuffix() != null) {
                this.compiledPatterns.put(id + "-suffix", Pattern.compile(input.getSuffix()));
            }

            if (input.getInlinePrefix() != null) {
                this.compiledPatterns.put(id + "-inline-prefix", Pattern.compile(input.getInlinePrefix()));
            }

            if (input.getInlineSuffix() != null) {
                this.compiledPatterns.put(id + "-inline-suffix", Pattern.compile(input.getInlineSuffix()));
            }
        }

        // The stop should always be parsed as regex.
        if (input.getStop() != null) {
            this.compiledPatterns.put(id + "-stop", Pattern.compile(input.getStop()));
        }

        Replacement replacement = new Replacement(id);
        replacement.setInput(input);
        replacement.setOutput(output);
        this.replacements.put(id, replacement);

        return replacement;
    }

    private ReplacementInput parseReplacementInput(ConfigurationSection inputSection) {
        ReplacementInput input = new ReplacementInput();

        for (String key : inputSection.getKeys(false)) {
            switch (key) {
                case "text" -> input.setText(inputSection.getString("text"));
                case "prefix" -> input.setPrefix(inputSection.getString("prefix"));
                case "suffix" -> input.setSuffix(inputSection.getString("suffix"));
                case "stop" -> input.setStop(inputSection.getString("stop"));
                case "inline-prefix" -> input.setInlinePrefix(inputSection.getString("inline-prefix"));
                case "inline-suffix" -> input.setInlineSuffix(inputSection.getString("inline-suffix"));
                case "is-regex" -> input.setIsRegex(inputSection.getBoolean("is-regex"));
                case "is-content-regex" -> input.setIsContentRegex(inputSection.getBoolean("is-content-regex"));
                case "is-inline-regex" -> input.setIsInlineRegex(inputSection.getBoolean("is-inline-regex"));
                case "can-toggle" -> input.setCanToggle(inputSection.getBoolean("can-toggle"));
                case "is-emoji" -> input.setIsEmoji(inputSection.getBoolean("is-emoji"));
                case "permission" -> input.setPermission(inputSection.getString("permission"));
            }
        }

        return input;
    }

    private ReplacementOutput parseReplacementOutput(ConfigurationSection outputSection, boolean forceColorRetention) {
        ReplacementOutput output = new ReplacementOutput();

        if (forceColorRetention)
            output.setHasColorRetention(true);

        for (String key : outputSection.getKeys(false)) {
            switch (key) {
                case "text" -> output.setText(outputSection.getString("text"));
                case "hover" -> output.setHover(outputSection.getString("hover"));
                case "tag-online-players" -> output.setShouldTagOnlinePlayers(outputSection.getBoolean("tag-online-players"));
                case "match-length" -> output.setShouldMatchLength(outputSection.getBoolean("match-length"));
                case "color-retention" -> output.setHasColorRetention(outputSection.getBoolean("color-retention"));
                case "font" -> output.setFont(outputSection.getString("font"));
                case "discord-output" -> output.setDiscordOutput(outputSection.getString("discord-output"));
                case "sound" -> {
                    try {
                        String value = outputSection.getString("sound", "");
                        Sound sound;
                        if (NMSUtil.getVersionNumber() > 21 || (NMSUtil.getVersionNumber() == 21 && NMSUtil.getMinorVersionNumber() >= 3)) {
                            sound = Registry.SOUNDS.match(value);
                        } else {
                            sound = Sound.valueOf(value.toUpperCase());
                        }
                        output.setSound(sound);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        return output;
    }

    /**
     * Auto generates a closing tag based on the prefix.
     * This allows user to create a simple replacement like "<red>" and
     * automatically add a second tag for "<red>text here</red>".
     * @param replacement The replacement to grab info from.
     */
    private void generateClosingTag(Replacement replacement) {
        if (replacement.getInput().getText() == null)
            return;

        // Grab the text and convert it into a suffix by using the first and last chars
        // A prefix like "<red>" will make a suffix of "</red>".
        ReplacementInput input = new ReplacementInput(replacement.getInput());
        String prefix = input.getText();
        String suffix = prefix.charAt(0) + "/" + prefix.substring(1, prefix.length() - 1) + prefix.charAt(prefix.length() - 1);
        input.setText(null);
        input.setPrefix(prefix);
        input.setSuffix(suffix);

        // Grab the output text and convert it into something that can be used by prefixes and suffixes.
        // Adds the group_1 placeholder for the text inside the prefix and suffix,
        ReplacementOutput output = new ReplacementOutput(replacement.getOutput());
        String text = output.getText();
        output.setText(text + "%group_1%");
        output.setHasColorRetention(false);

        this.createReplacement(replacement.getId() + "-tag", input, output);
    }

    @Override
    public void disable() {

    }

    public Replacement getReplacement(String id) {
        return this.replacements.get(id);
    }

    public void addReplacement(Replacement replacement) {
        this.replacements.put(replacement.getId(), replacement);
    }

    public void deleteReplacement(Replacement replacement) {
        this.replacements.remove(replacement.getId());
    }

    public Map<String, Replacement> getReplacements() {
        return this.replacements;
    }

    public Map<String, Pattern> getCompiledPatterns() {
        return this.compiledPatterns;
    }

}
