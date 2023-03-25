package dev.rosewood.rosechat.database.migrations;

import dev.rosewood.rosegarden.database.DataMigration;
import dev.rosewood.rosegarden.database.DatabaseConnector;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _2_Create_Table_Hidden_Channels extends DataMigration {

    public _2_Create_Table_Hidden_Channels() {
        super(2);
    }

    @Override
    public void migrate(DatabaseConnector connector, Connection connection, String tablePrefix) throws SQLException {
        // Create hidden channels table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + tablePrefix + "hidden_channels (" +
                    "uuid VARCHAR(36) NOT NULL, " +
                    "channel VARCHAR(255) NOT NULL" +
                    ")");
        }
    }

}
