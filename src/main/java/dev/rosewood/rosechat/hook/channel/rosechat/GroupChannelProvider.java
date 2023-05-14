package dev.rosewood.rosechat.hook.channel.rosechat;

import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.hook.channel.ChannelProvider;
import dev.rosewood.rosegarden.config.CommentedConfigurationSection;
import java.util.List;

public class GroupChannelProvider extends ChannelProvider {

    @Override
    public List<Class<? extends Channel>> getChannels() {
        return null;
    }

    @Override
    public Class<? extends Channel> getChannelGenerator() {
        return GroupChannel.class;
    }

    @Override
    public CommentedConfigurationSection getConfigurationSection() {
        return null;
    }

    @Override
    public String getSupportedPlugin() {
        return "RoseChat";
    }

}
