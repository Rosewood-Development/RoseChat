package dev.rosewood.rosechat.config;

import dev.rosewood.rosegarden.config.RoseSettingSerializer;
import dev.rosewood.rosegarden.config.RoseSettingSerializers;
import org.bukkit.event.EventPriority;

public final class SettingSerializers {

    public static final RoseSettingSerializer<EventPriority> EVENT_PRIORITY = RoseSettingSerializers.createMapped(RoseSettingSerializers.STRING, EventPriority::name, EventPriority::valueOf);

}
