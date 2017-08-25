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
package io.janusproject.sre.tests.units.services.lifecycle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import io.janusproject.sre.services.lifecycle.BasicSkillUninstaller;
import io.janusproject.sre.skills.JanusBuiltin;
import io.janusproject.sre.tests.testutils.AbstractJanusTest;
import io.sarl.core.InnerContextAccess;
import io.sarl.core.Lifecycle;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.SREutils;
import io.sarl.lang.core.Skill;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.util.SynchronizedIterable;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class BasicSkillUninstallerTest extends AbstractJanusTest {

	@Nullable
	private BasicSkillUninstaller installer;

	@Before
	public void setUp() {
		this.installer = new BasicSkillUninstaller();
	}

	@Test
	public void uninstallSkillsBeforeDestroy() throws Exception {
		AtomicInteger order = new AtomicInteger();
		LifeSkill s1 = new LifeSkill(order);
		MySkill s2 = new MySkill(order);
		InnerSkill s3 = new InnerSkill(order);
		Agent agent = spy(new MyAgent(s1, s2, s3));
		Iterable<? extends Skill> skills = this.installer.uninstallSkillsBeforeDestroy(agent);
		assertEquals(0, s2.uninstallOrderPre);
		assertEquals(1, s1.uninstallOrderPre);
		assertEquals(2, s3.uninstallOrderPre);
		assertNull(s2.uninstallOrderPost);
		assertNull(s1.uninstallOrderPost);
		assertNull(s3.uninstallOrderPost);
		Iterator<? extends Skill> iterator = skills.iterator();
		assertSame(s2, iterator.next());
		assertSame(s1, iterator.next());
		assertSame(s3, iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void uninstallSkillsAfterDestroy() throws Exception {
		AtomicInteger order = new AtomicInteger();
		LifeSkill s1 = new LifeSkill(order);
		MySkill s2 = new MySkill(order);
		InnerSkill s3 = new InnerSkill(order);
		Agent agent = spy(new MyAgent(s1, s2, s3));
		List<? extends Skill> skills = Arrays.asList(s2, s1, s3);
		this.installer.uninstallSkillsAfterDestroy(agent, skills);
		assertNull(s2.uninstallOrderPre);
		assertNull(s1.uninstallOrderPre);
		assertNull(s3.uninstallOrderPre);
		assertEquals(0, s2.uninstallOrderPost);
		assertEquals(1, s1.uninstallOrderPost);
		assertEquals(2, s3.uninstallOrderPost);
	}

	private static class MyAgent extends Agent {

		public MyAgent(LifeSkill s1, MySkill s2, InnerSkill s3) throws Exception {
			super(UUID.randomUUID(), UUID.randomUUID());
			SREutils.createSkillMapping(this, Lifecycle.class, s1);
			SREutils.createSkillMapping(this, MyCap.class, s2);
			SREutils.createSkillMapping(this, InnerContextAccess.class, s3);
		}
		
	}
	
	private interface MyCap extends Capacity {
		
	}
	
	private static class MySkill extends Skill implements MyCap {
		private final AtomicInteger orderSource;
		public Integer installOrder = null;
		public Integer uninstallOrderPre = null;
		public Integer uninstallOrderPost = null;
		
		public MySkill(AtomicInteger s) {
			this.orderSource = s;
		}
		
		protected void install() {
			this.installOrder = this.orderSource.getAndIncrement();
		}

		protected void uninstall(UninstallationStage stage) {
			if (stage == UninstallationStage.PRE_DESTROY_EVENT) {
				this.uninstallOrderPre = this.orderSource.getAndIncrement();
			} else {
				this.uninstallOrderPost = this.orderSource.getAndIncrement();
			}
		}
	}

	private static class InnerSkill extends JanusBuiltin implements InnerContextAccess {

		private final AtomicInteger orderSource;
		public Integer installOrder = null;
		public Integer uninstallOrderPre = null;
		public Integer uninstallOrderPost = null;
		
		public InnerSkill(AtomicInteger s) {
			this.orderSource = s;
		}
		
		protected void install() {
			this.installOrder = this.orderSource.getAndIncrement();
		}

		protected void uninstall(UninstallationStage stage) {
			if (stage == UninstallationStage.PRE_DESTROY_EVENT) {
				this.uninstallOrderPre = this.orderSource.getAndIncrement();
			} else {
				this.uninstallOrderPost = this.orderSource.getAndIncrement();
			}
		}

		@Override
		public AgentContext getInnerContext() {
			return null;
		}

		@Override
		public boolean hasMemberAgent() {
			return false;
		}

		@Override
		public int getMemberAgentCount() {
			return 0;
		}

		@Override
		public SynchronizedIterable<UUID> getMemberAgents() {
			return null;
		}

		@Override
		public boolean isInnerDefaultSpace(Space space) {
			return false;
		}

		@Override
		public boolean isInnerDefaultSpace(SpaceID spaceID) {
			return false;
		}

		@Override
		public boolean isInnerDefaultSpace(UUID spaceID) {
			return false;
		}

		@Override
		public boolean isInInnerDefaultSpace(Event event) {
			return false;
		}

		@Override
		public int getInstallationOrder() {
			return 0;
		}
		
	}

	private static class LifeSkill extends JanusBuiltin implements Lifecycle {

		private final AtomicInteger orderSource;
		public Integer installOrder = null;
		public Integer uninstallOrderPre = null;
		public Integer uninstallOrderPost = null;
		
		public LifeSkill(AtomicInteger s) {
			this.orderSource = s;
		}
		
		protected void install() {
			this.installOrder = this.orderSource.getAndIncrement();
		}

		protected void uninstall(UninstallationStage stage) {
			if (stage == UninstallationStage.PRE_DESTROY_EVENT) {
				this.uninstallOrderPre = this.orderSource.getAndIncrement();
			} else {
				this.uninstallOrderPost = this.orderSource.getAndIncrement();
			}
		}

		@Override
		public UUID spawn(Class<? extends Agent> agentType, Object... params) {
			return null;
		}

		@Override
		public Iterable<UUID> spawn(int nbAgents, Class<? extends Agent> agentType, Object... params) {
			return null;
		}

		@Override
		public UUID spawnInContext(Class<? extends Agent> agentClass, AgentContext context, Object... params) {
			return null;
		}

		@Override
		public Iterable<UUID> spawnInContext(int nbAgents, Class<? extends Agent> agentClass, AgentContext context,
				Object... params) {
			return null;
		}

		@Override
		public UUID spawnInContextWithID(Class<? extends Agent> agentClass, UUID agentID, AgentContext context,
				Object... params) {
			return null;
		}

		@Override
		public void killMe() {
		}

		@Override
		public int getInstallationOrder() {
			return 1;
		}
		
	}

}
