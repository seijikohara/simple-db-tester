package test;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.relaxism.testing.db.tester.DatabaseTestExecutionListener;
import net.relaxism.testing.db.tester.annotation.DataSet;
import net.relaxism.testing.db.tester.annotation.Expectation;
import net.relaxism.testing.db.tester.annotation.Operation;
import net.relaxism.testing.db.tester.annotation.Preparation;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.util.ResourceUtils;

import javax.sql.DataSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    DatabaseTestExecutionListener.class,
    StaticApplicationContextInjectorTestExecutionListener.class})
@Slf4j
public class XlsTestCase {

    private static ApplicationContext applicationContext;

    @BeforeClass
    public static void setUpClass() throws Exception {
        log.info(">>> BEFORE test class");

        val dataSource = applicationContext.getBean("dataSource", DataSource.class);
        ScriptUtils.executeSqlScript(
            dataSource.getConnection(),
            new FileSystemResource(ResourceUtils
                .getFile(ResourceUtils.CLASSPATH_URL_PREFIX
                    + "test/ddl-datasource.sql")));

        val dataSource2 = applicationContext.getBean("dataSource2", DataSource.class);
        ScriptUtils.executeSqlScript(
            dataSource2.getConnection(),
            new FileSystemResource(ResourceUtils
                .getFile(ResourceUtils.CLASSPATH_URL_PREFIX
                    + "test/ddl-datasource2.sql")));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        log.info(">>> AFTER test class");
    }

    @Before
    public void setUp() throws Exception {
        log.info(">>> BEFORE test method");
    }

    @After
    public void tearDown() throws Exception {
        log.info(">>> AFTER test method");
    }

    @Test
    @Preparation
    @Expectation
    public void pattern1() throws Exception {
        log.info(">>> TEST METHOD");
    }

    @Test
    @Preparation
    @Expectation
    public void pattern2() throws Exception {
        log.info(">>> TEST METHOD");
    }

    @Test
    @Preparation(dataSets = {@DataSet(patternNames = {"customPattern1", "customPattern2"}, dataSourceName = "dataSource2", resourceLocation = "classpath:test/custom-file/XlsTestCase-dataSource2.xls")}, operation = Operation.CLEAN_INSERT)
    @Expectation(dataSets = {@DataSet(dataSourceName = "dataSource2", resourceLocation = "classpath:test/custom-file/XlsTestCase-dataSource2-expected.xls")})
    public void dataSource2() throws Exception {
        log.info(">>> TEST METHOD");
    }

}
