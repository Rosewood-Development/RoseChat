package dev.rosewood.rosechat.config;

import dev.rosewood.rosegarden.config.SettingSerializer;
import dev.rosewood.rosegarden.config.SettingSerializers;
import org.bukkit.event.EventPriority;

public final class ChatSettingSerializers {

    public static final SettingSerializer<EventPriority> EVENT_PRIORITY = SettingSerializers.ofEnum(EventPriority.class);

}
