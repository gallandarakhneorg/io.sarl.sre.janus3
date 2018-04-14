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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.sarl.sre.capacities.InternalEventBusCapacity;
import io.sarl.sre.services.context.Context;
import io.sarl.sre.services.lifecycle.AgentLife;
import io.sarl.sre.services.lifecycle.ContextReference;
import io.sarl.sre.services.lifecycle.SkillUninstaller;
import io.sarl.sre.services.logging.LoggingService;
import io.sarl.sre.skills.EventBus;
import io.sarl.core.Destroy;
import io.sarl.core.Initialize;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.Skill;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.util.SynchronizedIterable;
import io.sarl.sre.services.lifecycle.AgentState;
import io.sarl.sre.tests.testutils.AbstractSreTest;
import io.sarl.tests.api.Nullable;
import io.sarl.util.OpenEventSpace;
import io.sarl.util.OpenEventSpaceSpecification;
import junit.framework.AssertionFailedError;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class AgentLifeTest extends AbstractSreTest {

	@Nullable
	private MySkill eventBus;

	@Nullable
	private Agent agent;

	@Nullable
	private AgentLife life;

	@Before
	public void setUp() {
		this.eventBus = spy(new MySkill());
		this.agent = spy(new MyAgent(this.eventBus));
		this.life = AgentLife.getLife(agent);
		// Remove the interaction
		reset(this.eventBus);
	}

	@Test
	public void getAgent() {
		assertSame(this.agent, this.life.getAgent());
	}

	@Test
	public void setAgent() {
		Agent ag = mock(Agent.class);
		this.life.setAgent(ag);
		assertSame(ag, this.life.getAgent());
	}

	@Test
	public void getState() {
		assertSame(AgentState.UNSTARTED, this.life.getState());
	}

	@Test
	public void setState() {
		this.life.setState(AgentState.DEAD);
		assertSame(AgentState.DEAD, this.life.getState());
	}

	@Test
	public void setInnerContext() {
		Context ctx1;
		Context ctx2;
		Context ctx3;
		
		ctx2 = mock(Context.class);
		ctx1 = this.life.setInnerContext(ctx2);
		assertNull(ctx1);
		assertSame(ctx2, this.life.getInnerContext());

		ctx3 = mock(Context.class);
		ctx1 = this.life.setInnerContext(ctx3);
		assertSame(ctx2, ctx1);
		assertSame(ctx3, this.life.getInnerContext());
	}

	@Test
	public void getInnerContext() {
		assertNull(this.life.getInnerContext());
	}

	@Test
	public void getAddressInInnerDefaultSpace() {
		Address adr = this.life.getAddressInInnerDefaultSpace();
		assertNotNull(adr);
		assertEquals(this.agent.getID(), adr.getUUID());
		assertEquals(this.agent.getID(), adr.getSpaceID().getContextID());
		assertNotEquals(this.agent.getID(), adr.getSpaceID().getID());
	}

	@Test
	public void getExternalContexts() {
		SynchronizedIterable<ContextReference> context = this.life.getExternalContexts();
		assertFalse(context.iterator().hasNext());
	}

	@Test
	public void getEnclosingContexts_0() {
		SynchronizedIterable<ContextReference> context = this.life.getEnclosingContexts();
		assertFalse(context.iterator().hasNext());
	}

	@Test
	public void getEnclosingContexts_1() {
		Context ctx = mock(Context.class);
		Address adr = mock(Address.class);
		this.life.setDefaultContext(ctx, adr);
		SynchronizedIterable<ContextReference> context = this.life.getEnclosingContexts();
		Iterator<ContextReference> iterator = context.iterator();
		ContextReference ref;
		
		assertTrue(iterator.hasNext());
		ref = iterator.next();
		assertSame(ctx, ref.getContext());
		assertSame(adr, ref.getAddressInDefaultSpace());

		assertFalse(iterator.hasNext());
	}

	private static UUID[] sortIds(UUID... ids) {
		Arrays.sort(ids, (a, b) -> {
			return a.compareTo(b);
		});
		return ids;
	}
	
	@Test
	public void getEnclosingContexts_2() {
		UUID[] minmax = sortIds(UUID.randomUUID(), UUID.randomUUID());
		Context ctx1 = mock(Context.class);
		when(ctx1.getID()).thenReturn(minmax[0]);
		Address adr1 = mock(Address.class);
		Context ctx2 = mock(Context.class);
		when(ctx2.getID()).thenReturn(minmax[1]);
		Address adr2 = mock(Address.class);
		this.life.addExternalContext(ctx1, adr1);
		this.life.addExternalContext(ctx2, adr2);
		SynchronizedIterable<ContextReference> context = this.life.getEnclosingContexts();
		Iterator<ContextReference> iterator = context.iterator();
		ContextReference ref;

		assertTrue(iterator.hasNext());
		ref = iterator.next();
		assertSame(ctx1, ref.getContext());
		assertSame(adr1, ref.getAddressInDefaultSpace());

		assertTrue(iterator.hasNext());
		ref = iterator.next();
		assertSame(ctx2, ref.getContext());
		assertSame(adr2, ref.getAddressInDefaultSpace());

		assertFalse(iterator.hasNext());
	}

	@Test
	public void getEnclosingContexts_3() {
		UUID[] minmax = sortIds(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
		Context ctx0 = mock(Context.class);
		when(ctx0.getID()).thenReturn(minmax[0]);
		Address adr0 = mock(Address.class);
		Context ctx1 = mock(Context.class);
		when(ctx1.getID()).thenReturn(minmax[1]);
		Address adr1 = mock(Address.class);
		Context ctx2 = mock(Context.class);
		when(ctx2.getID()).thenReturn(minmax[2]);
		Address adr2 = mock(Address.class);
		this.life.setDefaultContext(ctx0, adr0);
		this.life.addExternalContext(ctx1, adr1);
		this.life.addExternalContext(ctx2, adr2);
		SynchronizedIterable<ContextReference> context = this.life.getEnclosingContexts();
		Iterator<ContextReference> iterator = context.iterator();
		ContextReference ref;
		
		assertTrue(iterator.hasNext());
		ref = iterator.next();
		assertSame(ctx0, ref.getContext());
		assertSame(adr0, ref.getAddressInDefaultSpace());

		assertTrue(iterator.hasNext());
		ref = iterator.next();
		assertSame(ctx1, ref.getContext());
		assertSame(adr1, ref.getAddressInDefaultSpace());

		assertTrue(iterator.hasNext());
		ref = iterator.next();
		assertSame(ctx2, ref.getContext());
		assertSame(adr2, ref.getAddressInDefaultSpace());

		assertFalse(iterator.hasNext());
	}

	@Test
	public void getEnclosingContexts_4() {
		UUID[] minmax = sortIds(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
		Context ctx0 = mock(Context.class);
		when(ctx0.getID()).thenReturn(minmax[2]);
		Address adr0 = mock(Address.class);
		Context ctx1 = mock(Context.class);
		when(ctx1.getID()).thenReturn(minmax[0]);
		Address adr1 = mock(Address.class);
		Context ctx2 = mock(Context.class);
		when(ctx2.getID()).thenReturn(minmax[1]);
		Address adr2 = mock(Address.class);
		this.life.setDefaultContext(ctx0, adr0);
		this.life.addExternalContext(ctx1, adr1);
		this.life.addExternalContext(ctx2, adr2);
		SynchronizedIterable<ContextReference> context = this.life.getEnclosingContexts();
		Iterator<ContextReference> iterator = context.iterator();
		ContextReference ref;
		
		assertTrue(iterator.hasNext());
		ref = iterator.next();
		assertSame(ctx0, ref.getContext());
		assertSame(adr0, ref.getAddressInDefaultSpace());

		assertTrue(iterator.hasNext());
		ref = iterator.next();
		assertSame(ctx1, ref.getContext());
		assertSame(adr1, ref.getAddressInDefaultSpace());

		assertTrue(iterator.hasNext());
		ref = iterator.next();
		assertSame(ctx2, ref.getContext());
		assertSame(adr2, ref.getAddressInDefaultSpace());

		assertFalse(iterator.hasNext());
	}

	@Test
	public void getExternalContexts_0() {
		SynchronizedIterable<ContextReference> context = this.life.getExternalContexts();
		assertFalse(context.iterator().hasNext());
	}

	@Test
	public void getExternalContexts_1() {
		Context ctx = mock(Context.class);
		Address adr = mock(Address.class);
		this.life.setDefaultContext(ctx, adr);
		SynchronizedIterable<ContextReference> context = this.life.getExternalContexts();
		Iterator<ContextReference> iterator = context.iterator();
		
		assertFalse(iterator.hasNext());
	}
	
	@Test
	public void getExternalContexts_2() {
		UUID[] minmax = sortIds(UUID.randomUUID(), UUID.randomUUID());
		Context ctx1 = mock(Context.class);
		when(ctx1.getID()).thenReturn(minmax[0]);
		Address adr1 = mock(Address.class);
		Context ctx2 = mock(Context.class);
		when(ctx2.getID()).thenReturn(minmax[1]);
		Address adr2 = mock(Address.class);
		this.life.addExternalContext(ctx1, adr1);
		this.life.addExternalContext(ctx2, adr2);
		SynchronizedIterable<ContextReference> context = this.life.getExternalContexts();
		Iterator<ContextReference> iterator = context.iterator();
		ContextReference ref;

		assertTrue(iterator.hasNext());
		ref = iterator.next();
		assertSame(ctx1, ref.getContext());
		assertSame(adr1, ref.getAddressInDefaultSpace());

		assertTrue(iterator.hasNext());
		ref = iterator.next();
		assertSame(ctx2, ref.getContext());
		assertSame(adr2, ref.getAddressInDefaultSpace());

		assertFalse(iterator.hasNext());
	}

	@Test
	public void getExternalContexts_3() {
		UUID[] minmax = sortIds(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
		Context ctx0 = mock(Context.class);
		when(ctx0.getID()).thenReturn(minmax[0]);
		Address adr0 = mock(Address.class);
		Context ctx1 = mock(Context.class);
		when(ctx1.getID()).thenReturn(minmax[1]);
		Address adr1 = mock(Address.class);
		Context ctx2 = mock(Context.class);
		when(ctx2.getID()).thenReturn(minmax[2]);
		Address adr2 = mock(Address.class);
		this.life.setDefaultContext(ctx0, adr0);
		this.life.addExternalContext(ctx1, adr1);
		this.life.addExternalContext(ctx2, adr2);
		SynchronizedIterable<ContextReference> context = this.life.getExternalContexts();
		Iterator<ContextReference> iterator = context.iterator();
		ContextReference ref;
		
		assertTrue(iterator.hasNext());
		ref = iterator.next();
		assertSame(ctx1, ref.getContext());
		assertSame(adr1, ref.getAddressInDefaultSpace());

		assertTrue(iterator.hasNext());
		ref = iterator.next();
		assertSame(ctx2, ref.getContext());
		assertSame(adr2, ref.getAddressInDefaultSpace());

		assertFalse(iterator.hasNext());
	}

	@Test
	public void getExternalContextCount_0() {
		assertEquals(0, this.life.getExternalContextCount());
	}

	@Test
	public void getExternalContextCount_1() {
		Context ctx = mock(Context.class);
		Address adr = mock(Address.class);
		this.life.setDefaultContext(ctx, adr);
		assertEquals(0, this.life.getExternalContextCount());
	}
	
	@Test
	public void getExternalContextCount_2() {
		UUID[] minmax = sortIds(UUID.randomUUID(), UUID.randomUUID());
		Context ctx1 = mock(Context.class);
		when(ctx1.getID()).thenReturn(minmax[0]);
		Address adr1 = mock(Address.class);
		Context ctx2 = mock(Context.class);
		when(ctx2.getID()).thenReturn(minmax[1]);
		Address adr2 = mock(Address.class);
		this.life.addExternalContext(ctx1, adr1);
		this.life.addExternalContext(ctx2, adr2);
		assertEquals(2, this.life.getExternalContextCount());
	}

	@Test
	public void getExternalContextCount_3() {
		UUID[] minmax = sortIds(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
		Context ctx0 = mock(Context.class);
		when(ctx0.getID()).thenReturn(minmax[0]);
		Address adr0 = mock(Address.class);
		Context ctx1 = mock(Context.class);
		when(ctx1.getID()).thenReturn(minmax[1]);
		Address adr1 = mock(Address.class);
		Context ctx2 = mock(Context.class);
		when(ctx2.getID()).thenReturn(minmax[2]);
		Address adr2 = mock(Address.class);
		this.life.setDefaultContext(ctx0, adr0);
		this.life.addExternalContext(ctx1, adr1);
		this.life.addExternalContext(ctx2, adr2);
		
		assertEquals(2, this.life.getExternalContextCount());
	}

	@Test
	public void setDefaultContext() {
		Context ctx;
		ctx = mock(Context.class);
		this.life.setDefaultContext(ctx, mock(Address.class));
		assertSame(ctx, this.life.getDefaultContext().getContext());
		ctx = mock(Context.class);
		ContextReference ref = this.life.setDefaultContext(ctx, mock(Address.class));
		assertSame(ctx, this.life.getDefaultContext().getContext());
		assertNotNull(ref);
		assertNotEquals(ref.getContext(), ctx);
	}

	@Test
	public void getDefaultContext() {
		Context ctx;
		assertNull(this.life.getDefaultContext());
		ctx = mock(Context.class);
		this.life.setDefaultContext(ctx, mock(Address.class));
		assertSame(ctx, this.life.getDefaultContext().getContext());
		ctx = mock(Context.class);
		this.life.setDefaultContext(ctx, mock(Address.class));
		assertSame(ctx, this.life.getDefaultContext().getContext());
	}

	@Test
	public void addExternalContext() {
		UUID[] minmax = sortIds(UUID.randomUUID(), UUID.randomUUID());
		Context ctx1 = mock(Context.class);
		when(ctx1.getID()).thenReturn(minmax[0]);
		Address adr1 = mock(Address.class);
		Context ctx2 = mock(Context.class);
		when(ctx2.getID()).thenReturn(minmax[1]);
		Address adr2 = mock(Address.class);
		ContextReference ref1 = this.life.addExternalContext(ctx1, adr1);
		ContextReference ref2 = this.life.addExternalContext(ctx2, adr2);
		
		assertNotNull(ref1);
		assertSame(ctx1, ref1.getContext());
		assertSame(adr1, ref1.getAddressInDefaultSpace());
		assertNotNull(ref2);
		assertSame(ctx2, ref2.getContext());
		assertSame(adr2, ref2.getAddressInDefaultSpace());
		
		SynchronizedIterable<ContextReference> context = this.life.getExternalContexts();
		Iterator<ContextReference> iterator = context.iterator();
		ContextReference ref;

		assertTrue(iterator.hasNext());
		ref = iterator.next();
		assertSame(ctx1, ref.getContext());
		assertSame(adr1, ref.getAddressInDefaultSpace());

		assertTrue(iterator.hasNext());
		ref = iterator.next();
		assertSame(ctx2, ref.getContext());
		assertSame(adr2, ref.getAddressInDefaultSpace());

		assertFalse(iterator.hasNext());
	}

	@Test
	public void removeExternalContextContext() {
		Context ctx1 = mock(Context.class);
		when(ctx1.getID()).thenReturn(UUID.randomUUID());
		Address adr1 = mock(Address.class);
		Context ctx2 = mock(Context.class);
		when(ctx2.getID()).thenReturn(UUID.randomUUID());
		Address adr2 = mock(Address.class);
		this.life.addExternalContext(ctx1, adr1);
		this.life.addExternalContext(ctx2, adr2);
		
		this.life.removeExternalContext(ctx1);
		
		SynchronizedIterable<ContextReference> context = this.life.getExternalContexts();
		Iterator<ContextReference> iterator = context.iterator();
		ContextReference ref;

		assertTrue(iterator.hasNext());
		ref = iterator.next();
		assertSame(ctx2, ref.getContext());
		assertSame(adr2, ref.getAddressInDefaultSpace());

		assertFalse(iterator.hasNext());
	}

	@Test
	public void removeExternalContextContextReference() {
		Context ctx1 = mock(Context.class);
		when(ctx1.getID()).thenReturn(UUID.randomUUID());
		Address adr1 = mock(Address.class);
		Context ctx2 = mock(Context.class);
		when(ctx2.getID()).thenReturn(UUID.randomUUID());
		Address adr2 = mock(Address.class);
		ContextReference ref1 = this.life.addExternalContext(ctx1, adr1);
		this.life.addExternalContext(ctx2, adr2);
		
		this.life.removeExternalContext(ref1);
		
		SynchronizedIterable<ContextReference> context = this.life.getExternalContexts();
		Iterator<ContextReference> iterator = context.iterator();
		ContextReference ref;

		assertTrue(iterator.hasNext());
		ref = iterator.next();
		assertSame(ctx2, ref.getContext());
		assertSame(adr2, ref.getAddressInDefaultSpace());

		assertFalse(iterator.hasNext());
	}

	@Test
	public void start_failing() {
		SkillUninstaller installer = mock(SkillUninstaller.class);
		LoggingService logger = mock(LoggingService.class);
		Logger loglog = mock(Logger.class);
		when(logger.getKernelLogger()).thenReturn(loglog);
		UUID parent = UUID.randomUUID();
		UUID spawner = UUID.randomUUID();
		Context spawningContext = mock(Context.class);
		when(spawningContext.getID()).thenReturn(parent);
		Object[] params = new Object[] { UUID.randomUUID(), UUID.randomUUID().toString() };
		
		assertFalse(this.life.start(installer, logger, spawner, spawningContext, params));
		
		assertSame(AgentState.DEAD, this.life.getState());
		
		ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Boolean> gath = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Boolean> thro = ArgumentCaptor.forClass(Boolean.class);
		verify(this.eventBus, never()).fireEventAndWait(event.capture(), gath.capture(), thro.capture());
	}

	@Test
	public void start_success() {
		EventListener eventListener = mock(EventListener.class);
		when(this.eventBus.getAssociatedEventBusListener()).thenReturn(eventListener);
		SkillUninstaller installer = mock(SkillUninstaller.class);
		LoggingService logger = mock(LoggingService.class);
		Logger loglog = mock(Logger.class);
		when(logger.getKernelLogger()).thenReturn(loglog);
		// Force the logger to forward the exception
		doAnswer((it) -> {
			Exceptions.sneakyThrow((Throwable) it.getArgument(0));
			return null;
		}).when(loglog).log(any(Level.class), anyString(), any(Throwable.class));
		UUID parent = UUID.randomUUID();
		UUID spawner = UUID.randomUUID();
		Context spawningContext = mock(Context.class);
		when(spawningContext.getID()).thenReturn(parent);
		Object[] params = new Object[] { UUID.randomUUID(), UUID.randomUUID().toString() };
		OpenEventSpace defaultSpace = mock(OpenEventSpace.class);
		when(spawningContext.getDefaultSpace()).thenReturn(defaultSpace);
		SpaceID defaultSpaceID = new SpaceID(parent, UUID.randomUUID(), OpenEventSpaceSpecification.class);
		when(defaultSpace.getSpaceID()).thenReturn(defaultSpaceID);
		
		assertTrue(this.life.start(installer, logger, spawner, spawningContext, params));
		
		assertSame(AgentState.ALIVE, this.life.getState());
		
		verify(this.eventBus, times(1)).getAssociatedEventBusListener();

		ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Boolean> gath = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Boolean> thro = ArgumentCaptor.forClass(Boolean.class);
		verify(this.eventBus, times(1)).fireEventAndWait(event.capture(), gath.capture(), thro.capture());
		assertInstanceOf(Initialize.class, event.getValue());
		Initialize init = (Initialize) event.getValue();
		assertNotNull(init.getSource());
		assertSame(params, init.parameters);
		assertEquals(spawner, init.spawner);
		
		assertTrue(gath.getValue());
		assertTrue(thro.getValue());
		
		verifyNoMoreInteractions(installer);
		verifyNoMoreInteractions(this.eventBus);
	}

	@Test
	public void stop() {
		SkillUninstaller installer = mock(SkillUninstaller.class);
		Iterable it = Collections.singletonList(this.eventBus);
		when(installer.uninstallSkillsBeforeDestroy(any())).thenReturn(it);
		LoggingService logger = mock(LoggingService.class);
		
		this.life.stop(installer, logger, true);
		
		assertSame(AgentState.DEAD, this.life.getState());
		
		ArgumentCaptor<Agent> agent = ArgumentCaptor.forClass(Agent.class);
		verify(installer).uninstallSkillsBeforeDestroy(agent.capture());
		assertSame(this.agent, agent.getValue());
		ArgumentCaptor<Iterable<Skill>> skills = ArgumentCaptor.forClass(Iterable.class);
		verify(installer).uninstallSkillsAfterDestroy(agent.capture(), skills.capture());
		assertSame(this.agent, agent.getValue());
		Iterator<Skill> iterator = skills.getValue().iterator();
		assertTrue(iterator.hasNext());
		assertSame(this.eventBus, iterator.next());
		assertFalse(iterator.hasNext());
		
		ArgumentCaptor<Event> event = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Boolean> gath = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Boolean> thro = ArgumentCaptor.forClass(Boolean.class);
		verify(this.eventBus).fireEventAndWait(event.capture(), gath.capture(), thro.capture());
		assertInstanceOf(Destroy.class, event.getValue());
		Destroy destroy = (Destroy) event.getValue();
		assertNotNull(destroy.getSource());
		
		assertFalse(gath.getValue());
		assertFalse(thro.getValue());
		
		verify(this.eventBus, times(1)).getAssociatedEventBusListener();

		verifyNoMoreInteractions(installer, this.eventBus);
	}

	private static class MyAgent extends Agent {

		public MyAgent(InternalEventBusCapacity cap) {
			super(UUID.randomUUID(), UUID.randomUUID());
			setSkill((Skill) cap, InternalEventBusCapacity.class);
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
			return Collections.emptyList();
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
