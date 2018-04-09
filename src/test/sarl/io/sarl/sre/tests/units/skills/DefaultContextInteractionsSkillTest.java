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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.SpaceID;
import io.sarl.sre.services.context.Context;
import io.sarl.sre.services.lifecycle.AgentLife;
import io.sarl.sre.skills.DefaultContextInteractionsSkill;
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
@SuppressWarnings("all")
public class DefaultContextInteractionsSkillTest extends AbstractSreTest {

	@Nullable
	private UUID contextId;

	@Nullable
	private UUID agentId;

	@Nullable
	private Agent agent;

	@Nullable
	private DefaultContextInteractionsSkill skill;

	@Before
	public void setUp() {
		this.contextId = UUID.randomUUID();
		this.agentId = UUID.randomUUID();
		this.agent = spy(new MyAgent(contextId, this.agentId));
		this.skill = new DefaultContextInteractionsSkill(this.agent);
	}

	@Test
	public void getDefaultContext() {
		Context ctx = mock(Context.class);
		AgentLife.getLife(this.agent).setDefaultContext(ctx, mock(Address.class));

		assertSame(ctx, this.skill.getDefaultContext());
	}

	@Test
	public void getDefaultSpace() {
		Context ctx = mock(Context.class);
		OpenEventSpace space = mock(OpenEventSpace.class);
		when(ctx.getDefaultSpace()).thenReturn(space);
		AgentLife.getLife(this.agent).setDefaultContext(ctx, mock(Address.class));

		assertSame(space, this.skill.getDefaultSpace());
	}

	@Test
	public void getDefaultAddress() {
		Address adr = mock(Address.class);
		AgentLife.getLife(this.agent).setDefaultContext(mock(Context.class), adr);

		assertSame(adr, this.skill.getDefaultAddress());
	}

	@Test
	public void isDefaultContextAgentContext() {
		UUID id0 = UUID.randomUUID();
		Context ctx0 = mock(Context.class);
		when(ctx0.getID()).thenReturn(id0);
		UUID id1 = UUID.randomUUID();
		Context ctx1 = mock(Context.class);
		when(ctx1.getID()).thenReturn(id1);
		AgentLife.getLife(this.agent).setDefaultContext(ctx1, mock(Address.class));

		assertFalse(this.skill.isDefaultContext(ctx0));
		assertTrue(this.skill.isDefaultContext(ctx1));
	}

	@Test
	public void isDefaultContextUUID() {
		UUID id0 = UUID.randomUUID();
		Context ctx0 = mock(Context.class);
		when(ctx0.getID()).thenReturn(id0);
		UUID id1 = UUID.randomUUID();
		Context ctx1 = mock(Context.class);
		when(ctx1.getID()).thenReturn(id1);
		AgentLife.getLife(this.agent).setDefaultContext(ctx1, mock(Address.class));

		assertFalse(this.skill.isDefaultContext(id0));
		assertTrue(this.skill.isDefaultContext(id1));
	}

	@Test
	public void isDefaultSpaceSpace() {
		Context ctx0 = mock(Context.class);
		OpenEventSpace space0 = mock(OpenEventSpace.class);
		SpaceID id0 = mock(SpaceID.class);
		when(space0.getSpaceID()).thenReturn(id0);
		when(ctx0.getDefaultSpace()).thenReturn(space0);
		Context ctx1 = mock(Context.class);
		OpenEventSpace space1 = mock(OpenEventSpace.class);
		SpaceID id1 = mock(SpaceID.class);
		when(space1.getSpaceID()).thenReturn(id1);
		when(ctx1.getDefaultSpace()).thenReturn(space1);
		AgentLife.getLife(this.agent).setDefaultContext(ctx1, mock(Address.class));

		assertFalse(this.skill.isDefaultSpace(space0));
		assertTrue(this.skill.isDefaultSpace(space1));
	}

	@Test
	public void isDefaultSpaceSpaceID() {
		Context ctx0 = mock(Context.class);
		OpenEventSpace space0 = mock(OpenEventSpace.class);
		SpaceID id0 = mock(SpaceID.class);
		when(space0.getSpaceID()).thenReturn(id0);
		when(ctx0.getDefaultSpace()).thenReturn(space0);
		Context ctx1 = mock(Context.class);
		OpenEventSpace space1 = mock(OpenEventSpace.class);
		SpaceID id1 = mock(SpaceID.class);
		when(space1.getSpaceID()).thenReturn(id1);
		when(ctx1.getDefaultSpace()).thenReturn(space1);
		AgentLife.getLife(this.agent).setDefaultContext(ctx1, mock(Address.class));

		assertFalse(this.skill.isDefaultSpace(id0));
		assertTrue(this.skill.isDefaultSpace(id1));
	}

	@Test
	public void isDefaultSpaceUUID() {
		Context ctx0 = mock(Context.class);
		OpenEventSpace space0 = mock(OpenEventSpace.class);
		UUID id0 = UUID.randomUUID();
		when(space0.getSpaceID()).thenReturn(new SpaceID(UUID.randomUUID(), id0, OpenEventSpaceSpecification.class));
		when(ctx0.getDefaultSpace()).thenReturn(space0);
		Context ctx1 = mock(Context.class);
		OpenEventSpace space1 = mock(OpenEventSpace.class);
		UUID id1 = UUID.randomUUID();
		when(space1.getSpaceID()).thenReturn(new SpaceID(UUID.randomUUID(), id1, OpenEventSpaceSpecification.class));
		when(ctx1.getDefaultSpace()).thenReturn(space1);
		AgentLife.getLife(this.agent).setDefaultContext(ctx1, mock(Address.class));

		assertFalse(this.skill.isDefaultSpace(id0));
		assertTrue(this.skill.isDefaultSpace(id1));
	}

	@Test
	public void isInDefaultSpace() {
		SpaceID id0 = mock(SpaceID.class);
		Address adr0 = new Address(id0, UUID.randomUUID());
		Event event0 = mock(Event.class);
		when(event0.getSource()).thenReturn(adr0);
		OpenEventSpace space0 = mock(OpenEventSpace.class);
		when(space0.getSpaceID()).thenReturn(id0);

		SpaceID id1 = mock(SpaceID.class);
		Address adr1 = new Address(id1, UUID.randomUUID());
		Event event1 = mock(Event.class);
		when(event1.getSource()).thenReturn(adr1);
		OpenEventSpace space1 = mock(OpenEventSpace.class);
		when(space1.getSpaceID()).thenReturn(id1);

		Context ctx0 = mock(Context.class);
		when(ctx0.getDefaultSpace()).thenReturn(space1);
		AgentLife.getLife(this.agent).setDefaultContext(ctx0, mock(Address.class));

		assertFalse(this.skill.isInDefaultSpace(event0));
		assertTrue(this.skill.isInDefaultSpace(event1));
	}

	@Test
	public void emit_noScope() {
		OpenEventSpace space = mock(OpenEventSpace.class);
		Context ctx = mock(Context.class);
		when(ctx.getDefaultSpace()).thenReturn(space);
		Event event = spy(new Event() {});
		AgentLife.getLife(this.agent).setDefaultContext(ctx, mock(Address.class));
		
		this.skill.emit(event);
		
		ArgumentCaptor<UUID> capturedSource = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Scope<Address>> capturedScope = ArgumentCaptor.forClass(Scope.class);
		verify(space).emit(capturedSource.capture(), capturedEvent.capture(), capturedScope.capture());
		assertEquals(this.agentId, capturedSource.getValue());
		assertSame(event, capturedEvent.getValue());
		assertNull(capturedScope.getValue());
		assertNotNull(event.getSource());
	}

	@Test
	public void emit_scope() {
		OpenEventSpace space = mock(OpenEventSpace.class);
		Context ctx = mock(Context.class);
		when(ctx.getDefaultSpace()).thenReturn(space);
		AgentLife.getLife(this.agent).setDefaultContext(ctx, mock(Address.class));
		Event event = spy(new Event() {});
		Scope<Address> scope = mock(Scope.class);
		
		this.skill.emit(event, scope);
		
		ArgumentCaptor<UUID> capturedSource = ArgumentCaptor.forClass(UUID.class);
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Scope<Address>> capturedScope = ArgumentCaptor.forClass(Scope.class);
		verify(space).emit(capturedSource.capture(), capturedEvent.capture(), capturedScope.capture());
		assertEquals(this.agentId, capturedSource.getValue());
		assertSame(event, capturedEvent.getValue());
		assertSame(scope, capturedScope.getValue());
		assertNotNull(event.getSource());
	}

	private static class MyAgent extends Agent {

		public MyAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}
		
	}

}
