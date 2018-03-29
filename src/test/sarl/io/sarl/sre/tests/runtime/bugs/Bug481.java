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

package io.sarl.sre.tests.runtime.bugs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

import io.sarl.core.DefaultContextInteractions;
import io.sarl.core.Lifecycle;
import io.sarl.lang.SARLVersion;
import io.sarl.lang.annotation.PerceptGuardEvaluator;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.Event;
import io.sarl.sre.tests.testutils.AbstractJanusRunTest;

/**
 * Unit test for the issue #481: Spawn more than one agent at the same time.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @see https://github.com/sarl/sarl/issues/481
 */
@SuppressWarnings("all")
public class Bug481 extends AbstractJanusRunTest {

	private static final boolean LOG = false;
	
	private static final int NB_AGENTS = 50;

	@Test
	public void spawnSubAgents() throws Exception {
		runJanus(SpawnerAgent.class, false, true, STANDARD_TIMEOUT);
		assertEquals(NB_AGENTS, getNumberOfResults());
	}

	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class SpawnerAgent extends TestingAgent {

		private final Set<UUID> agents = new HashSet<>();
		
		public SpawnerAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			Lifecycle skill = getSkill(Lifecycle.class);
			skill.spawn(NB_AGENTS, ChildAgent.class, getRawResults());
			return false;
		}

		@PerceptGuardEvaluator
		private void guardReadyEvent(ReadyEvent occurrence, Collection<Runnable> handlers) {
			assert occurrence != null;
			assert handlers != null;
			handlers.add(() -> onReadyEvent(occurrence));
		}

		private void onReadyEvent(ReadyEvent occurrence) {
			synchronized(this) {
				if (LOG) {
					System.err.println(getID() + " received event");
				}
				int size;
				synchronized (this) {
					this.agents.add(occurrence.getSource().getUUID());
					size = this.agents.size();
				}
				if (LOG) {
					System.err.println(getID() + " received event, size = " + size);
				}
				if (size >= NB_AGENTS) {
					synchronized(this) {
						if (LOG) {
							System.err.println("results = " + this.agents);
						}
						for (UUID ag : this.agents) {
							addResult(ag);
						}
					}
					if (LOG) {
						System.err.println("Commit suicide");
					}
					getSkill(Lifecycle.class).killMe();
				}
			}
		}

	}

	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class ChildAgent extends TestingAgent {

		public ChildAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			DefaultContextInteractions skill = getSkill(DefaultContextInteractions.class);
			skill.emit(new ReadyEvent());
			if (LOG) {
				System.err.println(getID() + " is sending event");
			}
			return true;
		}

	}

	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class ReadyEvent extends Event {
	}

}
