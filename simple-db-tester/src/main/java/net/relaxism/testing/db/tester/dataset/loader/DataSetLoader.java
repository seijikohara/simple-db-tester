package net.relaxism.testing.db.tester.dataset.loader;

import net.relaxism.testing.db.tester.context.DatabaseTesterContext;
import net.relaxism.testing.db.tester.dataset.PatternDataSet;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.Collection;

public interface DataSetLoader {

    Collection<PatternDataSet> loadPreparationDataSets(
        DatabaseTesterContext context, Class<?> testClass, Method testMethod)
        throws FileNotFoundException;

    Collection<PatternDataSet> loadExpectationDataSets(
        DatabaseTesterContext context, Class<?> testClass, Method testMethod)
        throws FileNotFoundException;

}
