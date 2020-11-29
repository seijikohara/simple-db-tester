package net.relaxism.testing.db.tester;

import net.relaxism.testing.db.tester.context.DatabaseTesterContext;
import org.springframework.core.Ordered;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.util.Map;

public class DatabaseTestExecutionListener extends
    AbstractTestExecutionListener {

    private DatabaseTester databaseTester;

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        DatabaseTester databaseTester = new DatabaseTester(
            getDatabaseTesterContext(testContext),
            testContext.getTestClass());
        this.databaseTester = databaseTester;
    }

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        super.beforeTestClass(testContext);
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        super.afterTestClass(testContext);
    }

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        databaseTester.beforeTestMethod(testContext.getTestMethod());
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        if (testContext.getTestException() == null) {
            databaseTester.afterTestMethod(testContext.getTestMethod());
        }
    }

    protected DatabaseTesterContext getDatabaseTesterContext(
        TestContext testContext) {
        final Map<String, DatabaseTesterContext> contextMap = testContext
            .getApplicationContext().getBeansOfType(
                DatabaseTesterContext.class);
        if (contextMap.isEmpty()) {
            throw new IllegalStateException(
                "\"DatabaseTesterContext\" not found in SpringContext.");
        }
        if (contextMap.size() > 1) {
            throw new IllegalStateException(
                "Multiple \"DatabaseTesterContext\" found in SpringContext.");
        }

        return contextMap.values().iterator().next();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
