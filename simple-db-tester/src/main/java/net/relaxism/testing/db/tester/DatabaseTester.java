package net.relaxism.testing.db.tester;

import java.lang.reflect.Method;
import java.util.Collection;

import javax.sql.DataSource;

import net.relaxism.testing.db.tester.annotation.Expectation;
import net.relaxism.testing.db.tester.annotation.Preparation;
import net.relaxism.testing.db.tester.assertion.DatabaseAssertion;
import net.relaxism.testing.db.tester.connection.DatabaseConnectionFactory;
import net.relaxism.testing.db.tester.context.DatabaseTesterContext;
import net.relaxism.testing.db.tester.dataset.PatternDataSet;

import org.apache.commons.collections.CollectionUtils;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

import com.google.common.base.Joiner;

public class DatabaseTester {

	private static final Joiner COLUMN_JOINER = Joiner.on(", ");

	private final DatabaseTesterContext context;
	private final Class<?> testClass;

	public DatabaseTester(DatabaseTesterContext context, Class<?> testClass) {
		Assert.notNull(context);
		Assert.notNull(testClass);
		this.context = context;
		this.testClass = testClass;
	}

	public void beforeTestMethod(Method testMethod) throws Exception {
		Preparation preparation = AnnotationUtils.getAnnotation(testMethod,
				Preparation.class);
		if (preparation == null)
			return;

		Collection<PatternDataSet> dataSets = context.getDataSetLoader()
				.loadPreparationDataSets(context, testClass, testMethod);
		if (CollectionUtils.isEmpty(dataSets)) {
			return;
		}

		DatabaseConnectionFactory factory = context
				.getDatabaseConnectionFactory();
		for (PatternDataSet dataSet : dataSets) {
			DataSource dataSource = dataSet.getDataSource();
			IDatabaseConnection connection = factory
					.getDatabaseConnection(dataSource);
			preparation.operation().getOperation().execute(connection, dataSet);
		}
	}

	public void afterTestMethod(Method testMethod) throws Exception {
		Expectation expectation = AnnotationUtils.getAnnotation(testMethod,
				Expectation.class);
		if (expectation == null)
			return;

		Collection<PatternDataSet> dataSets = context.getDataSetLoader()
				.loadExpectationDataSets(context, testClass, testMethod);
		if (CollectionUtils.isEmpty(dataSets)) {
			return;
		}

		DatabaseConnectionFactory factory = context
				.getDatabaseConnectionFactory();
		for (PatternDataSet expectedDataSet : dataSets) {
			DataSource dataSource = expectedDataSet.getDataSource();
			IDatabaseConnection connection = factory
					.getDatabaseConnection(dataSource);

			IDataSet databaseDataSet = connection.createDataSet();
			DefaultDataSet actualDataSet = new DefaultDataSet();
			for (ITable expectedDataTable : expectedDataSet.getTables()) {
				ITableMetaData metaData = expectedDataTable.getTableMetaData();
				String tableName = metaData.getTableName();
				String[] columnNames = getColumnNames(metaData.getColumns());

				ITableMetaData actualMetaData = databaseDataSet
						.getTableMetaData(tableName);
				String[] primaryKeyColumnNames = getColumnNames(actualMetaData
						.getPrimaryKeys());

				String selectQueryOrderBy = "";
				if (primaryKeyColumnNames.length > 0) {
					selectQueryOrderBy = "ORDER BY "
							+ COLUMN_JOINER.join(primaryKeyColumnNames);
				}
				String selectQuery = String.format("SELECT %s FROM %s %s",
						COLUMN_JOINER.join(columnNames), tableName,
						selectQueryOrderBy);

				ITable actualTable = connection.createQueryTable(tableName,
						selectQuery);
				actualDataSet.addTable(actualTable);
			}

			DatabaseAssertion.assertEquals(expectedDataSet, actualDataSet);
		}
	}

	public DatabaseTesterContext getContext() {
		return context;
	}

	public Class<?> getTestClass() {
		return testClass;
	}

	private String[] getColumnNames(Column... columns) {
		if (columns == null) {
			return new String[0];
		}

		String[] columnNames = new String[columns.length];
		for (int i = 0; i < columns.length; i++) {
			columnNames[i] = columns[i].getColumnName();
		}
		return columnNames;
	}

}
