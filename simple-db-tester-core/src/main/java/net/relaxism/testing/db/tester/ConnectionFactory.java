package net.relaxism.testing.db.tester;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;

public class ConnectionFactory {

	private final DataSource dataSource;

	public ConnectionFactory(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public IDatabaseConnection getConnection() throws SQLException {
		DatabaseDataSourceConnection connection = new DatabaseDataSourceConnection(
				dataSource);
		return connection;
	}

}
