package org.daisy.pipeline.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.ParentRunner;

import static org.ops4j.pax.exam.Constants.EXAM_REACTOR_STRATEGY_KEY;
import static org.ops4j.pax.exam.Constants.EXAM_REACTOR_STRATEGY_PER_CLASS;
import org.ops4j.pax.exam.junit.impl.ProbeRunner;

/**
 * Runs a JUnit test both in an OSGi environment (with Pax-Exam) and
 * in a normal environment (using plain SPI to do dependency
 * injection)
 */
public class TestRunner extends ParentRunner<Runner> {
	
	private final ProbeRunner osgiRunner;
	private final OSGiLessRunner osgilessRunner;
	
	public TestRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
		// set default strategy to PerClass instead of PerMethod
		System.setProperty(EXAM_REACTOR_STRATEGY_KEY, EXAM_REACTOR_STRATEGY_PER_CLASS);
		osgiRunner = new ProbeRunner(testClass);
		osgilessRunner = new OSGiLessRunner(testClass);
	}
	
	@Override
	public void runChild(Runner runner, RunNotifier notifier) {
		if (runner == osgiRunner)
			System.out.println("Running test with OSGi");
		else if (runner == osgilessRunner)
			System.out.println("Running test without OSGi");
		else
			throw new RuntimeException("coding error");
		runner.run(notifier);
	}

	@Override
	protected Description describeChild(Runner runner) {
		return runner.getDescription();
	}

	@Override
	protected List<Runner> getChildren() {
		List<Runner> children = new ArrayList<Runner>();
		children.add(osgiRunner);
		children.add(osgilessRunner);
		return children;
	}
}
