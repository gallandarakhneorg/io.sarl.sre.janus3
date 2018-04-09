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

import java.util.List;
import java.util.UUID;

import org.junit.Test;

import io.sarl.core.ExternalContextAccess;
import io.sarl.lang.SARLVersion;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.AgentContext;
import io.sarl.sre.boot.factories.BootFactory;
import io.sarl.sre.tests.testutils.AbstractSreRunTest;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public abstract class AbstractContextServiceTest extends AbstractSreRunTest {

	@Test
	public void getRootContext() throws Exception {
		runSre(GetRootContextTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertEquals(1, results.size());
		if (results.get(0) instanceof Throwable) {
			throw new Exception((Throwable) results.get(0));
		}
		assertInstanceOf(AgentContext.class, results.get(0));
		assertEquals(UUID.fromString(BootFactory.ROOT_CONTEXT_ID_VALUE), ((AgentContext) results.get(0)).getID());
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class GetRootContextTestAgent extends TestingAgent {

		public GetRootContextTestAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			AgentContext ctx = getSkill(ExternalContextAccess.class).getUniverseContext();
			addResult(ctx);
			return true;
		}

	}

	@Test
	public void getContext() throws Exception {
		runSre(GetContextTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertEquals(2, results.size());
		assertInstanceOf(UUID.class, results.get(0));
		UUID agentId = (UUID) results.get(0);
		assertInstanceOf(AgentContext.class, results.get(1));
		assertEquals(UUID.fromString(BootFactory.ROOT_CONTEXT_ID_VALUE), ((AgentContext) results.get(1)).getID());
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class GetContextTestAgent extends TestingAgent {

		public GetContextTestAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			AgentContext ctx = getSkill(ExternalContextAccess.class).getContext(UUID.fromString(BootFactory.ROOT_CONTEXT_ID_VALUE));
			addResult(getID());
			addResult(ctx);
			return true;
		}

	}

}
