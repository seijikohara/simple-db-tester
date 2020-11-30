package net.relaxism.testing.db.tester.dataset.xls;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.relaxism.testing.db.tester.dataset.PatternTable;
import net.relaxism.testing.db.tester.util.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.dataset.excel.XlsDataSetWriter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class XlsPatternTable extends PatternTable {

    private static final String PATTERN_MARKER = "[Pattern]";

    private final ITableMetaData metaData;

    private final Data data;

    protected static class Data {
        private final Map<String, Integer> columnIndexes;
        private final List<List<Object>> data = new ArrayList<>();

        public Data(final String[] columns) {
            columnIndexes = IntStream.range(0, columns.length)
                .boxed()
                .collect(Collectors.toMap(index -> columns[index], index -> index, (i1, i2) -> i2, TreeMap::new));
        }

        public void addRow(final Collection<Object> rowData) {
            data.add(new ArrayList<>(rowData));
        }

        public Object getValue(final int rowIndex, final String columnName) {
            val rowData = data.get(rowIndex);
            val columnIndex = columnIndexes.get(columnName);
            return rowData.get(columnIndex);
        }

        public List<String> getColumns() {
            return new ArrayList<>(columnIndexes.keySet());
        }

        public int getRowCount() {
            return data.size();
        }

        @Override
        public String toString() {
            return super.toString() + data.toString();
        }
    }

    private final DecimalFormatSymbols symbols = new DecimalFormatSymbols() {
        private static final long serialVersionUID = 9024345894289991069L;

        {
            // Needed for later "BigDecimal"/"Number" conversion
            setDecimalSeparator('.');
        }
    };

    public XlsPatternTable(final Sheet sheet, final String... patternNames) throws DataSetException {
        val sheetName = sheet.getSheetName();
        val rowCount = sheet.getLastRowNum();

        // Column header initialize
        if (rowCount >= 0 && sheet.getRow(0) != null) {
            metaData = createMetaData(sheetName, sheet.getRow(0));
        } else {
            metaData = new DefaultTableMetaData(sheetName, new Column[0]);
        }

        // Data loading
        data = loadData(sheet, patternNames);
    }

    protected Data loadData(final Sheet sheet, final String... patternNames) throws DataSetException {
        val columns = metaData.getColumns();
        val columnNames = Arrays.stream(columns)
            .map(Column::getColumnName)
            .toArray(String[]::new);
        val data = new Data(columnNames);

        val patternNameSet = Arrays.stream(patternNames).collect(Collectors.toSet());

        val patterned = isPatternedSheet(sheet);
        val columnIndexOffset = patterned ? 1 : 0;

        String currentPatternName = "!_DUMMY_!";

        val rowCount = sheet.getLastRowNum();
        val columnCount = metaData.getColumns().length;
        for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
            if (patterned) {
                val cell = sheet.getRow(rowIndex).getCell(0);

                String patternName = null;
                if (cell != null) {
                    patternName = cell.getStringCellValue();
                }
                if (StringUtils.isNotEmpty(patternName)) {
                    currentPatternName = patternName;
                }
                if (!patternNameSet.contains(currentPatternName)) {
                    continue;
                }
            }

            val rowData = new ArrayList<>();
            for (int columnIndex = columnIndexOffset; columnIndex <= columnCount; columnIndex++) {
                val value = getValue(sheet, rowIndex, columnIndex);
                rowData.add(value);
            }
            data.addRow(rowData);
        }

        return data;
    }

    protected boolean isPatternedSheet(final Sheet sheet) {
        val row = sheet.getRow(0);
        if (row == null || row.getLastCellNum() < 1) {
            return false;
        }
        return Objects.equals(PATTERN_MARKER, row.getCell(0).getRichStringCellValue().getString());
    }

    protected ITableMetaData createMetaData(final String tableName, final Row columnHeaderRow) {
        log.debug(
            "createMetaData(tableName={}, columnHeaderRow={}) - start",
            tableName, columnHeaderRow);

        val columnList = new ArrayList<>();
        for (int i = 0; ; i++) {
            val cell = columnHeaderRow.getCell(i);
            if (cell == null) {
                break;
            }

            val columnName = StringUtils.trim(cell.getRichStringCellValue().getString());
            if (Objects.equals(columnName, PATTERN_MARKER)) {
                continue;
            }

            // Bugfix for issue ID 2818981 - if a cell has a formatting but no
            // name also ignore it
            if (columnName.length() <= 0) {
                log.debug(
                    "The column name of column # {} is empty - will skip here assuming the last column was reached",
                    i);
                break;
            }

            val column = new Column(columnName, DataType.UNKNOWN);
            columnList.add(column);
        }
        val columns = columnList.toArray(new Column[0]);
        return new DefaultTableMetaData(tableName, columns);
    }

    // //////////////////////////////////////////////////////////////////////////
    // ITable interface

    @Override
    public int getRowCount() {
        log.debug("getRowCount() - start");

        return data.getRowCount();
    }

    @Override
    public ITableMetaData getTableMetaData() {
        log.debug("getTableMetaData() - start");

        return metaData;
    }

    @Override
    public Object getValue(final int row, final String column) throws DataSetException {
        if (log.isDebugEnabled())
            log.debug("getValue(row={}, columnName={}) - start",
                row, column);

        assertValidRowIndex(row);

        return data.getValue(row, column);
    }

    /**
     * @param rowIndex    シート上の行番号
     * @param columnIndex シート上の列番号
     * @return
     * @throws DataSetException
     */
    public Object getValue(final Sheet sheet, final int rowIndex, final int columnIndex) throws DataSetException {
        val cell = sheet.getRow(rowIndex).getCell(columnIndex);
        if (cell == null) {
            return null;
        }

        val type = cell.getCellTypeEnum();
        switch (type) {
            case NUMERIC:
                val style = cell.getCellStyle();
                if (DateUtil.isCellDateFormatted(cell)) {
                    return getDateValue(cell);
                } else if (XlsDataSetWriter.DATE_FORMAT_AS_NUMBER_DBUNIT.equals(style.getDataFormatString())) {
                    // The special dbunit date format
                    return getDateValueFromJavaNumber(cell);
                }
                return getNumericValue(cell);
            case STRING:
                return cell.getRichStringCellValue().getString();
            case FORMULA:
                throw new DataTypeException("Formula not supported at rowIndex=" + rowIndex + ", columnIndex=" + columnIndex);
            case BLANK:
                return null;
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case ERROR:
                throw new DataTypeException("Error at rowIndex=" + rowIndex + ", columnIndex=" + columnIndex);
            default:
                throw new DataTypeException("Unsupported type at row=Index" + rowIndex + ", columnIndex=" + columnIndex);
        }
    }

    protected Object getDateValueFromJavaNumber(final Cell cell) {
        log.debug("getDateValueFromJavaNumber(cell={}) - start", cell);

        val numericValue = cell.getNumericCellValue();
        val numericValueBigDecimal = stripTrailingZeros(new BigDecimal(numericValue));
        return new Timestamp(numericValueBigDecimal.longValue());
    }

    protected Object getDateValue(final Cell cell) {
        log.debug("getDateValue(cell={}) - start", cell);

        val numericValue = cell.getNumericCellValue();
        return new Timestamp(DateUtil.getJavaDate(numericValue).getTime());
    }

    /**
     * Removes all trailing zeros from the end of the given BigDecimal value up
     * to the decimal point.
     *
     * @param value The value to be stripped
     * @return The value without trailing zeros
     */
    private BigDecimal stripTrailingZeros(final BigDecimal value) {
        if (value.scale() <= 0) {
            return value;
        }

        String valueAsString = String.valueOf(value);
        int indexOfPeriod = valueAsString.indexOf(".");
        if (indexOfPeriod == -1) {
            return value;
        }

        for (int i = valueAsString.length() - 1; i > indexOfPeriod; i--) {
            if (valueAsString.charAt(i) == '0') {
                valueAsString = valueAsString.substring(0, i);
            } else if (valueAsString.charAt(i) == '.') {
                valueAsString = valueAsString.substring(0, i);
                // Stop when decimal point is reached
                break;
            } else {
                break;
            }
        }
        return new BigDecimal(valueAsString);
    }

    protected BigDecimal getNumericValue(final Cell cell) {
        log.debug("getNumericValue(cell={}) - start", cell);

        val cellValue = cell.getNumericCellValue();
        val formatString = cell.getCellStyle().getDataFormatString();
        final String resultString = Optional.ofNullable(formatString)
            .filter(format -> !format.equals("General"))
            .filter(format -> !format.equals("@"))
            .map(format -> {
                val decimalFormat = new DecimalFormat(format, symbols);
                return decimalFormat.format(cellValue);
            })
            .orElse(null);

        if (resultString != null) {
            try {
                return new BigDecimal(resultString);
            } catch (NumberFormatException e) {
                log.debug(
                    "Exception occurred while trying create a BigDecimal. value={}",
                    resultString);
                // Probably was not a BigDecimal format retrieved from the
                // excel. Some
                // date formats are not yet recognized by HSSF as DateFormats so
                // that
                // we could get here.
                return toBigDecimal(cellValue);
            }
        }
        return toBigDecimal(cellValue);
    }

    /**
     * @param cellValue
     * @return
     * @since 2.4.6
     */
    private BigDecimal toBigDecimal(final double cellValue) {
        val cellValueString = String.valueOf(cellValue);
        // To ensure that intergral numbers do not have decimal point and
        // trailing zero
        // (to restore backward compatibility and provide a string
        // representation consistent with Excel)
        return new BigDecimal(
            cellValueString.endsWith(".0")
                ? cellValueString.substring(0, cellValueString.length() - 2)
                : cellValueString
        );

    }

    @Override
    public String toString() {
        return String.format(
            "%s[name=%s,rows=%d]",
            getClass().getSimpleName(),
            getTableMetaData().getTableName(),
            getRowCount());
    }

}
