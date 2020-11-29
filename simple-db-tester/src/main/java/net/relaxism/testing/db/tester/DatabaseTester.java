package net.relaxism.testing.db.tester;

import lombok.Getter;
import lombok.val;
import net.relaxism.testing.db.tester.annotation.Expectation;
import net.relaxism.testing.db.tester.annotation.Preparation;
import net.relaxism.testing.db.tester.assertion.DatabaseAssertion;
import net.relaxism.testing.db.tester.context.DatabaseTesterContext;
import net.relaxism.testing.db.tester.util.ArrayUtils;
import net.relaxism.testing.db.tester.util.CollectionUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultDataSet;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Arrays;

public class DatabaseTester {

    @Getter
    private final DatabaseTesterContext context;
    @Getter
    private final Class<?> testClass;

    public DatabaseTester(final DatabaseTesterContext context, final Class<?> testClass) {
        if (context == null) {
            throw new NullPointerException("The parameter 'context' must not be null");
        }
        if (testClass == null) {
            throw new NullPointerException("The parameter 'testClass' must not be null");
        }

        this.context = context;
        this.testClass = testClass;
    }

    public void beforeTestMethod(final Method testMethod) throws FileNotFoundException, SQLException, DatabaseUnitException {
        val preparation = AnnotationUtils.getAnnotation(testMethod, Preparation.class);
        if (preparation == null)
            return;

        val dataSets = context.getDataSetLoader().loadPreparationDataSets(context, testClass, testMethod);
        if (CollectionUtils.isEmpty(dataSets)) {
            return;
        }

        val factory = context.getDatabaseConnectionFactory();
        for (val dataSet : dataSets) {
            val dataSource = dataSet.getDataSource();
            val connection = factory.getDatabaseConnection(dataSource);
            preparation.operation().getOperation().execute(connection, dataSet);
        }
    }

    public void afterTestMethod(final Method testMethod) throws FileNotFoundException, SQLException, DatabaseUnitException {
        val expectation = AnnotationUtils.getAnnotation(testMethod, Expectation.class);
        if (expectation == null)
            return;

        val dataSets = context.getDataSetLoader().loadExpectationDataSets(context, testClass, testMethod);
        if (CollectionUtils.isEmpty(dataSets)) {
            return;
        }

        val factory = context.getDatabaseConnectionFactory();
        for (val expectedDataSet : dataSets) {
            val dataSource = expectedDataSet.getDataSource();
            val connection = factory.getDatabaseConnection(dataSource);

            val databaseDataSet = connection.createDataSet();
            val actualDataSet = new DefaultDataSet();
            for (val expectedDataTable : expectedDataSet.getTables()) {
                val metaData = expectedDataTable.getTableMetaData();
                val tableName = metaData.getTableName();
                val columnNames = getColumnNames(metaData.getColumns());

                val actualMetaData = databaseDataSet.getTableMetaData(tableName);
                val primaryKeyColumnNames = getColumnNames(actualMetaData.getPrimaryKeys());

                val selectQueryOrderBy = ArrayUtils.isNotEmpty(primaryKeyColumnNames)
                    ? "ORDER BY " + String.join(", ", primaryKeyColumnNames)
                    : "";
                val selectQuery = String.format("SELECT %s FROM %s %s", String.join(", ", columnNames), tableName, selectQueryOrderBy);

                val actualTable = connection.createQueryTable(tableName, selectQuery);
                actualDataSet.addTable(actualTable);
            }

            DatabaseAssertion.assertEquals(expectedDataSet, actualDataSet);
        }
    }

    private String[] getColumnNames(final Column... columns) {
        if (columns == null) {
            return new String[0];
        }

        return Arrays.stream(columns)
            .map(Column::getColumnName)
            .toArray(String[]::new);
    }

}
