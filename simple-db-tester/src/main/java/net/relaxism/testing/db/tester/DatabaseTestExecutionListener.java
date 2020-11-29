package net.relaxism.testing.db.tester;

import lombok.val;
import net.relaxism.testing.db.tester.context.DatabaseTesterContext;
import org.springframework.core.Ordered;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class DatabaseTestExecutionListener extends AbstractTestExecutionListener {

    private DatabaseTester databaseTester;

    @Override
    public void prepareTestInstance(final TestContext testContext) throws Exception {
        this.databaseTester = new DatabaseTester(getDatabaseTesterContext(testContext), testContext.getTestClass());
    }

    @Override
    public void beforeTestClass(final TestContext testContext) throws Exception {
        super.beforeTestClass(testContext);
    }

    @Override
    public void afterTestClass(final TestContext testContext) throws Exception {
        super.afterTestClass(testContext);
    }

    @Override
    public void beforeTestMethod(final TestContext testContext) throws Exception {
        databaseTester.beforeTestMethod(testContext.getTestMethod());
    }

    @Override
    public void afterTestMethod(final TestContext testContext) throws Exception {
        if (testContext.getTestException() == null) {
            databaseTester.afterTestMethod(testContext.getTestMethod());
        }
    }

    protected DatabaseTesterContext getDatabaseTesterContext(final TestContext testContext) {
        val contextMap = testContext.getApplicationContext().getBeansOfType(DatabaseTesterContext.class);
        if (contextMap.isEmpty()) {
            throw new IllegalStateException("\"DatabaseTesterContext\" not found in SpringContext.");
        }
        if (contextMap.size() > 1) {
            throw new IllegalStateException("Multiple \"DatabaseTesterContext\" found in SpringContext.");
        }

        return contextMap.values().iterator().next();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}
