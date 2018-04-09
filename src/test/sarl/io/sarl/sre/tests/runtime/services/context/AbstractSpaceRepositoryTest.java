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

package io.sarl.sre.tests.runtime.services.context;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.List;
import java.util.UUID;

import org.junit.Test;

import io.sarl.core.DefaultContextInteractions;
import io.sarl.lang.SARLVersion;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.core.SpaceSpecification;
import io.sarl.lang.util.SynchronizedSet;
import io.sarl.sre.tests.testutils.AbstractSreRunTest;
import io.sarl.util.Collections3;
import io.sarl.util.OpenEventSpace;
import io.sarl.util.OpenEventSpaceSpecification;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public abstract class AbstractSpaceRepositoryTest extends AbstractSreRunTest {

	@Test
	public void createSpace() throws Exception {
		runSre(CreateSpaceTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertNotNull(results);
		assertContains(results, "FOUND");
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class CreateSpaceTestAgent extends TestingAgent {

		public CreateSpaceTestAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			UUID id = UUID.randomUUID();
			AgentContext ctx = getSkill(DefaultContextInteractions.class).getDefaultContext();
			OpenEventSpace space = ctx.createSpace(OpenEventSpaceSpecification.class, id);
			assert space != null;
			for (Space sp : ctx.getSpaces()) {
				if (id.equals(sp.getSpaceID().getID())) {
					addResult("FOUND");
				}
			}
			return true;
		}

	}

	@Test
	public void getOrCreateSpaceWithID() throws Exception {
		runSre(GetOrCreateSpaceWithIDTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertNotNull(results);
		assertContains(results, "FOUND1", "FOUND2");
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class GetOrCreateSpaceWithIDTestAgent extends TestingAgent {

		public GetOrCreateSpaceWithIDTestAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			UUID id = UUID.randomUUID();
			AgentContext ctx = getSkill(DefaultContextInteractions.class).getDefaultContext();
			OpenEventSpace space = ctx.getOrCreateSpaceWithID(OpenEventSpaceSpecification.class, id);
			assert space != null;
			for (Space sp : ctx.getSpaces()) {
				if (id.equals(sp.getSpaceID().getID())) {
					addResult("FOUND1");
				}
			}
			OpenEventSpace space2 = ctx.getOrCreateSpaceWithID(OpenEventSpaceSpecification.class, id);
			assert space != null;
			for (Space sp : ctx.getSpaces()) {
				if (id.equals(sp.getSpaceID().getID())) {
					addResult("FOUND2");
				}
			}
			return true;
		}

	}

	@Test
	public void getOrCreateSpaceWithSpec() throws Exception {
		runSre(GetOrCreateSpaceWithSpecTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertNotNull(results);
		assertContains(results, "FOUND2", "IS_DEFAULT_SPACE");
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class GetOrCreateSpaceWithSpecTestAgent extends TestingAgent {

		public GetOrCreateSpaceWithSpecTestAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			UUID id = UUID.randomUUID();
			AgentContext ctx = getSkill(DefaultContextInteractions.class).getDefaultContext();
			OpenEventSpace space = ctx.getOrCreateSpaceWithSpec(OpenEventSpaceSpecification.class, id);
			assert space != null;
			if (ctx.getDefaultSpace().getSpaceID().equals(space.getSpaceID())) {
				addResult("IS_DEFAULT_SPACE");
			}
			for (Space sp : ctx.getSpaces()) {
				if (id.equals(sp.getSpaceID().getID())) {
					addResult("FOUND1");
				}
			}
			OpenEventSpace space2 = ctx.getOrCreateSpaceWithSpec(OpenEventSpaceSpecification.class, id);
			assert space != null;
			assertSame(space, space2);
			MySpace space3 = ctx.getOrCreateSpaceWithSpec(MySpaceSpec.class, id);
			for (Space sp : ctx.getSpaces()) {
				if (id.equals(sp.getSpaceID().getID())) {
					addResult("FOUND2");
				}
			}
			MySpace space4 = ctx.getOrCreateSpaceWithSpec(MySpaceSpec.class, id);
			assertSame(space3, space4);
			return true;
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class MySpaceSpec implements SpaceSpecification<MySpace> {

		@Override
		public MySpace create(SpaceID id, Object... params) {
			return new MySpace(id);
		}
		
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class MySpace implements Space {

		private final SpaceID id;

		MySpace(SpaceID id) {
			this.id = id;
		}
		
		@Override
		@Deprecated
		public SpaceID getID() {
			return this.id;
		}

		@Override
		public SpaceID getSpaceID() {
			return this.id;
		}

		@Override
		public SynchronizedSet<UUID> getParticipants() {
			return Collections3.emptySynchronizedSet();
		}
		
	}

	@Test
	public void getSpace() throws Exception {
		runSre(GetSpaceTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertNotNull(results);
		assertContains(results, "FOUND", "IS_DEFAULT_SPACE");
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class GetSpaceTestAgent extends TestingAgent {

		public GetSpaceTestAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			AgentContext ctx = getSkill(DefaultContextInteractions.class).getDefaultContext();
			OpenEventSpace space = ctx.getSpace(ctx.getDefaultSpace().getSpaceID().getID());
			assert space != null;
			if (ctx.getDefaultSpace().getSpaceID().equals(space.getSpaceID())) {
				addResult("IS_DEFAULT_SPACE");
			}
			UUID id = UUID.randomUUID();
			ctx.createSpace(OpenEventSpaceSpecification.class, id);
			for (Space sp : ctx.getSpaces()) {
				if (id.equals(sp.getSpaceID().getID())) {
					addResult("FOUND");
				}
			}
			return true;
		}

	}

	@Test
	public void getSpaces() throws Exception {
		runSre(GetSpacesTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertNotNull(results);
		assertContains(results, "1/OpenEventSpaceSpecification", "2/OpenEventSpaceSpecification", "2/MySpaceSpec");
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class GetSpacesTestAgent extends TestingAgent {

		public GetSpacesTestAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			AgentContext ctx = getSkill(DefaultContextInteractions.class).getDefaultContext();
			for (Space sp : ctx.getSpaces()) {
				addResult("1/" + sp.getSpaceID().getSpaceSpecification().getSimpleName());
			}
			ctx.createSpace(MySpaceSpec.class, UUID.randomUUID());
			for (Space sp : ctx.getSpaces()) {
				addResult("2/" + sp.getSpaceID().getSpaceSpecification().getSimpleName());
			}
			return true;
		}

	}

	@Test
	public void getSpacesClass() throws Exception {
		runSre(GetSpacesClassTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertNotNull(results);
		assertContains(results, "1/OpenEventSpaceSpecification", "2/OpenEventSpaceSpecification",
				"3/MySpaceSpec", "3/MySpaceSpec");
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class GetSpacesClassTestAgent extends TestingAgent {

		public GetSpacesClassTestAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			AgentContext ctx = getSkill(DefaultContextInteractions.class).getDefaultContext();
			for (Space sp : ctx.getSpaces(OpenEventSpaceSpecification.class)) {
				addResult("1/" + sp.getSpaceID().getSpaceSpecification().getSimpleName());
			}
			ctx.createSpace(MySpaceSpec.class, UUID.randomUUID());
			for (Space sp : ctx.getSpaces(OpenEventSpaceSpecification.class)) {
				addResult("2/" + sp.getSpaceID().getSpaceSpecification().getSimpleName());
			}
			ctx.createSpace(MySpaceSpec.class, UUID.randomUUID());
			for (Space sp : ctx.getSpaces(MySpaceSpec.class)) {
				addResult("3/" + sp.getSpaceID().getSpaceSpecification().getSimpleName());
			}
			return true;
		}

	}

}
