package net.relaxism.testing.db.tester.dataset.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import net.relaxism.testing.db.tester.annotation.DataSet;
import net.relaxism.testing.db.tester.annotation.Expectation;
import net.relaxism.testing.db.tester.annotation.Preparation;
import net.relaxism.testing.db.tester.context.DatabaseTesterContext;
import net.relaxism.testing.db.tester.dataset.PatternDataSet;
import net.relaxism.testing.db.tester.dataset.xls.XlsPatternDataSet;

import org.dbunit.dataset.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ResourceUtils;

import com.google.common.base.Strings;

public class TestClassNameBasedDataSetLoader implements DataSetLoader {

	private static final Logger logger = LoggerFactory
			.getLogger(TestClassNameBasedDataSetLoader.class);

	@Override
	public Collection<PatternDataSet> loadPreparationDataSets(
			DatabaseTesterContext context, Class<?> testClass, Method testMethod)
			throws FileNotFoundException {
		Preparation preparation = AnnotationUtils.getAnnotation(testMethod,
				Preparation.class);
		if (preparation == null)
			return Collections.emptyList();

		logger.debug("Load preparation data : {}", preparation);

		DataSet[] dataSetAnnotations = preparation.dataSets();
		String suffix = "";

		Collection<PatternDataSet> dataSets = loadDataSets(context,
				dataSetAnnotations, testClass, testMethod, suffix);

		return dataSets;
	}

	@Override
	public Collection<PatternDataSet> loadExpectationDataSets(
			DatabaseTesterContext context, Class<?> testClass, Method testMethod)
			throws FileNotFoundException {
		Expectation expectation = AnnotationUtils.getAnnotation(testMethod,
				Expectation.class);
		if (expectation == null)
			return Collections.emptyList();

		logger.debug("Load expectation data : {}", expectation);

		DataSet[] dataSetAnnotations = expectation.dataSets();
		String suffix = context.getExpectFileSuffix();

		Collection<PatternDataSet> dataSets = loadDataSets(context,
				dataSetAnnotations, testClass, testMethod, suffix);

		return dataSets;
	}

	private Collection<PatternDataSet> loadDataSets(
			DatabaseTesterContext context, DataSet[] dataSetAnnotations,
			Class<?> testClass, Method testMethod, String suffix)
			throws FileNotFoundException {
		Collection<PatternDataSet> dataSets = new ArrayList<PatternDataSet>();
		for (DataSet dataSetAnnotation : dataSetAnnotations) {
			logger.debug("\"DataSet\" : {}", dataSetAnnotation);

			File file = resolveFile(dataSetAnnotation.resourceLocation(),
					testClass, suffix);

			String[] patternNames = dataSetAnnotation.patternNames();
			if (patternNames == null || patternNames.length < 1) {
				patternNames = new String[] { testMethod.getName() };
			}
			PatternDataSet dataSet = loadDataSet(file, patternNames);
			dataSet.setDataSource(context.getDataSource(dataSetAnnotation
					.dataSourceName()));

			dataSets.add(dataSet);
		}
		return dataSets;
	}

	protected File resolveFile(String resourceLocation, Class<?> testClass,
			String suffix) throws FileNotFoundException {
		if (Strings.isNullOrEmpty(resourceLocation)) {
			resourceLocation = String.format("%s%s%s.xls",
					ResourceUtils.CLASSPATH_URL_PREFIX, testClass.getName()
							.replace('.', '/'), suffix);
		}

		return ResourceUtils.getFile(resourceLocation);
	}

	protected PatternDataSet loadDataSet(File file, String... patternNames) {
		try {
			logger.debug("Load file : {} {}", file.getAbsolutePath(),
					patternNames);

			return new XlsPatternDataSet(file, patternNames);
		} catch (DataSetException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
