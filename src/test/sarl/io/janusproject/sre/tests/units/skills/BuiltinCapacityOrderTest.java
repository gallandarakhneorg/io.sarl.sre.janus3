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
package io.janusproject.sre.tests.units.skills;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.arakhne.afc.util.IntegerList;
import org.junit.Before;
import org.junit.Test;

import io.janusproject.sre.skills.BehaviorsSkill;
import io.janusproject.sre.skills.BuiltinCapacitiesOrder;
import io.janusproject.sre.skills.DefaultContextInteractionsSkill;
import io.janusproject.sre.skills.ExternalContextAccessSkill;
import io.janusproject.sre.skills.InnerContextAccessSkill;
import io.janusproject.sre.skills.InternalEventBusSkill;
import io.janusproject.sre.skills.LifecycleSkill;
import io.janusproject.sre.skills.LoggingSkill;
import io.janusproject.sre.skills.MicroKernelSkill;
import io.janusproject.sre.skills.SchedulesSkill;
import io.janusproject.sre.skills.TimeSkill;
import io.janusproject.sre.tests.testutils.AbstractJanusTest;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.Skill;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class BuiltinCapacityOrderTest extends AbstractJanusTest {

	private static final int NB_BUILTIN_CAPACITIES = 10;
	
	@Before
	public void setUp() {
		
	}

	@Test
	public void ensureAllBuilinSkillsAreSpecified() {
		Set<Class<? extends Skill>> skills = new HashSet<>();
		for (Class<? extends Skill> type : BuiltinCapacitiesOrder.SKILL_INSTALLATION_ORDER) {
			skills.add(type);
		}
		// Janus specific builtin capacities
		assertNotNull(skills.remove(MicroKernelSkill.class));
		assertNotNull(skills.remove(InternalEventBusSkill.class));
		// SARL specific builtin capacities
		assertNotNull(skills.remove(LoggingSkill.class));
		assertNotNull(skills.remove(TimeSkill.class));
		assertNotNull(skills.remove(LifecycleSkill.class));
		assertNotNull(skills.remove(SchedulesSkill.class));
		assertNotNull(skills.remove(InnerContextAccessSkill.class));
		assertNotNull(skills.remove(BehaviorsSkill.class));
		assertNotNull(skills.remove(ExternalContextAccessSkill.class));
		assertNotNull(skills.remove(DefaultContextInteractionsSkill.class));
		assertTrue(skills.isEmpty());
	}

	@Test
	public void computeInstallationOrder() {
		IntegerList indexes = new IntegerList();
		int order;

		for (Skill skill : Arrays.asList(
				mock(MicroKernelSkill.class),
				mock(InternalEventBusSkill.class),
				mock(LoggingSkill.class),
				mock(TimeSkill.class),
				mock(LifecycleSkill.class),
				mock(SchedulesSkill.class),
				mock(InnerContextAccessSkill.class),
				mock(BehaviorsSkill.class),
				mock(ExternalContextAccessSkill.class),
				mock(DefaultContextInteractionsSkill.class))) {
			order = BuiltinCapacitiesOrder.computeInstallationOrder(skill);
			assertTrue(order < NB_BUILTIN_CAPACITIES);
			assertTrue(indexes.add(order));
		}

		order = BuiltinCapacitiesOrder.computeInstallationOrder(mock(MySkill.class));
		assertTrue(order >= NB_BUILTIN_CAPACITIES);
		assertTrue(indexes.add(order));
	}

	private static interface MyCap extends Capacity {
	}

	private static class MySkill extends Skill implements MyCap {
	}
}