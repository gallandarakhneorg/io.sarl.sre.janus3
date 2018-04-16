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

package io.sarl.sre.tests.units.services.time;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.sarl.sre.services.time.JreTimeService;
import io.sarl.sre.services.time.TimeListener;
import io.sarl.sre.services.time.TimeService;
import io.sarl.sre.tests.testutils.AbstractSreTest;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class JreTimeServiceTest extends AbstractSreTest {

	@Nullable
	private JreTimeService service;
	
	@Nullable
	private TimeListener listener;

	@Before
	public void setUp() {
		this.listener = mock(TimeListener.class);
		this.service = new MyTimeService();
		this.service.addTimeListener(this.listener);
	}

	@Test
	public void getTime() {
		assertEquals(0.025, this.service.getTime(TimeUnit.MINUTES));
		assertEquals(1.5, this.service.getTime(TimeUnit.SECONDS));
		assertEquals(1500., this.service.getTime(TimeUnit.MILLISECONDS));
		verifyZeroInteractions(this.listener);
	}

	@Test
	public void getOSTimeFactor() {
		assertEquals(1., this.service.getOSTimeFactor());
		verifyZeroInteractions(this.listener);
	}

	@Test
	public void evolveTimeIfPossible() {
		this.service.evolveTimeIfPossible(15);
		
		ArgumentCaptor<TimeService> serviceCaptor = ArgumentCaptor.forClass(TimeService.class);
		verify(this.listener).timeChanged(serviceCaptor.capture());
		assertSame(this.service, serviceCaptor.getValue());
	}

	private static class MyTimeService extends JreTimeService {
		
		public long getOSCurrentTime() {
			return 1500;
		}
	}

}
