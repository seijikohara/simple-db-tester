package net.relaxism.testing.db.tester.dataset;

import org.dbunit.dataset.AbstractDataSet;

import javax.sql.DataSource;

public abstract class PatternDataSet extends AbstractDataSet {

    private DataSource dataSource;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

}
