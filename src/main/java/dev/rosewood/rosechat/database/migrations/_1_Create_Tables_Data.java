package dev.rosewood.rosechat.database.migrations;

import dev.rosewood.rosegarden.database.DataMigration;
import dev.rosewood.rosegarden.database.DatabaseConnector;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _1_Create_Tables_Data extends DataMigration {

    public _1_Create_Tables_Data() {
        super(1);
    }

    @Override
    public void migrate(DatabaseConnector connector, Connection connection, String tablePrefix) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE " + tablePrefix + "player_data (" +
                    "uuid VARCHAR(36) UNIQUE NOT NULL, " +
                    "has_message_spy BOOLEAN NOT NULL, " +
                    "has_channel_spy BOOLEAN NOT NULL, " +
                    "has_group_spy BOOLEAN NOT NULL, " +
                    "can_be_messaged BOOLEAN NOT NULL, " +
                    "has_tag_sounds BOOLEAN NOT NULL, " +
                    "has_message_sounds BOOLEAN NOT NULL, " +
                    "has_emojis BOOLEAN NOT NULL, " +
                    "current_channel VARCHAR(255) NOT NULL, " +
                    "chat_color VARCHAR(32) NOT NULL, " +
                    "mute_time BIGINT NOT NULL, " +
                    "nickname VARCHAR(255)" +
                    ")");

            statement.executeUpdate("CREATE TABLE " + tablePrefix + "group_chat (" +
                    "id VARCHAR(255) NOT NULL," +
                    "name VARCHAR(255) NOT NULL, " +
                    "owner VARCHAR(36) NOT NULL, " +
                    "UNIQUE (owner)" +
                    ")");

            statement.executeUpdate("CREATE TABLE " + tablePrefix + "group_chat_member (" +
                    "group_chat INTEGER NOT NULL, " +
                    "uuid VARCHAR(36) NOT NULL, " +
                    "UNIQUE (group_chat, uuid)" +
                    ")");

            statement.executeUpdate("CREATE TABLE " + tablePrefix + "player_data_ignore (" +
                    "ignoring_uuid VARCHAR(36) NOT NULL, " +
                    "ignored_uuid VARCHAR(36) NOT NULL" +
                    ")");

            statement.executeUpdate("CREATE TABLE " + tablePrefix + "muted_channels (" +
                    "id VARCHAR(255) UNIQUE NOT NULL" +
                    ")");
        }
    }

}
