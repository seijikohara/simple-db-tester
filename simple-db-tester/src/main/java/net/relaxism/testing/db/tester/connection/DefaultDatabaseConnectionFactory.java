package net.relaxism.testing.db.tester.connection;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;

public class DefaultDatabaseConnectionFactory implements
		DatabaseConnectionFactory {

	@Override
	public IDatabaseConnection getDatabaseConnection(DataSource dataSource)
			throws SQLException {
		DatabaseDataSourceConnection connection = new DatabaseDataSourceConnection(
				dataSource);
		return connection;
	}

}
