import java.io.File;

import javax.inject.Inject;

import org.daisy.maven.xproc.xprocspec.XProcSpecRunner;
import org.daisy.maven.xproc.xprocspec.XProcSpecRunner.TestLogger;
import org.daisy.maven.xproc.xprocspec.XProcSpecRunner.TestResult;
import static org.daisy.maven.xproc.xprocspec.XProcSpecRunner.TestResult.getErrors;
import static org.daisy.maven.xproc.xprocspec.XProcSpecRunner.TestResult.getFailures;

import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackBundles;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.xprocspecBundles;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class XProcSpecTest {
	
	@Configuration
	public Option[] config() {
		return options(
			logbackConfigFile(),
			logbackBundles(),
			felixDeclarativeServices(),
			xprocspecBundles(),
			junitBundles()
		);
	}
	
	@Inject
	private XProcSpecRunner xprocspecRunner;
	
	@Test
	public void runXProcSpec() {
		File baseDir = new File(PathUtils.getBaseDir());
		TestResult[] results = xprocspecRunner.run(new File(baseDir, "src/test/xprocspec"),
		                                           new File(baseDir, "target/xprocspec-reports"),
		                                           new File(baseDir, "target/surefire-reports"),
		                                           new File(baseDir, "target/xprocspec"),
		                                           new TestLogger.PrintStreamLogger(System.out));
		assertEquals("Number of failures and errors should be zero", 0L, getFailures(results)+ getErrors(results));
	}
}
