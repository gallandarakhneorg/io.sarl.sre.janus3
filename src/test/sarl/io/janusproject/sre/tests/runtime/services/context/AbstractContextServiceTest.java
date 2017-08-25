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
package io.janusproject.sre.tests.runtime.services.context;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import io.janusproject.sre.JanusConfig;
import io.janusproject.sre.Kernel;
import io.janusproject.sre.tests.testutils.AbstractJanusRunTest;
import io.janusproject.sre.tests.units.skills.LoggingSkillTest.TestAgent;
import io.sarl.core.AgentTask;
import io.sarl.core.Behaviors;
import io.sarl.core.DefaultContextInteractions;
import io.sarl.core.Destroy;
import io.sarl.core.ExternalContextAccess;
import io.sarl.core.Initialize;
import io.sarl.core.InnerContextAccess;
import io.sarl.core.Lifecycle;
import io.sarl.core.Schedules;
import io.sarl.lang.SARLVersion;
import io.sarl.lang.annotation.PerceptGuardEvaluator;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.Space;
import io.sarl.util.OpenEventSpace;
import io.sarl.util.OpenEventSpaceSpecification;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public abstract class AbstractContextServiceTest extends AbstractJanusRunTest {

	@Test
	public void getRootContext() throws Exception {
		runJanus(GetRootContextTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertEquals(1, results.size());
		if (results.get(0) instanceof Throwable) {
			throw new Exception((Throwable) results.get(0));
		}
		assertInstanceOf(AgentContext.class, results.get(0));
		assertEquals(UUID.fromString(JanusConfig.DEFAULT_CONTEXT_ID_VALUE), ((AgentContext) results.get(0)).getID());
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
		runJanus(GetContextTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertEquals(2, results.size());
		assertInstanceOf(UUID.class, results.get(0));
		UUID agentId = (UUID) results.get(0);
		assertInstanceOf(AgentContext.class, results.get(1));
		assertEquals(UUID.fromString(JanusConfig.DEFAULT_CONTEXT_ID_VALUE), ((AgentContext) results.get(1)).getID());
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
			AgentContext ctx = getSkill(ExternalContextAccess.class).getContext(UUID.fromString(JanusConfig.DEFAULT_CONTEXT_ID_VALUE));
			addResult(getID());
			addResult(ctx);
			return true;
		}

	}

}
