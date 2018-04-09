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

package io.sarl.sre.tests.units.skills;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.sre.services.context.Context;
import io.sarl.sre.services.lifecycle.AgentLife;
import io.sarl.sre.services.lifecycle.AgentState;
import io.sarl.sre.services.lifecycle.LifecycleService;
import io.sarl.sre.services.lifecycle.SpawnResult;
import io.sarl.sre.skills.LifecycleSkill;
import io.sarl.sre.tests.testutils.AbstractSreTest;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class LifecycleSkillTest extends AbstractSreTest {

	@Nullable
	private LifecycleService service;
	
	@Nullable
	private UUID spawnerID;

	@Nullable
	private Context context;

	@Nullable
	private Agent agent;

	@Nullable
	private LifecycleSkill skill;

	@Before
	public void setUp() {
		this.spawnerID = UUID.randomUUID();
		this.service = mock(LifecycleService.class);
		this.context = mock(Context.class);
		when(this.context.getID()).thenReturn(UUID.randomUUID());
		this.agent = spy(new MyAgent(this.context.getID(), this.spawnerID));
		AgentLife.getLife(agent).setDefaultContext(this.context, mock(Address.class));
		this.skill = new LifecycleSkill(this.agent);
		this.skill.setLifecycleService(this.service);
	}

	private void forceAlive() {
		// Force being alive
		AgentLife.getLife(this.agent).setState(AgentState.ALIVE);
	}

	@Test
	public void spawn_notAlive() {
		UUID agentID = UUID.randomUUID();
		SpawnResult result = new SpawnResult(Collections.singletonList(agentID), Collections.emptyList());
		when(this.service.spawnAgent(anyInt(), any(), any(), any(), any(), any())).thenReturn(result);
		
		UUID id = this.skill.spawn(MyAgent.class, 1, "value");
		
		assertNull(id);
		
		verifyZeroInteractions(this.service);
	}

	@Test
	public void spawn_alive() {
		UUID agentID = UUID.randomUUID();
		SpawnResult result = new SpawnResult(Collections.singletonList(agentID), Collections.emptyList());
		when(this.service.spawnAgent(anyInt(), any(), any(), any(), any(), any())).thenReturn(result);

		forceAlive();
		
		UUID id = this.skill.spawn(MyAgent.class, 1, "value");
		
		assertSame(agentID, id);
		
		ArgumentCaptor<Integer> argument0 = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<UUID> argument1 = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Context> argument2 = ArgumentCaptor.forClass(Context.class);
		ArgumentCaptor<UUID> argument3 = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Class> argument4 = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Object> argument5 = ArgumentCaptor.forClass(Object.class);
		verify(this.service, times(1)).spawnAgent(argument0.capture(), argument1.capture(), argument2.capture(),
				argument3.capture(), argument4.capture(), argument5.capture());
		assertEquals(1, argument0.getValue());
		assertSame(this.spawnerID, argument1.getValue());
		assertSame(this.context, argument2.getValue());
		assertNull(argument3.getValue());
		assertEquals(MyAgent.class, argument4.getValue());
		assertArrayEquals(new Object[] { 1, "value" }, argument5.getAllValues().toArray()); //$NON-NLS-1$
	}

	@Test
	public void spawnInteger_notAlive() {
		UUID agentID1 = UUID.randomUUID();
		UUID agentID2 = UUID.randomUUID();
		UUID agentID3 = UUID.randomUUID();
		SpawnResult result = new SpawnResult(Arrays.asList(agentID1, agentID2, agentID3), Collections.emptyList());
		when(this.service.spawnAgent(anyInt(), any(), any(), any(), any(), any())).thenReturn(result);
		
		Iterable<UUID> id = this.skill.spawn(3, MyAgent.class, 1, "value");
		
		assertNotNull(id);
		assertContains(id);
		verifyZeroInteractions(this.service);
	}

	@Test
	public void spawnInteger_alive() {
		UUID agentID1 = UUID.randomUUID();
		UUID agentID2 = UUID.randomUUID();
		UUID agentID3 = UUID.randomUUID();
		SpawnResult result = new SpawnResult(Arrays.asList(agentID1, agentID2, agentID3), Collections.emptyList());
		when(this.service.spawnAgent(anyInt(), any(), any(), any(), any(), any())).thenReturn(result);

		forceAlive();
		
		Iterable<UUID> id = this.skill.spawn(3, MyAgent.class, 1, "value");
		
		assertNotNull(id);
		Iterator<UUID> iterator = id.iterator();
		assertSame(agentID1, iterator.next());
		assertSame(agentID2, iterator.next());
		assertSame(agentID3, iterator.next());
		assertFalse(iterator.hasNext());
		
		ArgumentCaptor<Integer> argument0 = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<UUID> argument1 = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Context> argument2 = ArgumentCaptor.forClass(Context.class);
		ArgumentCaptor<UUID> argument3 = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Class> argument4 = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Object> argument5 = ArgumentCaptor.forClass(Object.class);
		verify(this.service, times(1)).spawnAgent(argument0.capture(), argument1.capture(), argument2.capture(),
				argument3.capture(), argument4.capture(), argument5.capture());
		assertEquals(3, argument0.getValue());
		assertSame(this.spawnerID, argument1.getValue());
		assertSame(this.context, argument2.getValue());
		assertNull(argument3.getValue());
		assertEquals(MyAgent.class, argument4.getValue());
		assertArrayEquals(new Object[] { 1, "value" }, argument5.getAllValues().toArray()); //$NON-NLS-1$
	}

	@Test
	public void spawnInContext_notAlive() {
		UUID agentID = UUID.randomUUID();
		SpawnResult result = new SpawnResult(Collections.singletonList(agentID), Collections.emptyList());
		when(this.service.spawnAgent(anyInt(), any(), any(), any(), any(), any())).thenReturn(result);
		Context otherContext = mock(Context.class);
		
		UUID id = this.skill.spawnInContext(MyAgent.class, otherContext, 1, "value");
		
		assertNull(id);

		verifyZeroInteractions(this.service);
	}

	@Test
	public void spawnInContext_alive() {
		UUID agentID = UUID.randomUUID();
		SpawnResult result = new SpawnResult(Collections.singletonList(agentID), Collections.emptyList());
		when(this.service.spawnAgent(anyInt(), any(), any(), any(), any(), any())).thenReturn(result);
		Context otherContext = mock(Context.class);

		forceAlive();
		
		UUID id = this.skill.spawnInContext(MyAgent.class, otherContext, 1, "value");
		
		assertSame(agentID, id);
		
		ArgumentCaptor<Integer> argument0 = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<UUID> argument1 = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Context> argument2 = ArgumentCaptor.forClass(Context.class);
		ArgumentCaptor<UUID> argument3 = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Class> argument4 = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Object> argument5 = ArgumentCaptor.forClass(Object.class);
		verify(this.service, times(1)).spawnAgent(argument0.capture(), argument1.capture(), argument2.capture(),
				argument3.capture(), argument4.capture(), argument5.capture());
		assertEquals(1, argument0.getValue());
		assertSame(this.spawnerID, argument1.getValue());
		assertSame(otherContext, argument2.getValue());
		assertNull(argument3.getValue());
		assertEquals(MyAgent.class, argument4.getValue());
		assertArrayEquals(new Object[] { 1, "value" }, argument5.getAllValues().toArray()); //$NON-NLS-1$
	}

	@Test
	public void spawnInContextInteger_notAlive() {
		UUID agentID1 = UUID.randomUUID();
		UUID agentID2 = UUID.randomUUID();
		UUID agentID3 = UUID.randomUUID();
		SpawnResult result = new SpawnResult(Arrays.asList(agentID1, agentID2, agentID3), Collections.emptyList());
		when(this.service.spawnAgent(anyInt(), any(), any(), any(), any(), any())).thenReturn(result);
		Context otherContext = mock(Context.class);

		Iterable<UUID> id = this.skill.spawnInContext(3, MyAgent.class, otherContext, 1, "value");
		
		assertNotNull(id);
		assertContains(id);
		verifyZeroInteractions(this.service);
	}

	@Test
	public void spawnInContextInteger_alive() {
		UUID agentID1 = UUID.randomUUID();
		UUID agentID2 = UUID.randomUUID();
		UUID agentID3 = UUID.randomUUID();
		SpawnResult result = new SpawnResult(Arrays.asList(agentID1, agentID2, agentID3), Collections.emptyList());
		when(this.service.spawnAgent(anyInt(), any(), any(), any(), any(), any())).thenReturn(result);
		Context otherContext = mock(Context.class);

		forceAlive();

		Iterable<UUID> id = this.skill.spawnInContext(3, MyAgent.class, otherContext, 1, "value");
		
		assertNotNull(id);
		Iterator<UUID> iterator = id.iterator();
		assertSame(agentID1, iterator.next());
		assertSame(agentID2, iterator.next());
		assertSame(agentID3, iterator.next());
		assertFalse(iterator.hasNext());
		
		ArgumentCaptor<Integer> argument0 = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<UUID> argument1 = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Context> argument2 = ArgumentCaptor.forClass(Context.class);
		ArgumentCaptor<UUID> argument3 = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Class> argument4 = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Object> argument5 = ArgumentCaptor.forClass(Object.class);
		verify(this.service, times(1)).spawnAgent(argument0.capture(), argument1.capture(), argument2.capture(),
				argument3.capture(), argument4.capture(), argument5.capture());
		assertEquals(3, argument0.getValue());
		assertSame(this.spawnerID, argument1.getValue());
		assertSame(otherContext, argument2.getValue());
		assertNull(argument3.getValue());
		assertEquals(MyAgent.class, argument4.getValue());
		assertArrayEquals(new Object[] { 1, "value" }, argument5.getAllValues().toArray()); //$NON-NLS-1$
	}

	@Test
	public void spawnInContextWithID_notAlive() {
		UUID agentID = UUID.randomUUID();
		SpawnResult result = new SpawnResult(Collections.singletonList(agentID), Collections.emptyList());
		when(this.service.spawnAgent(anyInt(), any(), any(), any(), any(), any())).thenReturn(result);
		Context otherContext = mock(Context.class);
		
		UUID id = this.skill.spawnInContextWithID(MyAgent.class, agentID, otherContext, 1, "value");
		
		assertNull(id);
		
		verifyZeroInteractions(this.service);
	}

	@Test
	public void spawnInContextWithID_alive() {
		UUID agentID = UUID.randomUUID();
		SpawnResult result = new SpawnResult(Collections.singletonList(agentID), Collections.emptyList());
		when(this.service.spawnAgent(anyInt(), any(), any(), any(), any(), any())).thenReturn(result);
		Context otherContext = mock(Context.class);

		forceAlive();

		UUID id = this.skill.spawnInContextWithID(MyAgent.class, agentID, otherContext, 1, "value");
		
		assertSame(agentID, id);
		
		ArgumentCaptor<Integer> argument0 = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<UUID> argument1 = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Context> argument2 = ArgumentCaptor.forClass(Context.class);
		ArgumentCaptor<UUID> argument3 = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Class> argument4 = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Object> argument5 = ArgumentCaptor.forClass(Object.class);
		verify(this.service, times(1)).spawnAgent(argument0.capture(), argument1.capture(), argument2.capture(),
				argument3.capture(), argument4.capture(), argument5.capture());
		assertEquals(1, argument0.getValue());
		assertSame(this.spawnerID, argument1.getValue());
		assertSame(otherContext, argument2.getValue());
		assertSame(agentID, argument3.getValue());
		assertEquals(MyAgent.class, argument4.getValue());
		assertArrayEquals(new Object[] { 1, "value" }, argument5.getAllValues().toArray()); //$NON-NLS-1$
	}

	private static class MyAgent extends Agent {

		public MyAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}
		
	}

}
