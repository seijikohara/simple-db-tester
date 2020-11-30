package net.relaxism.testing.db.tester.connection.oracle;

import lombok.Getter;
import lombok.Setter;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Properties;

public class OracleDriverManagerDataSource extends DriverManagerDataSource {

    @Getter
    @Setter
    private String schema;

    public OracleDriverManagerDataSource() {
        super();
    }

    public OracleDriverManagerDataSource(final String url, final Properties properties, final String schema) {
        super(url, properties);
        this.schema = schema;
    }

    public OracleDriverManagerDataSource(final String url, final String username, final String password, final String schema) {
        super(url, username, password);
        this.schema = schema;
    }

    public OracleDriverManagerDataSource(final String url, final String schema) {
        super(url);
        this.schema = schema;
    }

}
