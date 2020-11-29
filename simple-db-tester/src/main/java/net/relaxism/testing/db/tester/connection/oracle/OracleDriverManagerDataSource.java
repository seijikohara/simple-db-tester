package net.relaxism.testing.db.tester.connection.oracle;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Properties;

public class OracleDriverManagerDataSource extends DriverManagerDataSource {

    private String schema;

    public OracleDriverManagerDataSource() {
        super();
    }

    public OracleDriverManagerDataSource(String url, Properties conProps,
                                         String schema) {
        super(url, conProps);
        this.schema = schema;
    }

    public OracleDriverManagerDataSource(String url, String username,
                                         String password, String schema) {
        super(url, username, password);
        this.schema = schema;
    }

    public OracleDriverManagerDataSource(String url, String schema) {
        super(url);
        this.schema = schema;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

}
