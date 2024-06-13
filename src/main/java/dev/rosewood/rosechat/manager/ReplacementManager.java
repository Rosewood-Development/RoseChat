package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.chat.replacement.Replacement;
import dev.rosewood.rosechat.chat.replacement.ReplacementInput;
import dev.rosewood.rosechat.chat.replacement.ReplacementOutput;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
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

            this.createReplacement(id, input, output);
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

            this.createReplacement(id, input, output);
        }
    }

    private void createReplacement(String id, ReplacementInput input, ReplacementOutput output) {
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
                case "is-emoji" -> input.setIsEmoji(inputSection.getBoolean("is-emoji"));
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
                case "sound" -> {
                    try {
                        Sound sound = Sound.valueOf(outputSection.getString("sound").toUpperCase());
                        output.setSound(sound);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        return output;
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
