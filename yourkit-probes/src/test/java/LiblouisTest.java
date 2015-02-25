import javax.inject.Inject;

import java.io.File;

import org.daisy.pipeline.braille.liblouis.Liblouis;

import static org.daisy.pipeline.pax.exam.Options.brailleModule;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.forThisPlatform;
import static org.daisy.pipeline.pax.exam.Options.logbackBundles;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;

import static org.ops4j.pax.exam.CoreOptions.bootDelegationPackage;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.vmOption;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class LiblouisTest {
	
	@Inject
	Liblouis liblouis;
	
	@Test
	public void testLiblouisTranslation() {
		assertEquals("foobar",
		             liblouis.translate("file:" + PathUtils.getBaseDir() + "/target/test-classes/foobar.cti",
		                                "foobar", false, null));
	}
	
	@Configuration
	public Option[] config() {
		File probeClasspath = new File(PathUtils.getBaseDir() + "/target/classes");
		return options(
			vmOption("-agentpath:${yourkit.home}/bin/mac/libyjpagent.jnilib="
			         + "dir=" + PathUtils.getBaseDir() + "/target/yourkit"
			         + ",onexit=memory"
			         + ",probeclasspath=" + probeClasspath
			         + ",probe=org.daisy.pipeline.yourkit.probes.Liblouis"
			),
			vmOption("-Xbootclasspath/a:" + probeClasspath),
			bootDelegationPackage("org.daisy.pipeline.yourkit.probes"),
			logbackConfigFile(),
			logbackBundles(),
			felixDeclarativeServices(),
			mavenBundle().groupId("com.google.guava").artifactId("guava").versionAsInProject(),
			mavenBundle().groupId("net.java.dev.jna").artifactId("jna").versionAsInProject(),
			mavenBundle().groupId("org.liblouis").artifactId("liblouis-java").versionAsInProject(),
			brailleModule("common-java"),
			brailleModule("liblouis-core"),
			forThisPlatform(brailleModule("liblouis-native")),
			junitBundles()
		);
	}
}
