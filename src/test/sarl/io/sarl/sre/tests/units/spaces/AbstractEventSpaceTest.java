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
package io.sarl.sre.tests.units.spaces;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import io.sarl.lang.core.Address;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.SpaceID;
import io.sarl.sre.services.executor.ExecutorService;
import io.sarl.sre.spaces.AbstractEventSpace;
import io.sarl.sre.spaces.AbstractEventSpace.Participant;
import io.sarl.sre.tests.testutils.AbstractJanusTest;
import io.sarl.tests.api.ManualMocking;
import io.sarl.tests.api.Nullable;
import io.sarl.util.OpenEventSpaceSpecification;
import io.sarl.util.Scopes;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
//@SuppressWarnings("all")
@ManualMocking
public class AbstractEventSpaceTest extends AbstractJanusTest {

	@Nullable
	private UUID agentId;

	@Nullable
	private UUID agentId2;

	@Nullable
	private SpaceID spaceId;

	@Nullable
	private Address address;

	@Nullable
	private EventListener listener1;

	@Nullable
	private EventListener listener2;

	@Mock
	private ExecutorService executor;

	@Mock
	private Map<UUID, Participant> participants;

	@InjectMocks
	private AbstractEventSpace space;

	@Before
	public void setUp() {
		this.agentId = UUID.randomUUID();

		this.agentId2 = UUID.randomUUID();

		this.spaceId = new SpaceID(UUID.randomUUID(), UUID.randomUUID(), OpenEventSpaceSpecification.class);

		this.address = new Address(this.spaceId, this.agentId);

		this.space = new AbstractEventSpace(this.spaceId) {
			protected Map<UUID, Participant> getInternalParticipantStructure() {
				return AbstractEventSpaceTest.this.participants;
			}
		};

		this.listener1 = mock(EventListener.class);
		when(this.listener1.getID()).thenReturn(this.agentId);

		this.listener2 = mock(EventListener.class);
		when(this.listener2.getID()).thenReturn(this.agentId2);

		MockitoAnnotations.initMocks(this);

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Runnable r = (Runnable) invocation.getArguments()[0];
				r.run();
				return null;
			}
		}).when(this.executor).executeAsap(any(Runnable.class));
	}

	private void register() throws Exception {
		Participant participant1 = mock(Participant.class);
		when(participant1.getAddress()).thenReturn(this.address);
		when(participant1.getParticipant()).thenReturn(this.listener1);
		Participant participant2 = mock(Participant.class);
		when(participant2.getAddress()).thenReturn(new Address(this.spaceId, this.agentId2));
		when(participant2.getParticipant()).thenReturn(this.listener2);
		Set<UUID> keys = new HashSet<>();
		keys.add(this.agentId);
		keys.add(this.agentId2);
		when(this.participants.keySet()).thenReturn(keys);
		Set<Participant> values = new HashSet<>();
		values.add(participant1);
		values.add(participant2);
		when(this.participants.values()).thenReturn(values);
		when(this.participants.size()).thenReturn(2);
		when(this.participants.remove(any())).thenReturn(participant1);
		when(this.participants.get(any())).thenAnswer((it) -> {
			if (this.agentId.equals(it.getArgument(0))) {
				return participant1;
			}
			if (this.agentId2.equals(it.getArgument(0))) {
				return participant2;
			}
			return null;
		});
	}

	@Test
	public void getAddressUUID() throws Exception {
		assertNull(this.space.getAddress(this.listener1.getID()));
		register();
		assertSame(this.address, this.space.getAddress(this.listener1.getID()));
	}

	@Test
	public void getParticipants() throws Exception {
		Set<UUID> set;
		set = this.space.getParticipants();
		assertNotNull(set);
		assertTrue(set.isEmpty());
		register();
		set = this.space.getParticipants();
		assertNotNull(set);
		assertEquals(2, set.size());
		assertTrue(set.contains(this.agentId));
		assertTrue(set.contains(this.agentId2));
	}

	private void emitLocally(Event event, Scope<Address> scope) throws Exception {
		this.reflect.invoke(this.space, "emitLocally", event, scope);
	}

	@Test
	public void emitLocally_nullScope() throws Exception {
		Event event;

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		emitLocally(event, null);
		verifyZeroInteractions(this.listener1);
		verifyZeroInteractions(this.listener2);

		register();

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		emitLocally(event, null);

		ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);

		verify(this.listener1).receiveEvent(argument.capture());
		assertSame(event, argument.getValue());

		verify(this.listener2).receiveEvent(argument.capture());
		assertSame(event, argument.getValue());
	}

	@Test
	@Deprecated
	public void emitLocally_allParticipants() throws Exception {
		Event event;

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		emitLocally(event, Scopes.allParticipants());
		verifyZeroInteractions(this.listener1);
		verifyZeroInteractions(this.listener2);

		register();

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		emitLocally(event, Scopes.allParticipants());

		ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);

		verify(this.listener1).receiveEvent(argument.capture());
		assertSame(event, argument.getValue());

		verify(this.listener2).receiveEvent(argument.capture());
		assertSame(event, argument.getValue());
	}

	@Test
	@Deprecated
	public void emitLocally_singleAddress() throws Exception {
		Event event;

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		emitLocally(event, Scopes.addresses(this.address));
		verifyZeroInteractions(this.listener1);
		verifyZeroInteractions(this.listener2);

		register();

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		emitLocally(event, Scopes.addresses(this.address));

		ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);

		verify(this.listener1).receiveEvent(argument.capture());
		assertSame(event, argument.getValue());
		verifyZeroInteractions(this.listener2);
	}

	@Test
	public void emitLocally_complexScope() throws Exception {
		Event event;

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		emitLocally(event, (it) -> it.equals(this.address));
		verifyZeroInteractions(this.listener1);
		verifyZeroInteractions(this.listener2);

		register();

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		emitLocally(event, (it) -> it.equals(this.address));

		ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);

		verify(this.listener1).receiveEvent(argument.capture());
		assertSame(event, argument.getValue());
		verifyZeroInteractions(this.listener2);
	}

	@Test
	public void emit_nullScope() throws Exception {
		Event event;

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		this.space.emit(null, event, null);
		verifyZeroInteractions(this.listener1);
		verifyZeroInteractions(this.listener2);

		register();

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		this.space.emit(null, event, null);

		ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);

		verify(this.listener1).receiveEvent(argument.capture());
		assertSame(event, argument.getValue());

		verify(this.listener2).receiveEvent(argument.capture());
		assertSame(event, argument.getValue());
	}

	@Test
	@Deprecated
	public void emit_allParticipants() throws Exception {
		Event event;

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		this.space.emit(null, event, Scopes.allParticipants());
		verifyZeroInteractions(this.listener1);
		verifyZeroInteractions(this.listener2);

		register();

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		this.space.emit(null, event, Scopes.allParticipants());

		ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);

		verify(this.listener1).receiveEvent(argument.capture());
		assertSame(event, argument.getValue());

		verify(this.listener2).receiveEvent(argument.capture());
		assertSame(event, argument.getValue());
	}

	@Test
	@Deprecated
	public void emit_singleAddress() throws Exception {
		Event event;

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		this.space.emit(null, event, Scopes.addresses(this.address));
		verifyZeroInteractions(this.listener1);
		verifyZeroInteractions(this.listener2);

		register();

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		this.space.emit(null, event, Scopes.addresses(this.address));

		ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);

		verify(this.listener1).receiveEvent(argument.capture());
		assertSame(event, argument.getValue());
		verifyZeroInteractions(this.listener2);
	}

	@Test
	public void emit_complexScope() throws Exception {
		Event event;

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		this.space.emit(null, event, (it) -> it.equals(this.address));
		verifyZeroInteractions(this.listener1);
		verifyZeroInteractions(this.listener2);

		register();

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		this.space.emit(null, event, (it) -> it.equals(this.address));

		ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);

		verify(this.listener1).receiveEvent(argument.capture());
		assertSame(event, argument.getValue());
		verifyZeroInteractions(this.listener2);
	}

}
