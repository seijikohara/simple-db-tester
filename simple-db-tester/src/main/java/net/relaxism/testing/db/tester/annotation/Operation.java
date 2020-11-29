package net.relaxism.testing.db.tester.annotation;

import org.dbunit.operation.DatabaseOperation;

public enum Operation {

    NONE(DatabaseOperation.NONE),

    /**
     * Updates the contents of existing database tables from the dataset.
     */
    UPDATE(DatabaseOperation.UPDATE),

    /**
     * Inserts new database tables and contents from the dataset.
     */
    INSERT(DatabaseOperation.INSERT),

    /**
     * Refresh the contents of existing database tables. Rows from the dataset
     * will insert or replace existing data. Any database rows that are not in
     * the dataset remain unaffected.
     */
    REFRESH(DatabaseOperation.REFRESH),

    /**
     * Deletes database table rows that matches rows from the dataset.
     */
    DELETE(DatabaseOperation.DELETE),

    /**
     * Deletes all rows from a database table when the table is specified in the
     * dataset. Tables in the database but not in the dataset remain unaffected.
     *
     * @see #TRUNCATE_TABLE
     */
    DELETE_ALL(DatabaseOperation.DELETE_ALL),

    /**
     * Deletes all rows from a database table when the table is specified in the
     * dataset. Tables in the database but not in the dataset are unaffected.
     * Identical to {@link #DELETE_ALL} expect this operation cannot be rolled
     * back and is supported by less database vendors.
     *
     * @see #DELETE_ALL
     */
    TRUNCATE_TABLE(DatabaseOperation.TRUNCATE_TABLE),

    /**
     * Deletes all rows from a database table when the tables is specified in
     * the dataset and subsequently insert new contents. Equivalent to calling
     * {@link #DELETE_ALL} followed by {@link #INSERT}.
     */
    CLEAN_INSERT(DatabaseOperation.CLEAN_INSERT);

    private final DatabaseOperation operation;

    private Operation(DatabaseOperation operation) {
        this.operation = operation;
    }

    public DatabaseOperation getOperation() {
        return operation;
    }

}
