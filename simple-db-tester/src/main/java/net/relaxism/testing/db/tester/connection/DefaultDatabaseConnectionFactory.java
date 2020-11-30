package net.relaxism.testing.db.tester.connection;

import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;

import javax.sql.DataSource;
import java.sql.SQLException;

public class DefaultDatabaseConnectionFactory implements DatabaseConnectionFactory {

    @Override
    public IDatabaseConnection getDatabaseConnection(final DataSource dataSource) throws SQLException {
        return new DatabaseDataSourceConnection(dataSource);
    }

}
