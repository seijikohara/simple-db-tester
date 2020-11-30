package net.relaxism.testing.db.tester.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface DataSet {

    String[] patternNames() default {};

    String resourceLocation() default "";

    String dataSourceName() default "";

}
