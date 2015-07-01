package net.relaxism.testing.db.tester.connection;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.dbunit.database.IDatabaseConnection;

public interface DatabaseConnectionFactory {

	IDatabaseConnection getDatabaseConnection(DataSource dataSource)
			throws SQLException;

}
