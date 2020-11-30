# Introduction

![Java CI with Maven](https://github.com/seijikohara/simple-db-tester/workflows/Java%20CI%20with%20Maven/badge.svg)

Simple Database Tester using Spring testing framework and DBUnit framework.

## Usage

To enable the feature, you adds `DatabaseTestExecutionListener` into `@TestExecutionListeners` annotation.

```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DatabaseTestExecutionListener.class })
```

`DatabaseTestExecutionListener` scans `DatabaseTesterContext` class bean in Spring test context.

```xml
<bean id="dataSource"
	class="org.springframework.jdbc.datasource.DriverManagerDataSource">
	<property name="driverClassName" value="org.h2.Driver" />
	<property name="url" value="jdbc:h2:./target/data1" />
	<property name="username" value="sa" />
	<property name="password" value="" />
</bean>

<bean id="dataSource2"
	class="org.springframework.jdbc.datasource.DriverManagerDataSource">
	<property name="driverClassName" value="org.h2.Driver" />
	<property name="url" value="jdbc:h2:./target/data2" />
	<property name="username" value="sa" />
	<property name="password" value="" />
</bean>

<bean id="testerContext"
	class="net.relaxism.testing.db.tester.context.DatabaseTesterContext">
	<property name="dataSources">
		<map>
			<entry key="dataSource" value-ref="dataSource" />
			<entry key="dataSource2" value-ref="dataSource2" />
		</map>
	</property>
	<property name="defaultDataSourceName" value="dataSource" />
</bean>
```

## Example

Simple test:

* `@Preparation` load xls file into "dataSource"
* `@Expectation` validate table data on "dataSource" with xls file
* xls sheet name is same as table name
* xls file name is same as target test class name on classpath, expect xls file name has "-expected" suffix
* Sheet entered is [Pattern] to A1 cell, are filtered in the pattern name
* Default pattern name is test target method name

```java
package test;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DatabaseTestExecutionListener.class,
		StaticApplicationContextInjectorTestExecutionListener.class })
public class XlsTestCase {

	@Test
	@Preparation
	@Expectation
	public void pattern1() throws Exception {
		logger.info(">>> TEST METHOD");
	}
}
```

Configure prepare/expect by `@DataSet` annotation:

```java
package test;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DatabaseTestExecutionListener.class,
		StaticApplicationContextInjectorTestExecutionListener.class })
public class XlsTestCase {

	@Test
	@Preparation(dataSets = { @DataSet(patternNames = { "customPattern1", "customPattern2" }, dataSourceName = "dataSource2", resourceLocation = "classpath:test/XlsTestCase/XlsTestCase-dataSource2.xls") }, operation = Operation.CLEAN_INSERT)
	@Expectation(dataSets = { @DataSet(dataSourceName = "dataSource2", resourceLocation = "classpath:test/XlsTestCase/XlsTestCase-dataSource2-expected.xls") })
	public void dataSource2() throws Exception {
		logger.info(">>> TEST METHOD");
	}
}
```
