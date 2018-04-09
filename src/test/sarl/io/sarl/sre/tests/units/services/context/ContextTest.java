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

package io.sarl.sre.tests.units.services.context;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Provider;

import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.sre.services.context.Context;
import io.sarl.sre.services.context.SpaceRepository;
import io.sarl.sre.services.context.SpaceRepositoryListener;
import io.sarl.sre.services.context.SpaceRepositoryListenerFactory;
import io.sarl.sre.services.logging.LoggingService;
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
public class ContextTest extends AbstractSreTest {

	private boolean root;

	@Nullable
	private UUID contextId;

	@Nullable
	private UUID spaceId;

	@Nullable
	private Context context;

	@Nullable
	private SpaceRepository repository;
	
	@Nullable
	private LoggingService logging;

	@Before
	public void setUp() {
		this.logging = mock(LoggingService.class);
		this.contextId = UUID.randomUUID();
		this.spaceId = UUID.randomUUID();
		this.root = Math.random() <= 0.5;
		this.context = new Context(this.contextId, this.spaceId, this.root);
		this.repository = mock(SpaceRepository.class);
		Provider<SpaceRepository> provider = () -> {
			return this.repository;
		};
		this.context.setRepositoryProvider(provider);
		this.context.setLoggingService(this.logging);
		this.context.setSpaceRepositiroyListenerFactory(new SpaceRepositoryListenerFactory() {
			@Override
			public SpaceRepositoryListener create(UUID contextID, EventSpace defaultSpace, Logger logger) {
				return mock(SpaceRepositoryListener.class);
			}
		});
	}

	@Test
	public void getID() {
		assertSame(this.contextId, this.context.getID());
	}

	@Test
	public void isRootContext() {
		assertEquals(this.root, this.context.isRootContext());
	}

	@Test
	public void getDefaultSpace() {
		final OpenEventSpace ospace = mock(OpenEventSpace.class);
		when(this.repository.getOrCreateSpaceWithID(any(), any(), any())).thenAnswer((it) -> {
			if (((SpaceID) it.getArgument(0)).getID().equals(this.spaceId)) {
				return ospace; 
			}
			return null;
		});
		//
		OpenEventSpace space = this.context.getDefaultSpace();
		assertNotNull(space);
		assertSame(ospace, space);
	}

	@Test
	public void getSpace_0() {
		OpenEventSpace ospace = mock(OpenEventSpace.class);
		when(this.repository.getOrCreateSpaceWithID(any(), any(), any())).thenAnswer((it) -> {
			if (((SpaceID) it.getArgument(0)).getID().equals(this.spaceId)) {
				return ospace; 
			}
			return null;
		});
		//
		Space space = this.context.getSpace(this.spaceId);
		assertNotNull(space);
		assertSame(ospace, space);
	}

	@Test
	public void getSpace_1() {
		OpenEventSpace ospace = mock(OpenEventSpace.class);
		Space xspace = mock(Space.class);
		when(this.repository.getOrCreateSpaceWithID(any(), any(), any())).thenAnswer((it) -> {
			if (((SpaceID) it.getArgument(0)).getID().equals(this.spaceId)) {
				return ospace; 
			}
			return null;
		});
		when(this.repository.getSpace(any())).thenAnswer((it) -> {
			if (((SpaceID) it.getArgument(0)).getID().equals(this.spaceId)) {
				return ospace; 
			}
			return xspace;
		});
		//
		Space space = this.context.getSpace(UUID.randomUUID());
		assertNotNull(space);
		assertSame(xspace, space);
	}

	@Test
	public void createSpace_createDefaultSpace() {
		final OpenEventSpace ospace = mock(OpenEventSpace.class);
		when(this.repository.getOrCreateSpaceWithID(any(), any(), any())).thenAnswer((it) -> {
			if (((SpaceID) it.getArgument(0)).getID().equals(this.spaceId)) {
				return ospace; 
			}
			return null;
		});
		//
		Space space = this.context.createSpace(OpenEventSpaceSpecification.class, this.spaceId);
		assertNotNull(space);
		assertSame(ospace, space);
	}

	@Test
	public void getOrCreateSpaceWithSpec_createDefaultSpace() {
		final OpenEventSpace ospace = mock(OpenEventSpace.class);
		when(this.repository.getOrCreateSpaceWithID(any(), any(), any())).thenAnswer((it) -> {
			if (((SpaceID) it.getArgument(0)).getID().equals(this.spaceId)) {
				return ospace; 
			}
			return null;
		});
		//
		Space space = this.context.getOrCreateSpaceWithSpec(OpenEventSpaceSpecification.class, this.spaceId);
		assertNotNull(space);
		assertSame(ospace, space);
	}

	@Test
	public void getOrCreateSpaceWithID_createDefaultSpace() {
		final OpenEventSpace ospace = mock(OpenEventSpace.class);
		when(this.repository.getOrCreateSpaceWithID(any(), any(), any())).thenAnswer((it) -> {
			if (((SpaceID) it.getArgument(0)).getID().equals(this.spaceId)) {
				return ospace; 
			}
			return null;
		});
		//
		Space space = this.context.getOrCreateSpaceWithID(OpenEventSpaceSpecification.class, this.spaceId);
		assertNotNull(space);
		assertSame(ospace, space);
	}

}
