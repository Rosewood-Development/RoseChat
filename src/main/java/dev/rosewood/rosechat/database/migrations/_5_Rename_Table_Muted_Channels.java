package dev.rosewood.rosechat.database.migrations;

import dev.rosewood.rosegarden.database.DataMigration;
import dev.rosewood.rosegarden.database.DatabaseConnector;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class _5_Rename_Table_Muted_Channels extends DataMigration {

    public _5_Rename_Table_Muted_Channels() {
        super(5);
    }

    @Override
    public void migrate(DatabaseConnector connector, Connection connection, String tablePrefix) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + tablePrefix + "muted_channels RENAME TO " + tablePrefix + "channel_settings");
            statement.execute("ALTER TABLE " + tablePrefix + "channel_settings ADD COLUMN muted BOOLEAN DEFAULT true");
            statement.execute("ALTER TABLE " + tablePrefix + "channel_settings ADD COLUMN slowmode INTEGER DEFAULT 0");
        }
    }

}
