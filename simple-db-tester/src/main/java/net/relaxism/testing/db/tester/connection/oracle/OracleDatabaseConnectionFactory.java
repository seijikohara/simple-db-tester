package net.relaxism.testing.db.tester.connection.oracle;

import net.relaxism.testing.db.tester.connection.DefaultDatabaseConnectionFactory;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.oracle.OracleConnection;

import javax.sql.DataSource;
import java.sql.SQLException;

public class OracleDatabaseConnectionFactory extends
    DefaultDatabaseConnectionFactory {

    @Override
    public IDatabaseConnection getDatabaseConnection(DataSource dataSource)
        throws SQLException {
        OracleDriverManagerDataSource oracleDataSource = (OracleDriverManagerDataSource) dataSource;

        try {
            return new OracleConnection(dataSource.getConnection(),
                oracleDataSource.getSchema());
        } catch (DatabaseUnitException e) {
            throw new IllegalStateException(e);
        }
    }

}
