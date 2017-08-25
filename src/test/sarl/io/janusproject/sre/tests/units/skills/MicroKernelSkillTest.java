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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.google.common.util.concurrent.Service;

import io.janusproject.sre.Kernel;
import io.janusproject.sre.skills.MicroKernelSkill;
import io.janusproject.sre.tests.testutils.AbstractJanusTest;
import io.sarl.lang.core.Agent;
import io.sarl.tests.api.ManualMocking;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@ManualMocking
public class MicroKernelSkillTest extends AbstractJanusTest {

	@Nullable
	private Kernel kernel;

	@Nullable
	private Service service;

	@Nullable
	private Agent agent;

	@Nullable
	private MicroKernelSkill skill;

	@Before
	public void setUp() throws Exception {
		this.service = mock(Service.class);
		this.kernel = mock(Kernel.class);
		when(this.kernel.getService(any())).thenReturn(this.service);
		this.agent = mock(Agent.class);
		this.skill = new MicroKernelSkill(this.agent, this.kernel);
	}

	@Test
	public void getService_0() {
		assertNull(this.skill.getService(null));
	}

	@Test
	public void getService_1() {
		assertSame(this.service, this.skill.getService(Service.class));
	}

}
