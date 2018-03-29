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
package io.sarl.sre.tests.units.skills;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.sarl.core.Logging;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.Skill;
import io.sarl.lang.util.SynchronizedIterable;
import io.sarl.sre.services.lifecycle.AgentLife;
import io.sarl.sre.services.lifecycle.AgentState;
import io.sarl.sre.skills.EventBus;
import io.sarl.sre.skills.InternalEventBusSkill;
import io.sarl.sre.tests.testutils.AbstractJanusTest;
import io.sarl.tests.api.Nullable;


/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class InternalEventBusSkillTest extends AbstractJanusTest {

	@Nullable
	private UUID contextId;

	@Nullable
	private UUID agentId;

	@Nullable
	private Agent agent;

	@Nullable
	private InternalEventBusSkill skill;

	@Nullable
	private EventBus eventBus;

	@Nullable
	private MyLoggingSkill logger;

	@Before
	public void setUp() {
		this.contextId = UUID.randomUUID();
		this.agentId = UUID.randomUUID();
		this.eventBus = mock(EventBus.class);
		this.logger = spy(new MyLoggingSkill());
		this.agent = spy(new MyAgent(contextId, this.agentId, this.logger));
		this.skill = new InternalEventBusSkill(this.agent);
		this.skill.setEventBus(this.eventBus);
	}

	@Test
	public void getAssociatedEventBusListener() {
		EventListener listener1 = this.skill.getAssociatedEventBusListener();
		EventListener listener2 = this.skill.getAssociatedEventBusListener();
		assertNotNull(listener1);
		assertNotNull(listener2);
		assertSame(listener1, listener2);
	}

	@Test
	public void getRegisteredEventBusListeners() {
		SynchronizedIterable<Object> iterable = mock(SynchronizedIterable.class);
		when(this.eventBus.getRegisteredEventListeners(any())).thenReturn(iterable);
		SynchronizedIterable<Object> list = this.skill.getRegisteredEventBusListeners(null);
		assertSame(iterable, list);
	}

	@Test
	public void registerEventBusListener_notFilter_noCallback() {
		EventListener listener1 = mock(EventListener.class);
		this.skill.registerEventBusListener(listener1);
		
		ArgumentCaptor<Object> capturedListener = ArgumentCaptor.forClass(Object.class);
		ArgumentCaptor<Function1<? super Event, ? extends Boolean>> capturedFilter = ArgumentCaptor.forClass(Function1.class);
		ArgumentCaptor<Procedure1<? super Object>> capturedCallback = ArgumentCaptor.forClass(Procedure1.class);
		verify(this.eventBus).register(capturedListener.capture(), capturedFilter.capture(), capturedCallback.capture());
		assertSame(listener1, capturedListener.getValue());
		assertNull(capturedFilter.getValue());
		assertNull(capturedCallback.getValue());
	}

	@Test
	public void registerEventBusListener_filter_noCallback() {
		EventListener listener1 = mock(EventListener.class);
		Function1<? super Event, ? extends Boolean> filter = mock(Function1.class);
		this.skill.registerEventBusListener(listener1, filter);
		
		ArgumentCaptor<Object> capturedListener = ArgumentCaptor.forClass(Object.class);
		ArgumentCaptor<Function1<? super Event, ? extends Boolean>> capturedFilter = ArgumentCaptor.forClass(Function1.class);
		ArgumentCaptor<Procedure1<? super Object>> capturedCallback = ArgumentCaptor.forClass(Procedure1.class);
		verify(this.eventBus).register(capturedListener.capture(), capturedFilter.capture(), capturedCallback.capture());
		assertSame(listener1, capturedListener.getValue());
		assertSame(filter, capturedFilter.getValue());
		assertNull(capturedCallback.getValue());
	}

	@Test
	public void registerEventBusListener_notFilter_callback() {
		Procedure1<? super Object> callback = mock(Procedure1.class);
		EventListener listener1 = mock(EventListener.class);
		this.skill.registerEventBusListener(listener1, null, callback);
		
		ArgumentCaptor<Object> capturedListener = ArgumentCaptor.forClass(Object.class);
		ArgumentCaptor<Function1<? super Event, ? extends Boolean>> capturedFilter = ArgumentCaptor.forClass(Function1.class);
		ArgumentCaptor<Procedure1<? super Object>> capturedCallback = ArgumentCaptor.forClass(Procedure1.class);
		verify(this.eventBus).register(capturedListener.capture(), capturedFilter.capture(), capturedCallback.capture());
		assertSame(listener1, capturedListener.getValue());
		assertNull(capturedFilter.getValue());
		assertSame(callback, capturedCallback.getValue());
	}

	@Test
	public void registerEventBusListener_filter_callback() {
		Procedure1<? super Object> callback = mock(Procedure1.class);
		EventListener listener1 = mock(EventListener.class);
		Function1<? super Event, ? extends Boolean> filter = mock(Function1.class);
		this.skill.registerEventBusListener(listener1, filter, callback);
		
		ArgumentCaptor<Object> capturedListener = ArgumentCaptor.forClass(Object.class);
		ArgumentCaptor<Function1<? super Event, ? extends Boolean>> capturedFilter = ArgumentCaptor.forClass(Function1.class);
		ArgumentCaptor<Procedure1<? super Object>> capturedCallback = ArgumentCaptor.forClass(Procedure1.class);
		verify(this.eventBus).register(capturedListener.capture(), capturedFilter.capture(), capturedCallback.capture());
		assertSame(listener1, capturedListener.getValue());
		assertSame(filter, capturedFilter.getValue());
		assertSame(callback, capturedCallback.getValue());
	}

	@Test
	public void unregisterEventBusListener() {
		EventListener listener1 = mock(EventListener.class);
		this.skill.unregisterEventBusListener(listener1);
		
		ArgumentCaptor<Object> capturedListener = ArgumentCaptor.forClass(Object.class);
		ArgumentCaptor<Procedure1<? super Object>> capturedCallback = ArgumentCaptor.forClass(Procedure1.class);
		verify(this.eventBus).unregister(capturedListener.capture(), capturedCallback.capture());
		assertSame(listener1, capturedListener.getValue());
		assertNull(capturedCallback.getValue());
	}

	@Test
	public void fireEvent_agentNotStarted_notEventCaching() {
		AgentLife.getLife(this.agent).setState(AgentState.UNSTARTED);
		
		this.skill.setEventBuffering(false);
		Event event = mock(Event.class);
		
		this.skill.fireEvent(event);
		
		verifyZeroInteractions(this.eventBus);
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEvent_agentInitialization_notEventCaching() {
		AgentLife.getLife(this.agent).setState(AgentState.INITIALIZING);
		
		this.skill.setEventBuffering(false);
		Event event = mock(Event.class);
		
		this.skill.fireEvent(event);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
		verify(this.eventBus).asyncDispatch(capturedEvent.capture(), capturedLogger.capture());
		assertSame(event, capturedEvent.getValue());
		assertSame(this.logger.getLogger(), capturedLogger.getValue());
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEvent_agentAlive_notEventCaching() {
		AgentLife.getLife(this.agent).setState(AgentState.ALIVE);
		
		this.skill.setEventBuffering(false);
		Event event = mock(Event.class);
		
		this.skill.fireEvent(event);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
		verify(this.eventBus).asyncDispatch(capturedEvent.capture(), capturedLogger.capture());
		assertSame(event, capturedEvent.getValue());
		assertSame(this.logger.getLogger(), capturedLogger.getValue());
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEvent_agentDying_notEventCaching() {
		AgentLife.getLife(this.agent).setState(AgentState.DYING);
		
		this.skill.setEventBuffering(false);
		Event event = mock(Event.class);
		
		this.skill.fireEvent(event);
		
		verifyZeroInteractions(this.eventBus);
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEvent_agentDead_notEventCaching() {
		AgentLife.getLife(this.agent).setState(AgentState.DEAD);
		
		this.skill.setEventBuffering(false);
		Event event = mock(Event.class);
		
		this.skill.fireEvent(event);
		
		verifyZeroInteractions(this.eventBus);
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEvent_agentNotStarted_eventCaching() {
		AgentLife.getLife(this.agent).setState(AgentState.UNSTARTED);
		
		this.skill.setEventBuffering(true);
		Event event = mock(Event.class);
		
		this.skill.fireEvent(event);
		
		verifyZeroInteractions(this.eventBus);
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEvent_agentInitialization_eventCaching() {
		AgentLife.getLife(this.agent).setState(AgentState.INITIALIZING);
		
		this.skill.setEventBuffering(true);
		Event event = mock(Event.class);
		
		this.skill.fireEvent(event);
		
		verifyZeroInteractions(this.eventBus);
		assertContains(this.skill.getBufferedEvents(), event);
	}

	@Test
	public void fireEvent_agentAlive_eventCaching() {
		AgentLife.getLife(this.agent).setState(AgentState.ALIVE);
		
		this.skill.setEventBuffering(true);
		Event event = mock(Event.class);
		
		this.skill.fireEvent(event);
		
		verifyZeroInteractions(this.eventBus);
		assertContains(this.skill.getBufferedEvents(), event);
	}

	@Test
	public void fireEvent_agentDying_eventCaching() {
		AgentLife.getLife(this.agent).setState(AgentState.DYING);
		
		this.skill.setEventBuffering(true);
		Event event = mock(Event.class);
		
		this.skill.fireEvent(event);
		
		verifyZeroInteractions(this.eventBus);
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEvent_agentDead_eventCaching() {
		AgentLife.getLife(this.agent).setState(AgentState.DEAD);
		
		this.skill.setEventBuffering(true);
		Event event = mock(Event.class);
		
		this.skill.fireEvent(event);
		
		verifyZeroInteractions(this.eventBus);
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_notEventGathering_notExceptionThrowing_agentNotStarted() {
		AgentLife.getLife(this.agent).setState(AgentState.UNSTARTED);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, false, false);
		
		verifyZeroInteractions(this.eventBus);
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_notEventGathering_notExceptionThrowing_agentInitializing() {
		AgentLife.getLife(this.agent).setState(AgentState.INITIALIZING);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, false, false);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Boolean> capturedExceptions = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
		verify(this.eventBus).immediateDispatch(capturedEvent.capture(), capturedExceptions.capture(), capturedLogger.capture());
		assertSame(event, capturedEvent.getValue());
		assertFalse(capturedExceptions.getValue());
		assertSame(this.logger.getLogger(), capturedLogger.getValue());
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_notEventGathering_notExceptionThrowing_agentAlive() {
		AgentLife.getLife(this.agent).setState(AgentState.ALIVE);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, false, false);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Boolean> capturedExceptions = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
		verify(this.eventBus).immediateDispatch(capturedEvent.capture(), capturedExceptions.capture(), capturedLogger.capture());
		assertSame(event, capturedEvent.getValue());
		assertFalse(capturedExceptions.getValue());
		assertSame(this.logger.getLogger(), capturedLogger.getValue());
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_notEventGathering_notExceptionThrowing_agentDying() {
		AgentLife.getLife(this.agent).setState(AgentState.DYING);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, false, false);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Boolean> capturedExceptions = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
		verify(this.eventBus).immediateDispatch(capturedEvent.capture(), capturedExceptions.capture(), capturedLogger.capture());
		assertSame(event, capturedEvent.getValue());
		assertFalse(capturedExceptions.getValue());
		assertSame(this.logger.getLogger(), capturedLogger.getValue());
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_notEventGathering_notExceptionThrowing_agentDead() {
		AgentLife.getLife(this.agent).setState(AgentState.DEAD);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, false, false);
		
		verifyZeroInteractions(this.eventBus);
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_notEventGathering_exceptionThrowing_agentNotStarted() {
		AgentLife.getLife(this.agent).setState(AgentState.UNSTARTED);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, false, true);
		
		verifyZeroInteractions(this.eventBus);
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_notEventGathering_exceptionThrowing_agentInitializing() {
		AgentLife.getLife(this.agent).setState(AgentState.INITIALIZING);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, false, true);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Boolean> capturedExceptions = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
		verify(this.eventBus).immediateDispatch(capturedEvent.capture(), capturedExceptions.capture(), capturedLogger.capture());
		assertSame(event, capturedEvent.getValue());
		assertTrue(capturedExceptions.getValue());
		assertSame(this.logger.getLogger(), capturedLogger.getValue());
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_notEventGathering_exceptionThrowing_agentAlive() {
		AgentLife.getLife(this.agent).setState(AgentState.ALIVE);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, false, true);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Boolean> capturedExceptions = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
		verify(this.eventBus).immediateDispatch(capturedEvent.capture(), capturedExceptions.capture(), capturedLogger.capture());
		assertSame(event, capturedEvent.getValue());
		assertTrue(capturedExceptions.getValue());
		assertSame(this.logger.getLogger(), capturedLogger.getValue());
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_notEventGathering_exceptionThrowing_agentDying() {
		AgentLife.getLife(this.agent).setState(AgentState.DYING);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, false, true);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Boolean> capturedExceptions = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
		verify(this.eventBus).immediateDispatch(capturedEvent.capture(), capturedExceptions.capture(), capturedLogger.capture());
		assertSame(event, capturedEvent.getValue());
		assertTrue(capturedExceptions.getValue());
		assertSame(this.logger.getLogger(), capturedLogger.getValue());
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_notEventGathering_exceptionThrowing_agentDead() {
		AgentLife.getLife(this.agent).setState(AgentState.DEAD);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, false, true);
		
		verifyZeroInteractions(this.eventBus);
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_eventGathering_notExceptionThrowing_agentNotStarted() {
		AgentLife.getLife(this.agent).setState(AgentState.UNSTARTED);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, true, false);
		
		verifyZeroInteractions(this.eventBus);
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_eventGathering_notExceptionThrowing_agentInitializing() {
		AgentLife.getLife(this.agent).setState(AgentState.INITIALIZING);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, true, false);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Boolean> capturedExceptions = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
		verify(this.eventBus).immediateDispatch(capturedEvent.capture(), capturedExceptions.capture(), capturedLogger.capture());
		assertSame(event, capturedEvent.getValue());
		assertFalse(capturedExceptions.getValue());
		assertSame(this.logger.getLogger(), capturedLogger.getValue());
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_eventGathering_notExceptionThrowing_agentAlive() {
		AgentLife.getLife(this.agent).setState(AgentState.ALIVE);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, true, false);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Boolean> capturedExceptions = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
		verify(this.eventBus).immediateDispatch(capturedEvent.capture(), capturedExceptions.capture(), capturedLogger.capture());
		assertSame(event, capturedEvent.getValue());
		assertFalse(capturedExceptions.getValue());
		assertSame(this.logger.getLogger(), capturedLogger.getValue());
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_eventGathering_notExceptionThrowing_agentDying() {
		AgentLife.getLife(this.agent).setState(AgentState.DYING);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, true, false);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Boolean> capturedExceptions = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
		verify(this.eventBus).immediateDispatch(capturedEvent.capture(), capturedExceptions.capture(), capturedLogger.capture());
		assertSame(event, capturedEvent.getValue());
		assertFalse(capturedExceptions.getValue());
		assertSame(this.logger.getLogger(), capturedLogger.getValue());
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_eventGathering_notExceptionThrowing_agentDead() {
		AgentLife.getLife(this.agent).setState(AgentState.DEAD);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, true, false);
		
		verifyZeroInteractions(this.eventBus);
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_eventGathering_exceptionThrowing_agentNotStarted() {
		AgentLife.getLife(this.agent).setState(AgentState.UNSTARTED);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, true, true);
		
		verifyZeroInteractions(this.eventBus);
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_eventGathering_exceptionThrowing_agentInitializing() {
		AgentLife.getLife(this.agent).setState(AgentState.INITIALIZING);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, true, true);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Boolean> capturedExceptions = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
		verify(this.eventBus).immediateDispatch(capturedEvent.capture(), capturedExceptions.capture(), capturedLogger.capture());
		assertSame(event, capturedEvent.getValue());
		assertTrue(capturedExceptions.getValue());
		assertSame(this.logger.getLogger(), capturedLogger.getValue());
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_eventGathering_exceptionThrowing_agentAlive() {
		AgentLife.getLife(this.agent).setState(AgentState.ALIVE);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, true, true);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Boolean> capturedExceptions = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
		verify(this.eventBus).immediateDispatch(capturedEvent.capture(), capturedExceptions.capture(), capturedLogger.capture());
		assertSame(event, capturedEvent.getValue());
		assertTrue(capturedExceptions.getValue());
		assertSame(this.logger.getLogger(), capturedLogger.getValue());
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_eventGathering_exceptionThrowing_agentDying() {
		AgentLife.getLife(this.agent).setState(AgentState.DYING);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, true, true);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Boolean> capturedExceptions = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
		verify(this.eventBus).immediateDispatch(capturedEvent.capture(), capturedExceptions.capture(), capturedLogger.capture());
		assertSame(event, capturedEvent.getValue());
		assertTrue(capturedExceptions.getValue());
		assertSame(this.logger.getLogger(), capturedLogger.getValue());
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	@Test
	public void fireEventAndWait_eventGathering_exceptionThrowing_agentDead() {
		AgentLife.getLife(this.agent).setState(AgentState.DEAD);
		
		Event event = mock(Event.class);
		
		this.skill.fireEventAndWait(event, true, true);
		
		verifyZeroInteractions(this.eventBus);
		assertFalse(this.skill.getBufferedEvents().iterator().hasNext());
	}

	private static class MyAgent extends Agent {

		public MyAgent(UUID parentID, UUID agentID, MyLoggingSkill skill) {
			super(parentID, agentID);
			setSkill(skill);
		}
		
	}

	private static class MyLoggingSkill extends Skill implements Logging {

		@Override
		public void setLoggingName(String name) {
		}

		@Override
		public void println(Object message) {
		}

		@Override
		public void error(Object message, Throwable exception, Object... parameters) {
		}

		@Override
		public void warning(Object message, Throwable exception, Object... parameters) {
		}

		@Override
		public void info(Object message, Object... parameters) {
		}

		@Override
		public void debug(Object message, Object... parameters) {
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

		private final Logger logger = mock(Logger.class);
		
		@Override
		public Logger getLogger() {
			return this.logger;
		}

		@Override
		public void error(Supplier<String> messageProvider) {
		}

		@Override
		public void warning(Supplier<String> messageProvider) {
		}

		@Override
		public void info(Supplier<String> messageProvider) {
		}

		@Override
		public void debug(Supplier<String> messageProvider) {
		}
		
	}

}
