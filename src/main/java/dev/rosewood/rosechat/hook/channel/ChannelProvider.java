package dev.rosewood.rosechat.hook.channel;

import dev.rosewood.rosechat.RoseChat;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.manager.ChannelManager;
import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import java.util.List;

public abstract class ChannelProvider {

    /**
     * @return A list of channels that this provider handles.
     */
    public abstract List<Class<? extends Channel>> getChannels();

    /**
     * This channel will be used for generating channels from the given configuration section.
     * @return The channel to use as the base for the generated channels.
     */
    public abstract Class<? extends Channel> getChannelGenerator();

    /**
     * @return A {@link CommentedConfigurationSection} containing information to be loaded by a channel.
     */
    public abstract CommentedConfigurationSection getConfigurationSection();

    /**
     * @return The name of the plugin that this channel provider supports.
     */
    public abstract String getSupportedPlugin();

    /**
     *  Registers this provider and the channels it contains.
     */
    public void register() {
        RoseChat.getInstance().getManager(ChannelManager.class).register(this);
    }

    /**
     * Uses the channel generator to create a channel dynamically.
     * @param id The id to use for the channel.
     */
    public void generateDynamicChannel(String id) {
        RoseChat.getInstance().getManager(ChannelManager.class).generateChannel(this, id);
    }

}
