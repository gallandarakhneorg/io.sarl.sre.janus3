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
import static org.junit.Assert.assertSame;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Injector;

import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.EventSpaceSpecification;
import io.sarl.lang.core.SpaceID;
import io.sarl.sre.spaces.JanusOpenEventSpaceSpecification;
import io.sarl.sre.tests.testutils.AbstractJanusTest;
import io.sarl.tests.api.ManualMocking;
import io.sarl.tests.api.Nullable;
import io.sarl.util.OpenEventSpace;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
//@SuppressWarnings("all")
@ManualMocking
public class JanusOpenEventSpaceSpecificationTest extends AbstractJanusTest {

	@Nullable
	private SpaceID spaceId;

	@Mock
	private Injector injector;

	@InjectMocks
	private JanusOpenEventSpaceSpecification specification;

	@Before
	public void setUp() {
		this.spaceId = new SpaceID(UUID.randomUUID(), UUID.randomUUID(), EventSpaceSpecification.class);
		MockitoAnnotations.initMocks(this);
		this.specification = new JanusOpenEventSpaceSpecification(this.injector);
	}

	@Test
	public void create() {
		EventSpace space = this.specification.create(this.spaceId, "a", "b", "c"); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
		assertNotNull(space);
		assertInstanceOf(OpenEventSpace.class, space);
		assertSame(this.spaceId, space.getSpaceID());
	}

}
