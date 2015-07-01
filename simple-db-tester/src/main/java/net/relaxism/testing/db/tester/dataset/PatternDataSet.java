package net.relaxism.testing.db.tester.dataset;

import javax.sql.DataSource;

import org.dbunit.dataset.AbstractDataSet;

public abstract class PatternDataSet extends AbstractDataSet {

	private DataSource dataSource;

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

}
