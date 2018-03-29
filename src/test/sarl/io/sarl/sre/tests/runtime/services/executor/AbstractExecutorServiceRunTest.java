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
package io.sarl.sre.tests.runtime.services.executor;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import io.sarl.sre.Boot;
import io.sarl.core.AgentTask;
import io.sarl.core.Destroy;
import io.sarl.core.Schedules;
import io.sarl.lang.SARLVersion;
import io.sarl.lang.annotation.PerceptGuardEvaluator;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.sre.tests.testutils.AbstractJanusRunTest;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public abstract class AbstractExecutorServiceRunTest extends AbstractJanusRunTest {

	@Test
	public void execute() throws Exception {
		runJanus(ExecuteAgent.class, false, true, 60);
		List<Object> results = getResults();
		assertNotNull(results);
		assertNumberOfResults(1);
		assertInstanceOf(UUID.class, results.get(0));
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class ExecuteAgent extends TestingAgent {

		public ExecuteAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			getSkill(Schedules.class).execute((it) -> {
				addResult(UUID.randomUUID());
				forceKillMe();
			});
			return false;
		}

	}

	@Test
	public void in() throws Exception {
		runJanus(InAgent.class, false, true, 60);
		List<Object> results = getResults();
		assertNotNull(results);
		assertNumberOfResults(1);
		assertInstanceOf(UUID.class, results.get(0));
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class InAgent extends TestingAgent {

		public InAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			getSkill(Schedules.class).in(2000, (it) -> {
				addResult(UUID.randomUUID());
				forceKillMe();
			});
			return false;
		}

	}

	@Test
	public void every() throws Exception {
		runJanus(EveryAgent.class, false, true, 60);
		List<Object> results = getResults();
		assertNotNull(results);
		assertNumberOfResults(1);
		assertEquals(2, results.get(0));
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class EveryAgent extends TestingAgent {

		private volatile int nb = 0;
		
		public EveryAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			getSkill(Schedules.class).every(1000, (it) -> {
				nb++;
				if (nb == 2) {
					forceKillMe();
				}
			});
			return false;
		}

		@PerceptGuardEvaluator
		private void guardDestroy(final Destroy occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
			assert occurrence != null;
			assert ___SARLlocal_runnableCollection != null;
			___SARLlocal_runnableCollection.add(() -> onDestroy(occurrence));
		}

		/**
		 * Invoked at the start of the agent.
		 * 
		 * @param occurrence - the initialization event.
		 */
		private void onDestroy(final Destroy occurrence) {
			addResult(this.nb);
		}

	}

	@Test
	public void atFixedDelay() throws Exception {
		runJanus(AtFixedDelayAgent.class, false, true, 60);
		List<Object> results = getResults();
		assertNotNull(results);
		assertNumberOfResults(1);
		assertEquals(2, results.get(0));
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class AtFixedDelayAgent extends TestingAgent {

		private volatile int nb = 0;
		
		public AtFixedDelayAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			getSkill(Schedules.class).atFixedDelay(1000, (it) -> {
				nb++;
				if (nb == 2) {
					forceKillMe();
				}
			});
			return false;
		}

		@PerceptGuardEvaluator
		private void guardDestroy(final Destroy occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
			assert occurrence != null;
			assert ___SARLlocal_runnableCollection != null;
			___SARLlocal_runnableCollection.add(() -> onDestroy(occurrence));
		}

		/**
		 * Invoked at the start of the agent.
		 * 
		 * @param occurrence - the initialization event.
		 */
		private void onDestroy(final Destroy occurrence) {
			addResult(this.nb);
		}

	}

	@Test
	public void getActiveTasks() throws Exception {
		runJanus(GetActiveTasksAgent.class, false, true, 60);
		assertNumberOfResults(3);
		List<String> actives1 = getResult(List.class, 0);
		assertContains(actives1, "T1");
		List<String> actives2 = getResult(List.class, 1);
		assertContains(actives2, "T1", "T2");
		List<String> actives3 = getResult(List.class, 2);
		assertContains(actives3);
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class GetActiveTasksAgent extends TestingAgent {
		
		public GetActiveTasksAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			Schedules sch = getSkill(Schedules.class);
			AgentTask t = sch.every(500, (it) -> {});
			sch.setName(t, "T1");
			addResult(new ArrayList<>(sch.getActiveTasks()));
			t = sch.in(1000, (it) -> {
				forceKillMe();
			});
			sch.setName(t, "T2");
			addResult(new ArrayList<>(sch.getActiveTasks()));
			return false;
		}

		@PerceptGuardEvaluator
		private void guardDestroy(final Destroy occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
			assert occurrence != null;
			assert ___SARLlocal_runnableCollection != null;
			___SARLlocal_runnableCollection.add(() -> onDestroy(occurrence));
		}

		/**
		 * Invoked at the start of the agent.
		 * 
		 * @param occurrence - the initialization event.
		 */
		private void onDestroy(final Destroy occurrence) {
			addResult(new ArrayList<>(getSkill(Schedules.class).getActiveTasks()));
		}

	}

	@Test
	public void cancel() throws Exception {
		runJanus(CancelTaskAgent.class, false, true, 60);
		assertNumberOfResults(4);
		List<String> actives1 = getResult(List.class, 0);
		assertContains(actives1, "T1");
		List<String> actives2 = getResult(List.class, 1);
		assertContains(actives2, "T1", "T2");
		List<String> actives3 = getResult(List.class, 2);
		assertContains(actives3, "T2");
		List<String> actives4 = getResult(List.class, 3);
		assertContains(actives4);
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class CancelTaskAgent extends TestingAgent {
		
		public CancelTaskAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			Schedules s = getSkill(Schedules.class);
			AgentTask t1 = s.every(500, (it) -> {});
			s.setName(t1, "T1");
			addResult(new ArrayList<>(s.getActiveTasks()));
			AgentTask t2 = s.in(1000, (it) -> {
				forceKillMe();
			});
			s.setName(t2, "T2");
			addResult(new ArrayList<>(s.getActiveTasks()));
			s.cancel(t1);
			addResult(new ArrayList<>(s.getActiveTasks()));
			return false;
		}

		@PerceptGuardEvaluator
		private void guardDestroy(final Destroy occurrence, final Collection<Runnable> ___SARLlocal_runnableCollection) {
			assert occurrence != null;
			assert ___SARLlocal_runnableCollection != null;
			___SARLlocal_runnableCollection.add(() -> onDestroy(occurrence));
		}

		/**
		 * Invoked at the start of the agent.
		 * 
		 * @param occurrence - the initialization event.
		 */
		private void onDestroy(final Destroy occurrence) {
			addResult(new ArrayList<>(getSkill(Schedules.class).getActiveTasks()));
		}

	}

}
