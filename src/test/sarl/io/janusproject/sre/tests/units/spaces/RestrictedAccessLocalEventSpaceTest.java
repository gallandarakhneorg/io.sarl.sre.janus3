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
package io.janusproject.sre.tests.units.spaces;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.security.AccessControlException;
import java.security.Principal;
import java.security.acl.Acl;
import java.security.acl.Permission;
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

import io.janusproject.sre.services.executor.ExecutorService;
import io.janusproject.sre.spaces.RestrictedAccessLocalEventSpace;
import io.janusproject.sre.tests.testutils.AbstractJanusTest;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.SpaceID;
import io.sarl.tests.api.ManualMocking;
import io.sarl.tests.api.Nullable;
import io.sarl.util.OpenEventSpaceSpecification;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
//@SuppressWarnings("all")
@ManualMocking
public class RestrictedAccessLocalEventSpaceTest extends AbstractJanusTest {

	@Nullable
	private UUID agentId;

	@Nullable
	private SpaceID spaceId;

	@Nullable
	private Address address;

	@Nullable
	private EventListener listener;

	@Mock
	private ExecutorService executor;

	@Nullable
	private Acl acl;

	@Nullable
	private Permission permission;

	@InjectMocks
	private RestrictedAccessLocalEventSpace space;

	@Before
	public void setUp() {
		this.agentId = UUID.randomUUID();

		this.spaceId = new SpaceID(UUID.randomUUID(), UUID.randomUUID(), OpenEventSpaceSpecification.class);

		this.address = new Address(this.spaceId, this.agentId);

		this.acl = mock(Acl.class);
		this.permission = mock(Permission.class);

		this.space = new RestrictedAccessLocalEventSpace(this.spaceId, this.acl, this.permission);

		this.listener = mock(EventListener.class);
		when(this.listener.getID()).thenReturn(this.agentId);

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

	private void doRegisterWithAccess() {
		when(this.acl.checkPermission(any(), any())).thenReturn(true);
		this.space.register(this.listener, mock(Principal.class));
	}

	private void doRegisterWithoutAccess() {
		when(this.acl.checkPermission(any(), any())).thenReturn(false);
		this.space.register(this.listener, mock(Principal.class));
	}

	private void doUnregister() {
		this.space.unregister(this.listener);
	}

	@Test
	public void getAddressUUID() {
		assertNull(this.space.getAddress(this.listener.getID()));
		doRegisterWithAccess();
		assertEquals(this.address, this.space.getAddress(this.listener.getID()));
		doUnregister();
		assertNull(this.space.getAddress(this.listener.getID()));
	}

	@Test
	public void getParticipants() {
		Set<UUID> set;
		set = this.space.getParticipants();
		assertNotNull(set);
		assertTrue(set.isEmpty());
		doRegisterWithAccess();
		set = this.space.getParticipants();
		assertNotNull(set);
		assertEquals(1, set.size());
		assertTrue(set.contains(this.listener.getID()));
		doUnregister();
		set = this.space.getParticipants();
		assertNotNull(set);
		assertTrue(set.isEmpty());
	}

	@Test
	public void register_access() throws Exception {
		Event event;

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		this.space.emit(null, event, null);
		verifyZeroInteractions(this.listener);

		doRegisterWithAccess();

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		this.space.emit(null, event, null);

		ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);

		verify(this.listener).receiveEvent(argument.capture());
		assertSame(event, argument.getValue());
	}

	@Test(expected = AccessControlException.class)
	public void register_noAccess() throws Exception {
		Event event;

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		this.space.emit(null, event, null);
		verifyZeroInteractions(this.listener);

		doRegisterWithoutAccess();
	}

	@Test
	public void unregister() throws Exception {
		Event event;

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		this.space.emit(null, event, null);
		verifyZeroInteractions(this.listener);

		doRegisterWithAccess();
		doUnregister();

		event = mock(Event.class);
		when(event.getSource()).thenReturn(this.address);
		this.space.emit(null, event, null);

		ArgumentCaptor<Event> argument = ArgumentCaptor.forClass(Event.class);
		verify(this.listener, never()).receiveEvent(argument.capture());

	}

}
