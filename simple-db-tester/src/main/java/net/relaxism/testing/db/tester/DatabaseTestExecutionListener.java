package net.relaxism.testing.db.tester;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.relaxism.testing.db.tester.annotation.DataSet;
import net.relaxism.testing.db.tester.annotation.Expectation;
import net.relaxism.testing.db.tester.annotation.Preparation;
import net.relaxism.testing.db.tester.dataset.XlsPatternDataSet;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

public class DatabaseTestExecutionListener extends
		AbstractTestExecutionListener {

	private static final Logger logger = LoggerFactory
			.getLogger(DatabaseTestExecutionListener.class);

	public final Map<String, DataSource> dataSources = new HashMap<String, DataSource>();

	private static final Joiner COLUMN_JOINER = Joiner.on(" ");

	@Override
	public void prepareTestInstance(TestContext testContext) throws Exception {
		final Map<String, DataSource> dataSources = testContext
				.getApplicationContext().getBeansOfType(DataSource.class);
		Assert.notEmpty(dataSources);
		this.dataSources.putAll(dataSources);

		if (!this.dataSources.containsKey(DataSet.DEFAULT_DATA_SOURCE_NAME)) {
			logger.warn("Not defined \"dataSource\" bean.");
		}
	}

	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		super.beforeTestClass(testContext);
	}

	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
	}

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		Preparation preparation = AnnotationUtils.getAnnotation(
				testContext.getTestMethod(), Preparation.class);
		if (preparation == null)
			return;

		logger.info("Prepare data set : " + preparation);
		for (DataSet dataSetAnnotation : preparation.dataSets()) {
			XlsPatternDataSet xlsDataSet = loadExcelDataSet(
					testContext.getTestClass(), testContext.getTestMethod(),
					dataSetAnnotation.resourceLocation(),
					dataSetAnnotation.patternNames(), DefaultFileSuffix.BEFORE);

			DataSource dataSource = dataSources.get(dataSetAnnotation
					.dataSourceName());

			ConnectionFactory factory = new ConnectionFactory(dataSource);
			IDatabaseConnection connection = factory.getConnection();
			preparation.operation().getOperation()
					.execute(connection, xlsDataSet);
		}
	}

	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
		Expectation expectation = AnnotationUtils.getAnnotation(
				testContext.getTestMethod(), Expectation.class);
		if (expectation == null)
			return;

		logger.info("Validate data set : " + expectation);
		for (DataSet dataSetAnnotation : expectation.dataSets()) {
			XlsPatternDataSet expectedDataSet = loadExcelDataSet(
					testContext.getTestClass(), testContext.getTestMethod(),
					dataSetAnnotation.resourceLocation(),
					dataSetAnnotation.patternNames(), DefaultFileSuffix.AFTER);

			DataSource dataSource = dataSources.get(dataSetAnnotation
					.dataSourceName());
			ConnectionFactory factory = new ConnectionFactory(dataSource);
			IDatabaseConnection connection = factory.getConnection();

			IDataSet databaseDataSet = connection.createDataSet();
			DefaultDataSet actualDataSet = new DefaultDataSet();
			for (ITable xlsDataTable : expectedDataSet.getTables()) {
				ITableMetaData metaData = xlsDataTable.getTableMetaData();
				String tableName = metaData.getTableName();
				String[] columnNames = getColumnNames(metaData.getColumns());

				ITableMetaData actualMetaData = databaseDataSet
						.getTableMetaData(tableName);
				String[] primaryKeyColumnNames = getColumnNames(actualMetaData
						.getPrimaryKeys());
				String selectStatement = "SELECT "
						+ Joiner.on(", ").join(columnNames)
						+ " FROM "
						+ tableName
						+ (primaryKeyColumnNames.length > 0 ? " ORDER BY "
								+ COLUMN_JOINER.join(primaryKeyColumnNames)
								: "");

				ITable actualTable = connection.createQueryTable(tableName,
						selectStatement);
				actualDataSet.addTable(actualTable);
			}

			DatabaseAssertion.assertEquals(expectedDataSet, actualDataSet);
		}
	}

	private XlsPatternDataSet loadExcelDataSet(final Class<?> testClass,
			final Method testMethod, final String resourceLocation,
			String[] patternNames, final DefaultFileSuffix suffix)
			throws DataSetException, IOException {
		File file = loadExcelFile(testClass, resourceLocation, suffix);

		if (patternNames == null || patternNames.length < 1) {
			patternNames = new String[] { testMethod.getName() };
		}

		XlsPatternDataSet xlsDataSet = new XlsPatternDataSet(file, patternNames);
		return xlsDataSet;
	}

	private File loadExcelFile(final Class<?> testClass,
			String resourceLocation, final DefaultFileSuffix suffix)
			throws FileNotFoundException {
		if (Strings.isNullOrEmpty(resourceLocation)) {
			// デフォルトではテストケースクラス名と同名のxlsファイルを読み込む
			resourceLocation = ResourceUtils.CLASSPATH_URL_PREFIX
					+ testClass.getName().replace('.', '/') + suffix + ".xls";
		}
		File file = ResourceUtils.getFile(resourceLocation);
		Assert.isTrue(file.exists());
		return file;
	}

	enum DefaultFileSuffix {
		BEFORE(""), AFTER("-expected");
		private final String fileSuffix;

		private DefaultFileSuffix(String fileSuffix) {
			this.fileSuffix = fileSuffix;
		}

		@Override
		public String toString() {
			return fileSuffix;
		}
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

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
