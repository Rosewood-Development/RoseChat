package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.chat.filter.Filter;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.bukkit.configuration.ConfigurationSection;

public class FilterManager extends Manager {

    private final Map<String, Filter> filters;
    private final Map<String, List<Pattern>> compiledPatterns;

    public FilterManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.filters = new HashMap<>();
        this.compiledPatterns = new HashMap<>();
    }

    @Override
    public void reload() {
        this.filters.clear();

        File filtersFolder = new File(this.rosePlugin.getDataFolder(), "filters/");
        if (!filtersFolder.exists()) {
            filtersFolder.mkdirs();
            this.rosePlugin.saveResource("filters/colors.yml", false);
            this.rosePlugin.saveResource("filters/fun.yml", false);
            this.rosePlugin.saveResource("filters/swears.yml", false);
        }

        for (File file : filtersFolder.listFiles()) {
            CommentedFileConfiguration config = CommentedFileConfiguration.loadConfiguration(file);
            for (String id : config.getKeys(false)) {
                ConfigurationSection section = config.getConfigurationSection(id);
                if (section == null)
                    continue;

                Filter filter = this.parseFilter(id, section);
                this.precompilePatterns(id, filter);
                this.filters.put(id, filter);
            }
        }
    }

    private Filter parseFilter(String id, ConfigurationSection section) {
        Filter filter = new Filter(id,
                section.getStringList("matches"),
                section.getString("prefix"), section.getString("suffix"),
                section.getStringList("inline-matches"),
                section.getString("inline-prefix"), section.getString("inline-suffix"),
                section.getString("stop"),
                section.getInt("sensitivity"),
                section.getBoolean("use-regex"), section.getBoolean("is-emoji"),
                section.getBoolean("block"),
                section.getString("message"), section.getString("sound"),
                section.getBoolean("can-toggle"), section.getBoolean("color-retention"),
                section.getBoolean("tag-players"),section.getBoolean("match-length"),
                section.getBoolean("notify-staff"),
                !section.contains("add-to-suggestions") || section.getBoolean("add-to-suggestions"),
                section.getBoolean("escapable"),
                section.getString("permission.bypass"), section.getString("permission.use"),
                section.getString("hover"), section.getString("font"),
                section.getString("replacement"), section.getString("discord-output"),
                section.getStringList("commands.server"), section.getStringList("commands.player"));

        if (section.getBoolean("is-tag"))
            this.filters.put(id + "-tag", filter.cloneAsTag());

        return filter;
    }

    private void precompilePatterns(String id, Filter filter) {
        List<Pattern> matches = new ArrayList<>();
        List<Pattern> inlineMatches = new ArrayList<>();

        if (filter.useRegex()) {
            for (String match : filter.matches())
                matches.add(Pattern.compile(match));

            if (!matches.isEmpty())
                this.compiledPatterns.put(id + "-matches", matches);

            for (String match : filter.inlineMatches())
                inlineMatches.add(Pattern.compile(match));

            if (!inlineMatches.isEmpty())
                this.compiledPatterns.put(id + "-inline-matches", inlineMatches);

            if (filter.prefix() != null && filter.inlineMatches().isEmpty())
                this.compiledPatterns.put(id + "-prefix", List.of(Pattern.compile(filter.prefix())));
        }

        if (filter.stop() != null)
            this.compiledPatterns.put(id + "-stop", List.of(Pattern.compile(filter.stop())));
    }

    @Override
    public void disable() {

    }

    public Filter getFilter(String id) {
        return this.filters.get(id);
    }

    public void addFilter(String id, Filter filter) {
        this.filters.put(id, filter);
    }

    public void deleteFilter(String id) {
        this.filters.remove(id);
    }

    public Map<String, Filter> getFilters() {
        return this.filters;
    }

    public Map<String, List<Pattern>> getCompiledPatterns() {
        return this.compiledPatterns;
    }

}
