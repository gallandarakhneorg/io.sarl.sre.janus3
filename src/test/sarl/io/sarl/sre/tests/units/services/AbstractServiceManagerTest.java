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

package io.sarl.sre.tests.units.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.Service;

import io.bootique.config.ConfigurationFactory;
import io.sarl.sre.boot.factories.ServiceManagerFactory;
import io.sarl.sre.services.AbstractServiceManager;
import io.sarl.sre.services.AbstractSreService;
import io.sarl.sre.services.infrastructure.InfrastructureService;
import io.sarl.sre.services.logging.LoggerCreator;
import io.sarl.sre.tests.testutils.AbstractSreTest;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public abstract class AbstractServiceManagerTest<T extends AbstractServiceManager> extends AbstractSreTest {

	@Nullable
	private AtomicInteger counter;
	
	@Nullable
	private AtomicInteger counter2;

	@Nullable
	private Serv1 service1;

	@Nullable
	private Serv2 service2;

	@Nullable
	private Serv3 service3;

	@Nullable
	private Serv4 service4;

	@Nullable
	private Serv5 service5;

	@Nullable
	private Serv6 service6;

	@Nullable
	private Logger logger;

	@Nullable
	private LoggerCreator loggerCreator;

	@Nullable
	private ConfigurationFactory configFactory;

	@Nullable
	private ServiceManagerFactory configuration;

	@Nullable
	private T manager;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		this.configuration = mock(ServiceManagerFactory.class);
		when(this.configuration.getStartTimeout()).thenReturn(0l);
		when(this.configuration.getStopTimeout()).thenReturn(0l);

		this.configFactory = mock(ConfigurationFactory.class);
		when(this.configFactory.config(any(Class.class), anyString())).thenReturn(this.configuration);

		this.logger = mock(Logger.class);
		this.loggerCreator = mock(LoggerCreator.class);
		when(this.loggerCreator.createPlatformLogger()).thenReturn(this.logger);
		when(this.loggerCreator.createModuleLogger(any(), any(Logger.class))).thenReturn(this.logger);
		when(this.loggerCreator.createConsoleLogger(any(), any(PrintStream.class))).thenReturn(this.logger);
		this.counter = new AtomicInteger();
		this.counter2 = new AtomicInteger();
		this.service1 = new Serv1Impl();
		this.service2 = new Serv2Impl();
		this.service3 = new Serv3Impl();
		this.service4 = new Serv4Impl();
		this.service5 = new Serv5Impl();
		this.service6 = new Serv6Impl();
		Iterable<? extends Service> services = Arrays.asList(this.service1, this.service2, this.service3, this.service4, this.service5, this.service6);
		this.manager = newServiceManagerInstance(this.loggerCreator, services, this.configFactory);
	}

	protected abstract T newServiceManagerInstance(LoggerCreator loggerCreator, Iterable<? extends Service> services,
			ConfigurationFactory configFactory);

	@Test
	public void startServices() {
		this.manager.startServices(this.logger);
		// Dependencies:
		// 1 ->
		// 2 -> 4, 5
		// 3 Free
		// 4 ->
		// 5 -> 1
		// 6 Infra ->
		//
		// Graph:
		// 6
		// 2 -> 5 -> 1
		//   -> 4
		// 3
		Set<Serv> services = new TreeSet<>((a, b) -> {
			return a.getStartOrder() <= b.getStartOrder() ? -1 : 1;
		});
		services.add(this.service1);
		services.add(this.service2);
		services.add(this.service3);
		services.add(this.service4);
		services.add(this.service5);
		services.add(this.service6);

		Iterator<Serv> iterator = services.iterator();
		assertSame(this.service1, iterator.next());
		assertSame(this.service2, iterator.next());
		assertSame(this.service3, iterator.next());
		assertSame(this.service4, iterator.next());
		assertSame(this.service5, iterator.next());
		assertSame(this.service6, iterator.next());
		assertFalse(iterator.hasNext());
	}
	
	@Test
	public void stopServices() {
		this.manager.startServices(this.logger);
		this.manager.stopServices(this.logger);
		// Dependencies:
		// 1 ->
		// 2 -> 4, 5
		// 3 Free
		// 4 ->
		// 5 -> 1
		// 6 Infra ->
		//
		// Graph:
		// 3
		// 2 -> 5 -> 1
		//   -> 4
		// 6
		Set<Serv> services = new TreeSet<>((a, b) -> {
			return a.getStopOrder() <= b.getStopOrder() ? -1 : 1;
		});
		services.add(this.service1);
		services.add(this.service2);
		services.add(this.service3);
		services.add(this.service4);
		services.add(this.service5);
		services.add(this.service6);

		Iterator<Serv> iterator = services.iterator();
		assertSame(this.service1, iterator.next());
		assertSame(this.service2, iterator.next());
		assertSame(this.service3, iterator.next());
		assertSame(this.service4, iterator.next());
		assertSame(this.service5, iterator.next());
		assertSame(this.service6, iterator.next());
		assertFalse(iterator.hasNext());
	}
	
	private interface Serv extends Service {
		int getStartOrder();
		int getStopOrder();
	}

	private interface Serv1 extends Serv {
	}

	private class Serv1Impl extends AbstractSreService implements Serv1 {
		public int order = -1;
		public int sorder = -1;
		@Override
		public int getStartOrder() {
			return this.order;
		}
		@Override
		public int getStopOrder() {
			return this.sorder;
		}
		@Override
		protected void onStart() {
			this.order = counter.incrementAndGet();
		}
		@Override
		protected void onStop() {
			this.sorder = counter2.incrementAndGet();
		}
		@Override
		public String toString() {
			return super.toString() + " order: " + getStartOrder() + " sorder: " + getStopOrder();
		}
	}

	private interface Serv2 extends Serv {		
	}

	private class Serv2Impl extends AbstractSreService implements Serv2 {
		public int order = -1;
		public int sorder = -1;
		@Override
		public int getStartOrder() {
			return this.order;
		}
		@Override
		public int getStopOrder() {
			return this.sorder;
		}
		@Override
		protected void onStart() {
			this.order = counter.incrementAndGet();
		}
		@Override
		protected void onStop() {
			this.sorder = counter2.incrementAndGet();
		}
		@Override
		public String toString() {
			return super.toString() + " order: " + getStartOrder() + " sorder: " + getStopOrder();
		}
	}

	private interface Serv3 extends Serv {		
	}

	private class Serv3Impl extends AbstractService implements Serv3 {
		public int order = -1;
		public int sorder = -1;
		@Override
		public int getStartOrder() {
			return this.order;
		}
		@Override
		public int getStopOrder() {
			return this.sorder;
		}
		@Override
		protected void doStart() {
			this.order = counter.incrementAndGet();
			notifyStarted();
		}
		@Override
		protected void doStop() {
			this.sorder = counter2.incrementAndGet();
			notifyStopped();
		}
		@Override
		public String toString() {
			return super.toString() + " order: " + getStartOrder() + " sorder: " + getStopOrder();
		}
	}

	private interface Serv4 extends Serv {		
	}

	private class Serv4Impl extends AbstractSreService implements Serv4 {
		public int order = -1;
		public int sorder = -1;
		@Override
		public int getStartOrder() {
			return this.order;
		}
		@Override
		public int getStopOrder() {
			return this.sorder;
		}
		@Override
		protected void onStart() {
			this.order = counter.incrementAndGet();
		}
		@Override
		protected void onStop() {
			this.sorder = counter2.incrementAndGet();
		}
		@Override
		public String toString() {
			return super.toString() + " order: " + getStartOrder() + " sorder: " + getStopOrder();
		}
	}

	private interface Serv5 extends Serv {		
	}

	private class Serv5Impl extends AbstractSreService implements Serv5 {
		public int order = -1;
		public int sorder = -1;
		@Override
		public int getStartOrder() {
			return this.order;
		}
		@Override
		public int getStopOrder() {
			return this.sorder;
		}
		@Override
		protected void onStart() {
			this.order = counter.incrementAndGet();
		}
		@Override
		protected void onStop() {
			this.sorder = counter2.incrementAndGet();
		}
		@Override
		public String toString() {
			return super.toString() + " order: " + getStartOrder() + " sorder: " + getStopOrder();
		}
	}

	private interface Serv6 extends Serv, InfrastructureService {
	}

	private class Serv6Impl extends AbstractSreService implements Serv6 {
		public int order = -1;
		public int sorder = -1;
		@Override
		public int getStartOrder() {
			return this.order;
		}
		@Override
		public int getStopOrder() {
			return this.sorder;
		}
		@Override
		protected void onStart() {
			this.order = counter.incrementAndGet();
		}
		@Override
		protected void onStop() {
			this.sorder = counter2.incrementAndGet();
		}
		@Override
		public String toString() {
			return super.toString() + " order: " + getStartOrder() + " sorder: " + getStopOrder();
		}
	}
}
