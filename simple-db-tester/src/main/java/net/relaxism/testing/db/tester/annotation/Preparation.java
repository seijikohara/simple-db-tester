package net.relaxism.testing.db.tester.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Preparation {

    DataSet[] dataSets() default {@DataSet};

    Operation operation() default Operation.CLEAN_INSERT;

}
