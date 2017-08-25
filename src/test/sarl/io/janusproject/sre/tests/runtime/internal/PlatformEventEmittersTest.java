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

package io.janusproject.sre.tests.runtime.internal;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.xtext.util.Strings;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import io.janusproject.sre.tests.runtime.bugs.Bug546;
import io.janusproject.sre.tests.testutils.AbstractJanusRunTest;
import io.sarl.core.AgentKilled;
import io.sarl.core.AgentSpawned;
import io.sarl.core.Behaviors;
import io.sarl.core.ContextJoined;
import io.sarl.core.ContextLeft;
import io.sarl.core.DefaultContextInteractions;
import io.sarl.core.Destroy;
import io.sarl.core.ExternalContextAccess;
import io.sarl.core.Initialize;
import io.sarl.core.InnerContextAccess;
import io.sarl.core.Lifecycle;
import io.sarl.core.Logging;
import io.sarl.core.MemberJoined;
import io.sarl.core.MemberLeft;
import io.sarl.core.Schedules;
import io.sarl.core.SpaceCreated;
import io.sarl.core.SpaceDestroyed;
import io.sarl.lang.SARLVersion;
import io.sarl.lang.annotation.PerceptGuardEvaluator;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Behavior;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.SpaceID;
import io.sarl.util.OpenEventSpace;
import io.sarl.util.OpenEventSpaceSpecification;
import net.bytebuddy.implementation.bind.annotation.TargetMethodAnnotationDrivenBinder.DefaultsProvider;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@RunWith(Suite.class)
@SuiteClasses({
	PlatformEventEmittersTest.InitializeTest.class,
	PlatformEventEmittersTest.DestroyTest.class,
	PlatformEventEmittersTest.AgentSpawnedTest.class,
	PlatformEventEmittersTest.AgentKilledTest.class,
	PlatformEventEmittersTest.SpaceCreatedTest.class,
	PlatformEventEmittersTest.SpaceDestroyedTest.class,
	PlatformEventEmittersTest.MemberLeftTest.class,
	PlatformEventEmittersTest.MemberLeftAtTheSameTimeAsAgentKilledTest.class,
	PlatformEventEmittersTest.MemberJoinedTest.class,
	PlatformEventEmittersTest.MemberJoinedAtTheSameTimeAsAgentSpawnedTest.class,
	PlatformEventEmittersTest.ContextLeftTest.class,
	PlatformEventEmittersTest.ContextJoinedTest.class,
})
@SuppressWarnings("all")
public class PlatformEventEmittersTest {

	@SuppressWarnings("all")
	public static class InitializeTest extends AbstractJanusRunTest {

		@SarlSpecification
		public static class MyAgent extends TestingAgent {

			public MyAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				addResult("AGENT");
				getSkill(Behaviors.class).registerBehavior(new MyBehavior(this, getRawResults()));
				return true;
			}
			
		}
		
		@SarlSpecification
		public static class MyBehavior extends Behavior {

			private final List<Object> results;
			
			public MyBehavior(Agent agent, List<Object> results) {
				super(agent);
				this.results = results;
			}

			@PerceptGuardEvaluator
			private void guardInitialize(Initialize occurrence, Collection<Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onInitialize(occurrence));
			}

			private void onInitialize(Initialize occurrence) {
				synchronized (this.results) {
					this.results.add("BEHAVIOR");
				}
			}

		}
		
		@Test
		public void withinAgentAndBehavior() throws Exception {
			runJanus(MyAgent.class, false, true, STANDARD_TIMEOUT);
			assertContains(getResults(), "AGENT", "BEHAVIOR");
		}

	}

	@SuppressWarnings("all")
	public static class DestroyTest extends AbstractJanusRunTest {

		@SarlSpecification
		public static class MyAgent extends TestingAgent {

			public MyAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				getSkill(Behaviors.class).registerBehavior(new MyBehavior(this, getRawResults()));
				return true;
			}
			
			@PerceptGuardEvaluator
			private void guardDestroy(Destroy occurrence, Collection<Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onDestroy(occurrence));
			}

			private void onDestroy(Destroy occurrence) {
				addResult("AGENT");
			}
		}
		
		@SarlSpecification
		public static class MyBehavior extends Behavior {

			private final List<Object> results;
			
			public MyBehavior(Agent agent, List<Object> results) {
				super(agent);
				this.results = results;
			}

			@PerceptGuardEvaluator
			private void guardDestroy(Destroy occurrence, Collection<Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onDestroy(occurrence));
			}

			private void onDestroy(Destroy occurrence) {
				synchronized (this.results) {
					this.results.add("BEHAVIOR");
				}
			}

		}

		@Test
		public void withinAgentAndBehavior() throws Exception {
			runJanus(MyAgent.class, false, true, STANDARD_TIMEOUT);
			assertContains(getResults(), "AGENT", "BEHAVIOR");
		}

	}

	@SuppressWarnings("all")
	public static class AgentSpawnedTest extends AbstractJanusRunTest {

		@SarlSpecification
		public static class MyWaiterAgent extends TestingAgent {

			private UUID spawnId;
			
			public MyWaiterAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				getSkill(Schedules.class).in(500, (it) -> {
					UUID id = getSkill(Lifecycle.class).spawn(MySpawnedAgent.class, getRawResults());
					synchronized(this) {
						this.spawnId = id;
					}
					assert id != null;
				});
				return false;
			}

			@PerceptGuardEvaluator
			private void guardAgentSpawned(AgentSpawned occurrence, Collection<Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onAgentSpawned(occurrence));
			}

			private void onAgentSpawned(AgentSpawned occurrence) {
				synchronized(getRawResults()) {
					addResult("SPAWNER");
					addResult(occurrence);
					addResult(getID());
					addResult(getSkill(DefaultContextInteractions.class).getDefaultSpace().getSpaceID());
					synchronized (this) {
						assert this.spawnId != null;
						addResult(this.spawnId);
					}
				}
				forceKillMe();
			}

		}
		
		@SarlSpecification
		public static class MySpawnedAgent extends TestingAgent {

			public MySpawnedAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				getSkill(Schedules.class).in(500, (it) -> forceKillMe());
				return false;
			}

			@PerceptGuardEvaluator
			private void guardAgentSpawned(AgentSpawned occurrence, Collection<Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onAgentSpawned(occurrence));
			}

			private void onAgentSpawned(AgentSpawned occurrence) {
				addResult("SPAWNED");
			}

		}

		@Test
		public void run() throws Exception {
			runJanus(MyWaiterAgent.class, false, true, STANDARD_TIMEOUT);
			
			assertEquals(5, getNumberOfResults());
			assertEquals("SPAWNER", getResult(String.class, 0));
			AgentSpawned event = getResult(AgentSpawned.class, 1);
			UUID spawner = getResult(UUID.class, 2);
			SpaceID space = getResult(SpaceID.class, 3);
			UUID spawnId = getResult(UUID.class, 4);

			assertNotNull(event);
			assertEquals(MySpawnedAgent.class.getName(), event.agentType);
			assertEquals(spawner, event.getSource().getUUID());
			assertEquals(space, event.getSource().getSpaceID());
			assertContains(event.agentIdentifiers, spawnId);
		}
		
	}

	@SuppressWarnings("all")
	public static class AgentKilledTest extends AbstractJanusRunTest {

		@SarlSpecification
		public static class MyWaiterAgent extends TestingAgent {

			private UUID spawnId;
			
			public MyWaiterAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				getSkill(Schedules.class).in(500, (it) -> {
					UUID id = getSkill(Lifecycle.class).spawn(MySpawnedAgent.class, getRawResults());
					synchronized (this) {
						this.spawnId = id;
					}
					assert id != null;
				});
				return false;
			}

			@PerceptGuardEvaluator
			private void guardAgentKilled(AgentKilled occurrence, Collection<Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onAgentKilled(occurrence));
			}

			private void onAgentKilled(AgentKilled occurrence) {
				synchronized(getRawResults()) {
					addResult("SPAWNER");
					addResult(occurrence);
					addResult(getSkill(DefaultContextInteractions.class).getDefaultSpace().getSpaceID());
					synchronized (this) {
						assert this.spawnId != null;
						addResult(this.spawnId);
					}
				}
				forceKillMe();
			}

		}
		
		@SarlSpecification
		public static class MySpawnedAgent extends TestingAgent {

			public MySpawnedAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				getSkill(Schedules.class).in(500, (it) -> forceKillMe());
				return false;
			}

			@PerceptGuardEvaluator
			private void guardAgentKilled(AgentKilled occurrence, Collection<Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onAgentKilled(occurrence));
			}

			private void onAgentKilled(AgentKilled occurrence) {
				addResult("SPAWNED");
			}

		}

		@Test
		public void run() throws Exception {
			runJanus(MyWaiterAgent.class, false, true, STANDARD_TIMEOUT);
			
			assertEquals(4, getNumberOfResults());
			assertEquals("SPAWNER", getResult(String.class, 0));
			AgentKilled event = getResult(AgentKilled.class, 1);
			SpaceID space = getResult(SpaceID.class, 2);
			UUID spawnId = getResult(UUID.class, 3);

			assertNotNull(event);
			assertEquals(MySpawnedAgent.class.getName(), event.agentType);
			assertEquals(spawnId, event.getSource().getUUID());
			assertEquals(space, event.getSource().getSpaceID());
			assertEquals(spawnId, event.getSource().getUUID());
		}

	}

	@SuppressWarnings("all")
	public static class MemberJoinedAtTheSameTimeAsAgentSpawnedTest extends AbstractJanusRunTest {

		@SarlSpecification
		public static class MyWaiterAgent extends TestingAgent {

			private UUID spawnId;
			
			public MyWaiterAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				getSkill(Schedules.class).in(500, (it) -> {
					UUID id = getSkill(Lifecycle.class).spawn(MySpawnedAgent.class, getRawResults());
					assert id != null;
					synchronized (this) {
						this.spawnId = id;
					}
				});
				return false;
			}

			@PerceptGuardEvaluator
			private void guardMemberJoined(MemberJoined occurrence, Collection<Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onMemberJoined(occurrence));
			}
			
			private void onMemberJoined(MemberJoined occurrence) {
				synchronized (getRawResults()) {
					addResult("SPAWNER");
					addResult(occurrence);
					addResult(getParentID());
					addResult(getSkill(DefaultContextInteractions.class).getDefaultSpace().getSpaceID());
					synchronized (this) {
						addResult(this.spawnId);
					}
				}
				forceKillMe();
			}

		}
		
		@SarlSpecification
		public static class MySpawnedAgent extends TestingAgent {

			public MySpawnedAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				getSkill(Schedules.class).in(500, (it) -> forceKillMe());
				return false;
			}

			@PerceptGuardEvaluator
			private void guardMemberJoined(MemberJoined occurrence, Collection<Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onMemberJoined(occurrence));
			}
			
			private void onMemberJoined(MemberJoined occurrence) {
				addResult("SPAWNED");
			}

		}

		@Test
		public void run() throws Exception {
			runJanus(MyWaiterAgent.class, false, true, STANDARD_TIMEOUT);
			
			assertEquals(5, getNumberOfResults());
			assertEquals("SPAWNER", getResult(String.class, 0));
			MemberJoined event = getResult(MemberJoined.class, 1);
			UUID parent = getResult(UUID.class, 2);
			SpaceID space = getResult(SpaceID.class, 3);
			UUID spawnId = getResult(UUID.class, 4);

			assertNotNull(event);
			assertEquals(parent, event.getSource().getUUID());
			assertEquals(space, event.getSource().getSpaceID());
			assertEquals(spawnId, event.agentID);
			assertEquals(MySpawnedAgent.class.getName(), event.agentType);
		}

	}

	@SuppressWarnings("all")
	public static class MemberLeftAtTheSameTimeAsAgentKilledTest extends AbstractJanusRunTest {

		@SarlSpecification
		public static class MyWaiterAgent extends TestingAgent {
			
			public MyWaiterAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				getSkill(Schedules.class).in(500, (it) -> {
					getSkill(Lifecycle.class).spawn(MySpawnedAgent.class, getRawResults());
				});
				return false;
			}

			@PerceptGuardEvaluator
			private void guardMemberLeft(MemberLeft occurrence, Collection<Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onMemberLeft(occurrence));
			}
			
			private void onMemberLeft(MemberLeft occurrence) {
				synchronized (getRawResults()) {
					addResult("SPAWNER");
					addResult(occurrence);
					addResult(getSkill(DefaultContextInteractions.class).getDefaultSpace().getSpaceID());
					addResult(getParentID());
				}
				forceKillMe();
			}

		}
		
		@SarlSpecification
		public static class MySpawnedAgent extends TestingAgent {

			public MySpawnedAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				getSkill(Schedules.class).in(500, (it) -> forceKillMe());
				return false;
			}

			@PerceptGuardEvaluator
			private void guardMemberLeft(MemberLeft occurrence, Collection<Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onMemberLeft(occurrence));
			}
			
			private void onMemberLeft(MemberLeft occurrence) {
				addResult("SPAWNED");
			}

		}

		@Test
		public void run() throws Exception {
			runJanus(MyWaiterAgent.class, false, true, STANDARD_TIMEOUT);
			
			assertEquals(4, getNumberOfResults());
			assertEquals("SPAWNER", getResult(String.class, 0));
			MemberLeft event = getResult(MemberLeft.class, 1);
			SpaceID space = getResult(SpaceID.class, 2);
			UUID parent = getResult(UUID.class, 3);

			assertNotNull(event);
			assertEquals(MySpawnedAgent.class.getName(), event.agentType);
			assertEquals(parent, event.getSource().getUUID());
			assertEquals(space, event.getSource().getSpaceID());
		}

	}

	@SuppressWarnings("all")
	public static class MemberJoinedTest extends AbstractJanusRunTest {

		private static String simpleName(String name) {
			int index = name.lastIndexOf("$");
			if (index >= 0) {
				return name.substring(index + 1);
			}
			return name;
		}
		
		@SarlSpecification
		public static class RootAgent extends TestingAgent {
			
			public RootAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				getSkill(Schedules.class).in(500, (it) -> {
					getSkill(Lifecycle.class).spawnInContext(
							ChildAgent.class, getSkill(InnerContextAccess.class).getInnerContext(),
							getRawResults());
				});
				return false;
			}

			@PerceptGuardEvaluator
			private void guardAgentSpawned(AgentSpawned occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				if (ChildAgent.class.getName().equals(occurrence.agentType)) {
					handlers.add(() -> onAgentSpawned(occurrence));
				}
			}

			private void onAgentSpawned(AgentSpawned occurrence) {
				getSkill(Lifecycle.class).spawn(
						Child2Agent.class,
						getRawResults(), getSkill(InnerContextAccess.class).getInnerContext());
			}
			
			@PerceptGuardEvaluator
			private void guardMemberJoined(MemberJoined occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				if (!getSkill(DefaultContextInteractions.class).isInDefaultSpace(occurrence)) {
					handlers.add(() -> onMemberJoined(occurrence));
				}
			}

			private void onMemberJoined(MemberJoined occurrence) {
				addResult("ROOT:" + simpleName(occurrence.agentType));
			}

			@PerceptGuardEvaluator
			private void guardAgentKilled(AgentKilled occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onAgentKilled(occurrence));
			}

			private void onAgentKilled(AgentKilled occurrence) {
				forceKillMe();
			}

		}
		
		@SarlSpecification
		public static class ChildAgent extends TestingAgent {

			public ChildAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				return false;
			}

			
			@PerceptGuardEvaluator
			private void guardMemberJoined(MemberJoined occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onMemberJoined(occurrence));
			}

			private void onMemberJoined(MemberJoined occurrence) {
				synchronized(getRawResults()) {
					addResult("CHILD1:" + simpleName(occurrence.agentType));
				}
			}

			@PerceptGuardEvaluator
			private void guardAgentKilled(AgentKilled occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onAgentKilled(occurrence));
			}

			private void onAgentKilled(AgentKilled occurrence) {
				forceKillMe();
			}

		}

		@SarlSpecification
		public static class Child2Agent extends TestingAgent {

			public Child2Agent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				final AgentContext newContext = (AgentContext) getAgentInitializationParameters()[1];
				getSkill(Schedules.class).in(500, (it) -> {
					if (getSkill(ExternalContextAccess.class).join(
							newContext.getID(), newContext.getDefaultSpace().getSpaceID().getID())) {
						getSkill(Schedules.class).in(500, (it2) -> forceKillMe());
					}
				});
				return false;
			}
	
			@PerceptGuardEvaluator
			private void guardMemberJoined(MemberJoined occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onMemberJoined(occurrence));
			}

			private void onMemberJoined(MemberJoined occurrence) {
				synchronized(getRawResults()) {
					addResult("CHILD2" + simpleName(occurrence.agentType));
				}
			}

		}

		@Test
		public void run() throws Exception {
			runJanus(RootAgent.class, false, true, STANDARD_TIMEOUT);
			
			assertContains(getResults(),
					"ROOT:ChildAgent", // Spawning
					"ROOT:Child2Agent", // Arrival of Child2 within the Inner context of Root.
					"CHILD1:Child2Agent"); // Arrival of Child2 within the Inner context of Root.
		}

	}

	@SuppressWarnings("all")
	public static class MemberLeftTest extends AbstractJanusRunTest {

		private static String simpleName(String name) {
			int index = name.lastIndexOf("$");
			if (index >= 0) {
				return name.substring(index + 1);
			}
			return name;
		}
		
		@SarlSpecification
		public static class RootAgent extends TestingAgent {
			
			public RootAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				getSkill(Schedules.class).in(500, (it) -> {
					getSkill(Lifecycle.class).spawnInContext(
							ChildAgent.class, getSkill(InnerContextAccess.class).getInnerContext(),
							getRawResults());
				});
				return false;
			}

			@PerceptGuardEvaluator
			private void guardAgentSpawned(AgentSpawned occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				if (ChildAgent.class.getName().equals(occurrence.agentType)) {
					handlers.add(() -> onAgentSpawned(occurrence));
				}
			}

			private void onAgentSpawned(AgentSpawned occurrence) {
				getSkill(Lifecycle.class).spawn(
						Child2Agent.class,
						getRawResults(), getSkill(InnerContextAccess.class).getInnerContext());
			}

			@PerceptGuardEvaluator
			private void guardMemberLeft(MemberLeft occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				if (!getSkill(DefaultContextInteractions.class).isInDefaultSpace(occurrence)) {
					handlers.add(() -> onMemberLeft(occurrence));
				}
			}

			private void onMemberLeft(MemberLeft occurrence) {
				addResult("ROOT:" + simpleName(occurrence.agentType));
			}

			@PerceptGuardEvaluator
			private void guardAgentKilled(AgentKilled occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onAgentKilled(occurrence));
			}

			private void onAgentKilled(AgentKilled occurrence) {
				forceKillMe();
			}

		}
		
		@SarlSpecification
		public static class ChildAgent extends TestingAgent {

			public ChildAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				return false;
			}

			@PerceptGuardEvaluator
			private void guardMemberLeft(MemberLeft occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onMemberLeft(occurrence));
			}

			private void onMemberLeft(MemberLeft occurrence) {
				synchronized(getRawResults()) {
					addResult("CHILD1:" + simpleName(occurrence.agentType));
				}
			}

			@PerceptGuardEvaluator
			private void guardAgentKilled(AgentKilled occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onAgentKilled(occurrence));
			}

			private void onAgentKilled(AgentKilled occurrence) {
				forceKillMe();
			}

		}

		@SarlSpecification
		public static class Child2Agent extends TestingAgent {

			public Child2Agent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				final AgentContext newContext = (AgentContext) getAgentInitializationParameters()[1];
				getSkill(Schedules.class).in(500, (it) -> {
					if (getSkill(ExternalContextAccess.class).join(
							newContext.getID(), newContext.getDefaultSpace().getSpaceID().getID())) {
						getSkill(Schedules.class).in(500, (it2) -> forceKillMe());
					}
				});
				return false;
			}
	
			@PerceptGuardEvaluator
			private void guardMemberLeft(MemberLeft occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onMemberLeft(occurrence));
			}

			private void onMemberLeft(MemberLeft occurrence) {
				synchronized(getRawResults()) {
					addResult("CHILD2" + simpleName(occurrence.agentType));
				}
			}

		}

		@Test
		public void run() throws Exception {
			runJanus(RootAgent.class, false, true, STANDARD_TIMEOUT);
			
			assertTrue(getResults().contains("ROOT:Child2Agent")); // Suicide of Child2
			assertTrue(getResults().contains("CHILD1:Child2Agent")); // Suicide of Child2
		}

	}

	@SuppressWarnings("all")
	public static class ContextJoinedTest extends AbstractJanusRunTest {
		
		@SarlSpecification
		public static class RootAgent extends TestingAgent {
			
			public RootAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				getSkill(Schedules.class).in(500, (it) -> {
					getSkill(Lifecycle.class).spawnInContext(
							ChildAgent.class, getSkill(InnerContextAccess.class).getInnerContext(),
							getRawResults());
				});
				return false;
			}

			@PerceptGuardEvaluator
			private void guardAgentSpawned(AgentSpawned occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				if (ChildAgent.class.getName().equals(occurrence.agentType)) {
					handlers.add(() -> onAgentSpawned(occurrence));
				}
			}

			private void onAgentSpawned(AgentSpawned occurrence) {
				getSkill(Lifecycle.class).spawn(
						Child2Agent.class,
						getRawResults(), getSkill(InnerContextAccess.class).getInnerContext());
			}
			
			@PerceptGuardEvaluator
			private void guardContextJoined(ContextJoined occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				if (!getSkill(DefaultContextInteractions.class).isInDefaultSpace(occurrence)) {
					handlers.add(() -> onContextJoined(occurrence));
				}
			}

			private void onContextJoined(ContextJoined occurrence) {
				addResult("ROOT");
			}

			@PerceptGuardEvaluator
			private void guardAgentKilled(AgentKilled occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onAgentKilled(occurrence));
			}

			private void onAgentKilled(AgentKilled occurrence) {
				forceKillMe();
			}

		}
		
		@SarlSpecification
		public static class ChildAgent extends TestingAgent {

			public ChildAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				return false;
			}

			
			@PerceptGuardEvaluator
			private void guardContextJoined(ContextJoined occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onContextJoined(occurrence));
			}

			private void onContextJoined(ContextJoined occurrence) {
				synchronized(getRawResults()) {
					addResult("CHILD1");
				}
			}

			@PerceptGuardEvaluator
			private void guardAgentKilled(AgentKilled occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onAgentKilled(occurrence));
			}

			private void onAgentKilled(AgentKilled occurrence) {
				forceKillMe();
			}

		}

		@SarlSpecification
		public static class Child2Agent extends TestingAgent {

			public Child2Agent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				final AgentContext newContext = (AgentContext) getAgentInitializationParameters()[1];
				getSkill(Schedules.class).in(500, (it) -> {
					if (getSkill(ExternalContextAccess.class).join(
							newContext.getID(), newContext.getDefaultSpace().getSpaceID().getID())) {
						getSkill(Schedules.class).in(500, (it2) -> forceKillMe());
					}
				});
				return false;
			}
	
			@PerceptGuardEvaluator
			private void guardContextJoined(ContextJoined occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onContextJoined(occurrence));
			}

			private void onContextJoined(ContextJoined occurrence) {
				synchronized(getRawResults()) {
					addResult("CHILD2");
				}
			}

		}

		@Test
		public void run() throws Exception {
			runJanus(RootAgent.class, false, true, STANDARD_TIMEOUT);
			
			assertContains(getResults(),
					"CHILD2"); // Child2 joined the inner context of ROOT
		}

	}

	@SuppressWarnings("all")
	public static class ContextLeftTest extends AbstractJanusRunTest {

		@SarlSpecification
		public static class Bye extends Event {
		}

		@SarlSpecification
		public static class RootAgent extends TestingAgent {
			
			public RootAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				getSkill(Logging.class).setLoggingName("ROOT");
				getSkill(Schedules.class).in(500, (it) -> {
					getSkill(Lifecycle.class).spawnInContext(
							ChildAgent.class, getSkill(InnerContextAccess.class).getInnerContext(),
							getRawResults());
				});
				return false;
			}

			@PerceptGuardEvaluator
			private void guardAgentSpawned(AgentSpawned occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				if (ChildAgent.class.getName().equals(occurrence.agentType)) {
					handlers.add(() -> onAgentSpawned(occurrence));
				}
			}

			private void onAgentSpawned(AgentSpawned occurrence) {
				getSkill(Lifecycle.class).spawn(
						Child2Agent.class,
						getRawResults(), getSkill(InnerContextAccess.class).getInnerContext());
			}
			
			@PerceptGuardEvaluator
			private void guardContextLeft(ContextLeft occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				if (!getSkill(DefaultContextInteractions.class).isInDefaultSpace(occurrence)) {
					handlers.add(() -> onContextLeft(occurrence));
				}
			}

			private void onContextLeft(ContextLeft occurrence) {
				addResult("ROOT");
			}

			@PerceptGuardEvaluator
			private void guardBye(Bye occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onBye(occurrence));
			}

			private void onBye(Bye occurrence) {
				getSkill(Logging.class).info("Child2 said Bye!");
				getSkill(Behaviors.class).wake(new Bye());
				forceKillMe();
			}

		}
		
		@SarlSpecification
		public static class ChildAgent extends TestingAgent {

			public ChildAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				getSkill(Logging.class).setLoggingName("CHILD1");
				return false;
			}

			
			@PerceptGuardEvaluator
			private void guardContextLeft(ContextLeft occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onContextLeft(occurrence));
			}

			private void onContextLeft(ContextLeft occurrence) {
				synchronized(getRawResults()) {
					addResult("CHILD1");
				}
			}

			@PerceptGuardEvaluator
			private void guardBye(Bye occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onBye(occurrence));
			}

			private void onBye(Bye occurrence) {
				getSkill(Logging.class).info("Child2 said Bye!");
				forceKillMe();
			}

		}

		@SarlSpecification
		public static class Child2Agent extends TestingAgent {

			private volatile UUID contextID;
			
			public Child2Agent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				getSkill(Logging.class).setLoggingName("CHILD2");
				final AgentContext newContext = (AgentContext) getAgentInitializationParameters()[1];
				this.contextID = newContext.getID();
				getSkill(Schedules.class).in(500, (it) -> {
					if (getSkill(ExternalContextAccess.class).join(
							newContext.getID(), newContext.getDefaultSpace().getSpaceID().getID())) {
						getSkill(Schedules.class).in(500, (it2) -> leftContext());
					}
				});
				return false;
			}

			public void leftContext() {
				getSkill(Logging.class).info("Leaving the context: " + this.contextID);
				getSkill(ExternalContextAccess.class).leave(this.contextID);
			}
	
			@PerceptGuardEvaluator
			private void guardContextLeft(ContextLeft occurrence, Collection <Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onContextLeft(occurrence));
			}

			private void onContextLeft(ContextLeft occurrence) {
				getSkill(Logging.class).info("Context leaved: " + this.contextID);
				synchronized(getRawResults()) {
					addResult("CHILD2");
				}
				getSkill(DefaultContextInteractions.class).emit(new Bye());
				getSkill(Logging.class).info("Commit a suicide");
				forceKillMe();
			}

		}

		@Test
		public void run() throws Exception {
			runJanus(RootAgent.class, false, true, STANDARD_TIMEOUT);
			
			assertContains(getResults(),
					"CHILD2"); // Child2 left the inner context of ROOT
		}

	}

	@SuppressWarnings("all")
	public static class SpaceCreatedTest extends AbstractJanusRunTest {

		private static class Data {
			public String label;
			public SpaceID spaceID;
			public Address source;
			public SpaceID defaultSpaceID;
			public OpenEventSpace createdSpace;
		}
		
		@SarlSpecification
		public static class MyWaiterAgent extends TestingAgent {

			public MyWaiterAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				getSkill(Schedules.class).in(500, (it) -> {
					getSkill(Lifecycle.class).spawn(MySpawnedAgent.class, getRawResults());
				});
				return false;
			}

			@PerceptGuardEvaluator
			private void guardSpaceCreated(SpaceCreated occurrence, Collection<Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onSpaceCreated(occurrence));
			}

			private void onSpaceCreated(SpaceCreated occurrence) {
				Data data = new Data();
				data.label = "SPAWNER";
				data.spaceID = occurrence.spaceID;
				data.source = occurrence.getSource();
				data.defaultSpaceID = getSkill(DefaultContextInteractions.class).getDefaultSpace().getSpaceID();
				addResult(data);
				forceKillMe();
			}

		}
		
		@SarlSpecification
		public static class MySpawnedAgent extends TestingAgent {

			private OpenEventSpace createdSpace;
			
			public MySpawnedAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				AgentContext ctx = getSkill(DefaultContextInteractions.class).getDefaultContext();
				UUID spaceID = UUID.randomUUID();
				this.createdSpace = ctx.createSpace(OpenEventSpaceSpecification.class, spaceID);
				getSkill(Schedules.class).in(500, (it) -> forceKillMe());
				return false;
			}

			@PerceptGuardEvaluator
			private void guardSpaceCreated(SpaceCreated occurrence, Collection<Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onSpaceCreated(occurrence));
			}
			
			private void onSpaceCreated(SpaceCreated occurrence) {
				Data data = new Data();
				data.label = "SPAWNED";
				data.spaceID = occurrence.spaceID;
				data.source = occurrence.getSource();
				data.createdSpace = this.createdSpace;
				data.defaultSpaceID = getSkill(DefaultContextInteractions.class).getDefaultSpace().getSpaceID();
				addResult(data);
				forceKillMe();
			}

		}

		@Test
		public void run() throws Exception {
			runJanus(MyWaiterAgent.class, false, true, STANDARD_TIMEOUT);
			
			assertEquals(2, getNumberOfResults());
			Data data1 = getResult(Data.class, 0);
			Data data2 = getResult(Data.class, 1);

			Data rest = validateSpawner(data1, data2);
			validateSpawned(rest);
		}

		private Data validateSpawner(Data d1, Data d2) {
			final Data sdata;
			final Data rdata;
			if (Strings.equal(d1.label, "SPAWNER")) {
				sdata = d1;
				rdata = d2;
			} else {
				sdata = d2;
				rdata = d1; 
			}
			assertNotNull(rdata.createdSpace);
			assertEquals("SPAWNER", sdata.label);
			assertEquals(rdata.createdSpace.getSpaceID(), sdata.spaceID);
			assertEquals(rdata.defaultSpaceID, sdata.source.getSpaceID());
			assertEquals(rdata.defaultSpaceID.getContextID(), sdata.source.getUUID());
			return rdata;
		}

		private void validateSpawned(Data d) {
			assertNotNull(d.createdSpace);
			assertEquals("SPAWNED", d.label);
			assertEquals(d.createdSpace.getSpaceID(), d.spaceID);
			assertEquals(d.defaultSpaceID, d.source.getSpaceID());
			assertEquals(d.defaultSpaceID.getContextID(), d.source.getUUID());
		}

	}

	@SuppressWarnings("all")
	public static class SpaceDestroyedTest extends AbstractJanusRunTest {

		@Test
		@Ignore("Destruction of spaces is not yet implemented")
		public void run() throws Exception {
			//
		}

	}

}
