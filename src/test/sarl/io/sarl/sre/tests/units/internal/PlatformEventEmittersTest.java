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

package io.sarl.sre.tests.units.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.mockito.ArgumentCaptor;

import io.sarl.core.AgentKilled;
import io.sarl.core.AgentSpawned;
import io.sarl.core.Behaviors;
import io.sarl.core.ContextJoined;
import io.sarl.core.ContextLeft;
import io.sarl.core.Logging;
import io.sarl.core.MemberJoined;
import io.sarl.core.MemberLeft;
import io.sarl.core.SpaceCreated;
import io.sarl.core.SpaceDestroyed;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Behavior;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.Skill;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.util.ClearableReference;
import io.sarl.lang.util.SynchronizedIterable;
import io.sarl.sre.internal.AgentEventEmitter;
import io.sarl.sre.internal.ContextMemberEventEmitter;
import io.sarl.sre.internal.SpaceEventEmitter;
import io.sarl.sre.internal.SubHolonContextEventEmitter;
import io.sarl.sre.services.context.Context;
import io.sarl.sre.services.lifecycle.AgentLife;
import io.sarl.sre.services.lifecycle.ContextReference;
import io.sarl.sre.tests.testutils.AbstractSreTest;
import io.sarl.tests.api.Nullable;
import io.sarl.util.OpenEventSpace;
import io.sarl.util.OpenEventSpaceSpecification;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@RunWith(Suite.class)
@SuiteClasses({
	PlatformEventEmittersTest.SpaceEventEmitterTest.class,
	PlatformEventEmittersTest.AgentEventEmitterTest.class,
	PlatformEventEmittersTest.ContextMemberEventEmitterTest.class,
	PlatformEventEmittersTest.SubHolonContextEventEmitterTest.class,
})
@SuppressWarnings("all")
public class PlatformEventEmittersTest {

	public static class SpaceEventEmitterTest extends AbstractSreTest {
		
		@Nullable
		private UUID contextID;
		
		@Nullable
		private SpaceEventEmitter emitter;
		
		@Nullable
		private EventSpace defaultSpace;

		@Nullable
		private Logger logger;

		@Before
		public void setUp() {
			this.contextID = UUID.randomUUID();
			SpaceID spaceID = mock(SpaceID.class);
			when(spaceID.getContextID()).thenReturn(this.contextID);
			when(spaceID.getID()).thenReturn(UUID.randomUUID());
			this.defaultSpace = mock(EventSpace.class);
			when(this.defaultSpace.getSpaceID()).thenReturn(spaceID);
			this.logger = Logger.getLogger("SOME");
			this.logger.setUseParentHandlers(false);
			this.logger = spy(this.logger);
			this.emitter = new SpaceEventEmitter(contextID, defaultSpace, logger);
		}
		
		@Test
		public void spaceCreated_localCreation() {
			SpaceID spaceID = mock(SpaceID.class);
			when(spaceID.getContextID()).thenReturn(this.contextID);
			Space space = mock(Space.class);
			when(space.getSpaceID()).thenReturn(spaceID);
			
			this.emitter.spaceCreated(space, true);
			
			ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
			ArgumentCaptor<Scope<Address>> scope = ArgumentCaptor.forClass(Scope.class);
			verify(this.defaultSpace).emit(any(), event.capture(), scope.capture());
			assertNull(scope.getValue());
			Event evt = event.getValue();
			assertNotNull(evt);
			assertInstanceOf(SpaceCreated.class, evt);
			SpaceCreated spaceEvent = (SpaceCreated) evt;
			assertNotNull(spaceEvent.getSource());
			assertSame(spaceID, spaceEvent.spaceID);
		}

		@Test
		public void spaceCreated_false() {
			SpaceID spaceID = mock(SpaceID.class);
			when(spaceID.getContextID()).thenReturn(this.contextID);
			Space space = mock(Space.class);
			when(space.getSpaceID()).thenReturn(spaceID);
			
			this.emitter.spaceCreated(space, false);
			
			ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
			ArgumentCaptor<Scope<Address>> scope = ArgumentCaptor.forClass(Scope.class);
			verify(this.defaultSpace, never()).emit(any(), event.capture(), scope.capture());
		}

		@Test
		public void spaceDestroyed_true() {
			SpaceID spaceID = mock(SpaceID.class);
			when(spaceID.getContextID()).thenReturn(this.contextID);
			Space space = mock(Space.class);
			when(space.getSpaceID()).thenReturn(spaceID);
			
			this.emitter.spaceDestroyed(space, true);
			
			ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
			ArgumentCaptor<Scope<Address>> scope = ArgumentCaptor.forClass(Scope.class);
			verify(this.defaultSpace).emit(any(), event.capture(), scope.capture());
			assertNull(scope.getValue());
			Event evt = event.getValue();
			assertNotNull(evt);
			assertInstanceOf(SpaceDestroyed.class, evt);
			SpaceDestroyed spaceEvent = (SpaceDestroyed) evt;
			assertNotNull(spaceEvent.getSource());
			assertSame(spaceID, spaceEvent.spaceID);
		}

		@Test
		public void spaceDestroyed_false() {
			SpaceID spaceID = mock(SpaceID.class);
			when(spaceID.getContextID()).thenReturn(this.contextID);
			Space space = mock(Space.class);
			when(space.getSpaceID()).thenReturn(spaceID);
			
			this.emitter.spaceDestroyed(space, false);
			
			ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
			ArgumentCaptor<Scope<Address>> scope = ArgumentCaptor.forClass(Scope.class);
			verify(this.defaultSpace, never()).emit(any(), event.capture(), scope.capture());
		}

	}

	public static class AgentEventEmitterTest extends AbstractSreTest {
		
		@Nullable
		private UUID contextID;
		
		@Nullable
		private Context parent;

		@Nullable
		private OpenEventSpace defaultSpace;

		@Nullable
		private Agent agent1;

		@Nullable
		private Agent agent2;

		@Nullable
		private AgentLife life2;

		@Nullable
		private Object[] params;

		@Nullable
		private AgentEventEmitter emitter;
		
		@Nullable
		private Logger logger;

		@Before
		public void setUp() {
			this.contextID = UUID.randomUUID();
			this.logger = Logger.getLogger("SOME");
			this.logger.setUseParentHandlers(false);
			this.logger = spy(this.logger);
			SpaceID spaceID = mock(SpaceID.class);
			when(spaceID.getContextID()).thenReturn(this.contextID);
			when(spaceID.getID()).thenReturn(UUID.randomUUID());
			this.defaultSpace = mock(OpenEventSpace.class);
			when(this.defaultSpace.getSpaceID()).thenReturn(spaceID);
			this.parent = mock(Context.class);
			when(parent.getID()).thenReturn(this.contextID);
			when(parent.getDefaultSpace()).thenReturn(this.defaultSpace);
			this.agent1 = spy(new MyAgent(this.contextID, UUID.randomUUID()));
			this.params = new Object[0];
			this.agent2 = spy(new MyAgent(this.contextID, UUID.randomUUID()));
			this.life2 = AgentLife.getLife(this.agent2);
			this.emitter = new AgentEventEmitter(this.logger);
		}
		
		@Test
		public void agentSpawned() {
			this.emitter.agentSpawned(this.contextID, this.parent, MyAgent.class,
					Arrays.asList(agent1, agent2), params);
			
			ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
			ArgumentCaptor<Scope<Address>> scope = ArgumentCaptor.forClass(Scope.class);
			verify(this.defaultSpace).emit(any(), event.capture(), scope.capture());
			assertNotNull(scope.getValue());
			Event evt = event.getValue();
			assertNotNull(evt);
			assertInstanceOf(AgentSpawned.class, evt);
			AgentSpawned spaceEvent = (AgentSpawned) evt;
			assertNotNull(spaceEvent.getSource());
			assertEquals(MyAgent.class.getName(), spaceEvent.agentType);
			assertContainsCollection(Arrays.asList(agent1.getID(), agent2.getID()), spaceEvent.agentIdentifiers);
		}

		@Test
		public void agentDestroyed()  throws Exception {
			Address adr1 = new Address(
					new SpaceID(this.contextID, UUID.randomUUID(), OpenEventSpaceSpecification.class),
					this.agent2.getID());
			Context ctx = mock(Context.class);
			when(ctx.getDefaultSpace()).thenReturn(this.defaultSpace);
			when(ctx.getID()).thenReturn(this.contextID);
			this.life2.setDefaultContext(ctx, adr1);
			ContextReference ref1 = this.life2.getDefaultContext();

			UUID secondContext = UUID.randomUUID();
			Address adr2 = new Address(
					new SpaceID(secondContext, UUID.randomUUID(), OpenEventSpaceSpecification.class),
					this.agent2.getID());
			ctx = mock(Context.class);
			OpenEventSpace space2 = mock(OpenEventSpace.class);
			when(ctx.getDefaultSpace()).thenReturn(space2);
			when(ctx.getID()).thenReturn(secondContext);
			ContextReference ref2 = this.life2.addExternalContext(ctx, adr2);
			
			List<ContextReference> contexts = Arrays.asList(ref1, ref2);
			
			this.emitter.agentDestroyed(agent2, contexts);

			ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
			ArgumentCaptor<Scope<Address>> scope = ArgumentCaptor.forClass(Scope.class);
			
			verify(this.defaultSpace).emit(any(), event.capture(), scope.capture());
			assertNotNull(scope.getValue());
			Event evt = event.getValue();
			assertNotNull(evt);
			assertInstanceOf(AgentKilled.class, evt);
			AgentKilled spaceEvent = (AgentKilled) evt;
			assertNotNull(spaceEvent.getSource());
			assertEquals(agent2.getID(), spaceEvent.getSource().getUUID());

			verify(space2).emit(any(), event.capture(), scope.capture());
			assertNotNull(scope.getValue());
			evt = event.getValue();
			assertNotNull(evt);
			assertInstanceOf(AgentKilled.class, evt);
			spaceEvent = (AgentKilled) evt;
			assertNotNull(spaceEvent.getSource());
			assertEquals(agent2.getID(), spaceEvent.getSource().getUUID());
		}

		private static class MyAgent extends Agent {

			public MyAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}
			
		}
	}

	public static class ContextMemberEventEmitterTest extends AbstractSreTest {
		
		@Nullable
		private UUID contextID;

		@Nullable
		private UUID agentId;

		@Nullable
		private MyAgent agent;

		@Nullable
		private ContextMemberEventEmitter emitter;

		@Nullable
		private Logger logger;

		@Before
		public void setUp() {
			this.contextID = UUID.randomUUID();
			this.agentId = UUID.randomUUID();
			this.logger = Logger.getLogger("SOME");
			this.logger.setUseParentHandlers(false);
			this.logger = spy(this.logger);
			this.agent = spy(new MyAgent(this.contextID, this.agentId));
			this.emitter = new ContextMemberEventEmitter(this.logger);
		}

		@Test
		public void memberJoined() {
			AgentContext ctx = mock(AgentContext.class);
			UUID ctxid = UUID.randomUUID();
			UUID spaceid = UUID.randomUUID();
			when(ctx.getID()).thenReturn(ctxid);
			EventSpace space = mock(EventSpace.class);
			SpaceID spaceidobj = new SpaceID(ctxid, spaceid, OpenEventSpaceSpecification.class);
			when(space.getSpaceID()).thenReturn(spaceidobj);
			when(ctx.getDefaultSpace()).thenReturn(space);
			this.emitter.memberJoined(ctx, spaceidobj, this.agentId, MyAgent.class.getName());

			ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
			ArgumentCaptor<Scope<Address>> scope = ArgumentCaptor.forClass(Scope.class);
			verify(space).emit(any(), event.capture(), scope.capture());
			assertNotNull(event.getValue());
			assertInstanceOf(MemberJoined.class, event.getValue());
			MemberJoined evt = (MemberJoined) event.getValue();
			assertNotNull(evt.getSource());
			assertEquals(this.agentId, evt.agentID);
			assertEquals(ctxid, evt.getSource().getUUID());
			assertNotNull(scope.getValue());
		}

		@Test
		public void memberLeft() {
			AgentContext ctx = mock(AgentContext.class);
			UUID ctxid = UUID.randomUUID();
			UUID spaceid = UUID.randomUUID();
			when(ctx.getID()).thenReturn(ctxid);
			EventSpace space = mock(EventSpace.class);
			SpaceID spaceidobj = new SpaceID(ctxid, spaceid, OpenEventSpaceSpecification.class);
			when(space.getSpaceID()).thenReturn(spaceidobj);
			when(ctx.getDefaultSpace()).thenReturn(space);
			this.emitter.memberLeft(ctx, spaceidobj, this.agentId, MyAgent.class.getName());

			ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
			ArgumentCaptor<Scope<Address>> scope = ArgumentCaptor.forClass(Scope.class);
			verify(space).emit(any(), event.capture(), scope.capture());
			assertNotNull(event.getValue());
			assertInstanceOf(MemberLeft.class, event.getValue());
			MemberLeft evt = (MemberLeft) event.getValue();
			assertNotNull(evt.getSource());
			assertEquals(this.agentId, evt.agentID);
			assertNotNull(scope.getValue());
		}
		
		private static class MyAgent extends Agent {

			public MyAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			public ClearableReference<Skill> $getSkill(Class<? extends Capacity> capacity) {
				return super.$getSkill(capacity);
			}
		}
	}

	public static class SubHolonContextEventEmitterTest extends AbstractSreTest {
		
		@Nullable
		private UUID contextID;

		@Nullable
		private UUID agentId;

		@Nullable
		private MyAgent agent;

		@Nullable
		private BehSkill behaviors;

		@Nullable
		private SubHolonContextEventEmitter emitter;

		@Nullable
		private Logging logger;

		@Before
		public void setUp() {
			this.contextID = UUID.randomUUID();
			this.agentId = UUID.randomUUID();
			this.logger = spy(new LogSkill());
			this.behaviors = spy(new BehSkill());
			this.agent = spy(new MyAgent(this.behaviors, this.logger, this.contextID, this.agentId));
			this.emitter = new SubHolonContextEventEmitter(this.agent);
		}
		
		@Test
		public void contextJoined() {
			UUID id1 = UUID.randomUUID();
			UUID id2 = UUID.randomUUID();
			this.emitter.contextJoined(id1, id2);
			
			ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
			verify(this.behaviors).wake(event.capture());
			assertNotNull(event.getValue());
			assertInstanceOf(ContextJoined.class, event.getValue());
			ContextJoined evt = (ContextJoined) event.getValue();
			assertNotNull(evt.getSource());
			assertEquals(id1, evt.holonContextID);
			assertEquals(id2, evt.defaultSpaceID);
		}

		@Test
		public void contextLeft() {
			UUID id1 = UUID.randomUUID();
			this.emitter.contextLeft(id1);
			
			ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
			verify(this.behaviors).wake(event.capture());
			assertNotNull(event.getValue());
			assertInstanceOf(ContextLeft.class, event.getValue());
			ContextLeft evt = (ContextLeft) event.getValue();
			assertNotNull(evt.getSource());
			assertEquals(id1, evt.holonContextID);
		}

		private static class BehSkill extends Skill implements Behaviors {

			@Override
			public boolean hasRegisteredBehavior() {
				return false;
			}

			@Override
			public SynchronizedIterable<Behavior> getRegisteredBehaviors() {
				return null;
			}

			@Override
			public Behavior registerBehavior(Behavior attitude, Function1<? super Event, ? extends Boolean> filter) {
				return null;
			}

			@Override
			public Behavior unregisterBehavior(Behavior attitude) {
				return null;
			}

			@Override
			public void wake(Event event, Scope<Address> scope) {
			}

			@Override
			public EventListener asEventListener() {
				return null;
			}
		}
		
		private static class LogSkill extends Skill implements Logging {

			@Override
			public void setLoggingName(String name) {
			}

			@Override
			@Deprecated
			public void println(Object message) {
			}

			@Override
			public void error(Object message, Throwable exception, Object... parameters) {
			}

			@Override
			public void error(Supplier<String> messageProvider) {
			}

			@Override
			public void warning(Object message, Throwable exception, Object... parameters) {
			}

			@Override
			public void warning(Supplier<String> messageProvider) {
			}

			@Override
			public void info(Object message, Object... parameters) {
			}

			@Override
			public void info(Supplier<String> messageProvider) {
			}

			@Override
			public void debug(Object message, Object... parameters) {
			}

			@Override
			public void debug(Supplier<String> messageProvider) {
			}

			@Override
			public boolean isErrorLogEnabled() {
				return false;
			}

			@Override
			public boolean isWarningLogEnabled() {
				return false;
			}

			@Override
			public boolean isInfoLogEnabled() {
				return false;
			}

			@Override
			public boolean isDebugLogEnabled() {
				return false;
			}

			@Override
			public int getLogLevel() {
				return 0;
			}

			@Override
			public void setLogLevel(int level) {
			}

			@Override
			public Logger getLogger() {
				return null;
			}

		}

		private static class MyAgent extends Agent {

			public MyAgent(Behaviors beh, Logging log, UUID parentID, UUID agentID) {
				super(parentID, agentID);
				setSkill((Skill) beh, Behaviors.class);
				setSkill((Skill) log, Logging.class);
			}

			@Override
			public ClearableReference<Skill> $getSkill(Class<? extends Capacity> capacity) {
				return super.$getSkill(capacity);
			}
		}
	}

}
