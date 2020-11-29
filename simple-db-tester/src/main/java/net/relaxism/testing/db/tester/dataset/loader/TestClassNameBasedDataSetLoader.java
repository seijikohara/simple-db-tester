package net.relaxism.testing.db.tester.dataset.loader;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.relaxism.testing.db.tester.annotation.DataSet;
import net.relaxism.testing.db.tester.annotation.Expectation;
import net.relaxism.testing.db.tester.annotation.Preparation;
import net.relaxism.testing.db.tester.context.DatabaseTesterContext;
import net.relaxism.testing.db.tester.dataset.PatternDataSet;
import net.relaxism.testing.db.tester.dataset.xls.XlsPatternDataSet;
import net.relaxism.testing.db.tester.util.ArrayUtils;
import net.relaxism.testing.db.tester.util.StringUtils;
import org.dbunit.dataset.DataSetException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
public class TestClassNameBasedDataSetLoader implements DataSetLoader {

    @Override
    public Collection<PatternDataSet> loadPreparationDataSets(final DatabaseTesterContext context,
                                                              final Class<?> testClass,
                                                              final Method testMethod) throws FileNotFoundException {
        val preparation = AnnotationUtils.getAnnotation(testMethod, Preparation.class);
        if (preparation == null)
            return Collections.emptyList();

        log.debug("Load preparation data : {}", preparation);

        val dataSetAnnotations = preparation.dataSets();
        val suffix = "";

        return loadDataSets(context, dataSetAnnotations, testClass, testMethod, suffix);
    }

    @Override
    public Collection<PatternDataSet> loadExpectationDataSets(final DatabaseTesterContext context,
                                                              final Class<?> testClass,
                                                              final Method testMethod) throws FileNotFoundException {
        val expectation = AnnotationUtils.getAnnotation(testMethod, Expectation.class);
        if (expectation == null)
            return Collections.emptyList();

        log.debug("Load expectation data : {}", expectation);

        val dataSetAnnotations = expectation.dataSets();
        val suffix = context.getExpectFileSuffix();

        return loadDataSets(context, dataSetAnnotations, testClass, testMethod, suffix);
    }

    private Collection<PatternDataSet> loadDataSets(final DatabaseTesterContext context,
                                                    final DataSet[] dataSetAnnotations,
                                                    final Class<?> testClass,
                                                    final Method testMethod,
                                                    final String suffix) throws FileNotFoundException {
        return Arrays.stream(dataSetAnnotations)
            .map(dataSetAnnotation -> {
                try {
                    log.debug("\"DataSet\" : {}", dataSetAnnotation);

                    val file = resolveFile(dataSetAnnotation.resourceLocation(), testClass, suffix);

                    val patternNames = dataSetAnnotation.patternNames();
                    val dataSet = loadDataSet(file, ArrayUtils.defaultValue(patternNames, new String[]{testMethod.getName()}));
                    dataSet.setDataSource(context.getDataSource(dataSetAnnotation.dataSourceName()));
                    return dataSet;
                } catch (FileNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            })
            .collect(Collectors.toList());
    }

    protected File resolveFile(final String resourceLocation,
                               final Class<?> testClass,
                               final String suffix) throws FileNotFoundException {
        return ResourceUtils.getFile(
            StringUtils.defaultValue(
                resourceLocation,
                String.format("%s%s%s.xls", ResourceUtils.CLASSPATH_URL_PREFIX, testClass.getName().replace('.', '/'), suffix)));
    }

    protected PatternDataSet loadDataSet(File file, String... patternNames) {
        try {
            log.debug("Load file : {} {}", file.getAbsolutePath(), patternNames);

            return new XlsPatternDataSet(file, patternNames);
        } catch (DataSetException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
