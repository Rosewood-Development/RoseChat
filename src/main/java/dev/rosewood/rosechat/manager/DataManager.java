package dev.rosewood.rosechat.manager;

import dev.rosewood.rosechat.api.RoseChatAPI;
import dev.rosewood.rosechat.chat.PlayerData;
import dev.rosewood.rosechat.chat.channel.Channel;
import dev.rosewood.rosechat.database.migrations._1_Create_Tables_Data;
import dev.rosewood.rosechat.database.migrations._2_Create_Table_Hidden_Channels;
import dev.rosewood.rosechat.database.migrations._3_Add_Data_Is_Group_Chat_Column;
import dev.rosewood.rosechat.database.migrations._4_Add_Data_Stripped_Name;
import dev.rosewood.rosechat.database.migrations._5_Rename_Table_Muted_Channels;
import dev.rosewood.rosechat.hook.channel.rosechat.GroupChannel;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.database.DataMigration;
import dev.rosewood.rosegarden.manager.AbstractDataManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DataManager extends AbstractDataManager {

    private final ChannelManager channelManager;

    public DataManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.channelManager = rosePlugin.getManager(ChannelManager.class);
    }

    @Override
    public List<Class<? extends DataMigration>> getDataMigrations() {
        return Arrays.asList(
                _1_Create_Tables_Data.class,
                _2_Create_Table_Hidden_Channels.class,
                _3_Add_Data_Is_Group_Chat_Column.class,
                _4_Add_Data_Stripped_Name.class,
                _5_Rename_Table_Muted_Channels.class
        );
    }

    public PlayerData getPlayerData(UUID uuid) {
        AtomicReference<PlayerData> value = new AtomicReference<>(null);
        this.databaseConnector.connect(connection -> {
            String dataQuery = "SELECT * FROM " + this.getTablePrefix() + "player_data WHERE uuid = ?";
            try (PreparedStatement statement = connection.prepareStatement(dataQuery)) {
                statement.setString(1, uuid.toString());
                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    boolean messageSpy = result.getBoolean("has_message_spy");
                    boolean channelSpy = result.getBoolean("has_channel_spy");
                    boolean groupSpy = result.getBoolean("has_group_spy");
                    boolean canBeMessaged = result.getBoolean("can_be_messaged");
                    boolean hasTagSounds = result.getBoolean("has_tag_sounds");
                    boolean hasMessageSounds = result.getBoolean("has_message_sounds");
                    boolean hasEmojis = result.getBoolean("has_emojis");
                    String currentChannel = result.getString("current_channel");
                    String color = result.getString("chat_color");
                    long muteTime = result.getLong("mute_time");
                    String nickname = result.getString("nickname");
                    boolean isCurrentlyChannelGroupChannel = result.getBoolean("is_currently_in_gc");
                    Channel channel = isCurrentlyChannelGroupChannel ?
                            RoseChatAPI.getInstance().getGroupChatById(currentChannel) : this.channelManager.getChannel(currentChannel);
                    String strippedDisplayName = result.getString("stripped_name");

                    PlayerData playerData = new PlayerData(uuid);
                    playerData.setMessageSpy(messageSpy);
                    playerData.setChannelSpy(channelSpy);
                    playerData.setGroupSpy(groupSpy);
                    playerData.setCanBeMessaged(canBeMessaged);
                    playerData.setTagSounds(hasTagSounds);
                    playerData.setMessageSounds(hasMessageSounds);
                    playerData.setEmojis(hasEmojis);
                    playerData.setCurrentChannel(channel);
                    playerData.setColor(color);
                    playerData.setNickname(nickname);
                    playerData.setIsInGroupChannel(isCurrentlyChannelGroupChannel);
                    playerData.setDisplayName(strippedDisplayName);

                    if (muteTime > 0)
                        playerData.mute(muteTime);

                    value.set(playerData);
                } else {
                    value.set(new PlayerData(uuid));
                }
            }

            String ignoreQuery = "SELECT * FROM " + this.getTablePrefix() + "player_data_ignore WHERE ignoring_uuid = ?";
            try (PreparedStatement statement = connection.prepareStatement(ignoreQuery)) {
                statement.setString(1, uuid.toString());
                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    UUID ignored = UUID.fromString(result.getString("ignored_uuid"));
                    value.get().ignore(ignored);
                }
            }

            String channelsQuery = "SELECT * FROM " + this.getTablePrefix() + "hidden_channels WHERE uuid = ?";
            try (PreparedStatement statement = connection.prepareStatement(channelsQuery)) {
                statement.setString(1, uuid.toString());
                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    value.get().hideChannel(result.getString("channel"));
                }
            }
        });

        return value.get();
    }

    public void updatePlayerData(PlayerData playerData) {
        this.databaseConnector.connect(connection -> {

            String query = "REPLACE INTO " + this.getTablePrefix() + "player_data (uuid, has_message_spy, has_channel_spy, has_group_spy, " +
                    "can_be_messaged, has_tag_sounds, has_message_sounds, has_emojis, " +
                    "current_channel, chat_color, mute_time, nickname, is_currently_in_gc, stripped_name) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, playerData.getUUID().toString());
                statement.setBoolean(2, playerData.hasMessageSpy());
                statement.setBoolean(3, playerData.hasChannelSpy());
                statement.setBoolean(4, playerData.hasGroupSpy());
                statement.setBoolean(5, playerData.canBeMessaged());
                statement.setBoolean(6, playerData.hasTagSounds());
                statement.setBoolean(7, playerData.hasMessageSounds());
                statement.setBoolean(8, playerData.hasEmojis());
                statement.setString(9, playerData.getCurrentChannel().getId());
                statement.setString(10, playerData.getColor());
                statement.setLong(11, playerData.getMuteExpirationTime());
                statement.setString(12, playerData.getNickname());
                statement.setBoolean(13, playerData.isCurrentChannelGroupChannel());
                statement.setString(14, playerData.getStrippedDisplayName().toLowerCase());
                statement.executeUpdate();
            }
        });
    }

    public boolean containsNickname(UUID nicknamer, String nickname) {
        AtomicBoolean contains = new AtomicBoolean();

        this.databaseConnector.connect(connection -> {
            String query = "SELECT 1 FROM " + this.getTablePrefix() + "player_data WHERE stripped_name = ? AND uuid IS NOT ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, nickname.toLowerCase());
                statement.setString(2, nicknamer.toString());
                ResultSet result = statement.executeQuery();
                contains.set(result.next());
            }
        });

        return contains.get();
    }

    public void addIgnore(UUID ignoring, UUID ignored) {
        this.databaseConnector.connect(connection -> {
            String insertQuery = "INSERT INTO " + this.getTablePrefix() + "player_data_ignore (ignoring_uuid, ignored_uuid) " +
                    "VALUES(?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                statement.setString(1, ignoring.toString());
                statement.setString(2, ignored.toString());
                statement.executeUpdate();
            }
        });
    }

    public void removeIgnore(UUID ignoring, UUID ignored) {
        this.databaseConnector.connect(connection -> {
            String deleteQuery = "DELETE FROM " + this.getTablePrefix() + "player_data_ignore WHERE ignoring_uuid = ? AND ignored_uuid = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                statement.setString(1, ignoring.toString());
                statement.setString(2, ignored.toString());
                statement.executeUpdate();
            }
        });
    }

    public void hideChannel(UUID uuid, String channel) {
        this.databaseConnector.connect(connection -> {
            String insertQuery = "INSERT INTO " + this.getTablePrefix() + "hidden_channels (uuid, channel) " +
                    "VALUES(?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                statement.setString(1, uuid.toString());
                statement.setString(2, channel);
                statement.executeUpdate();
            }
        });
    }

    public void showChannel(UUID uuid, String channel) {
        this.databaseConnector.connect(connection ->  {
            String deleteQuery = "DELETE FROM " + this.getTablePrefix() + "hidden_channels WHERE uuid = ? AND channel = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                statement.setString(1, uuid.toString());
                statement.setString(2, channel);
                statement.executeUpdate();
            }
        });
    }

    public void loadChannelSettings() {
        this.databaseConnector.connect(connection -> {
            String dataQuery = "SELECT * FROM " + this.getTablePrefix() + "channel_settings";
            try (PreparedStatement statement = connection.prepareStatement(dataQuery)) {
                ResultSet result = statement.executeQuery();
                while (result.next()) {
                    Channel channel = this.channelManager.getChannel(result.getString("id"));
                    boolean muted = result.getBoolean("muted");
                    int slowmode = result.getInt("slowmode");

                    channel.setMuted(muted);
                    channel.setSlowmodeSpeed(slowmode);

                    if (channel.getSlowmodeSpeed() != 0)
                        channel.startSlowmode();
                }
            }
        });
    }

    public void saveChannelSettings(Channel channel) {
        this.databaseConnector.connect(connection -> {
            String insertQuery = "REPLACE INTO " + this.getTablePrefix() + "channel_settings (id, muted, slowmode) " +
                    "VALUES(?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                statement.setString(1, channel.getId());
                statement.setBoolean(2, channel.isMuted());
                statement.setInt(3, channel.getSlowmodeSpeed());
                statement.executeUpdate();
            }
        });
    }

    public GroupChannel getGroupChannel(String id) {
        AtomicReference<GroupChannel> value = new AtomicReference<>(null);
        this.getDatabaseConnector().connect(connection -> {
            String query = "SELECT * FROM " + this.getTablePrefix() + "group_chat WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, id);
                ResultSet result = statement.executeQuery();

                while (result.next()) {
                    GroupChannel group = new GroupChannel(id);
                    group.setName(result.getString("name"));
                    group.setOwner(UUID.fromString(result.getString("owner")));
                    value.set(group);
                }
            }
        });

        return value.get();
    }

    public List<GroupChannel> getMemberGroupChats(UUID member) {
        List<GroupChannel> groupChats = new ArrayList<>();
        this.getDatabaseConnector().connect(connection -> {
            String groupQuery = "SELECT gc.id, gc.name, gc.owner, gcm.uuid AS member_uuid FROM " + this.getTablePrefix() + "group_chat_member gcm JOIN " +
                    this.getTablePrefix() + "group_chat gc ON gc.id = gcm.group_chat WHERE gc.id IN " +
                    "(SELECT group_chat FROM " + this.getTablePrefix() + "group_chat_member WHERE uuid = ?) ORDER BY id;";

            try (PreparedStatement statement = connection.prepareStatement(groupQuery)) {
                statement.setString(1, member.toString());
                ResultSet result = statement.executeQuery();
                GroupChannel current = null;
                String previousId = "";

                while (result.next()) {
                    String id = result.getString(1);
                    if (current != null && !id.equals(previousId)) {
                        groupChats.add(current);
                        current = null;
                    }

                    if (current == null) {
                        current = new GroupChannel(id);
                        current.setName(result.getString(2));
                        current.setOwner(UUID.fromString(result.getString(3)));
                    }

                    current.getMembers().add(UUID.fromString(result.getString(4)));
                    previousId = id;
                }

                if (current != null)
                    groupChats.add(current);
            }
        });

        return groupChats;
    }

    public List<String> getGroupChatNames() {
        List<String> groupChatNames = new ArrayList<>();
        this.getDatabaseConnector().connect(connection -> {
            String getQuery = "SELECT id FROM " + this.getTablePrefix() + "group_chat";
            try (PreparedStatement statement = connection.prepareStatement(getQuery)) {
                ResultSet result = statement.executeQuery();
                while (result.next())
                    groupChatNames.add(result.getString("id"));
            }
        });

        return groupChatNames;
    }


    public void addGroupChatMember(GroupChannel groupChat, UUID member) {
        this.getDatabaseConnector().connect(connection -> {
            String insertQuery = "INSERT INTO " + this.getTablePrefix() + "group_chat_member (group_chat, uuid) " +
                    "VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                statement.setString(1, groupChat.getId());
                statement.setString(2, member.toString());
                statement.executeUpdate();
            }
        });
    }

    public void removeGroupChatMember(GroupChannel groupChat, UUID member) {
        this.getDatabaseConnector().connect(connection -> {
            String deleteQuery = "DELETE FROM " + this.getTablePrefix() + "group_chat_member WHERE group_chat = ? AND uuid = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                statement.setString(1, groupChat.getId());
                statement.setString(2, member.toString());
                statement.executeUpdate();
            }
        });
    }

    public List<UUID> getGroupChatMembers(String id) {
        List<UUID> groupChatMembers = new ArrayList<>();
        this.getDatabaseConnector().connect(connection -> {
            String membersQuery = "SELECT * FROM " + this.getTablePrefix() + "group_chat_member WHERE group_chat = ?";
            try (PreparedStatement statement = connection.prepareStatement(membersQuery)) {
                statement.setString(1, id);
                ResultSet result = statement.executeQuery();
                if (result.next())
                    groupChatMembers.add(UUID.fromString(result.getString("uuid")));
            }
        });

        return groupChatMembers;
    }

    public void createOrUpdateGroupChat(GroupChannel groupChat) {
        this.getDatabaseConnector().connect(connection -> {
            boolean create;

            String checkQuery = "SELECT 1 FROM " + this.getTablePrefix() + "group_chat WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(checkQuery)) {
                statement.setString(1, groupChat.getId());
                ResultSet result = statement.executeQuery();
                create = !result.next();
            }

            if (create) {
                String insertQuery = "INSERT INTO " + this.getTablePrefix() + "group_chat (id, name, owner) " +
                        "VALUES (?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                    statement.setString(1, groupChat.getId());
                    statement.setString(2, groupChat.getName());
                    statement.setString(3, groupChat.getOwner().toString());
                    statement.executeUpdate();
                }
            } else {
                String updateQuery = "UPDATE " + this.getTablePrefix() + "group_chat SET " +
                        "name = ?, owner = ? WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
                    statement.setString(1, groupChat.getName());
                    statement.setString(2, groupChat.getOwner().toString());
                    statement.setString(3, groupChat.getId());
                    statement.executeUpdate();
                }
            }
        });
    }

    public void deleteGroupChat(GroupChannel groupChat) {
        this.getDatabaseConnector().connect(connection -> {
            String deleteQuery = "DELETE FROM " + this.getTablePrefix() + "group_chat WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
                statement.setString(1, groupChat.getId());
                statement.executeUpdate();
            }

            String deleteMembersQuery = "DELETE FROM " + this.getTablePrefix() + "group_chat_member WHERE group_chat = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteMembersQuery)) {
                statement.setString(1, groupChat.getId());
                statement.executeUpdate();
            }
        });
    }

    public GroupManager.GroupInfo getGroupInfo(String groupId) {
        AtomicReference<GroupManager.GroupInfo> groupInfo = new AtomicReference<>();
        this.getDatabaseConnector().connect(connection -> {
            String getQuery = "SELECT COUNT(gcm.group_chat) as members, gc.id, gc.name, gc.owner FROM " +
                    this.getTablePrefix() + "group_chat_member gcm JOIN " +
                    this.getTablePrefix() + "group_chat gc ON gc.id = gcm.group_chat WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(getQuery)) {
                statement.setString(1, groupId);
                ResultSet result = statement.executeQuery();

                if (result.next()) {
                    String id = result.getString("id");
                    String name = result.getString("name");
                    String owner = result.getString("owner");
                    int members = result.getInt("members");
                    groupInfo.set(new GroupManager.GroupInfo(id, name, owner, members));
                }
            }
        });

        return groupInfo.get();
    }

    public List<GroupManager.GroupInfo> getAllGroupInfo() {
        List<GroupManager.GroupInfo> groupInfo = new ArrayList<>();
        this.getDatabaseConnector().connect(connection -> {
            String getQuery = "SELECT * FROM " + this.getTablePrefix() + "group_chat";
            try (PreparedStatement statement = connection.prepareStatement(getQuery)) {
                ResultSet result = statement.executeQuery();
                while (result.next()) {
                    String id = result.getString("id");
                    String name = result.getString("name");
                    String owner = result.getString("owner");
                    groupInfo.add(new GroupManager.GroupInfo(id, name, owner, 0));
                }
            }
        });

        return groupInfo;
    }

}
