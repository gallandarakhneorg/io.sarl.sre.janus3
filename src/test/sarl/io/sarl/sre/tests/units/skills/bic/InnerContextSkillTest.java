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

package io.sarl.sre.tests.units.skills.bic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.Skill;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.util.SynchronizedIterable;
import io.sarl.sre.capacities.InternalEventBusCapacity;
import io.sarl.sre.services.context.Context;
import io.sarl.sre.services.context.ContextService;
import io.sarl.sre.services.lifecycle.AgentLife;
import io.sarl.sre.skills.bic.InnerContextAccessSkill;
import io.sarl.sre.skills.internal.EventBus;
import io.sarl.sre.tests.testutils.AbstractSreTest;
import io.sarl.tests.api.ManualMocking;
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
@ManualMocking
public class InnerContextSkillTest extends AbstractSreTest {

	@Nullable
	private ContextService service;
	
	@Nullable
	private UUID parentID;
	
	@Nullable
	private SpaceID innerSpaceID;

	@Nullable
	private Context context;

	@Nullable
	private Agent agent;

	@Nullable
	private InnerContextAccessSkill skill;

	@Before
	public void setUp() {
		this.parentID = UUID.randomUUID();
		this.innerSpaceID = new SpaceID(this.parentID, UUID.randomUUID(), OpenEventSpaceSpecification.class);
		this.service = mock(ContextService.class);
		this.context = mock(Context.class);
		when(this.context.getID()).thenReturn(UUID.randomUUID());
		this.agent = spy(new MyAgent(this.context.getID(), this.parentID));
		SpaceID defaultSpaceID = new SpaceID(this.context.getID(), this.parentID, OpenEventSpaceSpecification.class);
		Address defaultAdr = mock(Address.class);
		when(defaultAdr.getSpaceID()).thenReturn(defaultSpaceID);
		AgentLife.getLife(this.agent).setDefaultContext(this.context, defaultAdr);
		this.skill = new InnerContextAccessSkill(agent);
		this.skill.setContextService(this.service);
	}

	@Test
	public void getInnerContext_init() {
		Context ctx = mock(Context.class);
		when(this.service.createContext(any(), any())).thenReturn(ctx);
		OpenEventSpace space = mock(OpenEventSpace.class);
		when(ctx.getDefaultSpace()).thenReturn(space);
		
		AgentContext context = this.skill.getInnerContext();
		
		assertSame(ctx, context);
		
		ArgumentCaptor<EventListener> listener = ArgumentCaptor.forClass(EventListener.class);
		verify(space, only()).register(listener.capture());
		assertNotNull(listener.getValue());

		verify(this.service, only()).createContext(any(), any());
	}
	
	@Test
	public void getInnerContext_twoCalls() {
		Context ctx = mock(Context.class);
		when(this.service.createContext(any(), any())).thenReturn(ctx);
		OpenEventSpace space = mock(OpenEventSpace.class);
		when(ctx.getDefaultSpace()).thenReturn(space);
		
		AgentContext context1 = this.skill.getInnerContext();
		AgentContext context2 = this.skill.getInnerContext();
		
		assertSame(ctx, context1);
		assertSame(ctx, context2);
		
		ArgumentCaptor<EventListener> listener = ArgumentCaptor.forClass(EventListener.class);
		verify(space, only()).register(listener.capture());
		assertNotNull(listener.getValue());

		verify(this.service, only()).createContext(any(), any());
	}

	private Context forceInnerContextCreation() {
		assumeNotNull(this.context, this.innerSpaceID, this.parentID, this.service, this.skill, this.agent);
		OpenEventSpace space = mock(OpenEventSpace.class);
		when(space.getSpaceID()).thenReturn(this.innerSpaceID);
		Context ctx = mock(Context.class);
		when(ctx.getID()).thenReturn(this.parentID);
		when(ctx.getDefaultSpace()).thenReturn(space);
		when(this.service.createContext(any(), any())).thenReturn(ctx);
		return (Context) this.skill.getInnerContext();
	}
	
	@Test
	public void isInnerDefaultSpaceUUID_noInnerContextInstance() {
		assertFalse(this.skill.isInnerDefaultSpace(UUID.randomUUID()));
	}

	@Test
	public void isInnerDefaultSpaceUUID_withInnerContextInstance() {
		SpaceID innerSpaceID = forceInnerContextCreation().getDefaultSpace().getSpaceID();
		
		assertTrue(this.skill.isInnerDefaultSpace(innerSpaceID.getID()));
		assertFalse(this.skill.isInnerDefaultSpace(UUID.randomUUID()));
	}

	@Test
	public void isInnerDefaultSpaceSpaceID_noInnerContextInstance() {
		SpaceID otherId = new SpaceID(this.parentID, UUID.randomUUID(), OpenEventSpaceSpecification.class);
		assertFalse(this.skill.isInnerDefaultSpace(otherId));
	}

	@Test
	public void isInnerDefaultSpaceSpaceID_withInnerContextInstance() {
		SpaceID otherId = new SpaceID(this.parentID, UUID.randomUUID(), OpenEventSpaceSpecification.class);
		forceInnerContextCreation();
		
		assertTrue(this.skill.isInnerDefaultSpace(this.innerSpaceID));
		assertFalse(this.skill.isInnerDefaultSpace(otherId));
	}

	@Test
	public void isInnerDefaultSpaceSpace_noInnerContextInstance() {
		SpaceID otherId = new SpaceID(this.parentID, UUID.randomUUID(), OpenEventSpaceSpecification.class);
		assertFalse(this.skill.isInnerDefaultSpace(otherId));
	}

	@Test
	public void isInnerDefaultSpaceSpace_withInnerContextInstance() {
		Context innerContext = forceInnerContextCreation();
		SpaceID otherId = new SpaceID(this.parentID, UUID.randomUUID(), OpenEventSpaceSpecification.class);
		Space otherSpace = mock(Space.class);
		when(otherSpace.getSpaceID()).thenReturn(otherId);
		
		assertTrue(this.skill.isInnerDefaultSpace(innerContext.getDefaultSpace()));
		assertFalse(this.skill.isInnerDefaultSpace(otherSpace));
	}

	@Test
	public void isInInnerDefaultSpace_noInnerContextInstance() {
		SpaceID otherId = new SpaceID(this.parentID, UUID.randomUUID(), OpenEventSpaceSpecification.class);
		Address adr = mock(Address.class);
		when(adr.getSpaceID()).thenReturn(otherId);
		Event event = mock(Event.class);
		when(event.getSource()).thenReturn(adr);
		assertFalse(this.skill.isInInnerDefaultSpace(event));
	}

	@Test
	public void isInInnerDefaultSpace_withInnerContextInstance() {
		Context innerContext = forceInnerContextCreation();
		SpaceID otherId = new SpaceID(this.parentID, UUID.randomUUID(), OpenEventSpaceSpecification.class);
		
		Address adr1 = new Address(innerContext.getDefaultSpace().getSpaceID(), UUID.randomUUID());
		Event event1 = mock(Event.class);
		when(event1.getSource()).thenReturn(adr1);
		
		Address adr2 = new Address(otherId, UUID.randomUUID());
		Event event2 = mock(Event.class);
		when(event2.getSource()).thenReturn(adr2);
		
		assertTrue(this.skill.isInInnerDefaultSpace(event1));
		assertFalse(this.skill.isInInnerDefaultSpace(event2));
	}

	@Test
	public void getMemberAgentCount_noInnerContextInstance() {
		assertEquals(0, this.skill.getMemberAgentCount());
	}

	@Test
	public void getMemberAgentCount_withInnerContextInstance_noAgent() {
		Context innerContext = forceInnerContextCreation();
		when(innerContext.getDefaultSpace().getParticipants()).thenReturn(
				Collections3.synchronizedSet(Collections.singleton(this.parentID), this));
		assertEquals(0, this.skill.getMemberAgentCount());
	}

	@Test
	public void getMemberAgentCount_withInnerContextInstance_twoAgents() {
		Context innerContext = forceInnerContextCreation();
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		Set<UUID> ids = new HashSet<>(Arrays.asList(this.parentID, id1, id2));
		when(innerContext.getDefaultSpace().getParticipants()).thenReturn(
				Collections3.unmodifiableSynchronizedSet(ids, this));
		assertEquals(2, this.skill.getMemberAgentCount());
	}

	@Test
	public void getMemberAgents_noInnerContextInstance() {
		assertFalse(this.skill.getMemberAgents().iterator().hasNext());
	}

	@Test
	public void getMemberAgents_withInnerContextInstance_noAgent() {
		Context innerContext = forceInnerContextCreation();
		when(innerContext.getDefaultSpace().getParticipants()).thenReturn(
				Collections3.synchronizedSet(Collections.singleton(this.parentID), this));
		assertFalse(this.skill.getMemberAgents().iterator().hasNext());
	}

	@Test
	public void getMemberAgents_withInnerContextInstance_twoAgents() {
		Context innerContext = forceInnerContextCreation();
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		Set<UUID> ids = new HashSet<>(Arrays.asList(this.parentID, id1, id2));
		when(innerContext.getDefaultSpace().getParticipants()).thenReturn(
				Collections3.unmodifiableSynchronizedSet(ids, this));
		Iterable<UUID> actual = this.skill.getMemberAgents();
		assertContains(actual, id1, id2);
	}

	@Test
	public void hasMemberAgent_noInnerContextInstance() {
		assertFalse(this.skill.hasMemberAgent());
	}

	@Test
	public void hasMemberAgent_withInnerContextInstance_noAgent() {
		Context innerContext = forceInnerContextCreation();
		when(innerContext.getDefaultSpace().getParticipants()).thenReturn(
				Collections3.synchronizedSet(Collections.singleton(this.parentID), this));
		assertFalse(this.skill.hasMemberAgent());
	}

	@Test
	public void hasMemberAgent_withInnerContextInstance_twoAgents() {
		Context innerContext = forceInnerContextCreation();
		UUID id1 = UUID.randomUUID();
		UUID id2 = UUID.randomUUID();
		Set<UUID> ids = new HashSet<>(Arrays.asList(this.parentID, id1, id2));
		when(innerContext.getDefaultSpace().getParticipants()).thenReturn(
				Collections3.unmodifiableSynchronizedSet(ids, this));
		assertTrue(this.skill.hasMemberAgent());
	}

	private static class MyAgent extends Agent {

		public MyAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
			setSkill(new MySkill());
		}
		
	}

	private static class MySkill extends Skill implements InternalEventBusCapacity {

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
			return mock(EventListener.class);
		}

		@Override
		public <T> SynchronizedIterable<T> getRegisteredEventBusListeners(Class<T> type) {
			return null;
		}

		@Override
		public void setEventBus(EventBus bus) {
			throw new UnsupportedOperationException();
		}

		@Override
		public EventBus getEventBus() {
			throw new UnsupportedOperationException();
		}
		
	}

}
