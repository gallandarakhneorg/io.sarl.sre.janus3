/*
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on http://www.janusproject.io
 * 
 * Copyright (C) 2014-2015 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.janusproject.sre.tests.units;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assume.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;

import io.janusproject.sre.Booter;
import io.janusproject.sre.Exiter;
import io.janusproject.sre.JanusConfig;
import io.janusproject.sre.Kernel;
import io.janusproject.sre.tests.testutils.AbstractJanusTest;
import io.sarl.core.SRE;
import io.sarl.lang.SARLVersion;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.Agent;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public abstract class AbstractBooterTest<T extends Booter> extends AbstractJanusTest {
	
	static final UUID ID = UUID.fromString("63ee52ee-4739-47b1-9e73-0a7986d17bc5");
	
	static final UUID ID2 = UUID.fromString("63ee5000-4739-47b1-9e73-0a7986d17bc5");

	@Nullable
	protected PrintStream logger;
	
	@Nullable
	protected Exiter exiter;

	@Nullable
	protected T booter;
	
	@Before
	public void setUp() throws Exception {
		this.logger = mock(PrintStream.class);
		this.exiter = mock(Exiter.class);
		this.booter = newBooter();
		this.booter.setConsoleLogger(this.logger);
		this.booter.setExiter(this.exiter);
	}
	
	@After
	public void tearDown() {
		System.clearProperty(JanusConfig.JANUS_PROGRAM_NAME);
		System.clearProperty(JanusConfig.OFFLINE);
		System.clearProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
		System.clearProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		System.clearProperty(JanusConfig.VERBOSE_LEVEL_NAME);
		System.clearProperty(JanusConfig.BOOT_AGENT);
		System.clearProperty(JanusConfig.BOOT_AGENT_ID);
	}

	protected abstract T newBooter();

	protected static String[] args(String... strings) {
		return strings;
	}

	@Test
	public void getProgramName_01() {
		assertEquals(JanusConfig.JANUS_PROGRAM_NAME_VALUE, this.booter.getProgramName());
	}

	@Test
	public void getProgramName_02() {
		System.setProperty(JanusConfig.JANUS_PROGRAM_NAME, "abc");
		assertEquals("abc", this.booter.getProgramName());
	}

	@Test
	public void getConsoleLogger() {
		assertSame(this.logger, this.booter.getConsoleLogger());
	}

	@Test
	public void setConsoleLogger() {
		PrintStream stream = mock(PrintStream.class);
		this.booter.setConsoleLogger(stream);
		assertSame(stream, this.booter.getConsoleLogger());
	}

	@Test
	public void getExiter() {
		assertSame(this.exiter, this.booter.getExiter());
	}

	@Test
	public void setExiter() {
		Exiter exiter = mock(Exiter.class);
		this.booter.setExiter(exiter);
		assertSame(exiter, this.booter.getExiter());
	}
	
	@Test
	public void setOffline() {
		this.booter.setOffline(true);
		assertTrueStr(System.getProperty(JanusConfig.OFFLINE));
		this.booter.setOffline(false);
		assertFalseStr(System.getProperty(JanusConfig.OFFLINE));
	}

	@Test
	public void setRandomContextUUID() {
		assertEquals(null, System.getProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME));
		assertEquals(null, System.getProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME));
		this.booter.setRandomContextUUID();
		assertFalseStr(System.getProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME));
		assertTrueStr(System.getProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME));
	}

	@Test
	public void setBootAgentTypeContextUUID() {
		assertEquals(null, System.getProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME));
		assertEquals(null, System.getProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME));
		this.booter.setBootAgentTypeContextUUID();
		assertTrueStr(System.getProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME));
		assertFalseStr(System.getProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME));
	}

	@Test
	public void setDefaultContextUUID() {
		assertEquals(null, System.getProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME));
		assertEquals(null, System.getProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME));
		this.booter.setDefaultContextUUID();
		assertFalseStr(System.getProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME));
		assertFalseStr(System.getProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME));
	}

	@Test
	public void addToSystemClasspath() throws Exception {
		ClassLoader clo = ClassLoader.getSystemClassLoader();
		// This test has no sense if the class loader cannot be updated.
		assumeTrue(clo instanceof URLClassLoader);
		
		URLClassLoader cl = (URLClassLoader) clo;
		URL[] original = cl.getURLs();
		URL[] expected = new URL[original.length + 2];
		System.arraycopy(original, 0, expected, 0, original.length);
		expected[original.length] = new URL("file:/mypath");
		expected[original.length + 1] = new URL("file:/mypath2");
		
		this.booter.addToSystemClasspath(File.separator+"mypath:"+File.separator+"mypath2");
		
		URL[] newcp = cl.getURLs();
		assertContains(Arrays.asList(newcp), expected);
	}

	@Test
	public void setVerboseLevel() {
		this.booter.setVerboseLevel(0);
		assertEquals("0", System.getProperty(JanusConfig.VERBOSE_LEVEL_NAME));
		this.booter.setVerboseLevel(1);
		assertEquals("1", System.getProperty(JanusConfig.VERBOSE_LEVEL_NAME));
		this.booter.setVerboseLevel(2);
		assertEquals("2", System.getProperty(JanusConfig.VERBOSE_LEVEL_NAME));
		this.booter.setVerboseLevel(3);
		assertEquals("3", System.getProperty(JanusConfig.VERBOSE_LEVEL_NAME));
		this.booter.setVerboseLevel(4);
		assertEquals("4", System.getProperty(JanusConfig.VERBOSE_LEVEL_NAME));
		this.booter.setVerboseLevel(5);
		assertEquals("5", System.getProperty(JanusConfig.VERBOSE_LEVEL_NAME));
		this.booter.setVerboseLevel(6);
		assertEquals("6", System.getProperty(JanusConfig.VERBOSE_LEVEL_NAME));
	}

	@Test
	public void getBootAgentIdentifier_notStarted() {
		UUID actual = this.booter.getBootAgentIdentifier();
		assertNull(actual);
		actual = this.booter.getBootAgentIdentifier();
		assertNull(actual);
	}

	@Test
	public void getBootAgentIdentifier_started() throws Exception {
		this.booter.startJanusWithModuleType(TestModule.class, AgentMock.class);
		assertEquals(ID, this.booter.getBootAgentIdentifier());
		assertEquals(ID, this.booter.getBootAgentIdentifier());
	}

	@Test
	public void getBootAgentIdentifier_startedAgain() throws Exception {
		this.booter.startJanusWithModuleType(TestModule.class, AgentMock.class);
		assertEquals(ID, this.booter.getBootAgentIdentifier());
		this.booter.startJanusWithModuleType(TestModule2.class, AgentMock.class);
		assertEquals(ID2, this.booter.getBootAgentIdentifier());
	}

	@Test
	public abstract void getOptions_all() throws Exception;

	@Test
	public void startJanus() {
		// Force the injector for testing
		System.setProperty(JanusConfig.INJECTION_MODULE_NAME, TestModule.class.getName());
		
		Kernel kern = this.booter.startJanus(AgentMock.class, 1, "a");
		
		assertNotNull(kern);
		
		ArgumentCaptor<Class<? extends Agent>> capturedAgentType = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Object> capturedArgs = ArgumentCaptor.forClass(Object.class);
		verify(kern).spawn(capturedAgentType.capture(), capturedArgs.capture());
		assertEquals(AgentMock.class, capturedAgentType.getValue());
		assertContains(capturedArgs.getAllValues(), 1, "a");

		assertSame(this.booter, SRE.getBootstrap());
	}

	@Test
	public void startJanus_twoTimes() {
		// Force the injector for testing
		System.setProperty(JanusConfig.INJECTION_MODULE_NAME, TestModule.class.getName());
		
		Kernel kern1 = this.booter.startJanus(AgentMock.class, 1, "a");
		Kernel kern2 = this.booter.startJanus(AgentMock.class, 1, "a");
		
		assertNotNull(kern1);
		assertNotNull(kern2);
		assertNotSame(kern1, kern2);
		
		assertSame(this.booter, SRE.getBootstrap());
	}

	@Test
	public void startJanusWithModuleType() {
		Kernel kern = this.booter.startJanusWithModuleType(TestModule.class, AgentMock.class, 1, "a");
		
		assertNotNull(kern);
		
		ArgumentCaptor<Class<? extends Agent>> capturedAgentType = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Object> capturedArgs = ArgumentCaptor.forClass(Object.class);
		verify(kern).spawn(capturedAgentType.capture(), capturedArgs.capture());
		assertEquals(AgentMock.class, capturedAgentType.getValue());
		assertContains(capturedArgs.getAllValues(), 1, "a");
		
		assertSame(this.booter, SRE.getBootstrap());
	}

	@Test
	public void startJanusWithModule() {
		Module module = new TestModule();
		
		Kernel kern = this.booter.startJanusWithModule(module, AgentMock.class, 1, "a");
		
		assertNotNull(kern);
		
		ArgumentCaptor<Class<? extends Agent>> capturedAgentType = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Object> capturedArgs = ArgumentCaptor.forClass(Object.class);
		verify(kern).spawn(capturedAgentType.capture(), capturedArgs.capture());
		assertEquals(AgentMock.class, capturedAgentType.getValue());
		assertContains(capturedArgs.getAllValues(), 1, "a");
		
		assertSame(this.booter, SRE.getBootstrap());
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	protected static class AgentMock extends Agent {
		/**
		 */
		public AgentMock() {
			super(UUID.randomUUID(), ID);
		}
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	protected static class TestModule implements Module {

		public TestModule() {
			//
		}

		@Override
		public void configure(Binder binder) {
			//
		}

		@Provides
		public Kernel createKernel() {
			Kernel k = mock(Kernel.class);
			when(k.spawn(any(Class.class), any())).thenReturn(ID);
			when(k.getLogger()).thenReturn(mock(Logger.class));
			return k;
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	protected static class TestModule2 implements Module {

		public TestModule2() {
			//
		}

		@Override
		public void configure(Binder binder) {
			//
		}

		@Provides
		public Kernel createKernel() {
			Kernel k = mock(Kernel.class);
			when(k.spawn(any(Class.class), any())).thenReturn(ID2);
			when(k.getLogger()).thenReturn(mock(Logger.class));
			return k;
		}

	}

	//	/** 
//	 * Run the standard boot process.
//	 * 
//	 * @param args - command line arguments
//	 * @see #startJanus(Class, Object...)
//	 */
//	def runStandardBootProcess(args : String*)
//
//
//
//
}
