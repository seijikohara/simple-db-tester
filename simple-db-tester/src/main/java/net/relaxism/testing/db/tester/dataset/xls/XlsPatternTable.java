package net.relaxism.testing.db.tester.dataset.xls;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.relaxism.testing.db.tester.dataset.PatternTable;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

public class XlsPatternTable extends PatternTable {

	private static final Logger logger = LoggerFactory
			.getLogger(XlsPatternTable.class);

	private static final String PATTERN_MARKER = "[Pattern]";

	private final ITableMetaData metaData;

	private final Data data;

	protected class Data {
		private final Map<String, Integer> columnIndexes;
		private final List<List<Object>> data = new ArrayList<List<Object>>();

		public Data(String[] columns) {
			Map<String, Integer> columnIndexes = new TreeMap<String, Integer>();
			for (int i = 0; i < columns.length; i++) {
				columnIndexes.put(columns[i], i);
			}
			this.columnIndexes = Collections.unmodifiableMap(columnIndexes);
		}

		public void addRow(Collection<Object> rowData) {
			data.add(new ArrayList<Object>(rowData));
		}

		public Object getValue(int rowIndex, String columnName) {
			List<Object> rowData = data.get(rowIndex);
			int columnIndex = columnIndexes.get(columnName);
			return rowData.get(columnIndex);
		}

		public List<String> getColumns() {
			return new ArrayList<String>(columnIndexes.keySet());
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

	public XlsPatternTable(Sheet sheet, String... patternNames)
			throws DataSetException {
		final String sheetName = sheet.getSheetName();
		final int rowCount = sheet.getLastRowNum();

		// Column header initialize
		if (rowCount >= 0 && sheet.getRow(0) != null) {
			metaData = createMetaData(sheetName, sheet.getRow(0));
		} else {
			metaData = new DefaultTableMetaData(sheetName, new Column[0]);
		}

		// Data loading
		data = loadData(sheet, patternNames);
	}

	protected Data loadData(Sheet sheet, String... patternNames)
			throws DataSetException {
		final Column[] columns = metaData.getColumns();
		final String[] columnNames = new String[columns.length];
		for (int i = 0; i < columns.length; i++) {
			columnNames[i] = columns[i].getColumnName();
		}
		final Data data = new Data(columnNames);

		final Set<String> patternNameSet = Sets.newHashSet(patternNames);

		final boolean patterned = isPatternedSheet(sheet);
		final int columnIndexOffset = patterned ? 1 : 0;

		String currentPatternName = "!_DUMMY_!";

		final int rowCount = sheet.getLastRowNum();
		final int columnCount = metaData.getColumns().length;
		for (int rowIndex = 1; rowIndex <= rowCount; rowIndex++) {
			if (patterned) {
				Cell cell = sheet.getRow(rowIndex).getCell(0);

				String patternName = null;
				if (cell != null) {
					patternName = cell.getStringCellValue();
				}
				if (!Strings.isNullOrEmpty(patternName)) {
					currentPatternName = patternName;
				}
				if (!patternNameSet.contains(currentPatternName)) {
					continue;
				}
			}

			final List<Object> rowData = new ArrayList<Object>();
			for (int columnIndex = columnIndexOffset; columnIndex <= columnCount; columnIndex++) {
				final Object value = getValue(sheet, rowIndex, columnIndex);
				rowData.add(value);
			}
			data.addRow(rowData);
		}

		return data;
	}

	protected boolean isPatternedSheet(Sheet sheet) {
		Row row = sheet.getRow(0);
		if (row == null || row.getLastCellNum() < 1) {
			return false;
		}
		return Objects.equal(PATTERN_MARKER, row.getCell(0)
				.getRichStringCellValue().getString());
	}

	protected ITableMetaData createMetaData(String tableName,
			Row columnHeaderRow) {
		logger.debug(
				"createMetaData(tableName={}, columnHeaderRow={}) - start",
				tableName, columnHeaderRow);

		List<Object> columnList = new ArrayList<Object>();
		for (int i = 0;; i++) {
			Cell cell = columnHeaderRow.getCell(i);
			if (cell == null) {
				break;
			}

			String columnName = cell.getRichStringCellValue().getString();
			if (Objects.equal(columnName, PATTERN_MARKER)) {
				continue;
			}
			if (columnName != null) {
				columnName = columnName.trim();
			}

			// Bugfix for issue ID 2818981 - if a cell has a formatting but no
			// name also ignore it
			if (columnName.length() <= 0) {
				logger.debug(
						"The column name of column # {} is empty - will skip here assuming the last column was reached",
						String.valueOf(i));
				break;
			}

			Column column = new Column(columnName, DataType.UNKNOWN);
			columnList.add(column);
		}
		Column[] columns = columnList.toArray(new Column[0]);
		return new DefaultTableMetaData(tableName, columns);
	}

	// //////////////////////////////////////////////////////////////////////////
	// ITable interface

	@Override
	public int getRowCount() {
		logger.debug("getRowCount() - start");

		return data.getRowCount();
	}

	@Override
	public ITableMetaData getTableMetaData() {
		logger.debug("getTableMetaData() - start");

		return metaData;
	}

	@Override
	public Object getValue(int row, String column) throws DataSetException {
		if (logger.isDebugEnabled())
			logger.debug("getValue(row={}, columnName={}) - start",
					Integer.toString(row), column);

		assertValidRowIndex(row);

		return data.getValue(row, column);
	}

	/**
	 *
	 * @param rowIndex
	 *            シート上の行番号
	 * @param columnIndex
	 *            シート上の列番号
	 * @return
	 * @throws DataSetException
	 */
	public Object getValue(Sheet sheet, int rowIndex, int columnIndex)
			throws DataSetException {
		Cell cell = sheet.getRow(rowIndex).getCell(columnIndex);
		if (cell == null) {
			return null;
		}

		int type = cell.getCellType();
		switch (type) {
		case Cell.CELL_TYPE_NUMERIC:
			CellStyle style = cell.getCellStyle();
			if (DateUtil.isCellDateFormatted(cell)) {
				return getDateValue(cell);
			} else if (XlsDataSetWriter.DATE_FORMAT_AS_NUMBER_DBUNIT
					.equals(style.getDataFormatString())) {
				// The special dbunit date format
				return getDateValueFromJavaNumber(cell);
			} else {
				return getNumericValue(cell);
			}
		case Cell.CELL_TYPE_STRING:
			return cell.getRichStringCellValue().getString();
		case Cell.CELL_TYPE_FORMULA:
			throw new DataTypeException("Formula not supported at rowIndex="
					+ rowIndex + ", columnIndex=" + columnIndex);
		case Cell.CELL_TYPE_BLANK:
			return null;
		case Cell.CELL_TYPE_BOOLEAN:
			return cell.getBooleanCellValue() ? Boolean.TRUE : Boolean.FALSE;
		case Cell.CELL_TYPE_ERROR:
			throw new DataTypeException("Error at rowIndex=" + rowIndex
					+ ", columnIndex=" + columnIndex);
		default:
			throw new DataTypeException("Unsupported type at row=Index"
					+ rowIndex + ", columnIndex=" + columnIndex);
		}
	}

	protected Object getDateValueFromJavaNumber(Cell cell) {
		logger.debug("getDateValueFromJavaNumber(cell={}) - start", cell);

		double numericValue = cell.getNumericCellValue();
		BigDecimal numericValueBd = new BigDecimal(String.valueOf(numericValue));
		numericValueBd = stripTrailingZeros(numericValueBd);
		return new Timestamp(numericValueBd.longValue());
	}

	protected Object getDateValue(Cell cell) {
		logger.debug("getDateValue(cell={}) - start", cell);

		double numericValue = cell.getNumericCellValue();
		return new Timestamp(DateUtil.getJavaDate(numericValue).getTime());
	}

	/**
	 * Removes all trailing zeros from the end of the given BigDecimal value up
	 * to the decimal point.
	 *
	 * @param value
	 *            The value to be stripped
	 * @return The value without trailing zeros
	 */
	private BigDecimal stripTrailingZeros(BigDecimal value) {
		if (value.scale() <= 0) {
			return value;
		}

		String valueAsString = String.valueOf(value);
		int idx = valueAsString.indexOf(".");
		if (idx == -1) {
			return value;
		}

		for (int i = valueAsString.length() - 1; i > idx; i--) {
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
		BigDecimal result = new BigDecimal(valueAsString);
		return result;
	}

	protected BigDecimal getNumericValue(Cell cell) {
		logger.debug("getNumericValue(cell={}) - start", cell);

		String formatString = cell.getCellStyle().getDataFormatString();
		String resultString = null;
		double cellValue = cell.getNumericCellValue();

		if ((formatString != null)) {
			if (!formatString.equals("General") && !formatString.equals("@")) {
				logger.debug("formatString={}", formatString);
				DecimalFormat nf = new DecimalFormat(formatString, symbols);
				resultString = nf.format(cellValue);
			}
		}

		BigDecimal result;
		if (resultString != null) {
			try {
				result = new BigDecimal(resultString);
			} catch (NumberFormatException e) {
				logger.debug(
						"Exception occurred while trying create a BigDecimal. value={}",
						resultString);
				// Probably was not a BigDecimal format retrieved from the
				// excel. Some
				// date formats are not yet recognized by HSSF as DateFormats so
				// that
				// we could get here.
				result = toBigDecimal(cellValue);
			}
		} else {
			result = toBigDecimal(cellValue);
		}
		return result;
	}

	/**
	 * @param cellValue
	 * @return
	 * @since 2.4.6
	 */
	private BigDecimal toBigDecimal(double cellValue) {
		String resultString = String.valueOf(cellValue);
		// To ensure that intergral numbers do not have decimal point and
		// trailing zero
		// (to restore backward compatibility and provide a string
		// representation consistent with Excel)
		if (resultString.endsWith(".0")) {
			resultString = resultString.substring(0, resultString.length() - 2);
		}
		BigDecimal result = new BigDecimal(resultString);
		return result;

	}

	@Override
	public String toString() {
		return String.format("%s[name=%s,rows=%d]", getClass().getSimpleName(),
				getTableMetaData().getTableName(), getRowCount());
	}

}
