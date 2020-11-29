package test;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class StaticApplicationContextInjectorTestExecutionListener extends
    AbstractTestExecutionListener {

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        Field field = testContext.getTestClass().getDeclaredField(
            "applicationContext");
        ReflectionUtils.makeAccessible(field);
        field.set(null, testContext.getApplicationContext());
    }

}
