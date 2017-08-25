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
package io.janusproject.sre.tests.runtime.services.lifecycle;

import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import io.janusproject.sre.tests.testutils.AbstractJanusRunTest;
import io.sarl.core.AgentTask;
import io.sarl.core.Behaviors;
import io.sarl.core.Destroy;
import io.sarl.core.InnerContextAccess;
import io.sarl.core.Lifecycle;
import io.sarl.core.Schedules;
import io.sarl.lang.SARLVersion;
import io.sarl.lang.annotation.PerceptGuardEvaluator;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Event;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public abstract class AbstractLifecycleServiceTest extends AbstractJanusRunTest {

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class FakeAgent extends TestingAgent {

		public FakeAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			addResult("SPAWNED");
			return true;
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class FakeAgent2 extends TestingAgent {

		public FakeAgent2(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			addResult("SPAWNED");
			return false;
		}

		@PerceptGuardEvaluator
		private void guardMyEvent(MyEvent occurrence, Collection<Runnable> handlers) {
			assert occurrence != null;
			assert handlers != null;
			handlers.add(() -> onMyEvent(occurrence));
		}
		
		private void onMyEvent(MyEvent occurrence) {
			addResult("KILLED");
			forceKillMe();
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class FakeAgent3 extends TestingAgent {

		public FakeAgent3(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			addResult("SPAWNED");
			getSkill(Schedules.class).in(500, (it) -> forceKillMe());
			return false;
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class MyEvent extends Event {
		//
	}

	@Test
	public void spawnAgent_1() throws Exception {
		runJanus(OneAgentSpawnTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertContains(results, "SPAWNED");
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class OneAgentSpawnTestAgent extends TestingAgent {

		public OneAgentSpawnTestAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			getSkill(Lifecycle.class).spawn(FakeAgent3.class, getRawResults());
			return true;
		}

	}

	@Test
	public void spawnAgent_3() throws Exception {
		runJanus(ThreeAgentSpawnTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertNotNull(results);
		assertEquals(3, results.size());
		assertContains(results, "SPAWNED", "SPAWNED", "SPAWNED");
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class ThreeAgentSpawnTestAgent extends TestingAgent {

		public ThreeAgentSpawnTestAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			getSkill(Lifecycle.class).spawn(3, FakeAgent3.class, getRawResults());
			return true;
		}

	}

	@Test
	public void killAgent_duringInitialize() throws Exception {
		runJanus(InitializeKillTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertContains(results, "1");
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class InitializeKillTestAgent extends TestingAgent {

		public InitializeKillTestAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			addResult("1");
			forceKillMe();
			addResult("2");
			return false;
		}

	}

	@Test
	public void killAgent_duringInitializeAfterSpawn() throws Exception {
		runJanus(InitializeSpawnKillTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertNotNull(results);
		assertEquals(3, results.size());
		assertContains(results, "1", "2", "SPAWNED");
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class InitializeSpawnKillTestAgent extends TestingAgent {

		public InitializeSpawnKillTestAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			addResult("1");
			getSkill(Lifecycle.class).spawn(FakeAgent.class, getRawResults());
			addResult("2");
			forceKillMe();
			addResult("3");
			return false;
		}

	}

	@Test
	public void killAgent_duringEventHandling() throws Exception {
		runJanus(EventHanlderKillTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertNotNull(results);
		assertEquals(2, results.size());
		assertContains(results, "1", "2");
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class EventHanlderKillTestAgent extends TestingAgent {

		public EventHanlderKillTestAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			addResult("1");
			getSkill(Behaviors.class).wake(new MyEvent());
			return false;
		}

		@PerceptGuardEvaluator
		private void guardMyEvent(MyEvent occurrence, Collection<Runnable> handlers) {
			assert occurrence != null;
			assert handlers != null;
			handlers.add(() -> onMyEvent(occurrence));
		}

		private void onMyEvent(MyEvent occurrence) {
			addResult("2");
			forceKillMe();
			addResult("3");
		}

	}

	@Test
	public void killAgent_duringTask() throws Exception {
		runJanus(TaskKillTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertNotNull(results);
		assertEquals(2, results.size());
		assertContains(results, "1", "2");
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class TaskKillTestAgent extends TestingAgent {

		public TaskKillTestAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			addResult("1");
			getSkill(Schedules.class).in(1000, (it) -> forceKillMe());
			addResult("2");
			return false;
		}

	}

	@Test
	public void killAgent_duringDestroy() throws Exception {
		runJanus(DestroyKillTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertNotNull(results);
		assertEquals(2, results.size());
		assertContains(results, "1", "2");
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class DestroyKillTestAgent extends TestingAgent {

		public DestroyKillTestAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			addResult("1");
			return true;
		}

		@PerceptGuardEvaluator
		public void guardDestroy(Destroy occurrence, Collection<Runnable> handlers) {
			assert occurrence != null;
			assert handlers != null;
			handlers.add(() -> onDestroy(occurrence));
		}

		public void onDestroy(Destroy occurrence) {
			addResult("2");
			forceKillMe();
			addResult("3");
		}

	}

	@Test
	public void isKillableAgent_noChild_noInnerContext() throws Exception {
		runJanus(NoChildNoInnerContextIsKillableTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertNotNull(results);
		assertEquals(2, results.size());
		assertContains(results, "1", "2");
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class NoChildNoInnerContextIsKillableTestAgent extends TestingAgent {

		public NoChildNoInnerContextIsKillableTestAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			addResult("1");
			getSkill(Schedules.class).in(1000, (it) -> {
				addResult("2");
				forceKillMe();
			});
			return false;
		}

	}

	@Test
	public void isKillableAgent_noChild_innerContext() throws Exception {
		runJanus(NoChildInnerContextIsKillableTestAgent.class, false, true, STANDARD_TIMEOUT);
		List<Object> results = getResults();
		assertNotNull(results);
		assertEquals(2, results.size());
		assertContains(results, "1", "2");
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class NoChildInnerContextIsKillableTestAgent extends TestingAgent {

		public NoChildInnerContextIsKillableTestAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			addResult("1");
			getSkill(InnerContextAccess.class).getInnerContext();
			getSkill(Schedules.class).in(1000, (it) -> {
				addResult("2");
				forceKillMe();
			});
			return false;
		}

	}

}
