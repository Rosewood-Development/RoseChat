package dev.rosewood.rosechat.placeholder;

import org.bukkit.configuration.ConfigurationSection;
import java.util.LinkedList;
import java.util.List;

public class DiscordEmbedPlaceholder extends CustomPlaceholder {

    private final List<CustomPlaceholder> fields;

    public DiscordEmbedPlaceholder(String id) {
        super(id);

        this.fields = new LinkedList<>();
    }

    public void parse(ConfigurationSection configurationSection) {
        for (String option : configurationSection.getKeys(false)) {
            String condition;

            if (option.equalsIgnoreCase("image") || option.equalsIgnoreCase("thumbnail")) {
                if (configurationSection.contains(option + ".url")) {
                    condition = configurationSection.getString(option + ".url.condition");
                    this.placeholders.put(option + ".url",
                            ConditionManager.getCondition(configurationSection.getConfigurationSection(option + ".url"), condition)
                                    .parseValues());
                }

                if (configurationSection.contains(option + ".height")) {
                    condition = configurationSection.getString(option + ".height.condition");
                    this.placeholders.put(option + ".height",
                            ConditionManager.getCondition(configurationSection.getConfigurationSection(option + ".height"), condition)
                                    .parseValues());
                }

                if (configurationSection.contains(option + ".width")) {
                    condition = configurationSection.getString(option + ".width.condition");
                    this.placeholders.put(option + ".width",
                            ConditionManager.getCondition(configurationSection.getConfigurationSection(option + ".width"), condition)
                                    .parseValues());
                }
            } else if (option.equalsIgnoreCase("author")) {
                if (configurationSection.contains("author.name")) {
                    condition = configurationSection.getString("author.name.condition");
                    this.placeholders.put("author.name",
                            ConditionManager.getCondition(configurationSection.getConfigurationSection("author.name"), condition)
                                    .parseValues());
                }

                if (configurationSection.contains("author.url")) {
                    condition = configurationSection.getString("author.url.condition");
                    this.placeholders.put("author.url",
                            ConditionManager.getCondition(configurationSection.getConfigurationSection("author.url"), condition)
                                    .parseValues());
                }

                if (configurationSection.contains("author.icon-url")) {
                    condition = configurationSection.getString("author.icon-url.condition");
                    this.placeholders.put("author.icon-url",
                            ConditionManager.getCondition(configurationSection.getConfigurationSection("author.icon-url"), condition)
                                    .parseValues());
                }
            } else if (option.equalsIgnoreCase("footer")) {
                if (configurationSection.contains("footer.text")) {
                    condition = configurationSection.getString("footer.text.condition");
                    this.placeholders.put("footer.text",
                            ConditionManager.getCondition(configurationSection.getConfigurationSection("footer.text"), condition)
                                    .parseValues());
                }

                if (configurationSection.contains("footer.icon-url")) {
                    condition = configurationSection.getString("footer.icon-url.condition");
                    this.placeholders.put("footer.icon-url",
                            ConditionManager.getCondition(configurationSection.getConfigurationSection("footer.icon-url"), condition)
                                    .parseValues());
                }
            } else if (option.equalsIgnoreCase("fields")) {
                for (String field : configurationSection.getConfigurationSection("fields").getKeys(false)) {
                    CustomPlaceholder fieldPlaceholder = new CustomPlaceholder("fields");

                    condition = configurationSection.getString("fields." + field + ".name.condition");
                    fieldPlaceholder.add("name",
                            ConditionManager.getCondition(configurationSection.getConfigurationSection("fields." + field + ".name"), condition)
                                    .parseValues());

                    condition = configurationSection.getString("fields." + field + ".value.condition");
                    fieldPlaceholder.add("value",
                            ConditionManager.getCondition(configurationSection.getConfigurationSection("fields." + field + ".value"), condition)
                                    .parseValues());

                    condition = configurationSection.getString("fields." + field + ".inline.condition");
                    fieldPlaceholder.add("inline",
                            ConditionManager.getCondition(configurationSection.getConfigurationSection("fields." + field + ".inline"), condition)
                                    .parseValues());

                    this.fields.add(fieldPlaceholder);
                }
            } else {
                condition = configurationSection.getString(option + ".condition");
                this.placeholders.put(option,
                        ConditionManager.getCondition(configurationSection.getConfigurationSection(option), condition)
                                .parseValues());
            }
        }
    }

    public List<CustomPlaceholder> getFields() {
        return this.fields;
    }

}
