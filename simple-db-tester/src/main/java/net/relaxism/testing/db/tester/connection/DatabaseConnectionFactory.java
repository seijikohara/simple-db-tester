package net.relaxism.testing.db.tester.connection;

import org.dbunit.database.IDatabaseConnection;

import javax.sql.DataSource;
import java.sql.SQLException;

public interface DatabaseConnectionFactory {

    IDatabaseConnection getDatabaseConnection(DataSource dataSource)
        throws SQLException;

}
