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
import io.sarl.sre.boot.factories.NoBootAgentNameException;
import io.sarl.sre.boot.factories.RootContextType;
import io.sarl.sre.boot.factories.ServiceManagerFactory;
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
public class ServiceManagerFactoryTest extends AbstractSreTest {

	@Nullable
	private ServiceManagerFactory factory;
	
	@Before
	public void setUp() {
		this.factory = new ServiceManagerFactory();
	}
	
	@Test
	public void getConfigurationFactory() {
		ConfigurationFactory factory = mock(ConfigurationFactory.class);
		ServiceManagerFactory loggingFactory = mock(ServiceManagerFactory.class);
		when(factory.config(any(Class.class), any(String.class))).thenReturn(loggingFactory);
		assertSame(loggingFactory, ServiceManagerFactory.getConfigurationFactory(factory));
		ArgumentCaptor<Class<?>> arg0 = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
		verify(factory, only()).config(arg0.capture(), arg1.capture());
		assertEquals(ServiceManagerFactory.class, arg0.getValue());
		assertEquals(ServiceManagerFactory.SERVICE_MANAGER_PREFIX, arg1.getValue());
	}

	@Test
	public void getStartTimeout() {
		assertEquals(0l, this.factory.getStartTimeout());
	}

	@Test
	public void setStartTimeout() {
		assertEquals(0l, this.factory.getStartTimeout());
		this.factory.setStartTimeout(123l);
		assertEquals(123l, this.factory.getStartTimeout());
	}

	@Test
	public void setStartTimeout_negative() {
		assertEquals(0l, this.factory.getStartTimeout());
		this.factory.setStartTimeout(123l);
		this.factory.setStartTimeout(-456l);
		assertEquals(0l, this.factory.getStartTimeout());
	}

	@Test
	public void getStopTimeout() {
		assertEquals(0l, this.factory.getStopTimeout());
	}

	@Test
	public void setStopTimeout() {
		assertEquals(0l, this.factory.getStopTimeout());
		this.factory.setStopTimeout(123l);
		assertEquals(123l, this.factory.getStopTimeout());
	}

	@Test
	public void setStopTimeout_negative() {
		assertEquals(0l, this.factory.getStopTimeout());
		this.factory.setStopTimeout(123l);
		this.factory.setStopTimeout(-456l);
		assertEquals(0l, this.factory.getStopTimeout());
	}

}
