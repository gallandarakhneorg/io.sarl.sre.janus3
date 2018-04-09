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

package io.sarl.sre.tests.units.services.lifecycle;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.inject.Injector;
import com.google.inject.Module;

import io.sarl.sre.services.executor.ExecutorService;
import io.sarl.sre.services.lifecycle.InjectionBasedLifecycleService;
import io.sarl.sre.services.logging.LoggingService;
import io.sarl.lang.core.DynamicSkillProvider;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class InjectionBasedLifecycleServiceTest extends AbstractLifecycleServiceTest<InjectionBasedLifecycleService> {

	@Nullable
	private Injector injector;
	
	@Nullable
	private Injector childInjector;

	@Nullable
	private DynamicSkillProvider skillProvider;

	@Override
	public void setUp() {
		this.injector = mock(Injector.class);
		this.childInjector = mock(Injector.class);
		this.skillProvider = mock(DynamicSkillProvider.class);
		super.setUp();
		when(this.injector.createChildInjector(any(Module.class))).thenReturn(this.childInjector);
		when(this.childInjector.getInstance(any(Class.class))).thenReturn(this.agent2);
	}
	
	@Override
	protected InjectionBasedLifecycleService newService() {
		return new InjectionBasedLifecycleService(this.injector, this.checker, this.skillProvider);
	}

	@Override
	public void getServiceDependencies() {
		assertContains(this.service.getServiceDependencies(), ExecutorService.class, LoggingService.class);
	}


}
