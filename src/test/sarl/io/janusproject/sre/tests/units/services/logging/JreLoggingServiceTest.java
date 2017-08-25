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
package io.janusproject.sre.tests.units.services.logging;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import io.janusproject.sre.services.logging.JreLoggingService;
import io.janusproject.sre.services.logging.LoggingService;
import io.janusproject.sre.tests.testutils.AbstractJanusTest;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class JreLoggingServiceTest extends AbstractJanusTest {

	@Nullable
	private JreLoggingService service;
	
	@Before
	public void setUp() {
		this.service = new JreLoggingService();
	}

	@Test
	public void getServiceType() {
		assertEquals(LoggingService.class, this.service.getServiceType());
	}

	@Test
	public void getServiceDependencies() {
		assertTrue(this.service.getServiceDependencies().isEmpty());
	}

	@Test
	public void getsServiceWeakDependencies() {
		assertTrue(this.service.getServiceWeakDependencies().isEmpty());
	}

}
