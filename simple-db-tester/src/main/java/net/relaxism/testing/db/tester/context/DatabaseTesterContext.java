package net.relaxism.testing.db.tester.context;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import javax.sql.DataSource;

import net.relaxism.testing.db.tester.connection.DatabaseConnectionFactory;
import net.relaxism.testing.db.tester.connection.DefaultDatabaseConnectionFactory;
import net.relaxism.testing.db.tester.dataset.loader.DataSetLoader;
import net.relaxism.testing.db.tester.dataset.loader.TestClassNameBasedDataSetLoader;

import com.google.common.base.Strings;

public class DatabaseTesterContext {

	private Map<String, DataSource> dataSources;
	private String defaultDataSourceName = "defaultDataSource";
	private DataSetLoader dataSetLoader = new TestClassNameBasedDataSetLoader();
	private DatabaseConnectionFactory databaseConnectionFactory = new DefaultDatabaseConnectionFactory();
	private String patternMarkerText = "[Pattern]";
	private String expectFileSuffix = "-expected";

	public Map<String, DataSource> getDataSources() {
		return Collections.unmodifiableMap(dataSources);
	}

	public void setDataSources(Map<String, DataSource> dataSources) {
		if (dataSources == null || dataSources.isEmpty()) {
			throw new IllegalArgumentException("DataSource is undefined.");
		}
		this.dataSources = new TreeMap<String, DataSource>(dataSources);
	}

	public DataSource getDataSource(String dataSourceName) {
		if (Strings.isNullOrEmpty(dataSourceName)) {
			return getDefaultDataSource();
		}
		if (!dataSources.containsKey(dataSourceName)) {
			throw new IllegalStateException("DetaSource \"" + dataSourceName
					+ "\"is undefined.");
		}
		return dataSources.get(dataSourceName);
	}

	public DataSource getDefaultDataSource() {
		if (!dataSources.containsKey(defaultDataSourceName)) {
			throw new IllegalStateException("Default DetaSource is undefined.");
		}
		return dataSources.get(defaultDataSourceName);
	}

	public String getDefaultDataSourceName() {
		return defaultDataSourceName;
	}

	public void setDefaultDataSourceName(String defaultDatasourceName) {
		this.defaultDataSourceName = defaultDatasourceName;
	}

	public DataSetLoader getDataSetLoader() {
		return dataSetLoader;
	}

	public void setDataSetLoader(DataSetLoader dataSetLoader) {
		this.dataSetLoader = dataSetLoader;
	}

	public DatabaseConnectionFactory getDatabaseConnectionFactory() {
		return databaseConnectionFactory;
	}

	public void setDatabaseConnectionFactory(
			DatabaseConnectionFactory databaseConnectionFactory) {
		this.databaseConnectionFactory = databaseConnectionFactory;
	}

	public String getPatternMarkerText() {
		return patternMarkerText;
	}

	public void setPatternMarkerText(String patternMarkerText) {
		this.patternMarkerText = patternMarkerText;
	}

	public String getExpectFileSuffix() {
		return expectFileSuffix;
	}

	public void setExpectFileSuffix(String expectFileSuffix) {
		this.expectFileSuffix = expectFileSuffix;
	}

}
