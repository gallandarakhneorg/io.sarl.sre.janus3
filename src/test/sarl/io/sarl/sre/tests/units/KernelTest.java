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

package io.sarl.sre.tests.units;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.sarl.sre.tests.testutils.AbstractSreTest;
import io.sarl.lang.core.Agent;
import io.sarl.sre.Kernel;
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
public class KernelTest extends AbstractSreTest {

	@Nullable
	private UncaughtExceptionHandler handler;

	@Nullable
	private IServiceManager serviceManager;

	@Nullable
	private LoggingService logger;

	@Nullable
	private LifecycleService lifecycle;

	@Nullable
	private ContextService context;

	@Nullable
	private Context rootContext;

	@Nullable
	private Kernel kernel;

	@Before
	public void setUp() {
		this.rootContext = mock(Context.class);
		this.handler = mock(UncaughtExceptionHandler.class);
		this.logger = mock(LoggingService.class);
		this.lifecycle = mock(LifecycleService.class);
		this.context = mock(ContextService.class);
		when(this.context.getRootContext()).thenReturn(this.rootContext);
		this.serviceManager = mock(IServiceManager.class);
		when(this.serviceManager.getService(any())).thenAnswer((it) -> {
			if (LoggingService.class.equals(it.getArgument(0))) {
				return this.logger;
			}
			if (LifecycleService.class.equals(it.getArgument(0))) {
				return this.lifecycle;
			}
			if (ContextService.class.equals(it.getArgument(0))) {
				return this.context;
			}
			return null;
		});
		this.kernel = new Kernel(this.serviceManager, this.handler);
		verify(this.lifecycle).addKernelAgentLifecycleListener(any());
		verify(this.serviceManager).startServices(any());
	}

	@Test
	public void isRunning() {
		// As soon the kernel is created, it is running.
		assertTrue(this.kernel.isRunning());
	}

	@Test
	public void getLogger() {
		Logger log = mock(Logger.class);
		when(this.logger.getKernelLogger()).thenReturn(log);
		Logger l = this.kernel.getLogger();
		assertSame(log, l);
	}

	@Test
	public void spawnClassObjectArray() {
		when(this.lifecycle.spawnAgent(anyInt(), any(), any(), any(), any(), any())).then((it) -> {
			if (it.getArgument(3) != null) {
				return new SpawnResult(Arrays.asList((UUID) it.getArgument(3)), Collections.emptyList());
			}
			return new SpawnResult(Arrays.asList(UUID.randomUUID()), Collections.emptyList());
		});

		UUID id = this.kernel.spawn(MyAgent.class, 1, "a");
		
		ArgumentCaptor<Integer> capturedNb = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<UUID> capturedSpawner = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Context> capturedContext = ArgumentCaptor.forClass(Context.class);
		ArgumentCaptor<UUID> capturedAgentId = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Class<? extends Agent>> capturedAgentType = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Object> capturedParams = ArgumentCaptor.forClass(Object.class);
		verify(this.lifecycle).spawnAgent(capturedNb.capture(), capturedSpawner.capture(),
				capturedContext.capture(), capturedAgentId.capture(), capturedAgentType.capture(),
				capturedParams.capture());
		assertEquals(1, capturedNb.getValue());
		assertNull(capturedSpawner.getValue());
		assertSame(this.rootContext, capturedContext.getValue());
		assertNull(capturedAgentId.getValue());
		assertEquals(MyAgent.class, capturedAgentType.getValue());
		Assert.assertEquals(1, capturedParams.getAllValues().get(0));
		assertEquals("a", capturedParams.getAllValues().get(1));
	}

	@Test
	public void spawnUUIDClassObjectArray() {
		when(this.lifecycle.spawnAgent(anyInt(), any(), any(), any(), any(), any())).then((it) -> {
			if (it.getArgument(3) != null) {
				return new SpawnResult(Arrays.asList((UUID) it.getArgument(3)), Collections.emptyList());
			}
			return new SpawnResult(Arrays.asList(UUID.randomUUID()), Collections.emptyList());
		});
		UUID expectedId = UUID.randomUUID();

		UUID id = this.kernel.spawn(expectedId, MyAgent.class, 1, "a");
		
		assertSame(expectedId, id);
		
		ArgumentCaptor<Integer> capturedNb = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<UUID> capturedSpawner = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Context> capturedContext = ArgumentCaptor.forClass(Context.class);
		ArgumentCaptor<UUID> capturedAgentId = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Class<? extends Agent>> capturedAgentType = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Object> capturedParams = ArgumentCaptor.forClass(Object.class);
		verify(this.lifecycle).spawnAgent(capturedNb.capture(), capturedSpawner.capture(),
				capturedContext.capture(), capturedAgentId.capture(), capturedAgentType.capture(),
				capturedParams.capture());
		assertEquals(1, capturedNb.getValue());
		assertNull(capturedSpawner.getValue());
		assertSame(this.rootContext, capturedContext.getValue());
		assertSame(expectedId, capturedAgentId.getValue());
		assertEquals(MyAgent.class, capturedAgentType.getValue());
		Assert.assertEquals(1, capturedParams.getAllValues().get(0));
		assertEquals("a", capturedParams.getAllValues().get(1));
	}

	@Test
	public void spawnIntClassObjectArray() {
		List<UUID> ids = mock(List.class);
		SpawnResult result = new SpawnResult(ids, Collections.emptyList());
		when(this.lifecycle.spawnAgent(anyInt(), any(), any(), any(), any(), any())).thenReturn(result);

		Iterable<UUID> ids2 = this.kernel.spawn(34, MyAgent.class, 1, "a");
		
		assertSame(ids, ids2);
		
		ArgumentCaptor<Integer> capturedNb = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<UUID> capturedSpawner = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Context> capturedContext = ArgumentCaptor.forClass(Context.class);
		ArgumentCaptor<UUID> capturedAgentId = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Class<? extends Agent>> capturedAgentType = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Object> capturedParams = ArgumentCaptor.forClass(Object.class);
		verify(this.lifecycle).spawnAgent(capturedNb.capture(), capturedSpawner.capture(),
				capturedContext.capture(), capturedAgentId.capture(), capturedAgentType.capture(),
				capturedParams.capture());
		assertEquals(34, capturedNb.getValue());
		assertNull(capturedSpawner.getValue());
		assertSame(this.rootContext, capturedContext.getValue());
		assertNull(capturedAgentId.getValue());
		assertEquals(MyAgent.class, capturedAgentType.getValue());
		Assert.assertEquals(1, capturedParams.getAllValues().get(0));
		assertEquals("a", capturedParams.getAllValues().get(1));
	}

	@Test
	public void getService() {
		assertSame(this.logger, this.kernel.getService(LoggingService.class));
	}

	private static class MyAgent extends Agent {

		public MyAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

	}
}
