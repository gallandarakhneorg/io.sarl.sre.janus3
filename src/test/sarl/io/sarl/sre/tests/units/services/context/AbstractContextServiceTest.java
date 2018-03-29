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
package io.sarl.sre.tests.units.services.context;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import io.sarl.sre.services.context.AbstractContextService;
import io.sarl.sre.services.context.JanusContext;
import io.sarl.lang.core.SpaceID;
import io.sarl.sre.tests.testutils.AbstractJanusTest;
import io.sarl.tests.api.Nullable;
import io.sarl.util.OpenEventSpace;
import io.sarl.util.OpenEventSpaceSpecification;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public abstract class AbstractContextServiceTest<T extends AbstractContextService> extends AbstractJanusTest {

	@Nullable
	protected JanusContext context;

	@Nullable
	protected JanusContext rootContext;

	@Nullable
	protected UUID rootContextId;

	@Nullable
	protected UUID rootSpaceId;

	@Nullable
	protected T service;
	
	@Before
	public void setUp() {
		this.rootContextId = UUID.randomUUID();
		this.rootContext = mock(JanusContext.class);
		when(this.rootContext.getID()).thenReturn(this.rootContextId);
		this.rootSpaceId = UUID.randomUUID();
		OpenEventSpace space = mock(OpenEventSpace.class);
		when(space.getSpaceID()).thenReturn(new SpaceID(this.rootContextId, this.rootSpaceId, OpenEventSpaceSpecification.class));
		when(this.rootContext.getDefaultSpace()).thenReturn(space);
		this.context = mock(JanusContext.class);
		this.service = newService();
		this.service.setRootContext(this.rootContext);
	}

	protected abstract T newService();

	protected void startService() {
		startServiceManually(this.service);
	}

	@Test
	public abstract void getServiceDependencies();

	@Test
	public void createContextWithoutRegistration_root() {
		UUID contextId = UUID.randomUUID();
		UUID spaceId = UUID.randomUUID();
		JanusContext context = this.service.createContextWithoutRegistration(contextId, spaceId, true);

		assertSame(this.context, context);
		assertEquals(contextId, context.getID());
		assertTrue(context.isRootContext());
		
		assertTrue(context != this.service.getRootContext());
		assertNull(this.service.getContext(contextId));
	}

	@Test
	public void createContextWithoutRegistration_notRoot() {
		UUID contextId = UUID.randomUUID();
		UUID spaceId = UUID.randomUUID();
		JanusContext context = this.service.createContextWithoutRegistration(contextId, spaceId, false);

		assertSame(this.context, context);
		assertEquals(contextId, context.getID());
		assertFalse(context.isRootContext());
		
		assertTrue(context != this.service.getRootContext());
		assertNull(this.service.getContext(contextId));
	}

	@Test
	public void createContext_root() {
		JanusContext context = this.service.createContext(this.rootContextId, this.rootSpaceId);
		assertSame(this.rootContext, context);
	}

	@Test
	public void createContext_newContext() {
		UUID contextId = UUID.randomUUID();
		UUID spaceId = UUID.randomUUID();
	
		JanusContext context = this.service.createContext(contextId, spaceId);
		
		assertSame(this.context, context);
		assertEquals(contextId, context.getID());
		assertFalse(context.isRootContext());

		assertTrue(context != this.service.getRootContext());
		assertSame(this.context, this.service.getContext(contextId));
	}

	@Test
	public void createContext_existingContext() {
		UUID contextId = UUID.randomUUID();
		UUID spaceId = UUID.randomUUID();
	
		JanusContext originalContext = this.service.createContext(contextId, spaceId);
		
		JanusContext context = this.service.createContext(contextId, spaceId);

		assertSame(originalContext, context);
		assertSame(this.context, context);
		assertEquals(contextId, context.getID());
		assertFalse(context.isRootContext());

		assertTrue(context != this.service.getRootContext());
		assertSame(this.context, this.service.getContext(contextId));
	}

	@Test
	public void getContext_rootContext() {
		assertSame(this.rootContext, this.service.getContext(this.rootContextId));
	}

	@Test
	public void getContext_otherContext() {
		UUID contextId = UUID.randomUUID();
		UUID spaceId = UUID.randomUUID();
		
		assertNull(this.service.getContext(contextId));
		
		JanusContext context = this.service.createContext(contextId, spaceId);
		assertSame(context, this.service.getContext(contextId));
	}

	@Test
	public void removeContext_emptyRepository() {
		assertNull(this.service.removeContext(UUID.randomUUID()));
	}

	@Test
	public void removeContext_rootContext() {
		assertNull(this.service.removeContext(this.rootContextId));
	}

	@Test
	public void removeContext_unknownContext() {
		UUID contextId = UUID.randomUUID();
		UUID spaceId = UUID.randomUUID();
		this.service.createContext(contextId, spaceId);

		assertNull(this.service.removeContext(UUID.randomUUID()));
	}

	@Test
	public void removeContext_knownContext() {
		UUID contextId = UUID.randomUUID();
		UUID spaceId = UUID.randomUUID();
		JanusContext context = this.service.createContext(contextId, spaceId);

		assertSame(context, this.service.removeContext(contextId));
	}

}
