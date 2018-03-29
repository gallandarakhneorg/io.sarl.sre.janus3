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
package io.sarl.sre.tests.units.boot.adhoc;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.inject.Module;

import io.sarl.lang.core.Agent;
import io.sarl.sre.Boot;
import io.sarl.sre.boot.adhoc.Booter;
import io.sarl.sre.boot.adhoc.JanusBooter;
import io.sarl.sre.tests.testutils.AbstractJanusTest;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class BootTest extends AbstractJanusTest {

	@Nullable
	private PrintStream logger;
	
	@Nullable
	private Booter booter;

	@Before
	public void setUp() {
		this.logger = mock(PrintStream.class);
		this.booter = mock(Booter.class);
		when(this.booter.getConsoleLogger()).thenReturn(this.logger);
		Boot.setBooter(this.booter);
	}
	
	@Test
	public void getBooter() {
		// Reset the booter for testing
		Boot.setBooter(null);
		assertInstanceOf(JanusBooter.class, Boot.getBooter());
	}

	@Test
	public void setBooter() {
		Booter booter = mock(Booter.class);
		Boot.setBooter(booter);
		assertSame(booter, Boot.getBooter());
	}

	@Test
	public void main() {
		Boot.main("a", "b", "c");
		ArgumentCaptor<String> capturedArg = ArgumentCaptor.forClass(String.class);
		verify(this.booter).runStandardBootProcess(capturedArg.capture());
		assertContains(capturedArg.getAllValues(), "a", "b", "c");
	}

	@Test
	public void addToSystemClasspath() {
		Boot.addToSystemClasspath("/mypath");
		ArgumentCaptor<String> capturedArg = ArgumentCaptor.forClass(String.class);
		verify(this.booter).addToSystemClasspath(capturedArg.capture());
		assertEquals("/mypath", capturedArg.getValue());
	}

	@Test
	public void getConsoleLogger() {
		PrintStream log = Boot.getConsoleLogger();
		verify(this.booter).getConsoleLogger();
		assertSame(this.logger, log);
	}

	@Test
	public void setConsoleLogger() {
		PrintStream stream = mock(PrintStream.class);
		Boot.setConsoleLogger(stream);
		ArgumentCaptor<PrintStream> capturedLog = ArgumentCaptor.forClass(PrintStream.class);
		verify(this.booter).setConsoleLogger(capturedLog.capture());
		assertSame(stream, capturedLog.getValue());
	}

	@Test
	public void showHelp_noLogger() {
		Boot.showHelp();
		ArgumentCaptor<PrintWriter> capturedLog = ArgumentCaptor.forClass(PrintWriter.class);
		verify(this.booter).showHelp(capturedLog.capture());
		assertNull(capturedLog.getValue());
	}

	@Test
	public void showHelp_logger() {
		PrintWriter logger = mock(PrintWriter.class);
		Boot.showHelp(logger);
		ArgumentCaptor<PrintWriter> capturedLog = ArgumentCaptor.forClass(PrintWriter.class);
		verify(this.booter).showHelp(capturedLog.capture());
		assertSame(logger, capturedLog.getValue());
	}

	@Test
	public void showJanusLogo() {
		Boot.showJanusLogo();
		verify(this.booter).showJanusLogo();
	}

	@Test
	public void showClasspath() {
		Boot.showClasspath();
		verify(this.booter).showClasspath();
	}

	@Test
	public void showDefaults() {
		Boot.showDefaults();
		verify(this.booter).showDefaults();
	}

	@Test
	public void showVersion() {
		Boot.showVersion();
		verify(this.booter).showVersion();
	}

	@Test
	public void getProgramName() {
		Boot.getProgramName();
		verify(this.booter).getProgramName();
	}

	@Test
	public void setRandomContextUUID() {
		Boot.setRandomContextUUID();
		verify(this.booter).setRandomContextUUID();
	}

	@Test
	public void setBootAgentTypeContextUUID() {
		Boot.setBootAgentTypeContextUUID();
		verify(this.booter).setBootAgentTypeContextUUID();
	}

	@Test
	public void setDefaultContextUUID() {
		Boot.setDefaultContextUUID();
		verify(this.booter).setDefaultContextUUID();
	}

	@Test
	public void setVerboseLevel() {
		Boot.setVerboseLevel(345);
		ArgumentCaptor<Integer> capturedLevel = ArgumentCaptor.forClass(Integer.class);
		verify(this.booter).setVerboseLevel(capturedLevel.capture());
		assertEquals(345, capturedLevel.getValue());
	}

	@Test
	public void getBootAgentIdentifier() {
		Boot.getBootAgentIdentifier();
		verify(this.booter).getBootAgentIdentifier();
	}

	@Test
	public void startJanus() {
		Boot.startJanus(AbstractBooterTest.AgentMock.class, 1, "a");
		ArgumentCaptor<Class<? extends Agent>> capturedType = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Object> capturedArg = ArgumentCaptor.forClass(Object.class);
		verify(this.booter).startJanus(capturedType.capture(), capturedArg.capture());
		assertEquals(AbstractBooterTest.AgentMock.class, capturedType.getValue());
		assertContains(capturedArg.getAllValues(), 1, "a");
	}

	@Test
	public void startJanusWithModuleType() {
		Boot.startJanusWithModuleType(AbstractBooterTest.TestModule.class, AbstractBooterTest.AgentMock.class, 1, "a");
		ArgumentCaptor<Class<? extends Module>> capturedModule = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Class<? extends Agent>> capturedType = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Object> capturedArg = ArgumentCaptor.forClass(Object.class);
		verify(this.booter).startJanusWithModuleType(capturedModule.capture(), capturedType.capture(), capturedArg.capture());
		assertEquals(AbstractBooterTest.TestModule.class, capturedModule.getValue());
		assertEquals(AbstractBooterTest.AgentMock.class, capturedType.getValue());
		assertContains(capturedArg.getAllValues(), 1, "a");
	}

	@Test
	public void startJanusWithModule() {
		Module module = mock(Module.class);
		Boot.startJanusWithModule(module, AbstractBooterTest.AgentMock.class, 1, "a");
		ArgumentCaptor<Module> capturedModule = ArgumentCaptor.forClass(Module.class);
		ArgumentCaptor<Class<? extends Agent>> capturedType = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Object> capturedArg = ArgumentCaptor.forClass(Object.class);
		verify(this.booter).startJanusWithModule(capturedModule.capture(), capturedType.capture(), capturedArg.capture());
		assertSame(module, capturedModule.getValue());
		assertEquals(AbstractBooterTest.AgentMock.class, capturedType.getValue());
		assertContains(capturedArg.getAllValues(), 1, "a");
	}

}
