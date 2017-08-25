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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.janusproject.sre.capacities.InternalEventBusCapacity;
import io.janusproject.sre.capacities.InternalSchedules;
import io.janusproject.sre.services.context.JanusContext;
import io.janusproject.sre.services.lifecycle.AgentLife;
import io.janusproject.sre.services.lifecycle.AgentState;
import io.janusproject.sre.skills.BehaviorsSkill;
import io.janusproject.sre.tests.testutils.AbstractJanusTest;
import io.sarl.core.AgentTask;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Behavior;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.Skill;
import io.sarl.lang.util.SynchronizedIterable;
import io.sarl.lang.util.SynchronizedSet;
import io.sarl.tests.api.Nullable;
import io.sarl.util.Collections3;
import io.sarl.util.OpenEventSpace;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class BehaviorsSkillTest extends AbstractJanusTest {

	@Nullable
	private UUID contextId;
	
	@Nullable
	private UUID agentId;

	@Nullable
	private Agent agent;

	@Nullable
	private BehaviorsSkill skill;

	@Nullable
	private EventListener eventBusListener;

	@Nullable
	private MyInternalEventBusSkill eventBus;

	@Nullable
	private MyInternalSchedulesSkill schedules;

	@Before
	public void setUp() {
		this.contextId = UUID.randomUUID();
		this.eventBusListener = mock(EventListener.class);
		this.eventBus = spy(new MyInternalEventBusSkill(this.eventBusListener));
		this.schedules = spy(new MyInternalSchedulesSkill());
		this.agentId = UUID.randomUUID();
		this.agent = spy(new MyAgent(contextId, this.agentId, this.eventBus, this.schedules));
		this.skill = new BehaviorsSkill(this.agent);
		reset(this.schedules, this.eventBus, this.eventBusListener, this.agent);
	}

	private void forceAlive() {
		// Force being alive
		AgentLife.getLife(this.agent).setState(AgentState.ALIVE);
	}
	
	private void forceNoBehavior() {
		this.eventBus.$setRegisteredEventBusListeners(Collections3.emptySynchronizedSet());
		reset(this.schedules, this.eventBus, this.eventBusListener, this.agent);
	}

	private Behavior[] forceTwoBehaviors() {
		Behavior beh1 = mock(Behavior.class);

		Behavior beh2 = mock(Behavior.class);

		Set<Behavior> theset = new HashSet<>();
		theset.add(beh1);
		theset.add(beh2);
		this.eventBus.$setRegisteredEventBusListeners(
				Collections3.unmodifiableSynchronizedSet(theset, this));
		List<Behavior> list = new ArrayList<>(theset);
		Behavior[] array = new Behavior[list.size()];
		list.toArray(array);
		return array;
	}
	
	public OpenEventSpace forceInnerContext() {
		JanusContext innerContext = mock(JanusContext.class);
		OpenEventSpace defaultSpace = mock(OpenEventSpace.class);
		when(innerContext.getDefaultSpace()).thenReturn(defaultSpace);
		AgentLife.getLife(this.agent).setInnerContext(innerContext);
		return defaultSpace;
	}

	@Test
	public void asEventListener() {
		forceNoBehavior();
		assertSame(this.eventBusListener, this.skill.asEventListener());
	}

	@Test
	public void getRegisteredBehaviors_noRegistration() {
		forceNoBehavior();
		SynchronizedIterable<Behavior> iterable = this.skill.getRegisteredBehaviors();
		assertFalse(iterable.iterator().hasNext());
	}

	@Test
	public void getRegisteredBehaviors_registrations() {
		Behavior[] behs = forceTwoBehaviors();
		SynchronizedIterable<Behavior> iterable = this.skill.getRegisteredBehaviors();
		Iterator<Behavior> iterator = iterable.iterator();
		assertSame(behs[0], iterator.next());
		assertSame(behs[1], iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void hasRegisteredBehavior_noRegistration() {
		forceNoBehavior();
		assertFalse(this.skill.hasRegisteredBehavior());
	}

	@Test
	public void hasRegisteredBehavior_registrations() {
		forceTwoBehaviors();
		assertTrue(this.skill.hasRegisteredBehavior());
	}

	@Test
	public void registerBehavior_noFilter_notAlive() {
		Behavior beh1 = mock(Behavior.class);
		
		Behavior b = this.skill.registerBehavior(beh1);

		assertSame(beh1, b);
		verifyZeroInteractions(this.eventBus);
		verifyZeroInteractions(this.schedules);
	}

	@Test
	public void registerBehavior_noFilter_alive() {
		Behavior beh1 = mock(Behavior.class);

		forceAlive();

		Behavior b = this.skill.registerBehavior(beh1);

		assertSame(beh1, b);
		ArgumentCaptor<Object> capturedListener = ArgumentCaptor.forClass(Object.class);
		ArgumentCaptor<Function1<? super Event, ? extends Boolean>> capturedFilter = ArgumentCaptor.forClass(Function1.class);
		ArgumentCaptor<Procedure1<? super Object>> capturedCallback = ArgumentCaptor.forClass(Procedure1.class);
		verify(this.eventBus).registerEventBusListener(capturedListener.capture(), capturedFilter.capture(), capturedCallback.capture());
		assertSame(beh1, capturedListener.getValue());
		assertNull(capturedFilter.getValue());
		assertNotNull(capturedCallback.getValue());
		verifyZeroInteractions(this.schedules);
	}

	@Test
	public void registerBehavior_filter_notAlive() {
		Behavior beh1 = mock(Behavior.class);
		Function1<? super Event, ? extends Boolean> filter = mock(Function1.class);
		
		Behavior b = this.skill.registerBehavior(beh1, filter);

		assertSame(beh1, b);
		verifyZeroInteractions(this.eventBus);
		verifyZeroInteractions(this.schedules);
	}

	@Test
	public void registerBehavior_filter_alive() {
		Behavior beh1 = mock(Behavior.class);
		Function1<? super Event, ? extends Boolean> filter = mock(Function1.class);
		
		forceAlive();
		
		Behavior b = this.skill.registerBehavior(beh1, filter);

		assertSame(beh1, b);
		ArgumentCaptor<Object> capturedListener = ArgumentCaptor.forClass(Object.class);
		ArgumentCaptor<Function1<? super Event, ? extends Boolean>> capturedFilter = ArgumentCaptor.forClass(Function1.class);
		ArgumentCaptor<Procedure1<? super Object>> capturedCallback = ArgumentCaptor.forClass(Procedure1.class);
		verify(this.eventBus).registerEventBusListener(capturedListener.capture(), capturedFilter.capture(), capturedCallback.capture());
		assertSame(beh1, capturedListener.getValue());
		assertSame(filter, capturedFilter.getValue());
		assertNotNull(capturedCallback.getValue());
		verifyZeroInteractions(this.schedules);
	}

	@Test
	public void unregisterBehavior() {
		Behavior beh1 = mock(Behavior.class);
		
		Behavior b = this.skill.unregisterBehavior(beh1);

		assertSame(beh1, b);
		ArgumentCaptor<Object> capturedListener = ArgumentCaptor.forClass(Object.class);
		ArgumentCaptor<Procedure1<? super Object>> capturedCallback = ArgumentCaptor.forClass(Procedure1.class);
		verify(this.eventBus).unregisterEventBusListener(capturedListener.capture(), capturedCallback.capture());
		assertSame(beh1, capturedListener.getValue());
		assertNotNull(capturedCallback.getValue());
		ArgumentCaptor<Behavior> capturedBehavior = ArgumentCaptor.forClass(Behavior.class);
		verify(this.schedules).unregisterTasksForBehavior(capturedBehavior.capture());
		assertSame(beh1, capturedBehavior.getValue());
	}

	@Test
	public void wake_noScope_noInnerContext_notAlive() {
		Event event = new Event() {};
		assertNull(event.getSource());
		
		this.skill.wake(event);
		
		verifyZeroInteractions(this.eventBusListener);
		verifyZeroInteractions(this.schedules);
	}

	@Test
	public void wake_noScope_noInnerContext_alive() {
		Event event = new Event() {};
		assertNull(event.getSource());
		
		forceAlive();
		
		this.skill.wake(event);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		verify(this.eventBusListener).receiveEvent(capturedEvent.capture());
		assertSame(event, capturedEvent.getValue());
		assertNotNull(capturedEvent.getValue().getSource());
		verifyZeroInteractions(this.schedules);
	}

	@Test
	public void wake_matchingScope_noInnerContext_notAlive() {
		Event event = new Event() {};
		assertNull(event.getSource());
		
		this.skill.wake(event, (it) -> true);
		
		verifyZeroInteractions(this.eventBusListener);
	}

	@Test
	public void wake_matchingScope_noInnerContext_alive() {
		Event event = new Event() {};
		assertNull(event.getSource());

		forceAlive();
		
		this.skill.wake(event, (it) -> true);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		verify(this.eventBusListener).receiveEvent(capturedEvent.capture());
		assertSame(event, capturedEvent.getValue());
		assertNotNull(capturedEvent.getValue().getSource());
		verifyZeroInteractions(this.schedules);
	}

	@Test
	public void wake_notMatchingScope_noInnerContext() {
		Event event = new Event() {};
		assertNull(event.getSource());
		
		this.skill.wake(event, (it) -> false);
		
		verifyZeroInteractions(this.eventBusListener);
		verifyZeroInteractions(this.schedules);
	}

	@Test
	public void wake_noScope_innerContext_notAlive() {
		OpenEventSpace defSpace = forceInnerContext();
		Event event = new Event() {};
		assertNull(event.getSource());
		
		this.skill.wake(event);
		
		verifyZeroInteractions(this.eventBusListener);
		verifyZeroInteractions(this.schedules);
		verifyZeroInteractions(defSpace);
	}

	@Test
	public void wake_noScope_innerContext_alive() {
		OpenEventSpace defSpace = forceInnerContext();
		Event event = new Event() {};
		assertNull(event.getSource());

		forceAlive();
		
		this.skill.wake(event);
		
		verifyZeroInteractions(this.eventBusListener);
		verifyZeroInteractions(this.schedules);
		ArgumentCaptor<UUID> capturedSource = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Scope<Address>> capturedScope = ArgumentCaptor.forClass(Scope.class);
		verify(defSpace).emit(capturedSource.capture(), capturedEvent.capture(), capturedScope.capture());
		assertEquals(this.agentId, capturedSource.getValue());
		assertSame(event, capturedEvent.getValue());
		assertNotNull(capturedEvent.getValue().getSource());
		assertNull(capturedScope.getValue());
	}

	@Test
	public void wake_matchingScope_innerContext_notAlive() {
		OpenEventSpace defSpace = forceInnerContext();
		Event event = new Event() {};
		assertNull(event.getSource());
		Scope<Address> scope = (it) -> true;
		
		this.skill.wake(event, scope);
		
		verifyZeroInteractions(this.eventBusListener);
		verifyZeroInteractions(this.schedules);
		verifyZeroInteractions(defSpace);
	}

	@Test
	public void wake_matchingScope_innerContext_alive() {
		OpenEventSpace defSpace = forceInnerContext();
		Event event = new Event() {};
		assertNull(event.getSource());
		Scope<Address> scope = (it) -> true;

		forceAlive();
		
		this.skill.wake(event, scope);
		
		verifyZeroInteractions(this.eventBusListener);
		verifyZeroInteractions(this.schedules);
		ArgumentCaptor<UUID> capturedSource = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Scope<Address>> capturedScope = ArgumentCaptor.forClass(Scope.class);
		verify(defSpace).emit(capturedSource.capture(), capturedEvent.capture(), capturedScope.capture());
		assertEquals(this.agentId, capturedSource.getValue());
		assertSame(event, capturedEvent.getValue());
		assertNotNull(capturedEvent.getValue().getSource());
		assertSame(scope, capturedScope.getValue());
	}

	@Test
	public void wake_notMatchingScope_innerContext_notAlive() {
		OpenEventSpace defSpace = forceInnerContext();
		Event event = new Event() {};
		assertNull(event.getSource());
		Scope<Address> scope = (it) -> false;
		
		this.skill.wake(event, scope);
		
		verifyZeroInteractions(this.eventBusListener);
		verifyZeroInteractions(this.schedules);
		verifyZeroInteractions(defSpace);
	}

	@Test
	public void wake_notMatchingScope_innerContext_alive() {
		OpenEventSpace defSpace = forceInnerContext();
		Event event = new Event() {};
		assertNull(event.getSource());
		Scope<Address> scope = (it) -> false;

		forceAlive();
		
		this.skill.wake(event, scope);
		
		verifyZeroInteractions(this.eventBusListener);
		verifyZeroInteractions(this.schedules);
		ArgumentCaptor<UUID> capturedSource = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Scope<Address>> capturedScope = ArgumentCaptor.forClass(Scope.class);
		verify(defSpace).emit(capturedSource.capture(), capturedEvent.capture(), capturedScope.capture());
		assertEquals(this.agentId, capturedSource.getValue());
		assertSame(event, capturedEvent.getValue());
		assertNotNull(capturedEvent.getValue().getSource());
		assertSame(scope, capturedScope.getValue());
	}

	private static class MyAgent extends Agent {

		public MyAgent(UUID parentID, UUID agentID, MyInternalEventBusSkill skill, MyInternalSchedulesSkill skill2) {
			super(parentID, agentID);
			setSkill(skill);
			setSkill(skill2);
		}
		
	}

	private static class MyInternalEventBusSkill extends Skill implements InternalEventBusCapacity {

		private SynchronizedSet<Behavior> listeners = null;
		
		private final EventListener listener;

		MyInternalEventBusSkill(EventListener listener) {
			this.listener = listener == null ? mock(EventListener.class) : listener;
		}
		
		@Override
		public void registerEventBusListener(Object listener, Function1<? super Event, ? extends Boolean> filter,
				Procedure1<? super Object> callback) {
		}

		public void $setRegisteredEventBusListeners(SynchronizedSet<Behavior> listeners) {
			this.listeners = listeners;
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
			return this.listener;
		}

		@Override
		public <T> SynchronizedIterable<T> getRegisteredEventBusListeners(Class<T> type) {
			if (type.equals(Behavior.class)) {
				return (SynchronizedIterable<T>) this.listeners;
			}
			return null;
		}
		
	}

	private static class MyInternalSchedulesSkill extends Skill implements InternalSchedules {

		@Override
		public SynchronizedSet<String> getActiveTasks() {
			return null;
		}

		@Override
		public AgentTask in(AgentTask task, long delay, Procedure1<? super Agent> procedure) {
			return null;
		}

		@Override
		public AgentTask task(String name) {
			return null;
		}

		@Override
		public void setName(AgentTask task, String name) {
		}

		@Override
		public boolean cancel(AgentTask task, boolean mayInterruptIfRunning) {
			return false;
		}

		@Override
		public boolean isCanceled(AgentTask task) {
			return false;
		}

		@Override
		public AgentTask every(AgentTask task, long period, Procedure1<? super Agent> procedure) {
			return null;
		}

		@Override
		public AgentTask atFixedDelay(AgentTask task, long delay, Procedure1<? super Agent> procedure) {
			return null;
		}

		@Override
		public AgentTask execute(AgentTask task, Procedure1<? super Agent> procedure) {
			return null;
		}

		@Override
		public void unregisterTasksForBehavior(Behavior behavior) {
		}
		
	}

}