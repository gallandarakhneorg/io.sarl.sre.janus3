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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

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
import io.bootique.config.ConfigurationFactory;
import io.sarl.lang.core.Agent;
import io.sarl.sre.Kernel;
import io.sarl.sre.boot.factories.BootFactory;
import io.sarl.sre.boot.factories.InvalidAgentNameException;
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
public class BootFactoryTest extends AbstractSreTest {

	@Nullable
	private BootFactory factory;
	
	@Before
	public void setUp() {
		this.factory = new BootFactory();
	}
	
	@Test
	public void setBootAgent() {
		this.factory.setBootAgent(AgentMock.class.getName());
		assertEquals(AgentMock.class.getName(), this.factory.getBootAgent());
	}

	@Test
	public void setRootContextID() {
		UUID id = UUID.randomUUID();
		this.factory.setRootContextID(id);
		assertSame(id, this.factory.getRootContextID());
	}

	@Test
	public void setRootContextBootType() {
		for (RootContextType type : RootContextType.values()) {
			this.factory.setRootContextBootType(type);
			assertSame(type, this.factory.getRootContextBootType());
		}
	}

	@Test
	public void getRootContextBootType() {
		assertSame(RootContextType.DEFAULT_CONTEXT_ID, this.factory.getRootContextBootType());
		for (RootContextType type : RootContextType.values()) {
			this.factory.setRootContextBootType(type);
			assertSame(type, this.factory.getRootContextBootType());
		}
	}

	@Test
	public void setRootSpaceID() {
		UUID id = UUID.randomUUID();
		this.factory.setRootSpaceID(id);
		assertSame(id, this.factory.getRootSpaceID());
	}

	@Test
	public void setProgramName() {
		String name = UUID.randomUUID().toString();
		this.factory.setProgramName(name);
		assertSame(name, this.factory.getProgramName());
	}

	@Test
	public void getConfigurationFactory() {
		ConfigurationFactory factory = mock(ConfigurationFactory.class);
		BootFactory bootFactory = mock(BootFactory.class);
		when(factory.config(any(Class.class), any(String.class))).thenReturn(bootFactory);
		assertSame(bootFactory, BootFactory.getConfigurationFactory(factory));
		ArgumentCaptor<Class<?>> arg0 = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<String> arg1 = ArgumentCaptor.forClass(String.class);
		verify(factory, only()).config(arg0.capture(), arg1.capture());
		assertEquals(BootFactory.class, arg0.getValue());
		assertEquals(BootFactory.BOOT_PREFIX, arg1.getValue());
	}

	@Test
	public void getBootAgent_set() {
		this.factory.setBootAgent(AgentMock.class.getName());
		assertEquals(AgentMock.class.getName(), this.factory.getBootAgent());
	}

	@Test
	public void getBootAgent_notSet_commandLine() {
		this.factory.setCommandLineArguments(new String[] {AgentMock.class.getName(), "a", "b"});
		assertEquals(AgentMock.class.getName(), this.factory.getBootAgent());
	}

	@Test(expected = NoBootAgentNameException.class)
	public void getBootAgent_notSet_noCommandLine() {
		this.factory.getBootAgent();
	}

	@Test
	public void getBootAgentClassLoader_set() {
		this.factory.setBootAgent(AgentMock.class.getName());
		assertEquals(AgentMock.class, this.factory.getBootAgent(getClass().getClassLoader()));
	}

	@Test
	public void getBootAgentClassLoader_notSet_commandLine() {
		this.factory.setCommandLineArguments(new String[] {AgentMock.class.getName(), "a", "b"});
		assertEquals(AgentMock.class, this.factory.getBootAgent(getClass().getClassLoader()));
	}

	@Test(expected = NoBootAgentNameException.class)
	public void getBootAgentClassLoader_notSet_noCommandLine() {
		this.factory.getBootAgent(getClass().getClassLoader());
	}

	@Test(expected = InvalidAgentNameException.class)
	public void getBootAgentClassLoader_set_notAgentType() {
		this.factory.setBootAgent(String.class.getName());
		this.factory.getBootAgent(getClass().getClassLoader());
	}

	@Test(expected = InvalidAgentNameException.class)
	public void getBootAgentClassLoader_notSet_commandLine_notAgentType() {
		this.factory.setCommandLineArguments(new String[] {String.class.getName(), "a", "b"});
		this.factory.getBootAgent(getClass().getClassLoader());
	}

	@Test
	public void getCommandLineArgs() {
		assertArrayEquals(new String[0], this.factory.getCommandLineArgs());
	}

	@Test
	public void setCommandLineArgs() {
		final String[] args = new String[] { "a", "b", "c" };
		final String[] expected = new String[] { "b", "c" };
		this.factory.setCommandLineArguments(args);
		assertArrayEquals(expected, this.factory.getCommandLineArgs());
	}

	@Test
	public void getRootContextID_default() {
		assertEquals(UUID.fromString(BootFactory.ROOT_CONTEXT_ID_VALUE), this.factory.getRootContextID());
	}

	@Test
	public void getRootContextID_set() {
		UUID id = UUID.randomUUID();
		this.factory.setRootContextID(id);
		assertSame(id, this.factory.getRootContextID());
	}

	@Test
	public void getRootContextID_random() {
		this.factory.setRootContextBootType(RootContextType.RANDOM_CONTEXT_ID);
		UUID id = this.factory.getRootContextID();
		assertNotNull(id);
		assertNotEquals(BootFactory.ROOT_CONTEXT_ID_VALUE, id.toString());
	}

	@Test(expected = NoBootAgentNameException.class)
	public void getRootContextID_boot_notBootAgent() {
		this.factory.setRootContextBootType(RootContextType.BOOT_AGENT_NAME_CONTEXT_ID);
		UUID id = this.factory.getRootContextID();
		assertSame(BootFactory.ROOT_CONTEXT_ID_VALUE, id);
	}

	@Test
	public void getRootContextID_boot_bootAgent() {
		this.factory.setRootContextBootType(RootContextType.BOOT_AGENT_NAME_CONTEXT_ID);
		this.factory.setBootAgent(AgentMock.class.getName());
		UUID expected = UUID.nameUUIDFromBytes(AgentMock.class.getName().getBytes());
		UUID id = this.factory.getRootContextID();
		assertEquals(expected, id);
	}

	@Test
	public void getRootSpaceID() {
		assertEquals(UUID.fromString(BootFactory.ROOT_DEFAULT_SPACE_ID_VALUE), this.factory.getRootSpaceID());
		UUID id = UUID.randomUUID();
		this.factory.setRootSpaceID(id);
		assertEquals(id, this.factory.getRootSpaceID());
	}

	@Test
	public void getProgramName() {
		assertEquals(BootFactory.PROGRAM_NAME_VALUE, this.factory.getProgramName());
		String name = UUID.randomUUID().toString();
		this.factory.setProgramName(name);
		assertEquals(name, this.factory.getProgramName());
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
