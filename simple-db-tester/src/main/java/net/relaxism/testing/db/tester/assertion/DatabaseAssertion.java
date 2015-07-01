package net.relaxism.testing.db.tester.assertion;

import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.FailureHandler;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;

public class DatabaseAssertion {

	/**
	 * Object that will effectively do the assertions.
	 */
	private static final DatabaseAssert INSTANCE = new DatabaseAssert();

	private DatabaseAssertion() {
		throw new UnsupportedOperationException(
				"this class has only static methods");
	}

	public static void assertEqualsIgnoreCols(final IDataSet expectedDataset,
			final IDataSet actualDataset, final String tableName,
			final String[] ignoreCols) throws DatabaseUnitException {
		INSTANCE.assertEqualsIgnoreCols(expectedDataset, actualDataset,
				tableName, ignoreCols);
	}

	public static void assertEqualsIgnoreCols(final ITable expectedTable,
			final ITable actualTable, final String[] ignoreCols)
			throws DatabaseUnitException {
		INSTANCE.assertEqualsIgnoreCols(expectedTable, actualTable, ignoreCols);
	}

	public static void assertEqualsByQuery(final IDataSet expectedDataset,
			final IDatabaseConnection connection, final String sqlQuery,
			final String tableName, final String[] ignoreCols)
			throws DatabaseUnitException, SQLException {
		INSTANCE.assertEqualsByQuery(expectedDataset, connection, sqlQuery,
				tableName, ignoreCols);
	}

	public static void assertEqualsByQuery(final ITable expectedTable,
			final IDatabaseConnection connection, final String tableName,
			final String sqlQuery, final String[] ignoreCols)
			throws DatabaseUnitException, SQLException {
		INSTANCE.assertEqualsByQuery(expectedTable, connection, tableName,
				sqlQuery, ignoreCols);
	}

	public static void assertEquals(IDataSet expectedDataSet,
			IDataSet actualDataSet) throws DatabaseUnitException {
		INSTANCE.assertEquals(expectedDataSet, actualDataSet);
	}

	public static void assertEquals(IDataSet expectedDataSet,
			IDataSet actualDataSet, FailureHandler failureHandler)
			throws DatabaseUnitException {
		INSTANCE.assertEquals(expectedDataSet, actualDataSet, failureHandler);
	}

	public static void assertEquals(ITable expectedTable, ITable actualTable)
			throws DatabaseUnitException {
		INSTANCE.assertEquals(expectedTable, actualTable);
	}

	public static void assertEquals(ITable expectedTable, ITable actualTable,
			Column[] additionalColumnInfo) throws DatabaseUnitException {
		INSTANCE.assertEquals(expectedTable, actualTable, additionalColumnInfo);
	}

	public static void assertEquals(ITable expectedTable, ITable actualTable,
			FailureHandler failureHandler) throws DatabaseUnitException {
		INSTANCE.assertEquals(expectedTable, actualTable, failureHandler);
	}

}
