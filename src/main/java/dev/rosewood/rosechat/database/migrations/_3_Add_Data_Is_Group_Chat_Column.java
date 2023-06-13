package dev.rosewood.rosechat.database.migrations;

import dev.rosewood.rosegarden.database.DataMigration;
import dev.rosewood.rosegarden.database.DatabaseConnector;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _3_Add_Data_Is_Group_Chat_Column extends DataMigration {

    public _3_Add_Data_Is_Group_Chat_Column() {
        super(3);
    }

    @Override
    public void migrate(DatabaseConnector connector, Connection connection, String tablePrefix) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + tablePrefix + "player_data ADD COLUMN is_currently_in_gc BOOLEAN NOT NULL DEFAULT FALSE");
        }
    }

}
