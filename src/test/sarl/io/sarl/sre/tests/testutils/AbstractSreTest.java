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

package io.sarl.sre.tests.testutils;

import java.util.Properties;

import io.sarl.sre.services.executor.ExecutorService;

import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;

import io.sarl.tests.api.AbstractSarlTest;

/**
 * Abstract class that is providing useful tools for unit tests.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public abstract class AbstractSreTest extends AbstractSarlTest {

	private Properties savedProperties;

	/**
	 * This rule permits to clean automatically the fields at the end of the test.
	 */
	@Rule
	public TestWatcher rootSreWatchter = new TestWatcher() {
		@Override
		protected void starting(Description description) {
			// Clear the system properties
			final Properties props = System.getProperties();
			AbstractSreTest.this.savedProperties = (Properties) props.clone();
		}

		@Override
		protected void finished(Description description) {
			Properties sp = AbstractSreTest.this.savedProperties;
			AbstractSreTest.this.savedProperties = null;
			if (sp != null) {
				final Properties props = System.getProperties();
				props.clear();
				props.putAll(sp);
				
			}
		}
	};

	/** Start the given service manually.
	 *
	 * @param service the service to start.
	 */
	protected void startServiceManually(Service service) {
		service.startAsync();
		State state = service.state();
		while (state != null && state != State.STOPPING && state != State.FAILED && state != State.RUNNING) {
			ExecutorService.yield();
			state = service.state();
		}
		if (state == State.FAILED) {
			throw new RuntimeException(service.failureCause());
		}
	}

}
