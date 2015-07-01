package net.relaxism.testing.db.tester.dataset.xls;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.relaxism.testing.db.tester.dataset.PatternDataSet;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableIterator;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class XlsPatternDataSet extends PatternDataSet {

	private static final Logger logger = LoggerFactory
			.getLogger(XlsPatternDataSet.class);

	public XlsPatternDataSet(File file, String... patternNames)
			throws IOException, DataSetException {
		this(new FileInputStream(file), patternNames);
	}

	public XlsPatternDataSet(InputStream in, String... patternNames)
			throws IOException, DataSetException {
		_orderedTableNameMap = super.createTableNameMap();

		final Workbook workbook = createWorkbook(in);
		final int sheetCount = workbook.getNumberOfSheets();

		for (int i = 0; i < sheetCount; i++) {
			final ITable table = new XlsPatternTable(workbook.getSheetAt(i),
					patternNames);
			_orderedTableNameMap.add(table.getTableMetaData().getTableName(),
					table);
		}
	}

	private Workbook createWorkbook(InputStream in) throws IOException {
		try {
			return WorkbookFactory.create(in);
		} catch (InvalidFormatException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected ITableIterator createIterator(boolean reversed)
			throws DataSetException {
		if (logger.isDebugEnabled())
			logger.debug("createIterator(reversed={}) - start",
					String.valueOf(reversed));

		@SuppressWarnings("unchecked")
		ITable[] tables = (ITable[]) _orderedTableNameMap.orderedValues()
				.toArray(new ITable[0]);
		return new DefaultTableIterator(tables, reversed);
	}

	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(),
				Joiner.on(',').join(_orderedTableNameMap.getTableNames()));
	}

}
