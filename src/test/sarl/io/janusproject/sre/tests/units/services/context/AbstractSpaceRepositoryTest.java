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
package io.janusproject.sre.tests.units.services.context;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.arakhne.afc.util.IntegerList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.inject.Injector;

import io.janusproject.sre.services.context.SpaceRepository;
import io.janusproject.sre.services.context.SpaceRepository.SpaceDescription;
import io.janusproject.sre.services.context.SpaceRepositoryListener;
import io.janusproject.sre.services.executor.ExecutorService;
import io.janusproject.sre.tests.testutils.AbstractJanusTest;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.util.SynchronizedIterable;
import io.sarl.tests.api.ManualMocking;
import io.sarl.tests.api.Nullable;
import io.sarl.util.OpenEventSpace;
import io.sarl.util.OpenEventSpaceSpecification;
import io.sarl.util.RestrictedAccessEventSpace;
import io.sarl.util.RestrictedAccessEventSpaceSpecification;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
@ManualMocking
public abstract class AbstractSpaceRepositoryTest<T extends SpaceRepository> extends AbstractJanusTest {

	@Nullable
	protected Injector injector;

	@Mock
	protected ExecutorService executor;
	
	@Nullable
	protected Map<UUID, SpaceDescription> internalStructure;
	
	@Nullable
	protected T repository;

	@Before
	public void setUp() throws Exception {
		this.injector = Mockito.mock(Injector.class);
		MockitoAnnotations.initMocks(this);
		this.internalStructure = newInternalStructure();
		this.repository = newSpaceRepository();
	}

	protected abstract Map<UUID, SpaceDescription> newInternalStructure();

	protected abstract T newSpaceRepository();

	/** Fill the repository with three spaces for testing.
	 *
	 * @return the added descriptions.
	 */
	protected SpaceDescription[] fillRepository() {
		final UUID contextId = UUID.randomUUID();
		SpaceID sid;
		Space space;
		
		UUID id1 = UUID.randomUUID();
		sid = new SpaceID(contextId, id1, OpenEventSpaceSpecification.class);
		space = mock(OpenEventSpace.class);
		when(space.getSpaceID()).thenReturn(sid);
		SpaceDescription desc1 = new SpaceDescription(sid, space);
		this.internalStructure.put(id1, desc1);

		UUID id2 = UUID.randomUUID();
		sid = new SpaceID(contextId, id2, OpenEventSpaceSpecification.class);
		space = mock(OpenEventSpace.class);
		when(space.getSpaceID()).thenReturn(sid);
		SpaceDescription desc2 = new SpaceDescription(sid, space);
		this.internalStructure.put(id2, desc2);
		
		UUID id3 = UUID.randomUUID();
		sid = new SpaceID(contextId, id3, RestrictedAccessEventSpaceSpecification.class);
		space = mock(RestrictedAccessEventSpace.class);
		when(space.getSpaceID()).thenReturn(sid);
		SpaceDescription desc3 = new SpaceDescription(sid, space);
		this.internalStructure.put(id3, desc3);
		
		this.repository.refreshInternalStructures();

		if (id1.compareTo(id2) <= 0 && id1.compareTo(id3) <= 0) {
			if (id2.compareTo(id3) <= 0) {
				return new SpaceDescription[] {desc1, desc2, desc3};
			} else {
				return new SpaceDescription[] {desc1, desc3, desc2};
			}
		} else if (id2.compareTo(id1) <= 0 && id2.compareTo(id3) <= 0) {
			if (id1.compareTo(id3) <= 0) {
				return new SpaceDescription[] {desc2, desc1, desc3};
			} else {
				return new SpaceDescription[] {desc2, desc3, desc1};
			}
		} else {
			if (id1.compareTo(id2) <= 0) {
				return new SpaceDescription[] {desc3, desc1, desc2};
			} else {
				return new SpaceDescription[] {desc3, desc2, desc1};
			}
		}
	}
	
	@Test
	public void getSpaces_0() {
		SynchronizedIterable<? extends Space> spaces = this.repository.getSpaces();
		assertNotNull(spaces);
		assertFalse(spaces.iterator().hasNext());
	}

	@Test
	public void getSpaces_1() {
		SpaceDescription[] expectedDescs = fillRepository();
		SynchronizedIterable<? extends Space> spaces = this.repository.getSpaces();
		assertNotNull(spaces);
		Iterator<? extends Space> iterator = spaces.iterator();
		Space space = iterator.next();
		assertEquals(expectedDescs[0].getSpaceID(), space.getSpaceID());
		space = iterator.next();
		assertEquals(expectedDescs[1].getSpaceID(), space.getSpaceID());
		space = iterator.next();
		assertEquals(expectedDescs[2].getSpaceID(), space.getSpaceID());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void getSpacesClass_0() {
		SynchronizedIterable<? extends Space> spaces = this.repository.getSpaces(OpenEventSpaceSpecification.class);
		assertNotNull(spaces);
		assertFalse(spaces.iterator().hasNext());
	}

	@Test
	public void getSpacesClass_1() {
		SpaceDescription[] expectedDescs = fillRepository();
		IntegerList indexes = new IntegerList();
		for (int i = 0; i < expectedDescs.length; ++i) {
			SpaceDescription desc = expectedDescs[i];
			if (desc.getSpace() instanceof OpenEventSpace) {
				indexes.add(i);
			}
		}
		SynchronizedIterable<? extends Space> spaces = this.repository.getSpaces(OpenEventSpaceSpecification.class);
		assertNotNull(spaces);
		Iterator<? extends Space> iterator = spaces.iterator();
		while (!indexes.isEmpty() && iterator.hasNext()) {
			Space space = iterator.next();
			assertInstanceOf(OpenEventSpace.class, space);
			int idx = -1;
			for (int i = 0; idx == -1 && i < expectedDescs.length; ++i) {
				SpaceDescription desc = expectedDescs[i];
				if (desc.getSpaceID().equals(space.getSpaceID())) {
					idx = i;
				}
			}
			assertTrue(indexes.contains(idx));
			indexes.remove((Object) idx);
		}
		assertFalse(iterator.hasNext());
		assertTrue(indexes.isEmpty());
	}

	@Test
	public void getSpace_0() {
		UUID contextId = UUID.randomUUID();
		UUID id1 = UUID.randomUUID();
		SpaceID sid = new SpaceID(contextId, id1, OpenEventSpaceSpecification.class);
		assertNull(this.repository.getSpace(sid));
	}

	@Test
	public void getSpace_1() {
		SpaceDescription[] expectedDescs = fillRepository();
		for (SpaceDescription desc : expectedDescs) {
			Space space = this.repository.getSpace(desc.getSpaceID());
			assertNotNull(space);
			assertEquals(desc.getSpaceID(), space.getSpaceID());
			assertSame(desc.getSpace(), space);
		}
		UUID contextId = UUID.randomUUID();
		UUID id1 = UUID.randomUUID();
		SpaceID sid = new SpaceID(contextId, id1, OpenEventSpaceSpecification.class);
		assertNull(this.repository.getSpace(sid));
	}

	@Test
	public void createSpace() {
		UUID contextId = UUID.randomUUID();
		UUID id = UUID.randomUUID();
		SpaceID sid = new SpaceID(contextId, id, OpenEventSpaceSpecification.class);

		OpenEventSpace space = mock(OpenEventSpace.class);
		when(space.getSpaceID()).thenReturn(sid);
		OpenEventSpaceSpecification specification = mock(OpenEventSpaceSpecification.class);
		when(specification.create(any(), any())).thenReturn(space);
		when(this.injector.getInstance((Class) any())).thenReturn(specification);

		OpenEventSpace createdSpace = this.repository.createSpace(sid, OpenEventSpaceSpecification.class);
		
		assertSame(space, createdSpace);
		Space space2 = this.repository.getSpace(sid);
		assertSame(space, space2);
	}

	@Test
	public void removeSpace_0() {
		UUID contextId = UUID.randomUUID();
		UUID id = UUID.randomUUID();
		SpaceID sid = new SpaceID(contextId, id, OpenEventSpaceSpecification.class);
		Space space = mock(Space.class);
		when(space.getSpaceID()).thenReturn(sid);
		this.repository.removeSpace(space);
		Space space2 = this.repository.getSpace(sid);
		assertNull(space2);
	}

	@Test
	public void removeSpace_1() {
		SpaceDescription[] expectedDescs = fillRepository();
		for (SpaceDescription desc : expectedDescs) {
			this.repository.removeSpace(desc.getSpace());
			Space space = this.repository.getSpace(desc.getSpaceID());
			assertNull(space);
		}
	}

	@Test
	public void getOrCreateSpaceWithSpec_0() {
		UUID contextId = UUID.randomUUID();
		UUID id = UUID.randomUUID();
		SpaceID sid = new SpaceID(contextId, id, OpenEventSpaceSpecification.class);

		OpenEventSpace space = mock(OpenEventSpace.class);
		when(space.getSpaceID()).thenReturn(sid);
		OpenEventSpaceSpecification specification = mock(OpenEventSpaceSpecification.class);
		when(specification.create(any(), any())).thenReturn(space);
		when(this.injector.getInstance((Class) any())).thenReturn(specification);

		OpenEventSpace createdSpace = this.repository.getOrCreateSpaceWithSpec(sid, OpenEventSpaceSpecification.class);
		
		assertSame(space, createdSpace);
		Space space2 = this.repository.getSpace(sid);
		assertSame(space, space2);
	}

	@Test
	public void getOrCreateSpaceWithSpec_1() {
		SpaceDescription[] expectedDescs = fillRepository();
		for (SpaceDescription desc : expectedDescs) {
			Space createdSpace = this.repository.getOrCreateSpaceWithSpec(
					desc.getSpaceID(), (Class) desc.getSpaceID().getSpaceSpecification());
			assertNotNull(createdSpace);
			assertEquals(desc.getSpaceID().getSpaceSpecification(), createdSpace.getSpaceID().getSpaceSpecification());
		}
	}

	@Test
	public void getOrCreateSpaceWithID_0() {
		UUID contextId = UUID.randomUUID();
		UUID id = UUID.randomUUID();
		SpaceID sid = new SpaceID(contextId, id, OpenEventSpaceSpecification.class);

		OpenEventSpace space = mock(OpenEventSpace.class);
		when(space.getSpaceID()).thenReturn(sid);
		OpenEventSpaceSpecification specification = mock(OpenEventSpaceSpecification.class);
		when(specification.create(any(), any())).thenReturn(space);
		when(this.injector.getInstance((Class) any())).thenReturn(specification);

		OpenEventSpace createdSpace = this.repository.getOrCreateSpaceWithID(sid, OpenEventSpaceSpecification.class);
		
		assertSame(space, createdSpace);
		Space space2 = this.repository.getSpace(sid);
		assertSame(space, space2);
	}

	@Test
	public void getOrCreateSpaceWithID_1() {
		SpaceDescription[] expectedDescs = fillRepository();
		for (SpaceDescription desc : expectedDescs) {
			Space createdSpace = this.repository.getOrCreateSpaceWithID(
					desc.getSpaceID(), (Class) desc.getSpaceID().getSpaceSpecification());
			assertSame(desc.getSpace(), createdSpace);
		}
	}

	@Test
	public void addSpaceRepositoryListener_0() {
		SpaceRepositoryListener listener = mock(SpaceRepositoryListener.class);
		this.repository.addSpaceRepositoryListener(listener);
		
		UUID contextId = UUID.randomUUID();
		UUID id = UUID.randomUUID();
		SpaceID sid = new SpaceID(contextId, id, OpenEventSpaceSpecification.class);
		OpenEventSpace space = mock(OpenEventSpace.class);
		when(space.getSpaceID()).thenReturn(sid);
		OpenEventSpaceSpecification specification = mock(OpenEventSpaceSpecification.class);
		when(specification.create(any(), any())).thenReturn(space);
		when(this.injector.getInstance((Class) any())).thenReturn(specification);
		OpenEventSpace createdSpace = this.repository.getOrCreateSpaceWithID(sid, OpenEventSpaceSpecification.class);
		
		ArgumentCaptor<Space> spaceArg = ArgumentCaptor.forClass(Space.class);
		ArgumentCaptor<Boolean> creationArg = ArgumentCaptor.forClass(Boolean.class);
		verify(listener).spaceCreated(spaceArg.capture(), creationArg.capture());
		assertSame(space, spaceArg.getValue());
		assertTrue(creationArg.getValue());
	}

	@Test
	public void addSpaceRepositoryListener_1() {
		UUID contextId = UUID.randomUUID();
		UUID id = UUID.randomUUID();
		SpaceID sid = new SpaceID(contextId, id, OpenEventSpaceSpecification.class);
		OpenEventSpace space = mock(OpenEventSpace.class);
		when(space.getSpaceID()).thenReturn(sid);
		OpenEventSpaceSpecification specification = mock(OpenEventSpaceSpecification.class);
		when(specification.create(any(), any())).thenReturn(space);
		when(this.injector.getInstance((Class) any())).thenReturn(specification);
		OpenEventSpace createdSpace = this.repository.getOrCreateSpaceWithID(sid, OpenEventSpaceSpecification.class);
		
		SpaceRepositoryListener listener = mock(SpaceRepositoryListener.class);
		this.repository.addSpaceRepositoryListener(listener);
		
		this.repository.removeSpace(space);

		ArgumentCaptor<Space> spaceArg = ArgumentCaptor.forClass(Space.class);
		ArgumentCaptor<Boolean> destroyArg = ArgumentCaptor.forClass(Boolean.class);
		verify(listener).spaceDestroyed(spaceArg.capture(), destroyArg.capture());
		assertSame(space, spaceArg.getValue());
		assertTrue(destroyArg.getValue());
	}

	@Test
	public void removeSpaceRepositoryListener() {
		SpaceRepositoryListener listener = mock(SpaceRepositoryListener.class);
		this.repository.addSpaceRepositoryListener(listener);
		
		UUID contextId = UUID.randomUUID();
		UUID id = UUID.randomUUID();
		SpaceID sid = new SpaceID(contextId, id, OpenEventSpaceSpecification.class);
		OpenEventSpace space = mock(OpenEventSpace.class);
		when(space.getSpaceID()).thenReturn(sid);
		OpenEventSpaceSpecification specification = mock(OpenEventSpaceSpecification.class);
		when(specification.create(any(), any())).thenReturn(space);
		when(this.injector.getInstance((Class) any())).thenReturn(specification);

		this.repository.removeSpaceRepositoryListener(listener);
		
		OpenEventSpace createdSpace = this.repository.getOrCreateSpaceWithID(sid, OpenEventSpaceSpecification.class);
		
		verifyNoMoreInteractions(listener);
	}

}
