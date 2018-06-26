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

package io.sarl.sre.tests.units.boot;

import static org.junit.Assert.*;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;

import io.sarl.sre.tests.testutils.AbstractSreTest;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.config.ConfigurationFactory;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.sre.Kernel;
import io.sarl.sre.boot.ProgrammaticBootstrap;
import io.sarl.sre.boot.factories.BootFactory;
import io.sarl.sre.boot.factories.LoggingFactory;
import io.sarl.sre.boot.factories.NoBootAgentNameException;
import io.sarl.sre.boot.factories.RootContextType;
import io.sarl.sre.services.IServiceManager;
import io.sarl.sre.services.context.ContextService;
import io.sarl.sre.services.context.Context;
import io.sarl.sre.services.lifecycle.LifecycleService;
import io.sarl.sre.services.lifecycle.SpawnResult;
import io.sarl.sre.services.logging.LoggerCreator;
import io.sarl.sre.services.logging.LoggingService;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class ProgrammaticBootstrapTest extends AbstractSreTest {

	@Nullable
	private ProgrammaticBootstrap bootstrap;
	
	@Nullable
	private Kernel kernel;
	
	@Nullable
	private Bootique bootique;

	@Nullable
	private BQRuntime runtime;

	@Nullable
	private AgentContext rootContext;

	@Nullable
	private UUID id0;

	@Nullable
	private ConfigurationFactory configurationFactory;

	@Nullable
	private BootFactory bootFactory;

	@Nullable
	private Injector injector;

	private void createConfiguration(Class<? extends Agent> bootAgent) {
		this.bootFactory = new BootFactory();
		this.bootFactory.setBootAgent(bootAgent.getName());
		this.bootFactory.setCommandLineArgs(new String[] { AgentMock.class.getName(), "a" });
		this.bootFactory = spy(this.bootFactory);
		when(this.configurationFactory.config(any(Class.class), any(String.class))).thenReturn(this.bootFactory);
	}
	
	private void createBootiqueMock() {
		this.id0 = UUID.randomUUID();
		this.configurationFactory = mock(ConfigurationFactory.class);
		this.rootContext = mock(AgentContext.class);
		this.kernel = mock(Kernel.class);
		when(this.kernel.getRootContext()).thenReturn(this.rootContext);
		when(this.kernel.getService(any(Class.class))).thenReturn(mock(LifecycleService.class));
		when(this.kernel.spawn(any(Class.class), any(Object.class))).thenReturn(this.id0);
		when(this.kernel.spawn(anyInt(), any(Class.class), any(Object.class))).thenReturn(Arrays.asList(this.id0));
		this.injector = mock(Injector.class);
		when(this.injector.getInstance(any(Class.class))).thenAnswer((it) -> {
			final Class<?> type = it.getArgument(0);
			if (Kernel.class.equals(type)) {
				return this.kernel;
			}
			if (ConfigurationFactory.class.equals(type)) {
				return this.configurationFactory;
			}
			throw new IllegalStateException(type.getName());
		});
		this.runtime = mock(BQRuntime.class);
		when(this.runtime.getInstance(any(Class.class))).thenAnswer((it) -> {
			final Class<?> type = it.getArgument(0);
			if (Kernel.class.equals(type)) {
				return this.kernel;
			}
			if (ConfigurationFactory.class.equals(type)) {
				return this.configurationFactory;
			}
			if (BootFactory.class.equals(type)) {
				return this.bootFactory;
			}
			if (Injector.class.equals(type)) {
				return this.injector;
			}
			throw new IllegalStateException(type.getName());
		});
		this.bootique = mock(Bootique.class);
		when(this.bootique.module(any(Class.class))).thenReturn(this.bootique);
		when(this.bootique.autoLoadModules()).thenReturn(this.bootique);
		when(this.bootique.createRuntime()).thenReturn(this.runtime);
	}

	@Before
	public void setUp() {
		this.bootstrap = new ProgrammaticBootstrap();
	}

	@Test
	public void getKernel_start0() {
		assertNull(this.bootstrap.getKernel());
	}

	@Test
	public void getKernel_start1() {
		createBootiqueMock();
		this.bootstrap.startWithoutAgent(this.bootique);
		assertSame(this.kernel, this.bootstrap.getKernel());
	}

	@Test
	public void getRuntime_start0() {
		assertNull(this.bootstrap.getRuntime());
	}

	@Test
	public void getRuntime_start1() {
		createBootiqueMock();
		this.bootstrap.startWithoutAgent(this.bootique);
		assertSame(this.runtime, this.bootstrap.getRuntime());
	}

	@Test
	public void getBootAgentIdentifier_start0() {
		assertNull(this.bootstrap.getBootAgentIdentifier());
	}

	@Test
	public void getBootAgentIdentifier_start1() {
		createBootiqueMock();
		this.bootstrap.startWithoutAgent(this.bootique);
		assertNull(this.bootstrap.getBootAgentIdentifier());
	}

	@Test
	public void startAgentClassObjectArray() throws Exception {
		createBootiqueMock();
		createConfiguration(AgentMock.class);
		this.bootstrap.startWithoutAgent(this.bootique);
		UUID id = this.bootstrap.startAgent(AgentMock.class, "a");
		assertEquals(this.id0, id);
		ArgumentCaptor<Class<? extends Agent>> arg0 = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Object[]> arg1 = ArgumentCaptor.forClass(Object[].class);
		verify(this.kernel).spawn(arg0.capture(), arg1.capture());
		assertEquals(AgentMock.class, arg0.getValue());
		assertEquals("a", arg1.getAllValues().get(0));
		assertEquals(this.id0, this.bootstrap.getBootAgentIdentifier());
	}

	@Test
	public void startAgentIntClassObjectArray() throws Exception {
		createBootiqueMock();
		createConfiguration(AgentMock.class);
		this.bootstrap.startWithoutAgent(this.bootique);
		Iterable<UUID> id = this.bootstrap.startAgent(1, AgentMock.class, "a");
		Iterator<UUID> iterator = id.iterator();
		assertEquals(this.id0, iterator.next());
		assertFalse(iterator.hasNext());
		ArgumentCaptor<Integer> arg0 = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<Class<? extends Agent>> arg1 = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Object[]> arg2 = ArgumentCaptor.forClass(Object[].class);
		verify(this.kernel).spawn(arg0.capture(), arg1.capture(), arg2.capture());
		assertEquals(1, arg0.getValue());
		assertEquals(AgentMock.class, arg1.getValue());
		assertEquals("a", arg2.getAllValues().get(0));
		assertEquals(this.id0, this.bootstrap.getBootAgentIdentifier());
	}

	@Test
	public void startBootAgent() {
		createBootiqueMock();
		createConfiguration(AgentMock.class);
		this.bootstrap.startWithoutAgent(this.bootique);
		UUID id = this.bootstrap.startBootAgent();
		assertEquals(this.id0, id);
		ArgumentCaptor<Class<? extends Agent>> arg0 = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Object[]> arg1 = ArgumentCaptor.forClass(Object[].class);
		verify(this.kernel).spawn(arg0.capture(), arg1.capture());
		assertEquals(AgentMock.class, arg0.getValue());
		assertEquals("a", arg1.getAllValues().get(0));
		assertEquals(this.id0, this.bootstrap.getBootAgentIdentifier());
	}

	@Test
	public void startWithoutAgentBootique_start0() {
		createBootiqueMock();
		AgentContext ctx = this.bootstrap.startWithoutAgent(this.bootique);
		assertSame(this.rootContext, ctx);
		assertSame(this.kernel, this.bootstrap.getKernel());
		assertSame(this.runtime, this.bootstrap.getRuntime());
	}

	@Test
	public void startWithoutAgentBootique_start1() {
		createBootiqueMock();
		AgentContext ctx0 = this.bootstrap.startWithoutAgent(this.bootique);
		assertSame(this.rootContext, ctx0);
		assertSame(this.kernel, this.bootstrap.getKernel());
		assertSame(this.runtime, this.bootstrap.getRuntime());
		AgentContext ctx1 = this.bootstrap.startWithoutAgent(this.bootique);
		assertSame(this.rootContext, ctx1);
		assertSame(ctx0, ctx1);
		assertSame(this.kernel, this.bootstrap.getKernel());
		assertSame(this.runtime, this.bootstrap.getRuntime());
	}

	@Test
	public void startWithoutAgentBootiqueClass_start0() {
		createBootiqueMock();
		AgentContext ctx = this.bootstrap.startWithoutAgent(this.bootique, ModuleMock.class);
		assertSame(this.rootContext, ctx);
		assertSame(this.kernel, this.bootstrap.getKernel());
		assertSame(this.runtime, this.bootstrap.getRuntime());
	}

	@Test
	public void startWithoutAgentBootiqueClass_start1() {
		createBootiqueMock();
		AgentContext ctx0 = this.bootstrap.startWithoutAgent(this.bootique, ModuleMock.class);
		assertSame(this.rootContext, ctx0);
		assertSame(this.kernel, this.bootstrap.getKernel());
		assertSame(this.runtime, this.bootstrap.getRuntime());
		AgentContext ctx1 = this.bootstrap.startWithoutAgent(this.bootique, ModuleMock.class);
		assertSame(this.rootContext, ctx1);
		assertSame(ctx0, ctx1);
		assertSame(this.kernel, this.bootstrap.getKernel());
		assertSame(this.runtime, this.bootstrap.getRuntime());
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */	
	public static class ModuleMock implements Module {

		@Override
		public void configure(Binder binder) {
			//
		}
		
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */	
	public static class AgentMock extends Agent {

		public AgentMock(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}
		
	}

}
