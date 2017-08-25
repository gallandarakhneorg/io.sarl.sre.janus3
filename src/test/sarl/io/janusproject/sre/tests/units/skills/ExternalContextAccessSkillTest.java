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
package io.janusproject.sre.tests.units.skills;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.janusproject.sre.capacities.InternalEventBusCapacity;
import io.janusproject.sre.services.context.ContextService;
import io.janusproject.sre.services.context.ExternalContextMemberListener;
import io.janusproject.sre.services.context.InternalContextMembershipListener;
import io.janusproject.sre.services.context.JanusContext;
import io.janusproject.sre.services.lifecycle.AgentLife;
import io.janusproject.sre.services.lifecycle.AgentState;
import io.janusproject.sre.skills.ExternalContextAccessSkill;
import io.janusproject.sre.tests.testutils.AbstractJanusTest;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.Skill;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.util.SynchronizedIterable;
import io.sarl.tests.api.ManualMocking;
import io.sarl.tests.api.Nullable;
import io.sarl.util.OpenEventSpace;
import io.sarl.util.OpenEventSpaceSpecification;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@ManualMocking
public class ExternalContextAccessSkillTest extends AbstractJanusTest {

	@Nullable
	private ContextService service;
	
	@Nullable
	private UUID contextId;
	
	@Nullable
	private UUID agentId;

	@Nullable
	private JanusContext rootContext;

	@Nullable
	private JanusContext defaultContext;

	@Nullable
	private OpenEventSpace defaultSpace;

	@Nullable
	private Agent agent;

	@Nullable
	private ExternalContextAccessSkill skill;

	@Nullable
	private EventListener eventBusListener;

	@Nullable
	private MyInternalEventBusSkill eventBus;

	@Before
	public void setUp() {
		this.contextId = UUID.randomUUID();
		this.agentId = UUID.randomUUID();
		this.eventBusListener = mock(EventListener.class);
		this.eventBus = spy(new MyInternalEventBusSkill());
		when(this.eventBus.getAssociatedEventBusListener()).thenReturn(this.eventBusListener);
		this.rootContext = mock(JanusContext.class);
		this.service = mock(ContextService.class);
		when(this.service.getRootContext()).thenReturn(this.rootContext);
		this.agent = spy(new MyAgent(contextId, this.agentId, this.eventBus));
		SpaceID defSpaceId = new SpaceID(this.contextId, UUID.randomUUID(), OpenEventSpaceSpecification.class);
		Address adr = new Address(defSpaceId, this.agentId);
		this.defaultSpace = mock(OpenEventSpace.class);
		when(this.defaultSpace.getSpaceID()).thenReturn(defSpaceId);
		this.defaultContext = mock(JanusContext.class);
		when(this.defaultContext.getID()).thenReturn(this.contextId);
		when(this.defaultContext.getDefaultSpace()).thenReturn(this.defaultSpace);
		AgentLife.getLife(this.agent).setDefaultContext(defaultContext, adr);
		this.skill = new ExternalContextAccessSkill(this.agent);
		this.skill.setContextService(this.service);
	}

	private ExternalContextMemberListener forceExternalContextEventEmitter() {
		ExternalContextMemberListener emitter = mock(ExternalContextMemberListener.class);
		this.skill.setExternalContextMemberListenerProvider(() -> emitter);
		return emitter;
	}

	private InternalContextMembershipListener forceInternalContextEventEmitter() {
		InternalContextMembershipListener emitter = mock(InternalContextMembershipListener.class);
		this.skill.setInternalContextMembershipListenerFactory((agent) -> emitter);
		return emitter;
	}

	private JanusContext[] forceOneExternalContextCreation() {
		UUID id1 = UUID.fromString("00000001-0000-0000-0000-000000000000");
		JanusContext ctx1 = mock(JanusContext.class);
		when(ctx1.getID()).thenReturn(id1);
		SpaceID sid1 = new SpaceID(id1, UUID.fromString("00000001-0001-0000-0000-000000000000"), OpenEventSpaceSpecification.class);
		OpenEventSpace space1 = mock(OpenEventSpace.class);
		when(space1.getSpaceID()).thenReturn(sid1);
		when(ctx1.getDefaultSpace()).thenReturn(space1);
		Address adr1 = new Address(sid1, this.agentId);
		AgentLife.getLife(this.agent).addExternalContext(ctx1, adr1);
		return new JanusContext[] {ctx1};
	}

	private JanusContext[] forceTwoExternalContextCreation() {
		JanusContext[] first = forceOneExternalContextCreation();
		UUID id2 = UUID.fromString("00000002-0000-0000-0000-000000000000");
		JanusContext ctx2 = mock(JanusContext.class);
		when(ctx2.getID()).thenReturn(id2);
		SpaceID sid2 = new SpaceID(id2, UUID.fromString("00000001-0001-0000-0000-000000000000"), OpenEventSpaceSpecification.class);
		OpenEventSpace space2 = mock(OpenEventSpace.class);
		when(space2.getSpaceID()).thenReturn(sid2);
		when(ctx2.getDefaultSpace()).thenReturn(space2);
		Address adr2 = new Address(sid2, this.agentId);
		AgentLife.getLife(this.agent).addExternalContext(ctx2, adr2);

		return new JanusContext[] {first[0], ctx2};
	}

	@Test
	public void getUniverseContext() {
		assertSame(this.rootContext, this.skill.getUniverseContext());
	}

	@Test
	public void getContext_noExternalContext() {
		assertSame(this.defaultContext, this.skill.getContext(this.contextId));
		assertNull(this.skill.getContext(UUID.randomUUID()));
	}

	@Test
	public void getContext_withExternalContext() {
		JanusContext[] ctx = forceTwoExternalContextCreation();
		assertSame(this.defaultContext, this.skill.getContext(this.contextId));
		for (JanusContext c : ctx) {
			assertSame(c, this.skill.getContext(c.getID()));
		}
		assertNull(this.skill.getContext(UUID.randomUUID()));
	}

	@Test
	public void getAllContexts_noExternalContext() {
		Iterable<AgentContext> actual = this.skill.getAllContexts();
		assertContains(actual, this.defaultContext);
	}

	@Test
	public void getAllContexts_withExternalContext() {
		JanusContext[] ctx = forceTwoExternalContextCreation();
		Iterable<AgentContext> actual = this.skill.getAllContexts();
		assertContains(actual, ctx[0], ctx[1], this.defaultContext);
	}

	@Test
	public void isInSpaceUUID() {
		UUID spaceId1 = UUID.randomUUID();
		UUID spaceId2 = UUID.randomUUID();
		Event event = mock(Event.class);
		Address adr1 = new Address(new SpaceID(this.contextId, spaceId1, OpenEventSpaceSpecification.class), this.agentId);
		when(event.getSource()).thenReturn(adr1);
		
		assertTrue(this.skill.isInSpace(event, spaceId1));
		assertFalse(this.skill.isInSpace(event, spaceId2));
	}

	@Test
	public void isInSpaceSpaceID() {
		UUID spaceId1 = UUID.randomUUID();
		UUID spaceId2 = UUID.randomUUID();
		Event event = mock(Event.class);
		Address adr1 = new Address(new SpaceID(this.contextId, spaceId1, OpenEventSpaceSpecification.class), this.agentId);
		when(event.getSource()).thenReturn(adr1);
		
		assertTrue(this.skill.isInSpace(event, new SpaceID(this.contextId, spaceId1, OpenEventSpaceSpecification.class)));
		assertFalse(this.skill.isInSpace(event, new SpaceID(this.contextId, spaceId2, OpenEventSpaceSpecification.class)));
		assertFalse(this.skill.isInSpace(event, new SpaceID(UUID.randomUUID(), spaceId1, OpenEventSpaceSpecification.class)));
		assertFalse(this.skill.isInSpace(event, new SpaceID(UUID.randomUUID(), spaceId2, OpenEventSpaceSpecification.class)));
	}

	@Test
	public void isInSpaceSpace() {
		UUID spaceId1 = UUID.randomUUID();
		UUID spaceId2 = UUID.randomUUID();
		Event event = mock(Event.class);
		Address adr1 = new Address(new SpaceID(this.contextId, spaceId1, OpenEventSpaceSpecification.class), this.agentId);
		when(event.getSource()).thenReturn(adr1);
	
		Space space1 = mock(Space.class);
		when(space1.getSpaceID()).thenReturn(new SpaceID(this.contextId, spaceId1, OpenEventSpaceSpecification.class));
		
		Space space2 = mock(Space.class);
		when(space2.getSpaceID()).thenReturn(new SpaceID(this.contextId, spaceId2, OpenEventSpaceSpecification.class));

		Space space3 = mock(Space.class);
		when(space3.getSpaceID()).thenReturn(new SpaceID(UUID.randomUUID(), spaceId1, OpenEventSpaceSpecification.class));

		Space space4 = mock(Space.class);
		when(space4.getSpaceID()).thenReturn(new SpaceID(UUID.randomUUID(), spaceId2, OpenEventSpaceSpecification.class));

		assertTrue(this.skill.isInSpace(event, space1));
		assertFalse(this.skill.isInSpace(event, space2));
		assertFalse(this.skill.isInSpace(event, space3));
		assertFalse(this.skill.isInSpace(event, space4));
	}

	@Test
	public void join_defaultContext() {
		ExternalContextMemberListener emitter1 = forceExternalContextEventEmitter();
		InternalContextMembershipListener emitter2 = forceInternalContextEventEmitter();
		
		assertFalse(this.skill.join(this.contextId, this.defaultSpace.getSpaceID().getID()));
		
		Iterable<AgentContext> actual = this.skill.getAllContexts();
		assertContains(actual, this.defaultContext);
		verifyNoMoreInteractions(emitter1);
		verifyNoMoreInteractions(emitter2);
	}

	@Test
	public void join_joinedExternalContext() {
		JanusContext[] ctxs = forceTwoExternalContextCreation();
		ExternalContextMemberListener emitter1 = forceExternalContextEventEmitter();
		InternalContextMembershipListener emitter2 = forceInternalContextEventEmitter();
		
		assertFalse(this.skill.join(ctxs[0].getID(), ctxs[0].getDefaultSpace().getSpaceID().getID()));
		
		Iterable<AgentContext> actual = this.skill.getAllContexts();
		assertContains(actual, this.defaultContext, ctxs[0], ctxs[1]);
		verifyNoMoreInteractions(emitter1);
		verifyNoMoreInteractions(emitter2);
	}

	@Test
	public void join_unknownContext() {
		ExternalContextMemberListener emitter1 = forceExternalContextEventEmitter();
		InternalContextMembershipListener emitter2 = forceInternalContextEventEmitter();
	
		assertFalse(this.skill.join(UUID.randomUUID(), this.defaultSpace.getSpaceID().getID()));
		
		Iterable<AgentContext> actual = this.skill.getAllContexts();
		assertContains(actual, this.defaultContext);
		verifyNoMoreInteractions(emitter1);
		verifyNoMoreInteractions(emitter2);
	}

	@Test
	public void join_invalidSpaceId() {
		// Create a context into the context service
		UUID ctxId = UUID.randomUUID();
		UUID spId = UUID.randomUUID();
		OpenEventSpace space = mock(OpenEventSpace.class);
		when(space.getSpaceID()).thenReturn(new SpaceID(ctxId, spId, OpenEventSpaceSpecification.class));
		JanusContext context = mock(JanusContext.class);
		when(context.getDefaultSpace()).thenReturn(space);
		when(context.getID()).thenReturn(ctxId);
		when(this.service.getContext(any())).thenReturn(context);
		
		ExternalContextMemberListener emitter1 = forceExternalContextEventEmitter();
		InternalContextMembershipListener emitter2 = forceInternalContextEventEmitter();

		assertFalse(this.skill.join(ctxId, this.defaultSpace.getSpaceID().getID()));

		Iterable<AgentContext> actual = this.skill.getAllContexts();
		assertContains(actual, this.defaultContext);
		verifyNoMoreInteractions(emitter1);
		verifyNoMoreInteractions(emitter2);
	}

	@Test
	public void join_validSpaceId_firstExternalContext_notAlive() {
		// Create a context into the context service
		UUID ctxId = UUID.randomUUID();
		UUID spId = UUID.randomUUID();
		OpenEventSpace space = mock(OpenEventSpace.class);
		when(space.getSpaceID()).thenReturn(new SpaceID(ctxId, spId, OpenEventSpaceSpecification.class));
		JanusContext context = mock(JanusContext.class);
		when(context.getDefaultSpace()).thenReturn(space);
		when(context.getID()).thenReturn(ctxId);
		when(this.service.getContext(any())).thenReturn(context);

		forceExternalContextEventEmitter();
		forceInternalContextEventEmitter();
		
		assertFalse(this.skill.join(ctxId, spId));
	}

	@Test
	public void join_validSpaceId_firstExternalContext_alive() {
		// Create a context into the context service
		UUID ctxId = UUID.randomUUID();
		UUID spId = UUID.randomUUID();
		OpenEventSpace space = mock(OpenEventSpace.class);
		when(space.getSpaceID()).thenReturn(new SpaceID(ctxId, spId, OpenEventSpaceSpecification.class));
		JanusContext context = mock(JanusContext.class);
		when(context.getDefaultSpace()).thenReturn(space);
		when(context.getID()).thenReturn(ctxId);
		when(this.service.getContext(any())).thenReturn(context);

		ExternalContextMemberListener emitter1 = forceExternalContextEventEmitter();
		InternalContextMembershipListener emitter2 = forceInternalContextEventEmitter();
		
		// Force alive
		AgentLife.getLife(this.agent).setState(AgentState.ALIVE);
		
		assertTrue(this.skill.join(ctxId, spId));
		
		Iterable<AgentContext> actual = this.skill.getAllContexts();
		assertContains(actual, this.defaultContext, context);
		
		ArgumentCaptor<UUID> captedContextId = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<UUID> captedSpaceId = ArgumentCaptor.forClass(UUID.class);
		verify(emitter2, times(1)).contextJoined(captedContextId.capture(), captedSpaceId.capture());
		assertEquals(ctxId, captedContextId.getValue());
		assertEquals(spId, captedSpaceId.getValue());

		ArgumentCaptor<AgentContext> captedContext = ArgumentCaptor.forClass(AgentContext.class);
		ArgumentCaptor<SpaceID> captedSpaceID = ArgumentCaptor.forClass(SpaceID.class);
		ArgumentCaptor<UUID> captedAgentID = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<String> captedType = ArgumentCaptor.forClass(String.class);
		verify(emitter1, times(1)).memberJoined(
				captedContext.capture(), captedSpaceID.capture(),
				captedAgentID.capture(), captedType.capture());
		assertSame(context, captedContext.getValue());
		assertEquals(new SpaceID(ctxId, spId, OpenEventSpaceSpecification.class), captedSpaceID.getValue());
		assertEquals(this.agentId, captedAgentID.getValue());
		assertEquals(this.agent.getClass().getName(), captedType.getValue());
	}

	@Test
	public void join_validSpaceId_withExternalContexts_notAlive() {
		forceTwoExternalContextCreation();
		
		// Create a context into the context service
		UUID ctxId = UUID.randomUUID();
		UUID spId = UUID.randomUUID();
		OpenEventSpace space = mock(OpenEventSpace.class);
		when(space.getSpaceID()).thenReturn(new SpaceID(ctxId, spId, OpenEventSpaceSpecification.class));
		JanusContext context = mock(JanusContext.class);
		when(context.getDefaultSpace()).thenReturn(space);
		when(context.getID()).thenReturn(ctxId);
		when(this.service.getContext(any())).thenReturn(context);

		forceExternalContextEventEmitter();
		forceInternalContextEventEmitter();

		assertFalse(this.skill.join(ctxId, spId));
	}

	@Test
	public void join_validSpaceId_withExternalContexts_alive() {
		JanusContext[] ctxs = forceTwoExternalContextCreation();
		
		// Create a context into the context service
		UUID ctxId = UUID.randomUUID();
		UUID spId = UUID.randomUUID();
		OpenEventSpace space = mock(OpenEventSpace.class);
		when(space.getSpaceID()).thenReturn(new SpaceID(ctxId, spId, OpenEventSpaceSpecification.class));
		JanusContext context = mock(JanusContext.class);
		when(context.getDefaultSpace()).thenReturn(space);
		when(context.getID()).thenReturn(ctxId);
		when(this.service.getContext(any())).thenReturn(context);

		ExternalContextMemberListener emitter1 = forceExternalContextEventEmitter();
		InternalContextMembershipListener emitter2 = forceInternalContextEventEmitter();

		// Force alive
		AgentLife.getLife(this.agent).setState(AgentState.ALIVE);

		assertTrue(this.skill.join(ctxId, spId));
		
		Iterable<AgentContext> actual = this.skill.getAllContexts();
		assertContains(actual, this.defaultContext, ctxs[0], ctxs[1], context);

		ArgumentCaptor<UUID> captedContextId = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<UUID> captedSpaceId = ArgumentCaptor.forClass(UUID.class);
		verify(emitter2, times(1)).contextJoined(captedContextId.capture(), captedSpaceId.capture());
		assertEquals(ctxId, captedContextId.getValue());
		assertEquals(spId, captedSpaceId.getValue());

		ArgumentCaptor<AgentContext> captedContext = ArgumentCaptor.forClass(AgentContext.class);
		ArgumentCaptor<SpaceID> captedSpaceID= ArgumentCaptor.forClass(SpaceID.class);
		ArgumentCaptor<UUID> captedAgentID = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<String> captedType = ArgumentCaptor.forClass(String.class);
		verify(emitter1, times(1)).memberJoined(
				captedContext.capture(), captedSpaceID.capture(),
				captedAgentID.capture(), captedType.capture());
		assertSame(context, captedContext.getValue());
		assertEquals(new SpaceID(ctxId, spId, OpenEventSpaceSpecification.class), captedSpaceID.getValue());
		assertEquals(this.agentId, captedAgentID.getValue());
		assertEquals(this.agent.getClass().getName(), captedType.getValue());
	}

	@Test
	public void leave_unknownContext() {
		ExternalContextMemberListener emitter1 = forceExternalContextEventEmitter();
		InternalContextMembershipListener emitter2 = forceInternalContextEventEmitter();
		
		assertFalse(this.skill.leave(UUID.randomUUID()));
		
		Iterable<AgentContext> actual = this.skill.getAllContexts();
		assertContains(actual, this.defaultContext);
		assertSame(this.defaultContext, AgentLife.getLife(this.agent).getDefaultContext().getContext());
		verifyNoMoreInteractions(emitter1);
		verifyNoMoreInteractions(emitter2);
	}

	@Test
	public void leave_defaultContext_noExternalContext() {
		ExternalContextMemberListener emitter1 = forceExternalContextEventEmitter();
		InternalContextMembershipListener emitter2 = forceInternalContextEventEmitter();
		
		assertFalse(this.skill.leave(this.contextId));
		
		Iterable<AgentContext> actual = this.skill.getAllContexts();
		assertContains(actual, this.defaultContext);
		assertSame(this.defaultContext, AgentLife.getLife(this.agent).getDefaultContext().getContext());
		verifyNoMoreInteractions(emitter1);
		verifyNoMoreInteractions(emitter2);
	}

	@Test
	public void leave_defaultContext_twoExternalContext() {
		JanusContext[] ctxs = forceTwoExternalContextCreation();
		ExternalContextMemberListener emitter1 = forceExternalContextEventEmitter();
		InternalContextMembershipListener emitter2 = forceInternalContextEventEmitter();
		
		assertFalse(this.skill.leave(this.contextId));
		
		Iterable<AgentContext> actual = this.skill.getAllContexts();
		assertContains(actual, this.defaultContext, ctxs[0], ctxs[1]);
		assertSame(this.defaultContext, AgentLife.getLife(this.agent).getDefaultContext().getContext());
		verifyNoMoreInteractions(emitter1);
		verifyNoMoreInteractions(emitter2);
	}

	@Test
	public void leave_defaultContext_oneExternalContext() {
		JanusContext[] ctxs = forceOneExternalContextCreation();
		ExternalContextMemberListener emitter1 = forceExternalContextEventEmitter();
		InternalContextMembershipListener emitter2 = forceInternalContextEventEmitter();
		
		assertTrue(this.skill.leave(this.contextId));
		
		Iterable<AgentContext> actual = this.skill.getAllContexts();
		assertContains(actual, ctxs[0]);
		assertSame(ctxs[0], AgentLife.getLife(this.agent).getDefaultContext().getContext());

		ArgumentCaptor<UUID> captedContextId = ArgumentCaptor.forClass(UUID.class);
		verify(emitter2, times(1)).contextLeft(captedContextId.capture());
		assertEquals(this.contextId, captedContextId.getValue());

		ArgumentCaptor<AgentContext> captedContext = ArgumentCaptor.forClass(AgentContext.class);
		ArgumentCaptor<SpaceID> captedSpaceID = ArgumentCaptor.forClass(SpaceID.class);
		ArgumentCaptor<UUID> captedAgentID = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<String> captedType = ArgumentCaptor.forClass(String.class);
		verify(emitter1, times(1)).memberLeft(captedContext.capture(), captedSpaceID.capture(), captedAgentID.capture(), captedType.capture());
		assertSame(this.defaultContext, captedContext.getValue());
		assertEquals(new SpaceID(
				this.contextId,
				this.defaultSpace.getSpaceID().getID(),
				OpenEventSpaceSpecification.class),
				captedSpaceID.getValue());
		assertEquals(this.agentId, captedAgentID.getValue());
		assertEquals(this.agent.getClass().getName(), captedType.getValue());
	}

	@Test
	public void leave_externalContext() {
		JanusContext[] ctxs = forceTwoExternalContextCreation();
		ExternalContextMemberListener emitter1 = forceExternalContextEventEmitter();
		InternalContextMembershipListener emitter2 = forceInternalContextEventEmitter();
		
		assertTrue(this.skill.leave(ctxs[1].getID()));
		
		Iterable<AgentContext> actual = this.skill.getAllContexts();
		assertContains(actual, this.defaultContext, ctxs[0]);
		assertSame(this.defaultContext, AgentLife.getLife(this.agent).getDefaultContext().getContext());


		ArgumentCaptor<UUID> captedContextId = ArgumentCaptor.forClass(UUID.class);
		verify(emitter2, times(1)).contextLeft(captedContextId.capture());
		assertEquals(ctxs[1].getID(), captedContextId.getValue());

		ArgumentCaptor<AgentContext> captedContext = ArgumentCaptor.forClass(AgentContext.class);
		ArgumentCaptor<SpaceID> captedSpaceID = ArgumentCaptor.forClass(SpaceID.class);
		ArgumentCaptor<UUID> captedAgentID = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<String> captedType = ArgumentCaptor.forClass(String.class);
		verify(emitter1, times(1)).memberLeft(captedContext.capture(), captedSpaceID.capture(), captedAgentID.capture(), captedType.capture());
		assertSame(ctxs[1], captedContext.getValue());
		assertEquals(new SpaceID(
				ctxs[1].getID(),
				ctxs[1].getDefaultSpace().getSpaceID().getID(),
				OpenEventSpaceSpecification.class),
				captedSpaceID.getValue());
		assertEquals(this.agentId, captedAgentID.getValue());
		assertEquals(this.agent.getClass().getName(), captedType.getValue());
	}

	private static class MyAgent extends Agent {

		public MyAgent(UUID parentID, UUID agentID, MyInternalEventBusSkill eventBus) {
			super(parentID, agentID);
			setSkill(eventBus);
		}
		
	}

	private static class MyInternalEventBusSkill extends Skill implements InternalEventBusCapacity {

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

		private final EventListener listener = mock(EventListener.class);
		
		@Override
		public EventListener getAssociatedEventBusListener() {
			return this.listener;
		}

		@Override
		public <T> SynchronizedIterable<T> getRegisteredEventBusListeners(Class<T> type) {
			return null;
		}
		
	}

}
