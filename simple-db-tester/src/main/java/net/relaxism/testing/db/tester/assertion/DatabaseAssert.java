package net.relaxism.testing.db.tester.assertion;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.DbUnitAssert;
import org.dbunit.assertion.Difference;
import org.dbunit.assertion.FailureHandler;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;

import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
public class DatabaseAssert extends DbUnitAssert {

    @Override
    public void assertEquals(final IDataSet expectedDataSet,
                             final IDataSet actualDataSet,
                             FailureHandler failureHandler) throws DatabaseUnitException {
        if (log.isDebugEnabled())
            log.debug(
                "assertEquals(expectedDataSet={}, actualDataSet={}, failureHandler={}) - start",
                expectedDataSet, actualDataSet, failureHandler);

        // do not continue if same instance
        if (expectedDataSet == actualDataSet) {
            return;
        }

        if (failureHandler == null) {
            log.debug("FailureHandler is null. Using default implementation");
            failureHandler = getDefaultFailureHandler();
        }

        val expectedNames = getSortedTableNames(expectedDataSet);
        val actualNames = getSortedTableNames(actualDataSet);

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
        val errorMessages = new ArrayList<String>();
        for (val name : expectedNames) {
            try {
                assertEquals(expectedDataSet.getTable(name), actualDataSet.getTable(name), failureHandler);
            } catch (AssertionError error) {
                errorMessages.add(error.getMessage());
            }
        }

        if (!errorMessages.isEmpty()) {
            throw failureHandler
                .createFailure("Comparison failure"
                    + System.lineSeparator()
                    + String.join(System.lineSeparator(), errorMessages));
        }
    }

    @Override
    protected void compareData(final ITable expectedTable,
                               final ITable actualTable,
                               final ComparisonColumn[] comparisonCols,
                               final FailureHandler failureHandler) throws DataSetException {
        log.debug("compareData(expectedTable={}, actualTable={}, comparisonCols={}, failureHandler={}) - start",
            expectedTable, actualTable, comparisonCols, failureHandler);

        if (expectedTable == null) {
            throw new NullPointerException("The parameter 'expectedTable' must not be null");
        }
        if (actualTable == null) {
            throw new NullPointerException("The parameter 'actualTable' must not be null");
        }
        if (comparisonCols == null) {
            throw new NullPointerException("The parameter 'comparisonCols' must not be null");
        }
        if (failureHandler == null) {
            throw new NullPointerException("The parameter 'failureHandler' must not be null");
        }

        val errorMessages = new ArrayList<String>();

        // iterate over all rows
        for (int i = 0; i < expectedTable.getRowCount(); i++) {
            // iterate over all columns of the current row
            for (val compareColumn : comparisonCols) {
                val columnName = compareColumn.getColumnName();
                val dataType = compareColumn.getDataType();

                val expectedValue = expectedTable.getValue(i, columnName);
                val actualValue = actualTable.getValue(i, columnName);

                // Compare the values
                if (skipCompare(columnName, expectedValue, actualValue)) {
                    if (log.isTraceEnabled()) {
                        log.trace("ignoring comparison " + expectedValue + "=" + actualValue + " on column " + columnName);
                    }
                    continue;
                }

                if (dataType.compare(expectedValue, actualValue) != 0) {
                    val diff = new Difference(expectedTable, actualTable, i, columnName, expectedValue, actualValue);

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
            throw failureHandler.createFailure(String.join(System.lineSeparator(), errorMessages));
        }
    }

}
