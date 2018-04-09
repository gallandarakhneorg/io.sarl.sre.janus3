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

package io.sarl.sre.tests.runtime.bugs;

import java.util.UUID;

import org.junit.Test;

import io.sarl.core.Lifecycle;
import io.sarl.lang.SARLVersion;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.sre.Kernel;
import io.sarl.sre.tests.testutils.AbstractSreRunTest;

/**
 * Unit test for the issue #84: Problem with calling killMe in Initialize behavior of an agent.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @see https://github.com/janus-project/janusproject/issues/84
 */
@SuppressWarnings("all")
public class OldJanusBug84 extends AbstractSreRunTest {

	@Test
	public void killMeInInit() throws Exception {
		Kernel kern = runSre(KilledInInitAgent.class, false, true, STANDARD_TIMEOUT);
		assertNoErrorLog(kern);
		assertContains(getResults());
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class KilledInInitAgent extends TestingAgent {

		public KilledInInitAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			getSkill(Lifecycle.class).killMe();
			return false;
		}

	}

}
