# Introduction

Simple Database Tester using Spring testing framework and DBUnit framework.

## Usage

To enable feature, you adds `DatabaseTestExecutionListener` into `@TestExecutionListeners` annotation.

```java
	@RunWith(SpringJUnit4ClassRunner.class)
	@ContextConfiguration(locations = "classpath:test-context.xml")
	@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
			DatabaseTestExecutionListener.class })
```

`DatabaseTestExecutionListener` scans `javax.sql.DataSource` beans in Spring test context. You can use multiple data source, and default data source bean name is "dataSource".
The bean must allocated to top level.

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
	@Preparation(dataSets = { @DataSet(patternNames = { "customPattern1", "customPattern2" }, dataSourceName = "dataSource2", resourceLocation = "classpath:test/XlsTestCase/XlsTestCase-datasource2.xls") }, operation = Operation.CLEAN_INSERT)
	@Expectation(dataSets = { @DataSet(dataSourceName = "dataSource2", resourceLocation = "classpath:test/XlsTestCase/XlsTestCase-datasource2-expected.xls") })
	public void dataSource2() throws Exception {
		logger.info(">>> TEST METHOD");
	}
```
