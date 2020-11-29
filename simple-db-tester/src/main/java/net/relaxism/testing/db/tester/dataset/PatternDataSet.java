package net.relaxism.testing.db.tester.dataset;

import lombok.Getter;
import lombok.Setter;
import org.dbunit.dataset.AbstractDataSet;

import javax.sql.DataSource;

public abstract class PatternDataSet extends AbstractDataSet {

    @Getter
    @Setter
    private DataSource dataSource;

}
