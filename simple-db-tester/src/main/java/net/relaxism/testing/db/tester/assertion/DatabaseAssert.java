package net.relaxism.testing.db.tester.assertion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.DbUnitAssert;
import org.dbunit.assertion.Difference;
import org.dbunit.assertion.FailureHandler;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class DatabaseAssert extends DbUnitAssert {

	private static final Logger logger = LoggerFactory
			.getLogger(DatabaseAssert.class);

	@Override
	public void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet,
			FailureHandler failureHandler) throws DatabaseUnitException {
		if (logger.isDebugEnabled())
			logger.debug(
					"assertEquals(expectedDataSet={}, actualDataSet={}, failureHandler={}) - start",
					new Object[] { expectedDataSet, actualDataSet,
							failureHandler });

		// do not continue if same instance
		if (expectedDataSet == actualDataSet) {
			return;
		}

		if (failureHandler == null) {
			logger.debug("FailureHandler is null. Using default implementation");
			failureHandler = getDefaultFailureHandler();
		}

		String[] expectedNames = getSortedTableNames(expectedDataSet);
		String[] actualNames = getSortedTableNames(actualDataSet);

		// tables count
		if (expectedNames.length != actualNames.length) {
			throw failureHandler.createFailure("table count",
					String.valueOf(expectedNames.length),
					String.valueOf(actualNames.length));
		}

		// table names in no specific order
		for (int i = 0; i < expectedNames.length; i++) {
			if (!actualNames[i].equals(expectedNames[i])) {
				throw failureHandler.createFailure("tables",
						Arrays.asList(expectedNames).toString(),
						Arrays.asList(actualNames).toString());
			}

		}

		// tables
		final List<String> errorMessages = new ArrayList<String>();
		for (int i = 0; i < expectedNames.length; i++) {
			String name = expectedNames[i];
			try {
				assertEquals(expectedDataSet.getTable(name),
						actualDataSet.getTable(name), failureHandler);
			} catch (AssertionError error) {
				errorMessages.add(error.getMessage());
			}
		}

		if (!errorMessages.isEmpty()) {
			throw failureHandler
					.createFailure("Comparison failure"
							+ SystemUtils.LINE_SEPARATOR
							+ Joiner.on(SystemUtils.LINE_SEPARATOR).join(
									errorMessages));
		}
	}

	@Override
	protected void compareData(ITable expectedTable, ITable actualTable,
			ComparisonColumn[] comparisonCols, FailureHandler failureHandler)
			throws DataSetException {
		logger.debug("compareData(expectedTable={}, actualTable={}, "
				+ "comparisonCols={}, failureHandler={}) - start",
				new Object[] { expectedTable, actualTable, comparisonCols,
						failureHandler });

		if (expectedTable == null) {
			throw new NullPointerException(
					"The parameter 'expectedTable' must not be null");
		}
		if (actualTable == null) {
			throw new NullPointerException(
					"The parameter 'actualTable' must not be null");
		}
		if (comparisonCols == null) {
			throw new NullPointerException(
					"The parameter 'comparisonCols' must not be null");
		}
		if (failureHandler == null) {
			throw new NullPointerException(
					"The parameter 'failureHandler' must not be null");
		}

		final List<String> errorMessages = new ArrayList<String>();

		// iterate over all rows
		for (int i = 0; i < expectedTable.getRowCount(); i++) {
			// iterate over all columns of the current row
			for (int j = 0; j < comparisonCols.length; j++) {
				ComparisonColumn compareColumn = comparisonCols[j];

				String columnName = compareColumn.getColumnName();
				DataType dataType = compareColumn.getDataType();

				Object expectedValue = expectedTable.getValue(i, columnName);
				Object actualValue = actualTable.getValue(i, columnName);

				// Compare the values
				if (skipCompare(columnName, expectedValue, actualValue)) {
					if (logger.isTraceEnabled()) {
						logger.trace("ignoring comparison " + expectedValue
								+ "=" + actualValue + " on column "
								+ columnName);
					}
					continue;
				}

				if (dataType.compare(expectedValue, actualValue) != 0) {

					Difference diff = new Difference(expectedTable,
							actualTable, i, columnName, expectedValue,
							actualValue);

					// Handle the difference (throw error immediately or
					// something else)
					try {
						failureHandler.handle(diff);
					} catch (AssertionError error) {
						errorMessages.add(error.getMessage());
					}
				}
			}
		}

		if (!errorMessages.isEmpty()) {
			throw failureHandler.createFailure(Joiner.on(
					SystemUtils.LINE_SEPARATOR).join(errorMessages));
		}
	}

}
