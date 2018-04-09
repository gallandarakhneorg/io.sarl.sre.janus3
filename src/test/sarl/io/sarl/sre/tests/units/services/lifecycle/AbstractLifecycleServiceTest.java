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

package io.sarl.sre.tests.units.services.lifecycle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.sarl.core.Destroy;
import io.sarl.core.Initialize;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.Skill;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.util.SynchronizedIterable;
import io.sarl.sarlspecification.SarlSpecificationChecker;
import io.sarl.sre.capacities.InternalEventBusCapacity;
import io.sarl.sre.services.context.ExternalContextMemberListener;
import io.sarl.sre.services.context.Context;
import io.sarl.sre.services.executor.ExecutorService;
import io.sarl.sre.services.lifecycle.AbstractLifecycleService;
import io.sarl.sre.services.lifecycle.AgentLife;
import io.sarl.sre.services.lifecycle.AgentState;
import io.sarl.sre.services.lifecycle.ContextReference;
import io.sarl.sre.services.lifecycle.InvalidSarlSpecificationException;
import io.sarl.sre.services.lifecycle.KernelAgentLifecycleListener;
import io.sarl.sre.services.lifecycle.LifecycleService;
import io.sarl.sre.services.lifecycle.LifecycleServiceListener;
import io.sarl.sre.services.lifecycle.SkillUninstaller;
import io.sarl.sre.services.lifecycle.SpawnDisabledException;
import io.sarl.sre.services.lifecycle.SpawnResult;
import io.sarl.sre.services.logging.LoggingService;
import io.sarl.sre.tests.testutils.AbstractSreTest;
import io.sarl.tests.api.Nullable;
import io.sarl.util.Collections3;
import io.sarl.util.OpenEventSpace;
import io.sarl.util.OpenEventSpaceSpecification;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public abstract class AbstractLifecycleServiceTest<T extends AbstractLifecycleService> extends AbstractSreTest {

	@Nullable
	protected UUID contextId;

	@Nullable
	protected UUID agentId;
	
	@Nullable
	protected Agent agent;

	@Nullable
	protected Agent agent2;

	@Nullable
	protected Context outContext;

	@Nullable
	private ExecutorService executor;

	@Nullable
	private LoggingService logger;

	@Nullable
	protected SarlSpecificationChecker checker;

	@Nullable
	private SkillUninstaller skillInstaller;

	@Nullable
	private MyEventBus eventBus;

	@Nullable
	protected T service;
	
	@Before
	public void setUp() {
		this.contextId = UUID.randomUUID();
		this.outContext = mock(Context.class);
		this.eventBus = spy(new MyEventBus());
		when(this.outContext.getID()).thenReturn(this.contextId);
		OpenEventSpace space = mock(OpenEventSpace.class);
		when(this.outContext.getDefaultSpace()).thenReturn(space);
		when(space.getSpaceID()).thenReturn(new SpaceID(this.contextId, UUID.randomUUID(), OpenEventSpaceSpecification.class));
		this.agentId = UUID.randomUUID();
		this.agent = spy(new MyAgent(this.contextId, this.agentId));
		when(this.agent.getID()).thenReturn(this.agentId);
		this.agent2 = spy(new MyAgent(this.contextId, this.agentId, this.eventBus));
		when(this.agent.getID()).thenReturn(this.agentId);
		this.checker = mock(SarlSpecificationChecker.class);
		this.service = spy(newService());
		this.executor = mock(ExecutorService.class);
		this.service.setExecutorService(this.executor);
		this.logger = mock(LoggingService.class);
		this.service.setLoggingService(this.logger);
		this.skillInstaller = mock(SkillUninstaller.class);
		this.service.setSkillUninstaller(this.skillInstaller);
		this.service.setExternalContextMemberListenerProvider(() -> mock(ExternalContextMemberListener.class));
		this.service.setLifecycleServiceListenerProvider(() -> mock(LifecycleServiceListener.class));
	}

	protected abstract T newService();

	protected void startService() {
		startServiceManually(this.service);
	}
	
	private void createInnerContext(UUID... ids) {
		Context innerContext = mock(Context.class);
		AgentLife.getLife(this.agent).setInnerContext(innerContext);
		OpenEventSpace space = mock(OpenEventSpace.class);
		when(innerContext.getDefaultSpace()).thenReturn(space);
		Set<UUID> set = new HashSet<>();
		set.add(this.agentId);
		set.addAll(Arrays.asList(ids));
		when(space.getParticipants()).thenReturn(Collections3.unmodifiableSynchronizedSet(set, this));
	}

	private Context createOuterContext(Agent agent) {
		UUID contextId = UUID.randomUUID();
		Context context = mock(Context.class);
		when(context.getID()).thenReturn(contextId);
		OpenEventSpace space = mock(OpenEventSpace.class);
		when(context.getDefaultSpace()).thenReturn(space);
		when(space.getSpaceID()).thenReturn(new SpaceID(contextId, UUID.randomUUID(), OpenEventSpaceSpecification.class));
		Set<UUID> set = new HashSet<>();
		set.add(this.agentId);
		set.addAll(Arrays.asList(agent.getID()));
		when(space.getParticipants()).thenReturn(Collections3.unmodifiableSynchronizedSet(set, this));
		return context;
	}

	@Test
	public void getServiceType() {
		assertEquals(LifecycleService.class, this.service.getServiceType());
	}

	@Test
	public abstract void getServiceDependencies();

	@Test
	public void isKillableAgent_noInnerContext() {
		assertTrue(this.service.isKillableAgent(this.agent));
	}

	@Test
	public void isKillableAgent_emptyInnerContext() {
		createInnerContext();
		assertTrue(this.service.isKillableAgent(this.agent));
	}

	@Test
	public void isKillableAgent_oneChild() {
		createInnerContext(UUID.randomUUID());
		assertFalse(this.service.isKillableAgent(this.agent));
	}

	@Test
	public void isKillableAgent_twoChild() {
		createInnerContext(UUID.randomUUID(), UUID.randomUUID());
		assertFalse(this.service.isKillableAgent(this.agent));
	}

	@Test(expected = SpawnDisabledException.class)
	public void spawnAgent_0_serviceNotStarted() throws Exception {
		SpawnResult result = this.service.spawnAgent(0, null, this.outContext, null, null, null);
		result.throwAnyError();
	}
	
	@Test(expected = SpawnDisabledException.class)
	public void spawnAgent_0() throws Exception {
		startService();
		SpawnResult result = this.service.spawnAgent(0, null, this.outContext, null, null, null);
		result.throwAnyError();
	}

	@Test(expected = SpawnDisabledException.class)
	public void spawnAgent_1_serviceNotStarted() throws Exception {
		SpawnResult result = this.service.spawnAgent(1, null, this.outContext, null, null, null);
		result.throwAnyError();
	}

	@Test
	public void spawnAgent_1() {
		startService();
		UUID newAgentId = UUID.randomUUID();
		when(this.agent2.getID()).thenReturn(newAgentId);
		LifecycleServiceListener listener1 = mock(LifecycleServiceListener.class);
		this.service.addLifecycleServiceListener(listener1);
		KernelAgentLifecycleListener listener2 = mock(KernelAgentLifecycleListener .class);
		this.service.addKernelAgentLifecycleListener(listener2);
		
		when(this.checker.isValidSarlElement(any())).thenReturn(true);
		
		SpawnResult result = this.service.spawnAgent(1, this.agentId, this.outContext, newAgentId, MyAgent.class, 1, "a");
		Iterable<UUID> ids = result.getSpawnedAgents();

		// Created agent identifiers
		assertNotNull(ids);
		assertContains(ids, newAgentId);
	
		// Agent State
		assertSame(AgentState.ALIVE, AgentLife.getLife(this.agent2).getState());
		
		// Agent Initialize
		ArgumentCaptor<Event> capEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Boolean> capGatherEvents = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Boolean> capThrownExceptions = ArgumentCaptor.forClass(Boolean.class);
		verify(this.eventBus).fireEventAndWait(capEvent.capture(), capGatherEvents.capture(), capThrownExceptions.capture());
		assertTrue(capGatherEvents.getValue());
		assertTrue(capThrownExceptions.getValue());
		assertInstanceOf(Initialize.class, capEvent.getValue());
		Initialize initEvent = (Initialize) capEvent.getValue();
		assertNotNull(initEvent.getSource());
		assertEquals(this.agentId, initEvent.spawner);
		assertContains(Arrays.asList(initEvent.parameters), 1, "a");

		// Agent spawned
		ArgumentCaptor<UUID> capSpawningAgent = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Context> capParentContext = ArgumentCaptor.forClass(Context.class);
		ArgumentCaptor<Class<? extends Agent>> capAgentType = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<List<Agent>> capAgents = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<Object[]> capParams = ArgumentCaptor.forClass(Object[].class);
		verify(listener1).agentSpawned(
				capSpawningAgent.capture(),
				capParentContext.capture(),
				capAgentType.capture(),
				capAgents.capture(),
				capParams.capture());
		assertEquals(this.agentId, capSpawningAgent.getValue());
		assertSame(this.outContext, capParentContext.getValue());
		assertSame(MyAgent.class, capAgentType.getValue());
		assertContains(Arrays.asList(capParams.getValue()), 1, "a");
		List<Agent> agents = capAgents.getValue();
		assertEquals(1, agents.size());
		assertSame(this.agent2, agents.get(0));

		// Kernel event.
		verifyZeroInteractions(listener2);
	}

	@Test(expected = InvalidSarlSpecificationException.class)
	public void spawnAgent_1_noSpec() throws Exception {
		startService();
		UUID newAgentId = UUID.randomUUID();
		SpawnResult result = this.service.spawnAgent(1, this.agentId, this.outContext, newAgentId, InvalidMyAgent1.class, 1, "a");
		result.throwAnyError();
	}

	@Test(expected = InvalidSarlSpecificationException.class)
	public void spawnAgent_1_invalidSpec() throws Exception {
		startService();
		UUID newAgentId = UUID.randomUUID();
		SpawnResult result = this.service.spawnAgent(1, this.agentId, this.outContext, newAgentId, InvalidMyAgent2.class, 1, "a");
		result.throwAnyError();
	}

	@Test
	public void killAgent_noInnerContext_serviceNotStarted() {
		assertFalse(this.service.killAgent(this.agent));
	}

	@Test
	public void killAgent_noInnerContext_serviceStarted() {
		LifecycleServiceListener listener1 = mock(LifecycleServiceListener.class);
		this.service.addLifecycleServiceListener(listener1);
		KernelAgentLifecycleListener listener2 = mock(KernelAgentLifecycleListener .class);
		this.service.addKernelAgentLifecycleListener(listener2);

		startService();
		Context defaultContext = createOuterContext(this.agent2);
		AgentLife.getLife(this.agent2).setDefaultContext(defaultContext,
				new Address(defaultContext.getDefaultSpace().getSpaceID(), this.agent.getID()));
		Context outerContext = createOuterContext(this.agent2);
		AgentLife.getLife(this.agent2).addExternalContext(outerContext,
				new Address(outerContext.getDefaultSpace().getSpaceID(), this.agent.getID()));

		assertTrue(this.service.killAgent(this.agent2));
		
		// Uninstall the skills
		ArgumentCaptor<Agent> capAgent = ArgumentCaptor.forClass(Agent.class);
		verify(this.skillInstaller).uninstallSkillsBeforeDestroy(capAgent.capture());
		assertSame(this.agent2, capAgent.getValue());
		
		capAgent = ArgumentCaptor.forClass(Agent.class);
		ArgumentCaptor<Iterable<? extends Skill>> capSkills = ArgumentCaptor.forClass(Iterable.class);
		verify(this.skillInstaller).uninstallSkillsAfterDestroy(capAgent.capture(), capSkills.capture());
		assertSame(this.agent2, capAgent.getValue());
		assertNotNull(capSkills.getValue());

		// Destroy event
		ArgumentCaptor<Event> capEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Boolean> capGatherEvents = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Boolean> capThrownExceptions = ArgumentCaptor.forClass(Boolean.class);
		verify(this.eventBus).fireEventAndWait(capEvent.capture(), capGatherEvents.capture(), capThrownExceptions.capture());
		assertFalse(capGatherEvents.getValue());
		assertFalse(capThrownExceptions.getValue());
		assertInstanceOf(Destroy.class, capEvent.getValue());
		Destroy destroyEvent = (Destroy) capEvent.getValue();
		assertNotNull(destroyEvent.getSource());
		
		// Agent destroy notification
		capAgent = ArgumentCaptor.forClass(Agent.class);
		ArgumentCaptor<Iterable<ContextReference>> capContexts = ArgumentCaptor.forClass(Iterable.class);
		verify(listener1).agentDestroyed(capAgent.capture(), capContexts.capture());
		assertEquals(this.agent2, capAgent.getValue());
		assertContains(
				IterableExtensions.map(capContexts.getValue(), (it) -> it.getContext()),
				defaultContext, outerContext);
		
		// Kernel destroy
		verifyNoMoreInteractions(listener2);
	}

	@Test
	public void killAgent_emptyInnerContext_serviceNotStarted() {
		createInnerContext();
		assertTrue(this.service.isKillableAgent(this.agent));
	}

	@Test
	public void killAgent_emptyInnerContext_serviceStarted() {
		startService();
		createInnerContext();
		assertTrue(this.service.isKillableAgent(this.agent));
	}

	@Test
	public void killAgent_oneChild() {
		createInnerContext(UUID.randomUUID());
		assertFalse(this.service.isKillableAgent(this.agent));
	}

	@Test
	public void killAgent_twoChild() {
		createInnerContext(UUID.randomUUID(), UUID.randomUUID());
		assertFalse(this.service.isKillableAgent(this.agent));
	}

	@SarlSpecification
	public static class MyAgent extends Agent {

		public MyAgent(UUID parentID, UUID agentID, MyEventBus skill) {
			super(parentID, agentID);
			setSkill(skill);
		}
		
		public MyAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

	}

	public static class InvalidMyAgent1 extends Agent {

		public InvalidMyAgent1(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}
		
	}

	@SarlSpecification("0.1")
	public static class InvalidMyAgent2 extends Agent {

		public InvalidMyAgent2(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}
		
	}

	private static class MyEventBus extends Skill implements InternalEventBusCapacity {

		@Override
		public void registerEventBusListener(Object listener, Function1<? super Event, ? extends Boolean> filter,
				Procedure1<? super Object> callback) {
		}

		@Override
		public void unregisterEventBusListener(Object listener, Procedure1<? super Object> callback) {
		}

		@Override
		public void unregisterEventBusListener(Class<?> type, Procedure1<? super Object> callback) {
		}

		@Override
		public Iterable<Event> fireEventAndWait(Event event, boolean gatherEvents, boolean thrownExceptions) {
			return null;
		}

		@Override
		public Iterable<Event> fireEventAndWait(Event event, boolean gatherEvents, boolean thrownExceptions, Object listener) {
			return null;
		}

		@Override
		public void fireEvent(Event event) {
		}

		@Override
		public EventListener getAssociatedEventBusListener() {
			return null;
		}

		@Override
		public <T> SynchronizedIterable<T> getRegisteredEventBusListeners(Class<T> type) {
			return null;
		}
		
	}

}
