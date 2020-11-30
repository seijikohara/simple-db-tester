package net.relaxism.testing.db.tester.dataset.xls;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.relaxism.testing.db.tester.dataset.PatternDataSet;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableIterator;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class XlsPatternDataSet extends PatternDataSet {

    public XlsPatternDataSet(File file, String... patternNames) throws IOException, DataSetException {
        this(new FileInputStream(file), patternNames);
    }

    public XlsPatternDataSet(InputStream in, String... patternNames) throws IOException, DataSetException {
        _orderedTableNameMap = super.createTableNameMap();

        val workbook = createWorkbook(in);
        val sheetCount = workbook.getNumberOfSheets();

        for (int i = 0; i < sheetCount; i++) {
            val table = new XlsPatternTable(workbook.getSheetAt(i), patternNames);
            _orderedTableNameMap.add(table.getTableMetaData().getTableName(), table);
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
    protected ITableIterator createIterator(boolean reversed) throws DataSetException {
        if (log.isDebugEnabled())
            log.debug("createIterator(reversed={}) - start", String.valueOf(reversed));

        val tables = (ITable[]) _orderedTableNameMap.orderedValues().toArray(new ITable[0]);
        return new DefaultTableIterator(tables, reversed);
    }

    @Override
    public String toString() {
        return String.format(
            "%s[%s]",
            getClass().getSimpleName(),
            String.join(",", _orderedTableNameMap.getTableNames()));
    }

}
