package net.relaxism.testing.db.tester.context;

import lombok.Getter;
import lombok.Setter;
import net.relaxism.testing.db.tester.connection.DatabaseConnectionFactory;
import net.relaxism.testing.db.tester.connection.DefaultDatabaseConnectionFactory;
import net.relaxism.testing.db.tester.dataset.loader.DataSetLoader;
import net.relaxism.testing.db.tester.dataset.loader.TestClassNameBasedDataSetLoader;
import net.relaxism.testing.db.tester.util.MapUtils;
import net.relaxism.testing.db.tester.util.StringUtils;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class DatabaseTesterContext {

    private Map<String, DataSource> dataSources;

    @Getter
    @Setter
    private String defaultDataSourceName = "defaultDataSource";
    @Getter
    @Setter
    private DataSetLoader dataSetLoader = new TestClassNameBasedDataSetLoader();
    @Getter
    @Setter
    private DatabaseConnectionFactory databaseConnectionFactory = new DefaultDatabaseConnectionFactory();
    @Getter
    @Setter
    private String patternMarkerText = "[Pattern]";
    @Getter
    @Setter
    private String expectFileSuffix = "-expected";

    public Map<String, DataSource> getDataSources() {
        return Collections.unmodifiableMap(dataSources);
    }

    public void setDataSources(final Map<String, DataSource> dataSources) {
        if (MapUtils.isEmpty(dataSources)) {
            throw new IllegalArgumentException("DataSource is undefined.");
        }
        this.dataSources = new TreeMap<>(dataSources);
    }

    public DataSource getDataSource(final String dataSourceName) {
        if (StringUtils.isEmpty(dataSourceName)) {
            return getDefaultDataSource();
        }
        if (!dataSources.containsKey(dataSourceName)) {
            throw new IllegalStateException("DataSource \"" + dataSourceName + "\"is undefined.");
        }
        return dataSources.get(dataSourceName);
    }

    public DataSource getDefaultDataSource() {
        if (!dataSources.containsKey(defaultDataSourceName)) {
            throw new IllegalStateException("Default DataSource is undefined.");
        }
        return dataSources.get(defaultDataSourceName);
    }

}
