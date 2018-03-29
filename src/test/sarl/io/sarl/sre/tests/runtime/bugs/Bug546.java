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

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import io.sarl.sre.Kernel;
import io.sarl.sre.tests.testutils.AbstractJanusRunTest;
import io.sarl.core.AgentKilled;
import io.sarl.core.AgentTask;
import io.sarl.core.DefaultContextInteractions;
import io.sarl.core.Lifecycle;
import io.sarl.core.Schedules;
import io.sarl.lang.SARLVersion;
import io.sarl.lang.annotation.PerceptGuardEvaluator;
import io.sarl.lang.annotation.SarlSpecification;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.Scope;
import io.sarl.util.Scopes;

/**
 * Unit test for the issue #546: Not enough AgentKilled occurrences after killMe() calls.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @see https://github.com/sarl/sarl/issues/546
 */
@RunWith(Suite.class)
@SuiteClasses({
	Bug546.AgentKillTest.class,
	Bug546.LargeEventSetTest.class,
})
@SuppressWarnings("all")
public class Bug546 {

	private static final int NB_AGENTS = 300;

	private static final int NB_EVENTS = 15000;

	public static class AgentKillTest extends AbstractJanusRunTest {
		
		protected static class Hello extends Event {
		}
		
		protected static class KillYou extends Event {
		}

		@SarlSpecification
		protected static class InitAndWaitAgent extends TestingAgent {

			private static enum State { WAIT_ALL, WAIT_KILLS }
			
			private State state = State.WAIT_ALL;

			private int nbHellos = 0;

			private int nbKilled = 0;

			public InitAndWaitAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				Lifecycle lifecycle = getSkill(Lifecycle.class);
				for (int i = 0; i < NB_AGENTS; ++i) {
					lifecycle.spawn(ChildAgent.class, getRawResults(), getID());
				}
				return false;
			}

			@PerceptGuardEvaluator
			private void guardHello(Hello occurrence, Collection<Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				boolean valid;
				synchronized (this) {
					valid = this.state == State.WAIT_ALL;
				}
				if (valid) {
					handlers.add(() -> onHello(occurrence));
				}
			}

			private synchronized void onHello(Hello occurrence) {
				int n;
				synchronized(this) {
					++this.nbHellos;
					n = this.nbHellos;
				}
				if (n == NB_AGENTS) {
					synchronized (this) {
						this.state = State.WAIT_KILLS;
					}
					getSkill(DefaultContextInteractions.class).emit(new KillYou());
				}
			}
			
			@PerceptGuardEvaluator
			private void guardAgentKilled(AgentKilled occurrence, Collection<Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onAgentKilled(occurrence));
			}

			private synchronized void onAgentKilled(AgentKilled occurrence) {
				int n;
				synchronized (this) {
					++this.nbKilled;
					n = this.nbKilled;
				}
				if (n == NB_AGENTS) {
					forceKillMe();
				}
			}

		}

		@SarlSpecification
		protected static class ChildAgent extends TestingAgent {

			private UUID waiterId;
			
			public ChildAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				UUID id = (UUID) getAgentInitializationParameters()[1];
				synchronized (this) {
					this.waiterId = id;
				}
				getSkill(DefaultContextInteractions.class).emit(new Hello(), (it) -> {
					UUID id2;
					synchronized (this) {
						id2 = this.waiterId;
					}
					return it.getUUID().equals(id2);
				});
				return false;
			}
			
			@PerceptGuardEvaluator
			private void guardKillYou(KillYou occurrence, Collection<Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onKillYou(occurrence));
			}

			private void onKillYou(KillYou occurrence) {
				forceKillMe();
			}

		}

		@Test
		public void launchTest() throws Exception {
			runJanus(InitAndWaitAgent.class, false, true, EXTRA_TIMEOUT);
		}

	}

	public static class LargeEventSetTest extends AbstractJanusRunTest {

		public static final class MyEvent extends Event {

		}

		@Test
		public void sendHugeNumberEvents() throws Exception {
			runJanus(InitAndWaitAgent.class, false, true, EXTRA_TIMEOUT);
		}

		@SarlSpecification
		public static class InitAndWaitAgent extends TestingAgent {

			private int nbEvents = 0;
			
			public InitAndWaitAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				getSkill(Lifecycle.class).spawn(SendingAgent.class, getRawResults(), getID());
				return false;
			}

			@PerceptGuardEvaluator
			private void guardMyEvent(MyEvent occurrence, Collection<Runnable> handlers) {
				assert occurrence != null;
				assert handlers != null;
				handlers.add(() -> onMyEvent(occurrence));
			}

			private synchronized void onMyEvent(MyEvent occurrence) {
				++nbEvents;
				if (nbEvents == NB_EVENTS) {
					forceKillMe();
				}
			}

		}

		@SarlSpecification
		public static class SendingAgent extends TestingAgent {

			public SendingAgent(UUID parentID, UUID agentID) {
				super(parentID, agentID);
			}

			@Override
			protected boolean runAgentTest() {
				final UUID parentID = (UUID) getAgentInitializationParameters()[1];
				getSkill(Schedules.class).in(500, (it) -> {
					DefaultContextInteractions ctx = getSkill(DefaultContextInteractions.class);
					for (int i = 0; i < NB_EVENTS; ++i) {
						ctx.emit(new MyEvent(), (it2) -> it2.getUUID().equals(parentID));
					}
					forceKillMe();
				});
				return false;
			}

		}
	
	}

}
