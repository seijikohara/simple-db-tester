package net.relaxism.testing.db.tester.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Expectation {

    DataSet[] dataSets() default {@DataSet};

}
