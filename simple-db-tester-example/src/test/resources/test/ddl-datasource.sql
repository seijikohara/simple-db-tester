DROP TABLE IF EXISTS TABLE1;
CREATE TABLE TABLE1
(
    ID               INT PRIMARY KEY,
    COLUMN_CHAR      CHAR(10),
    COLUMN_VARCHAR   VARCHAR(100),
    COLUMN_NUMBER    DECIMAL,
    COLUMN_DATE      DATE,
    COLUMN_TIMESTAMP TIMESTAMP
);

DROP TABLE IF EXISTS TABLE2;
CREATE TABLE TABLE2
(
    ID               INT PRIMARY KEY,
    COLUMN_CHAR      CHAR(10),
    COLUMN_VARCHAR   VARCHAR(100),
    COLUMN_NUMBER    DECIMAL,
    COLUMN_DATE      DATE,
    COLUMN_TIMESTAMP TIMESTAMP
);
