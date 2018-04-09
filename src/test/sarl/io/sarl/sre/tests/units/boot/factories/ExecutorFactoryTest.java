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

package io.sarl.sre.tests.units.boot.factories;

import static org.junit.Assert.*;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.sarl.sre.tests.testutils.AbstractSreTest;
import io.bootique.config.ConfigurationFactory;
import io.sarl.lang.core.Agent;
import io.sarl.sre.Kernel;
import io.sarl.sre.boot.factories.ExecutorFactory;
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
public class ExecutorFactoryTest extends AbstractSreTest {

	@Nullable
	private ExecutorFactory factory;
	
	@Before
	public void setUp() {
		this.factory = new ExecutorFactory();
	}
	
	@Test
	public void getConfigurationFactory() {
		ConfigurationFactory factory = mock(ConfigurationFactory.class);
		ExecutorFactory executorFactory = mock(ExecutorFactory.class);
		when(factory.config(any(Class.class), any(String.class))).thenReturn(executorFactory);
		assertSame(executorFactory, ExecutorFactory.getConfigurationFactory(factory));
		ArgumentCaptor<Class<?>> arg0 = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
		verify(factory, only()).config(arg0.capture(), arg1.capture());
		assertEquals(ExecutorFactory.class, arg0.getValue());
		assertEquals(ExecutorFactory.EXECUTOR_PREFIX, arg1.getValue());
	}

	@Test
	public void getMaxThreads() {
		assertEquals(ExecutorFactory.MAX_NUMBER_OF_THREADS_IN_EXECUTOR_VALUE, this.factory.getMaxThreads());
	}

	@Test
	public void setMaxThreads() {
		assertEquals(ExecutorFactory.MAX_NUMBER_OF_THREADS_IN_EXECUTOR_VALUE, this.factory.getMaxThreads());
		this.factory.setMaxThreads(12345);
		assertEquals(12345, this.factory.getMaxThreads());
	}

	@Test
	public void getMinThreads() {
		assertEquals(ExecutorFactory.MIN_NUMBER_OF_THREADS_IN_EXECUTOR_VALUE, this.factory.getMinThreads());
	}

	@Test
	public void setMinThreads() {
		assertEquals(ExecutorFactory.MIN_NUMBER_OF_THREADS_IN_EXECUTOR_VALUE, this.factory.getMinThreads());
		this.factory.setMinThreads(12345);
		assertEquals(12345, this.factory.getMinThreads());
	}

	@Test
	public void getKeepAliveDuration() {
		assertEquals(ExecutorFactory.THREAD_KEEP_ALIVE_DURATION_VALUE, this.factory.getKeepAliveDuration());
	}

	@Test
	public void setKeepAliveDuration() {
		assertEquals(ExecutorFactory.THREAD_KEEP_ALIVE_DURATION_VALUE, this.factory.getKeepAliveDuration());
		this.factory.setKeepAliveDuration(12345);
		assertEquals(12345, this.factory.getKeepAliveDuration());
	}

	@Test
	public void getTimeOut() {
		assertEquals(ExecutorFactory.THREAD_TIMEOUT_VALUE, this.factory.getTimeout());
	}

	@Test
	public void setTimeOut() {
		assertEquals(ExecutorFactory.THREAD_TIMEOUT_VALUE, this.factory.getTimeout());
		this.factory.setTimeout(12345);
		assertEquals(12345, this.factory.getTimeout());
	}

	@Test
	public void setInternalErrorVerboseLevel() {
		assertEquals(ExecutorFactory.INTERNAL_ERROR_VERBOSE_LEVEL_VALUE, this.factory.getInternalErrorVerboseLevel());
		this.factory.setInternalErrorVerboseLevel("error");
		assertEquals("severe", this.factory.getInternalErrorVerboseLevel());
		this.factory.setInternalErrorVerboseLevel("warning");
		assertEquals("warning", this.factory.getInternalErrorVerboseLevel());
	}

	@Test
	public void getInternalErrorVerboseLevel() {
		assertEquals(ExecutorFactory.INTERNAL_ERROR_VERBOSE_LEVEL_VALUE, this.factory.getInternalErrorVerboseLevel());
	}

	@Test
	public void getInternalErrorVerboseLevelObject() {
		assertEquals(LoggerCreator.parseLoggingLevel(ExecutorFactory.INTERNAL_ERROR_VERBOSE_LEVEL_VALUE), this.factory.getInternalErrorVerboseLevelObject());
		this.factory.setInternalErrorVerboseLevel("error");
		assertEquals(Level.SEVERE, this.factory.getInternalErrorVerboseLevelObject());
		this.factory.setInternalErrorVerboseLevel("warning");
		assertEquals(Level.WARNING, this.factory.getInternalErrorVerboseLevelObject());
	}

}
