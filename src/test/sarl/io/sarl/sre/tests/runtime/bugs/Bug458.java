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

import java.util.Collection;
import java.util.UUID;

import org.junit.Test;

import io.sarl.core.Behaviors;
import io.sarl.core.DefaultContextInteractions;
import io.sarl.core.Destroy;
import io.sarl.core.Lifecycle;
import io.sarl.core.Schedules;
import io.sarl.lang.SARLVersion;
import io.sarl.lang.annotation.PerceptGuardEvaluator;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Behavior;
import io.sarl.lang.core.Event;
import io.sarl.sre.tests.testutils.AbstractSreRunTest;

/**
 * Unit test for the issue #458: Thread deadlock problem in agent's destroy function
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @see https://github.com/sarl/sarl/issues/458
 */
@SuppressWarnings("all")
public class Bug458 extends AbstractSreRunTest {

	@Test
	public void ExceptionInInit() throws Exception {
		runSre(KillMeInBehaviorAgent.class, false, true, STANDARD_TIMEOUT);
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class FakeEvent extends Event {

		public FakeEvent() {
			super();
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class KillMeBehavior extends Behavior {

		public KillMeBehavior(Agent agent) {
			super(agent);
		}

		@PerceptGuardEvaluator
		private void $guardEvaluator$FakeEvent(final FakeEvent occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
			assert occurrence != null;
			assert ___SARLlocal_runnableCollection != null;
			___SARLlocal_runnableCollection.add(() -> $behaviorUnit$FakeEvent$0(occurrence));
		}

		private void $behaviorUnit$FakeEvent$0(final FakeEvent occurrence) {
			getSkill(Lifecycle.class).killMe();
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class KillMeInBehaviorAgent extends TestingAgent {

		private KillMeBehavior behavior;
		
		public KillMeInBehaviorAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			this.behavior = new KillMeBehavior(this);
			getSkill(Behaviors.class).registerBehavior(this.behavior);
			getSkill(Schedules.class).in(500, (agent) -> {
				getSkill(DefaultContextInteractions.class).emit(new FakeEvent());
			});
			return false;
		}

		@PerceptGuardEvaluator
		private void guardDestroy(Destroy occurrence, Collection<Runnable> handlers) {
			assert occurrence != null;
			assert handlers != null;
			handlers.add(() -> onDestroy(occurrence));
		}

		private void onDestroy(Destroy occurrence) {
			getSkill(Behaviors.class).unregisterBehavior(this.behavior);
		}

	}

}
