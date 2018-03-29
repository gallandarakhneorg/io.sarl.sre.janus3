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
package io.sarl.sre.tests.runtime;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import io.sarl.core.DefaultContextInteractions;
import io.sarl.lang.SARLVersion;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.sre.Boot;
import io.sarl.sre.JanusConfig;
import io.sarl.sre.tests.testutils.AbstractJanusRunTest;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class BootTest extends AbstractJanusRunTest {

	@Nullable
	private UUID defaultID;

	@Nullable
	private UUID bootID;

	@Before
	public void setUp() {
		this.defaultID = UUID.fromString(JanusConfig.DEFAULT_CONTEXT_ID_VALUE);
		this.bootID = UUID.nameUUIDFromBytes(BootAgent.class.getName().getBytes());
	}

	@Test
	public void defaultContextUUID() throws Exception {
		Boot.setDefaultContextUUID();
		runJanus(BootAgent.class, false, true, STANDARD_TIMEOUT);
		UUID id = getResult(UUID.class, 0);
		assertNotNull(id);
		assertEquals(defaultID, id);
	}

	@Test
	public void bootContextUUID() throws Exception {
		Boot.setBootAgentTypeContextUUID();
		runJanus(BootAgent.class, false, true, STANDARD_TIMEOUT);
		UUID id = getResult(UUID.class, 0);
		assertNotNull(id);
		assertEquals(bootID, id);
	}

	@Test
	public void randomContextUUID() throws Exception {
		Boot.setRandomContextUUID();
		runJanus(BootAgent.class, false, true, STANDARD_TIMEOUT);
		UUID id = getResult(UUID.class, 0);
		assertNotNull(id);
		assertNotEquals(defaultID, id);
		assertNotEquals(bootID, id);
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class BootAgent extends TestingAgent {

		public BootAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			addResult(getSkill(DefaultContextInteractions.class).getDefaultContext().getID());
			return true;
		}

	}

}
