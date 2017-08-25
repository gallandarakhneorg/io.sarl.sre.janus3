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

package io.janusproject.sre.tests.runtime.bugs;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.janusproject.sre.tests.testutils.AbstractJanusRunTest;
import io.sarl.core.Behaviors;
import io.sarl.core.DefaultContextInteractions;
import io.sarl.core.Destroy;
import io.sarl.core.Initialize;
import io.sarl.core.Lifecycle;
import io.sarl.core.Schedules;
import io.sarl.lang.SARLVersion;
import io.sarl.lang.annotation.PerceptGuardEvaluator;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Behavior;
import io.sarl.lang.core.Event;

/**
 * Unit test for the issue #224: Equivalent of Skill.install for the Behavior class.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @see https://github.com/sarl/sarl/issues/224
 */
@SuppressWarnings("all")
public class Bug224 extends AbstractJanusRunTest {

	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class MyBehavior extends Behavior {

		public final AtomicInteger initializeEvaluations = new AtomicInteger();

		public final AtomicInteger destroyEvaluations = new AtomicInteger();

		public MyBehavior(Agent owner) {
			super(owner);
		}

		@PerceptGuardEvaluator
		private void onInitializeGuard(Initialize event, Collection<Runnable> handlers) {
			this.initializeEvaluations.incrementAndGet();
		}

		@PerceptGuardEvaluator
		private void onDestroyGuard(Destroy event, Collection<Runnable> handlers) {
			this.destroyEvaluations.incrementAndGet();
		}

	}

	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class MyEvent extends Event {
		//
	}

	@Test
	public void registerInInitialize() throws Exception {
		runJanus(RegisteredInInitializeAgent.class, false);
		assertEquals(1, getNumberOfResults());
		MyBehavior beh = getResult(MyBehavior.class, 0);
		assertEquals(1, beh.initializeEvaluations.intValue());
		assertEquals(1, beh.destroyEvaluations.intValue());
	}

	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class RegisteredInInitializeAgent extends TestingAgent {

		public MyBehavior behavior = new MyBehavior(this);


		public RegisteredInInitializeAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			addResult(this.behavior);
			getSkill(Behaviors.class).registerBehavior(this.behavior);
			getSkill(Schedules.class).in(1000, (it) -> forceKillMe());
			return false;
		}

	}

	@Test
	public void registerInHandler() throws Exception {
		runJanus(RegisteredInHandlerAgent.class, false);
		assertEquals(1, getNumberOfResults());
		MyBehavior beh = getResult(MyBehavior.class, 0);
		assertEquals(1, beh.initializeEvaluations.intValue());
		assertEquals(1, beh.destroyEvaluations.intValue());
	}

	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class RegisteredInHandlerAgent extends TestingAgent {

		public MyBehavior behavior = new MyBehavior(this);

		public RegisteredInHandlerAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			addResult(this.behavior);
			getSkill(DefaultContextInteractions.class).emit(new MyEvent());
			getSkill(Schedules.class).in(1000, (agent) -> forceKillMe());
			return false;
		}

		@PerceptGuardEvaluator
		private void onMyEventGuard(MyEvent event, Collection<Runnable> handlers) {
			handlers.add(() -> onMyEvent(event, event));
		}

		private void onMyEvent(MyEvent occurrence, MyEvent it) {
			getSkill(Behaviors.class).registerBehavior(this.behavior);
		}

	}

	@Test
	public void registerInDestroy() throws Exception {
		runJanus(RegisteredInDestroyAgent.class, false);
		assertEquals(1, getNumberOfResults());
		MyBehavior beh = getResult(MyBehavior.class, 0);
		assertEquals(0, beh.initializeEvaluations.intValue());
		assertEquals(0, beh.destroyEvaluations.intValue());
	}

	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class RegisteredInDestroyAgent extends TestingAgent {

		public MyBehavior behavior = new MyBehavior(this);

		public RegisteredInDestroyAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			addResult(this.behavior);
			return true;
		}

		@PerceptGuardEvaluator
		private void onDestroyGuard(Destroy event, Collection<Runnable> handlers) {
			handlers.add(() -> onDestroy(event, event));
		}

		private void onDestroy(Destroy occurrence, Destroy it) {
			getSkill(Behaviors.class).registerBehavior(this.behavior);
		}

	}

	@Test
	public void unregisterInInitialize() throws Exception {
		runJanus(UnregisteredInInitializeAgent.class, false);
		assertEquals(1, getNumberOfResults());
		MyBehavior beh = getResult(MyBehavior.class, 0);
		assertEquals(1, beh.initializeEvaluations.intValue());
		assertEquals(1, beh.destroyEvaluations.intValue());
	}

	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class UnregisteredInInitializeAgent extends TestingAgent {

		public MyBehavior behavior = new MyBehavior(this);


		public UnregisteredInInitializeAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			addResult(this.behavior);
			getSkill(Behaviors.class).registerBehavior(this.behavior);
			getSkill(Behaviors.class).unregisterBehavior(this.behavior);
			getSkill(Schedules.class).in(1000, (agent) -> forceKillMe());
			return false;
		}

	}

	@Test
	public void unregisterInHandler() throws Exception {
		runJanus(UnregisteredInHandlerAgent.class, false);
		assertEquals(1, getNumberOfResults());
		MyBehavior beh = getResult(MyBehavior.class, 0);
		assertEquals(1, beh.initializeEvaluations.intValue());
		assertEquals(1, beh.destroyEvaluations.intValue());
	}

	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class UnregisteredInHandlerAgent extends TestingAgent {

		public MyBehavior behavior = new MyBehavior(this);

		public UnregisteredInHandlerAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			addResult(this.behavior);
			getSkill(Behaviors.class).registerBehavior(this.behavior);
			getSkill(DefaultContextInteractions.class).emit(new MyEvent());
			getSkill(Schedules.class).in(1000, (agent) -> forceKillMe());
			return false;
		}

		@PerceptGuardEvaluator
		private void onMyEventGuard(MyEvent event, Collection<Runnable> handlers) {
			handlers.add(() -> onMyEvent(event, event));
		}

		private void onMyEvent(MyEvent occurrence, MyEvent it) {
			getSkill(Behaviors.class).unregisterBehavior(this.behavior);
		}

	}

	@Test
	public void unregisterInDestroy() throws Exception {
		runJanus(UnregisteredInDestroyAgent.class, false, true, NO_TIMEOUT);
		assertEquals(1, getNumberOfResults());
		MyBehavior beh = getResult(MyBehavior.class, 0);
		assertEquals(1, beh.initializeEvaluations.intValue());
		assertEquals(1, beh.destroyEvaluations.intValue());
	}

	@SarlSpecification(SARLVersion.SPECIFICATION_RELEASE_VERSION_STRING)
	public static class UnregisteredInDestroyAgent extends TestingAgent {

		public MyBehavior behavior = new MyBehavior(this);

		public UnregisteredInDestroyAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			addResult(this.behavior);
			getSkill(Behaviors.class).registerBehavior(this.behavior);
			return true;
		}

		@PerceptGuardEvaluator
		private void onDestroyGuard(Destroy event, Collection<Runnable> handlers) {
			handlers.add(() -> onDestroy(event, event));
		}

		private void onDestroy(Destroy occurrence, Destroy it) {
			getSkill(Behaviors.class).unregisterBehavior(this.behavior);
		}

	}

}
