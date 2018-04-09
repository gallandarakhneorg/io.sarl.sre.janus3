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

package io.sarl.sre.tests.units.skills;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.inject.Injector;

import io.sarl.core.Behaviors;
import io.sarl.core.DefaultContextInteractions;
import io.sarl.core.ExternalContextAccess;
import io.sarl.core.InnerContextAccess;
import io.sarl.core.Lifecycle;
import io.sarl.core.Logging;
import io.sarl.core.Schedules;
import io.sarl.core.Time;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.Skill;
import io.sarl.lang.util.ClearableReference;
import io.sarl.sre.capacities.InternalEventBusCapacity;
import io.sarl.sre.capacities.InternalSchedules;
import io.sarl.sre.capacities.MicroKernelCapacity;
import io.sarl.sre.skills.SreDynamicSkillProvider;
import io.sarl.sre.tests.testutils.AbstractSreTest;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class SreDynamicSkillProviderTest extends AbstractSreTest {

	@Nullable
	private Injector injector;

	@Nullable
	private Agent agent;

	@Nullable
	private MockedDynamicSkillProvider provider;
	
	@Before
	public void setUp() {
		this.injector = mock(Injector.class);
		this.agent = mock(Agent.class);
		this.provider = spy(new MockedDynamicSkillProvider(this.injector));
	}

	private void doInstallSkillTest(Class<? extends Capacity> capType, Class<? extends Capacity>... adds) {
		ClearableReference<Skill> reference = this.provider.installSkill(agent, capType);
		assertNotNull(reference);
		Skill skill = reference.get();
		assertNotNull(skill);
		assertInstanceOf(capType, skill);
		
		ArgumentCaptor<Agent> capturedAgent = ArgumentCaptor.forClass(Agent.class);
		ArgumentCaptor<Class<? extends Capacity>> capturedCapacity = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Skill> capturedSkill = ArgumentCaptor.forClass(Skill.class);
		verify(this.provider, times(1 + adds.length)).registerSkill(capturedAgent.capture(), capturedCapacity.capture(), capturedSkill.capture());
		assertContainsCollection(
				Collections.nCopies(1 + adds.length, this.agent),
				capturedAgent.getAllValues());
		List<Class<? extends Capacity>> caps = new ArrayList<>();
		caps.add(capType);
		caps.addAll(Arrays.asList(adds));
		assertContainsCollection(
				caps,
				capturedCapacity.getAllValues());
		assertContainsCollection(
				Collections.nCopies(1 + adds.length, skill),
				capturedSkill.getAllValues());

		capturedSkill = ArgumentCaptor.forClass(Skill.class);
		verify(this.provider).installSkill(capturedSkill.capture());
		assertSame(skill, capturedSkill.getValue());
	}

	@Test
	public void installSkill_MicroKernelCapacity() {
		doInstallSkillTest(MicroKernelCapacity.class);
	}

	@Test
	public void installSkill_Logging() {
		doInstallSkillTest(Logging.class);
	}

	@Test
	public void installSkill_Time() {
		doInstallSkillTest(Time.class);
	}

	@Test
	public void installSkill_InternalEventBusCapacity() {
		doInstallSkillTest(InternalEventBusCapacity.class);
	}

	@Test
	public void installSkill_Lifecycle() {
		doInstallSkillTest(Lifecycle.class);
	}

	@Test
	public void installSkill_InnerContextAccess() {
		doInstallSkillTest(InnerContextAccess.class);
	}

	@Test
	public void installSkill_Schedules() {
		doInstallSkillTest(Schedules.class, InternalSchedules.class);
	}

	@Test
	public void installSkill_InternalSchedules() {
		doInstallSkillTest(InternalSchedules.class, Schedules.class);
	}

	@Test
	public void installSkill_Behaviors() {
		doInstallSkillTest(Behaviors.class);
	}

	@Test
	public void installSkill_ExternalContextAccess() {
		doInstallSkillTest(ExternalContextAccess.class);
	}

	@Test
	public void installSkill_DefaultContextInteractions() {
		doInstallSkillTest(DefaultContextInteractions.class);
	}

	private static class MockedDynamicSkillProvider extends SreDynamicSkillProvider {
		public MockedDynamicSkillProvider(Injector injector) {
			super(injector);
		}
		@Override
		public ClearableReference<Skill> registerSkill(Agent agent, Class<? extends Capacity> capacity, Skill skill) {
			return new ClearableReference<Skill>(skill);
		}
		@Override
		public void installSkill(Skill skill) {
		}
	}
}