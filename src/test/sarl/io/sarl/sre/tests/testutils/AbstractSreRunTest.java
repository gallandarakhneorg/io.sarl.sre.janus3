/*
 * $Id$
 *
 * SARL is an general-purpose agent programming language.
 * More details on http://www.sarl.io
 *
 * Copyright (C) 2014-2018 the original authors or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sarl.sre.tests.testutils;

import static org.junit.Assert.assertNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.junit.ComparisonFailure;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.google.inject.Module;

import io.sarl.bootstrap.SRE;
import io.sarl.core.Initialize;
import io.sarl.core.Lifecycle;
import io.sarl.core.Schedules;
import io.sarl.lang.annotation.PerceptGuardEvaluator;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.DynamicSkillProvider;
import io.sarl.sre.Kernel;
import io.sarl.sre.boot.ProgrammaticBootstrap;
import io.sarl.sre.boot.factories.BootFactory;
import io.sarl.sre.boot.factories.Factories;
import io.sarl.sre.boot.factories.LoggingFactory;
import io.sarl.sre.boot.injection.modules.InjectionConstants;
import io.sarl.sre.services.executor.EarlyExitException;
import io.sarl.sre.services.logging.LoggingService;
import io.sarl.tests.api.Nullable;

/**
 * Abstract class for creating unit tests that needs to launch a SRE instance.
 *
 * @param <S> - the type of the service.
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
//@SuppressWarnings("all")
public abstract class AbstractSreRunTest extends AbstractSreTest {

	/** The logging level when logging is enable during tests.
	 *
	 * @since 0.7
	 */
	public static final Level TEST_LOGGING_LEVEL = Level.ALL;
	
	/** Standard timeout in seconds.
	 *
	 * @see #EXTRA_TIMEOUT
	 * @see #NO_TIMEOUT
	 */
	public static final int STANDARD_TIMEOUT = 40;

	/** Extra timeout in seconds.
	 *
	 * @see #STANDARD_TIMEOUT
	 * @see #NO_TIMEOUT
	 */
	public static final int EXTRA_TIMEOUT = 240;

	/** No timeout.
	 *
	 * @see #STANDARD_TIMEOUT
	 * @see #EXTRA_TIMEOUT
	 */
	public static final int NO_TIMEOUT = -1;

	/**
	 * Reference to the instance of the SRE kernel.
	 */
	protected Kernel sreKernel;

	/**
	 * Reference to the instance of the SRE bootstrap.
	 */
	protected ProgrammaticBootstrap bootstrap;

	@Nullable
	private List<Object> results;

	@Nullable
	private Class<? extends Module> testingModule;
	
	@Rule
	public TestWatcher sreRunWatcher = new TestWatcher() {
		@Override
		protected void starting(Description description) {
			SRE.resetServiceLoader();
			SRE.setBootstrap(null);
			AbstractSreRunTest.this.testingModule = InjectionConstants.DEFAULT_ROOT_INJECTION_MODULE;
			SreRun skipRun = description.getAnnotation(SreRun.class);
			if (skipRun != null) {
				try {
					runSre(skipRun.agent(), skipRun.enableLogging());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		@Override
		protected void finished(Description description) {
			SRE.resetServiceLoader();
			SRE.setBootstrap(null);
			if (AbstractSreRunTest.this.sreKernel != null) {
				AbstractSreRunTest.this.sreKernel = null;
			}
		}
	};

	/**
	 * Replies the number of results provided by the ran platform.
	 *
	 * @return the number of results.
	 */
	protected int getNumberOfResults() {
		synchronized(this.results) {
			return this.results.size();
		}
	}

	/**
	 * Test if the number of results provided by the SRE platform is equal to the given number.
	 *
	 * @param expected - the expected number of results.
	 */
	protected void assertNumberOfResults(int expected) {
		assertEquals("Invalid number of results provided by the platform.", expected, this.results.size());
	}

	/**
	 * Replies result at the given index of the run of the agent.
	 * 
	 * @param type - the type of the result.
	 * @param index - the index of the result.
	 * @return the value; or <code>null</code> if no result.
	 */
	protected <T> T getResult(Class<T> type, int index) {
		if (this.results != null) {
			synchronized(this.results) {
				try {
					return type.cast(this.results.get(index));
				} catch (Throwable exception) {
					//
				}
			}
		}
		return null;
	}

	/**
	 * Replies result at the given index of the run of the agent.
	 * @return the results.
	 */
	protected List<Object> getResults() {
		synchronized(this.results) {
			if (this.results != null) {
				return Collections.unmodifiableList(this.results);
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Replies the initialization parameters for the agents.
	 * @return the parameters.
	 */
	protected Object[] getAgentInitializationParameters() {
		return new Object[] {
				this.results,
		};
	}

	/**
	 * Replies the index of the first result of the given type.
	 * 
	 * @param type - the type of the result.
	 * @return the index; or <code>-1</code> if not found.
	 */
	protected int indexOfResult(Class<?> type) {
		return indexOfResult(type, 0);
	}

	/**
	 * Replies the index of the first result of the given type starting at the given index.
	 * 
	 * @param type - the type of the result.
	 * @param fromIndex - the start index.
	 * @return the index; or <code>-1</code> if not found.
	 */
	protected int indexOfResult(Class<?> type, int fromIndex) {
		synchronized(this.results) {
			if (this.results != null) {
				try {
					for (int i = fromIndex; i < this.results.size(); ++i) {
						Object r = this.results.get(i);
						if (type.isInstance(r)) {
							return i;
						}
					}
				} catch (Throwable exception) {
					//
				}
			}
		}
		return -1;
	}

	/**
	 * Start the SRE platform offline.
	 *
	 * This function has standard timeout for the end of the run.
	 *
	 * @param type - the type of the agent to launch at start-up.
	 * @param enableLogging - indicates if the logging is enable or not.
	 * @return the kernel.
	 * @throws Exception - if the kernel cannot be launched.
	 */
	protected Kernel runSre(Class<? extends TestingAgent> type, boolean enableLogging) throws Exception {
		return runSre(type, enableLogging, true, STANDARD_TIMEOUT);
	}

	/**
	 * Start the SRE platform offline with logging enabled.
	 *
	 * This function enables logging and has standard timeout for the end of the run.
	 * 
	 * @param type - the type of the agent to launch at start-up.
	 * @return the kernel.
	 * @throws Exception - if the kernel cannot be launched.
	 */
	protected Kernel runSre(Class<? extends TestingAgent> type) throws Exception {
		return runSre(type, true, true, STANDARD_TIMEOUT);
	}

	/**
	 * Start the SRE platform.
	 * 
	 * @param type - the type of the agent to launch at start-up.
	 * @param enableLogging - indicates if the logging is enable or not.
	 * @param timeout - the maximum waiting time in seconds, or <code>-1</code> to ignore the timeout.
	 *     See {@link #STANDARD_TIMEOUT}, {@link #EXTRA_TIMEOUT} or {@link #NO_TIMEOUT}.
	 * @return the kernel.
	 * @throws Exception - if the kernel cannot be launched.
	 */
	protected Kernel runSre(Class<? extends TestingAgent> type, boolean enableLogging, int timeout)
			throws Exception {
		return runSre(type, enableLogging, false, timeout);
	}

	/**
	 * Start the SRE platform.
	 * 
	 * @param type - the type of the agent to launch at start-up.
	 * @param enableLogging - indicates if the logging is enable or not.
	 * @param trackLogErrors indicates if the logged errors should be tracked.
	 * @param timeout - the maximum waiting time in seconds, or <code>-1</code> to ignore the timeout.
	 *     See {@link #STANDARD_TIMEOUT}, {@link #EXTRA_TIMEOUT} or {@link #NO_TIMEOUT}.
	 * @return the kernel.
	 * @throws Exception - if the kernel cannot be launched.
	 */
	protected Kernel runSre(Class<? extends TestingAgent> type, boolean enableLogging, boolean trackLogErrors, int timeout)
			throws Exception {
		Kernel kern = setupTheSreKernel(type, enableLogging, trackLogErrors);
		try {
			waitForTheKernel(timeout);
		} catch (TimeoutException exception) {
			kern.stopKernel();
			throw exception;
		}
		return kern;
	}

	/** Replies the module that should be injected in order to proceed tests.
	 *
	 * @return the module, never {@code null}.
	 */
	protected Class<? extends Module> getTestingModule() {
		return this.testingModule;
	}

	/** Change the module that should be injected in order to proceed tests.
	 *
	 * @param type the module, never {@code null}.
	 */
	protected void setTestingModule(Class<? extends Module> type) {
		assert type != null;
		this.testingModule = type;
	}

	/** Assert the the given kernel has no error on its logs.
	 *
	 * @param kern the kernel.
	 */
	protected void assertNoErrorLog(Kernel kern) {
		for (Object obj : getResults()) {
			if (obj instanceof LogRecord) {
				throw new ComparisonFailure("Unexpected error log", "", ((LogRecord) obj).toString());
			}
		}
	}
	
	/**
	 * Set-up the SRE platform.
	 * 
	 * @param type - the type of the agent to launch at start-up.
	 * @param enableLogging - indicates if the logging is enable or not, i.e. messages are output.
	 * @param trackLogErrors indicates if the logged errors should be tracked.
	 * @return the kernel.
	 * @throws Exception - if the kernel cannot be launched.
	 */
	protected Kernel setupTheSreKernel(Class<? extends TestingAgent> type, boolean enableLogging, boolean trackLogErrors)
			throws Exception {
		assertNull("SRE already launched.", this.sreKernel);
		final Level logLevel = enableLogging ? TEST_LOGGING_LEVEL : Level.OFF;
		System.setProperty(Factories.toPropertyName(LoggingFactory.VERBOSE_LEVEL_NAME), logLevel.getName());
		System.setProperty(Factories.toPropertyName(BootFactory.BOOT_AGENT_NAME), type.getName());
		Class<? extends Module> module = getTestingModule();
		this.results = new ArrayList<>();
		this.bootstrap = new ProgrammaticBootstrap();
		this.bootstrap.startWithoutAgent(module);
		this.sreKernel = this.bootstrap.getKernel();
		if (trackLogErrors) {
			this.sreKernel.getService(LoggingService.class).getPlatformLogger().addHandler(new Handler() {
				@SuppressWarnings("unchecked")
				@Override
				public void publish(LogRecord record) {
					if (record.getLevel() == Level.SEVERE) {
						List<Object> res = (List<Object>) getAgentInitializationParameters()[0];
						synchronized (res) {
							res.add(record);
						}
					}
				}
				@Override
				public void flush() {
				}
				@Override
				public void close() throws SecurityException {
				}
			});
		}
		this.bootstrap.startAgent(type, getAgentInitializationParameters());
		return this.sreKernel;
	}

	/**
	 * Wait for the end of the SRE platform.
	 * 
	 * @param timeout - the maximum waiting time in seconds, or <code>-1</code> to ignore the timeout.
	 *     See {@link #STANDARD_TIMEOUT}, {@link #EXTRA_TIMEOUT} or {@link #NO_TIMEOUT}.
	 * @throws Exception - if the kernel cannot be launched.
	 */
	public void waitForTheKernel(int timeout) throws Exception {
		long endTime;
		if (timeout >= 0) {
			endTime = System.currentTimeMillis() + timeout * 1000;
		} else {
			endTime = -1;
		}
		boolean isSreRunning = this.sreKernel.isRunning();
		while (isSreRunning && (endTime == -1 || System.currentTimeMillis() <= endTime)) {
			isSreRunning = this.sreKernel.isRunning();
			//System.out.println("RUN=" + isSreRunning + "; REST>"+(endTime - System.currentTimeMillis()));
			Thread.sleep(100);
		}
		if (isSreRunning) {
			throw new TimeoutException();
		}
	}

	/**
	 * Wait for the end of the SRE platform.
	 * 
	 * @param timeout - the maximum waiting time in seconds, or <code>-1</code> to ignore the timeout.
	 *     See {@link #STANDARD_TIMEOUT}, {@link #EXTRA_TIMEOUT} or {@link #NO_TIMEOUT}.
	 * @param predicate the predicate to use as stop condition.
	 * @throws Exception - if the kernel cannot be launched.
	 */
	public void waitForTheKernel(int timeout, Function1<List<Object>, Boolean> predicate) throws Exception {
		long endTime;
		if (timeout >= 0) {
			endTime = System.currentTimeMillis() + timeout * 1000;
		} else {
			endTime = -1;
		}
		boolean isSreRunning = this.sreKernel.isRunning();
		while (isSreRunning && (endTime == -1 || System.currentTimeMillis() <= endTime)) {
			isSreRunning = this.sreKernel.isRunning() || !(predicate.apply(this.results));
			Thread.sleep(100);
		}
		if (isSreRunning) {
			throw new TimeoutException();
		}
	}

	/**
	 * Interface that permits to mark a method that is manually launching the SRE.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	protected @interface SreRun {

		/**
		 * The type of the agent to launch.
		 *
		 * @return the type of the agent to launch.
		 */
		Class<? extends TestingAgent> agent();

		/**
		 * Indicates if the logging is enabled.
		 *
		 * @return <code>true</code> if the logging is enabled; <code>false</code> otherwise.
		 */
		boolean enableLogging() default false;

	}

	/**
	 * Abstract implementation of an agent that is used for testing SRE.
	 * 
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	protected static abstract class TestingAgent extends Agent {

		private List<Object> results;

		private Object[] initializationParameters;

		/**
		 * @param parentID - the identifier of the parent's agent.
		 * @param agentID - the identifier of the agent.
		 */
		public TestingAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		/**
		 * @param parentID - the identifier of the parent's agent.
		 * @param agentID - the identifier of the agent.
		 * @param provider - the skill provider.
		 */
		public TestingAgent(UUID parentID, UUID agentID, DynamicSkillProvider provider) {
			super(parentID, agentID, provider);
		}

		/**
		 * Add a result.
		 * 
		 * @param result - the result.
		 */
		protected void addResult(Object result) {
			synchronized(this.results) {
				this.results.add(result);
			}
		}

		/**
		 * Replies the number of results provided by the ran platform.
		 *
		 * @return the number of results.
		 */
		protected int getNumberOfResults() {
			synchronized(this.results) {
				return this.results.size();
			}
		}

		/**
		 * Add a result.
		 * 
		 * @param result - the result.
		 */
		protected void addResults(Collection<?> results) {
			synchronized(this.results) {
				this.results.addAll(results);
			}
		}

		/**
		 * Replies a unmodifiable view on the results.
		 * @return the results.
		 */
		protected List<Object> getResults() {
			synchronized(this.results) {
				if (this.results != null) {
					return Collections.unmodifiableList(this.results);
				}
			}
			return Collections.emptyList();
		}

		/**
		 * Replies the results.
		 * @return the results.
		 */
		protected List<Object> getRawResults() {
			return this.results;
		}

		/** Replies the initialization parameters of the agents.
		 *
		 * @return the initialization parameters.
		 */
		protected Object[] getAgentInitializationParameters() {
			return this.initializationParameters;
		}

		@PerceptGuardEvaluator
		private void $guardEvaluator$Initialize(final Initialize occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
			assert occurrence != null;
			assert ___SARLlocal_runnableCollection != null;
			___SARLlocal_runnableCollection.add(() -> $behaviorUnit$Initialize$0(occurrence));
		}

		/**
		 * Invoked at the start of the agent.
		 * 
		 * @param occurrence - the initialization event.
		 */
		@SuppressWarnings("unchecked")
		private void $behaviorUnit$Initialize$0(final Initialize occurrence) {
			this.initializationParameters = occurrence.parameters;
			this.results = (List<Object>) occurrence.parameters[0];
			try {
				if (runAgentTest()) {
					getSkill(Schedules.class).in(1000, (it) -> forceKillMe());
				}
			} catch (Throwable exception) {
				if (!(exception instanceof EarlyExitException)) {
					addResult(exception);
				}
				throw exception;
			}
		}

		protected void forceKillMe() {
			getSkill(Lifecycle.class).killMe();
		}

		/**
		 * Invoked to run the unit test. This function is invoked at agent initialization
		 *
		 * @return <code>true</code> for killing the agent during its initialization.
		 */
		protected abstract boolean runAgentTest();

	}

}
