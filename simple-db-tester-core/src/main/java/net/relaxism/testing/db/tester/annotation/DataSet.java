package net.relaxism.testing.db.tester.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
public @interface DataSet {

	String[] patternNames() default {};

	String resourceLocation() default "";

	String dataSourceName() default DEFAULT_DATA_SOURCE_NAME;

	public static final String DEFAULT_DATA_SOURCE_NAME = "dataSource";

}
