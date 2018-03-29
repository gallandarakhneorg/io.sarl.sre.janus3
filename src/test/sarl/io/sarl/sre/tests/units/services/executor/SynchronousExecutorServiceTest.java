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
package io.sarl.sre.tests.units.services.executor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.sarl.sre.services.executor.SynchronousExecutorService;
import io.sarl.sre.services.executor.SynchronousExecutorService.JanusScheduledFuture;
import io.sarl.sre.services.time.TimeService;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class SynchronousExecutorServiceTest extends AbstractExecutorServiceTest<SynchronousExecutorService> {

	@Nullable
	private TimeService timeService;
	
	@Override
	protected SynchronousExecutorService newService() {
		return new SynchronousExecutorService();
	}

	protected void moveToTime(long newTime) {
		when(this.timeService.getTime(any())).thenAnswer((it) -> {
			TimeUnit tu = (TimeUnit) it.getArgument(0);
			return (double) tu.convert(newTime, TimeUnit.MILLISECONDS);
		});
		this.service.timeChanged(this.timeService);
	}

	@Override
	public void setUp() {
		this.timeService = mock(TimeService.class);
		when(this.timeService.getTimePrecision()).thenReturn(TimeUnit.MILLISECONDS);
		super.setUp();
		this.service.setTimeService(this.timeService);
	}

	@Override
	public void getServiceDependencies() {
		assertContains(this.service.getServiceDependencies(), TimeService.class);
	}

	@Test
	public void execute() {
		Runnable run = mock(Runnable.class);
		this.service.executeAsap(this.logger, run);

		verify(run).run();

		verifyZeroInteractions(this.logger);
	}
	
	public void execute_exception() {
		FailingRunnable run = spy(new FailingRunnable());
		
		this.service.executeAsap(this.logger, run);

		verify(run).run();
		
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(1)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(run.getException(), capturedException.getValue());
	}

	@Test
	public void submitRunnable() {
		Runnable run = mock(Runnable.class);
		
		Future<?> future = this.service.executeAsap(this.logger, run);
		
		assertNotNull(future);

		verify(run).run();

		verifyZeroInteractions(this.logger);
	}
	
	@Test
	public void submitRunnable_exception() {
		FailingRunnable run = spy(new FailingRunnable());
		
		Future<?> future = this.service.executeAsap(this.logger, run);
		
		assertNotNull(future);

		verify(run).run();
		
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(1)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(run.getException(), capturedException.getValue());
	}

	@Test
	public void submitRunnableFuture() throws Exception {
		UUID value = UUID.randomUUID();
		Runnable run = mock(Runnable.class);
		
		Future<UUID> future = this.service.executeAsap(this.logger, value, run);
		
		assertNotNull(future);
		assertSame(value, future.get());

		verify(run).run();

		verifyZeroInteractions(this.logger);
	}

	@Test
	public void submitRunnableFuture_exception() throws Exception {
		UUID value = UUID.randomUUID();
		FailingRunnable run = spy(new FailingRunnable());
		
		Future<UUID> future = this.service.executeAsap(this.logger, value, run);
		
		assertNotNull(future);
		assertSame(value, future.get());

		verify(run).run();
		
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(1)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(run.getException(), capturedException.getValue());
	}

	@Test
	public void submitCallable() throws Exception {
		Callable<?> run = mock(Callable.class);
		
		Future<?> future = this.service.executeAsap(this.logger, run);
		
		assertNotNull(future);

		verify(run).call();

		verifyZeroInteractions(this.logger);
	}

	@Test
	public void submitCallable_exception() throws Exception {
		FailingCallable run = spy(new FailingCallable());
		
		Future<?> future = this.service.executeAsap(this.logger, run);
		
		assertNotNull(future);

		verify(run).call();
		
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(1)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(run.getException(), capturedException.getValue());
	}

	@Test
	public void scheduleRunnable() {
		Runnable run = mock(Runnable.class);
		
		ScheduledFuture<?> future = this.service.schedule(this.logger, 34, TimeUnit.DAYS, run);
		
		assertNotNull(future);

		verifyZeroInteractions(run);
		
		List<JanusScheduledFuture<?>> tasks = this.service.getScheduledTasks();
		assertNotNull(tasks);
		assertEquals(1, tasks.size());
		assertSame(future, tasks.get(0));
		assertEquals(2937600000l, (long) tasks.get(0).getTime());
		
		moveToTime(2937600000l);
		
		verify(run).run();

		tasks = this.service.getScheduledTasks();
		assertTrue(tasks.isEmpty());

		verifyZeroInteractions(this.logger);
	}

	@Test
	public void scheduleRunnable_exception() {
		FailingRunnable run = spy(new FailingRunnable());
		
		ScheduledFuture<?> future = this.service.schedule(this.logger, 34, TimeUnit.DAYS, run);
		
		assertNotNull(future);

		verifyZeroInteractions(run);
		
		List<JanusScheduledFuture<?>> tasks = this.service.getScheduledTasks();
		assertNotNull(tasks);
		assertEquals(1, tasks.size());
		assertSame(future, tasks.get(0));
		assertEquals(2937600000l, (long) tasks.get(0).getTime());
		
		moveToTime(2937600000l);
		
		verify(run).run();

		tasks = this.service.getScheduledTasks();
		assertTrue(tasks.isEmpty());

		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(1)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(run.getException(), capturedException.getValue());
	}

	@Test
	public void scheduleCallable() throws Exception {
		Callable<?> run = mock(Callable.class);
		
		ScheduledFuture<?> future = this.service.schedule(this.logger, 34, TimeUnit.DAYS, run);
		
		assertNotNull(future);

		verifyZeroInteractions(run);
		
		List<JanusScheduledFuture<?>> tasks = this.service.getScheduledTasks();
		assertNotNull(tasks);
		assertEquals(1, tasks.size());
		assertSame(future, tasks.get(0));
		assertEquals(2937600000l, (long) tasks.get(0).getTime());
		
		moveToTime(2937600000l);
		
		verify(run).call();

		tasks = this.service.getScheduledTasks();
		assertTrue(tasks.isEmpty());

		verifyZeroInteractions(this.logger);
	}

	@Test
	public void scheduleCallable_exception() throws Exception {
		FailingCallable run = spy(new FailingCallable());
		
		ScheduledFuture<?> future = this.service.schedule(this.logger, 34, TimeUnit.DAYS, run);
		
		assertNotNull(future);

		verifyZeroInteractions(run);
		
		List<JanusScheduledFuture<?>> tasks = this.service.getScheduledTasks();
		assertNotNull(tasks);
		assertEquals(1, tasks.size());
		assertSame(future, tasks.get(0));
		assertEquals(2937600000l, (long) tasks.get(0).getTime());
		
		moveToTime(2937600000l);
		
		verify(run).call();

		tasks = this.service.getScheduledTasks();
		assertTrue(tasks.isEmpty());

		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(1)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(run.getException(), capturedException.getValue());
	}

	@Test
	public void scheduleAtFixedRate() {
		Runnable run = mock(Runnable.class);
		
		ScheduledFuture<?> future = this.service.scheduleAtFixedRate(this.logger, 34, 4, TimeUnit.DAYS, run);
		
		assertNotNull(future);

		verifyZeroInteractions(run);
		
		List<JanusScheduledFuture<?>> tasks = this.service.getScheduledTasks();
		assertNotNull(tasks);
		assertEquals(1, tasks.size());
		assertSame(future, tasks.get(0));
		assertEquals(2937600000l, (long) tasks.get(0).getTime());
		
		moveToTime(2937600000l);

		verify(run).run();

		tasks = this.service.getScheduledTasks();
		assertNotNull(tasks);
		assertEquals(1, tasks.size());
		assertSame(future, tasks.get(0));
		assertEquals(3283200000l, (long) tasks.get(0).getTime());

		verifyZeroInteractions(this.logger);
	}

	@Test
	public void scheduleAtFixedRate_exception() {
		FailingRunnable run = spy(new FailingRunnable());
		
		ScheduledFuture<?> future = this.service.scheduleAtFixedRate(this.logger, 34, 4, TimeUnit.DAYS, run);
		
		assertNotNull(future);

		verifyZeroInteractions(run);
		
		List<JanusScheduledFuture<?>> tasks = this.service.getScheduledTasks();
		assertNotNull(tasks);
		assertEquals(1, tasks.size());
		assertSame(future, tasks.get(0));
		assertEquals(2937600000l, (long) tasks.get(0).getTime());
		
		moveToTime(2937600000l);

		verify(run).run();

		tasks = this.service.getScheduledTasks();
		assertTrue(tasks.isEmpty());

		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(1)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(run.getException(), capturedException.getValue());
	}

	@Test
	public void scheduleWithFixedDelay() {
		Runnable run = mock(Runnable.class);
		
		ScheduledFuture<?> future = this.service.scheduleWithFixedDelay(this.logger, 34, 4, TimeUnit.DAYS, run);
		
		assertNotNull(future);

		verifyZeroInteractions(run);
		
		List<JanusScheduledFuture<?>> tasks = this.service.getScheduledTasks();
		assertNotNull(tasks);
		assertEquals(1, tasks.size());
		assertSame(future, tasks.get(0));
		assertEquals(2937600000l, (long) tasks.get(0).getTime());
		
		moveToTime(2937600000l);

		verify(run).run();

		tasks = this.service.getScheduledTasks();
		assertNotNull(tasks);
		assertEquals(1, tasks.size());
		assertSame(future, tasks.get(0));
		assertEquals(3283200000l, (long) tasks.get(0).getTime());

		verifyZeroInteractions(this.logger);
	}

	@Test
	public void scheduleWithFixedDelay_exception() {
		FailingRunnable run = spy(new FailingRunnable());
		
		ScheduledFuture<?> future = this.service.scheduleWithFixedDelay(this.logger, 34, 4, TimeUnit.DAYS, run);
		
		assertNotNull(future);

		verifyZeroInteractions(run);
		
		List<JanusScheduledFuture<?>> tasks = this.service.getScheduledTasks();
		assertNotNull(tasks);
		assertEquals(1, tasks.size());
		assertSame(future, tasks.get(0));
		assertEquals(2937600000l, (long) tasks.get(0).getTime());
		
		moveToTime(2937600000l);

		verify(run).run();

		tasks = this.service.getScheduledTasks();
		assertTrue(tasks.isEmpty());

		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(1)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(run.getException(), capturedException.getValue());
	}

	private static class FailingRunnable implements Runnable {
		private final RuntimeException ex = new RuntimeException();
		@Override
		public void run() {
			throw this.ex;
		}
		public RuntimeException getException() {
			return this.ex;
		}
	}

	private static class FailingCallable implements Callable<Object> {
		private final RuntimeException ex = new RuntimeException();
		@Override
		public Object call() {
			throw this.ex;
		}
		public RuntimeException getException() {
			return this.ex;
		}
	}

}
