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

package io.sarl.sre.tests.units.services.executor;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.sarl.sre.services.executor.SreCallable;
import io.sarl.sre.services.executor.SreRunnable;
import io.sarl.sre.services.executor.JreExecutorService;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class JreExecutorServiceTest extends AbstractExecutorServiceTest<JreExecutorService> {

	@Nullable
	private UncaughtExceptionHandler handler;

	@Nullable
	private ScheduledExecutorService scheduledExecutor;

	@Override
	protected JreExecutorService newService() {
		return new JreExecutorService();
	}

	@Before
	public void setUp() {
		super.setUp();
		this.handler = mock(UncaughtExceptionHandler.class);
		this.service.setUncaughtExceptionHandler(this.handler);
		this.scheduledExecutor = mock(ScheduledExecutorService.class);
		this.service.setScheduledExecutorService(scheduledExecutor);
	}

	@Override
	public void getServiceDependencies() {
		assertTrue(this.service.getServiceDependencies().isEmpty());
	}

	@Test
	public void execute() {
		Runnable run = mock(Runnable.class);
		this.service.executeAsap(this.logger, run);

		ArgumentCaptor<Runnable> capturedRunnable = ArgumentCaptor.forClass(Runnable.class);
		verify(this.executor).submit(capturedRunnable.capture());
		assertInstanceOf(SreRunnable.class, capturedRunnable.getValue());
		SreRunnable jr = (SreRunnable) capturedRunnable.getValue();
		assertSame(logger, jr.getLogger());
		assertSame(run, jr.getWrappedRunnable());

		verifyZeroInteractions(this.logger);
	}
	
	public void execute_exception() {
		RuntimeException exception = mock(RuntimeException.class);
		Runnable run = () -> {throw exception;};
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return null;
		}).when(this.executor).submit(any(Runnable.class));
		
		this.service.executeAsap(this.logger, run);

		ArgumentCaptor<Runnable> capturedRunnable = ArgumentCaptor.forClass(Runnable.class);
		verify(this.executor).submit(capturedRunnable.capture());
		assertInstanceOf(SreRunnable.class, capturedRunnable.getValue());
		SreRunnable jr = (SreRunnable) capturedRunnable.getValue();
		assertSame(logger, jr.getLogger());
		assertSame(run, jr.getWrappedRunnable());
		
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(1)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(exception, capturedException.getValue());
	}

	@Test
	public void submitRunnable() {
		Future future = mock(Future.class);
		Runnable run = mock(Runnable.class);
		when(this.executor.submit(any(Runnable.class))).thenReturn(future);
		
		Future<?> rfuture = this.service.executeAsap(this.logger, run);
		
		assertSame(future, rfuture);

		ArgumentCaptor<Runnable> capturedRunnable = ArgumentCaptor.forClass(Runnable.class);
		verify(this.executor).submit(capturedRunnable.capture());
		assertInstanceOf(SreRunnable.class, capturedRunnable.getValue());
		SreRunnable jr = (SreRunnable) capturedRunnable.getValue();
		assertSame(logger, jr.getLogger());
		assertSame(run, jr.getWrappedRunnable());

		verifyZeroInteractions(this.logger);
	}
	
	@Test
	public void submitRunnable_exception() {
		Future future = mock(Future.class);
		RuntimeException exception = mock(RuntimeException.class);
		Runnable run = () -> {throw exception;};
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return future;
		}).when(this.executor).submit(any(Runnable.class));
		
		Future<?> rfuture = this.service.executeAsap(this.logger, run);
		
		assertSame(future, rfuture);

		ArgumentCaptor<Runnable> capturedRunnable = ArgumentCaptor.forClass(Runnable.class);
		verify(this.executor).submit(capturedRunnable.capture());
		assertInstanceOf(SreRunnable.class, capturedRunnable.getValue());
		SreRunnable jr = (SreRunnable) capturedRunnable.getValue();
		assertSame(logger, jr.getLogger());
		assertSame(run, jr.getWrappedRunnable());
		
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(1)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(exception, capturedException.getValue());
	}

	@Test
	public void submitRunnableFuture() {
		Future future = mock(Future.class);
		Runnable run = mock(Runnable.class);
		when(this.executor.submit(any(Runnable.class), any())).thenReturn(future);
		
		Future<?> rfuture = this.service.executeAsap(this.logger, future, run);
		
		assertSame(future, rfuture);

		ArgumentCaptor<Runnable> capturedRunnable = ArgumentCaptor.forClass(Runnable.class);
		ArgumentCaptor<Future<?>> capturedFuture = ArgumentCaptor.forClass(Future.class);
		verify(this.executor).submit(capturedRunnable.capture(), capturedFuture.capture());
		assertInstanceOf(SreRunnable.class, capturedRunnable.getValue());
		SreRunnable jr = (SreRunnable) capturedRunnable.getValue();
		assertSame(logger, jr.getLogger());
		assertSame(run, jr.getWrappedRunnable());
		assertSame(future, capturedFuture.getValue());

		verifyZeroInteractions(this.logger);
	}

	@Test
	public void submitRunnableFuture_exception() {
		Future future = mock(Future.class);
		RuntimeException exception = mock(RuntimeException.class);
		Runnable run = () -> {throw exception;};
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return future;
		}).when(this.executor).submit(any(Runnable.class), any(Future.class));
		
		Future<?> rfuture = this.service.executeAsap(this.logger, future, run);
		
		assertSame(future, rfuture);

		ArgumentCaptor<Runnable> capturedRunnable = ArgumentCaptor.forClass(Runnable.class);
		ArgumentCaptor<Future<?>> capturedFuture = ArgumentCaptor.forClass(Future.class);
		verify(this.executor).submit(capturedRunnable.capture(), capturedFuture.capture());
		assertInstanceOf(SreRunnable.class, capturedRunnable.getValue());
		SreRunnable jr = (SreRunnable) capturedRunnable.getValue();
		assertSame(logger, jr.getLogger());
		assertSame(run, jr.getWrappedRunnable());
		assertSame(future, capturedFuture.getValue());
		
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(1)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(exception, capturedException.getValue());
	}

	@Test
	public void submitCallable() {
		Future future = mock(Future.class);
		Callable<?> run = mock(Callable.class);
		when(this.executor.submit(any(Callable.class))).thenReturn(future);
		
		Future<?> rfuture = this.service.executeAsap(this.logger, run);
		
		assertSame(future, rfuture);

		ArgumentCaptor<Callable> capturedCallable = ArgumentCaptor.forClass(Callable.class);
		verify(this.executor).submit(capturedCallable.capture());
		assertInstanceOf(SreCallable.class, capturedCallable.getValue());
		SreCallable jc = (SreCallable) capturedCallable.getValue();
		assertSame(logger, jc.getLogger());
		assertSame(run, jc.getWrappedCallable());

		verifyZeroInteractions(this.logger);
	}

	@Test
	public void submitCallable_exception() {
		Future future = mock(Future.class);
		RuntimeException exception = mock(RuntimeException.class);
		Callable<?> run = () -> {throw exception;};
		doAnswer((it) -> {
			((Callable) it.getArgument(0)).call();
			return future;
		}).when(this.executor).submit(any(Callable.class));
		
		Future<?> rfuture = this.service.executeAsap(this.logger, run);
		
		assertSame(future, rfuture);

		ArgumentCaptor<Callable> capturedCallable = ArgumentCaptor.forClass(Callable.class);
		verify(this.executor).submit(capturedCallable.capture());
		assertInstanceOf(SreCallable.class, capturedCallable.getValue());
		SreCallable jc = (SreCallable) capturedCallable.getValue();
		assertSame(logger, jc.getLogger());
		assertSame(run, jc.getWrappedCallable());
		
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(1)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(exception, capturedException.getValue());
	}

	@Test
	public void scheduleRunnable() {
		ScheduledFuture future = mock(ScheduledFuture.class);
		Runnable run = mock(Runnable.class);
		when(this.scheduledExecutor.schedule(any(Runnable.class), anyLong(), any())).thenReturn(future);
		
		ScheduledFuture<?> rfuture = this.service.schedule(this.logger, 34, TimeUnit.DAYS, run);
		
		assertSame(future, rfuture);

		ArgumentCaptor<Runnable> capturedRunnable = ArgumentCaptor.forClass(Runnable.class);
		ArgumentCaptor<Long> capturedDelay = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<TimeUnit> capturedUnit = ArgumentCaptor.forClass(TimeUnit.class);
		verify(this.scheduledExecutor).schedule(capturedRunnable.capture(), capturedDelay.capture(), capturedUnit.capture());
		assertInstanceOf(SreRunnable.class, capturedRunnable.getValue());
		SreRunnable jr = (SreRunnable) capturedRunnable.getValue();
		assertSame(logger, jr.getLogger());
		assertSame(run, jr.getWrappedRunnable());
		Assert.assertEquals(34, capturedDelay.getValue().longValue());
		assertEquals(TimeUnit.DAYS, capturedUnit.getValue());

		verifyZeroInteractions(this.logger);
	}

	@Test
	public void scheduleRunnable_exception() {
		ScheduledFuture future = mock(ScheduledFuture.class);
		RuntimeException exception = mock(RuntimeException.class);
		Runnable run = () -> {throw exception;};
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return future;
		}).when(this.scheduledExecutor).schedule(any(Runnable.class), anyLong(), any());
		
		ScheduledFuture<?> rfuture = this.service.schedule(this.logger, 34, TimeUnit.DAYS, run);
		
		assertSame(future, rfuture);

		ArgumentCaptor<Runnable> capturedRunnable = ArgumentCaptor.forClass(Runnable.class);
		ArgumentCaptor<Long> capturedDelay = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<TimeUnit> capturedUnit = ArgumentCaptor.forClass(TimeUnit.class);
		verify(this.scheduledExecutor).schedule(capturedRunnable.capture(), capturedDelay.capture(), capturedUnit.capture());
		assertInstanceOf(SreRunnable.class, capturedRunnable.getValue());
		SreRunnable jr = (SreRunnable) capturedRunnable.getValue();
		assertSame(logger, jr.getLogger());
		assertSame(run, jr.getWrappedRunnable());
		Assert.assertEquals(34, capturedDelay.getValue().longValue());
		assertEquals(TimeUnit.DAYS, capturedUnit.getValue());
		
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(1)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(exception, capturedException.getValue());
	}

	@Test
	public void scheduleCallable() {
		ScheduledFuture future = mock(ScheduledFuture.class);
		Callable<?> run = mock(Callable.class);
		when(this.scheduledExecutor.schedule(any(Callable.class), anyLong(), any())).thenReturn(future);
		
		ScheduledFuture<?> rfuture = this.service.schedule(this.logger, 34, TimeUnit.DAYS, run);
		
		assertSame(future, rfuture);

		ArgumentCaptor<Callable<?>> capturedCallable = ArgumentCaptor.forClass(Callable.class);
		ArgumentCaptor<Long> capturedDelay = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<TimeUnit> capturedUnit = ArgumentCaptor.forClass(TimeUnit.class);
		verify(this.scheduledExecutor).schedule(capturedCallable.capture(), capturedDelay.capture(), capturedUnit.capture());
		assertInstanceOf(SreCallable.class, capturedCallable.getValue());
		SreCallable jc = (SreCallable) capturedCallable.getValue();
		assertSame(logger, jc.getLogger());
		assertSame(run, jc.getWrappedCallable());
		Assert.assertEquals(34, capturedDelay.getValue().longValue());
		assertEquals(TimeUnit.DAYS, capturedUnit.getValue());

		verifyZeroInteractions(this.logger);
	}

	@Test
	public void scheduleCallable_exception() {
		ScheduledFuture future = mock(ScheduledFuture.class);
		RuntimeException exception = mock(RuntimeException.class);
		Callable<?> run = () -> {throw exception;};
		doAnswer((it) -> {
			((Callable) it.getArgument(0)).call();
			return future;
		}).when(this.scheduledExecutor).schedule(any(Callable.class), anyLong(), any());
		
		ScheduledFuture<?> rfuture = this.service.schedule(this.logger, 34, TimeUnit.DAYS, run);
		
		assertSame(future, rfuture);

		ArgumentCaptor<Callable<?>> capturedCallable = ArgumentCaptor.forClass(Callable.class);
		ArgumentCaptor<Long> capturedDelay = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<TimeUnit> capturedUnit = ArgumentCaptor.forClass(TimeUnit.class);
		verify(this.scheduledExecutor).schedule(capturedCallable.capture(), capturedDelay.capture(), capturedUnit.capture());
		assertInstanceOf(SreCallable.class, capturedCallable.getValue());
		SreCallable jc = (SreCallable) capturedCallable.getValue();
		assertSame(logger, jc.getLogger());
		assertSame(run, jc.getWrappedCallable());
		Assert.assertEquals(34, capturedDelay.getValue().longValue());
		assertEquals(TimeUnit.DAYS, capturedUnit.getValue());
		
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(1)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(exception, capturedException.getValue());
	}

	@Test
	public void scheduleAtFixedRate() {
		ScheduledFuture future = mock(ScheduledFuture.class);
		Runnable run = mock(Runnable.class);
		when(this.scheduledExecutor.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any())).thenReturn(future);
		
		ScheduledFuture<?> rfuture = this.service.scheduleAtFixedRate(this.logger, 12, 34, TimeUnit.DAYS, run);
		
		assertSame(future, rfuture);

		ArgumentCaptor<Runnable> capturedRunnable = ArgumentCaptor.forClass(Runnable.class);
		ArgumentCaptor<Long> capturedDelay = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<Long> capturedPeriod = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<TimeUnit> capturedUnit = ArgumentCaptor.forClass(TimeUnit.class);
		verify(this.scheduledExecutor).scheduleAtFixedRate(capturedRunnable.capture(), capturedDelay.capture(), 
				capturedPeriod.capture(), capturedUnit.capture());
		assertInstanceOf(SreRunnable.class, capturedRunnable.getValue());
		SreRunnable jr = (SreRunnable) capturedRunnable.getValue();
		assertSame(logger, jr.getLogger());
		assertSame(run, jr.getWrappedRunnable());
		Assert.assertEquals(12, capturedDelay.getValue().longValue());
		Assert.assertEquals(34, capturedPeriod.getValue().longValue());
		assertEquals(TimeUnit.DAYS, capturedUnit.getValue());

		verifyZeroInteractions(this.logger);
	}

	@Test
	public void scheduleAtFixedRate_exception() {
		ScheduledFuture future = mock(ScheduledFuture.class);
		RuntimeException exception = mock(RuntimeException.class);
		Runnable run = () -> {throw exception;};
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return future;
		}).when(this.scheduledExecutor).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any());
		
		ScheduledFuture<?> rfuture = this.service.scheduleAtFixedRate(this.logger, 12, 34, TimeUnit.DAYS, run);
		
		assertSame(future, rfuture);

		ArgumentCaptor<Runnable> capturedRunnable = ArgumentCaptor.forClass(Runnable.class);
		ArgumentCaptor<Long> capturedDelay = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<Long> capturedPeriod = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<TimeUnit> capturedUnit = ArgumentCaptor.forClass(TimeUnit.class);
		verify(this.scheduledExecutor).scheduleAtFixedRate(capturedRunnable.capture(), capturedDelay.capture(), 
				capturedPeriod.capture(), capturedUnit.capture());
		assertInstanceOf(SreRunnable.class, capturedRunnable.getValue());
		SreRunnable jr = (SreRunnable) capturedRunnable.getValue();
		assertSame(logger, jr.getLogger());
		assertSame(run, jr.getWrappedRunnable());
		Assert.assertEquals(12, capturedDelay.getValue().longValue());
		Assert.assertEquals(34, capturedPeriod.getValue().longValue());
		assertEquals(TimeUnit.DAYS, capturedUnit.getValue());
		
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(1)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(exception, capturedException.getValue());
	}

	@Test
	public void scheduleWithFixedDelay() {
		ScheduledFuture future = mock(ScheduledFuture.class);
		Runnable run = mock(Runnable.class);
		when(this.scheduledExecutor.scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any())).thenReturn(future);
		
		ScheduledFuture<?> rfuture = this.service.scheduleWithFixedDelay(this.logger, 12, 34, TimeUnit.DAYS, run);
		
		assertSame(future, rfuture);

		ArgumentCaptor<Runnable> capturedRunnable = ArgumentCaptor.forClass(Runnable.class);
		ArgumentCaptor<Long> capturedDelay = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<Long> capturedPeriod = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<TimeUnit> capturedUnit = ArgumentCaptor.forClass(TimeUnit.class);
		verify(this.scheduledExecutor).scheduleWithFixedDelay(capturedRunnable.capture(), capturedDelay.capture(), 
				capturedPeriod.capture(), capturedUnit.capture());
		assertInstanceOf(SreRunnable.class, capturedRunnable.getValue());
		SreRunnable jr = (SreRunnable) capturedRunnable.getValue();
		assertSame(logger, jr.getLogger());
		assertSame(run, jr.getWrappedRunnable());
		Assert.assertEquals(12, capturedDelay.getValue().longValue());
		Assert.assertEquals(34, capturedPeriod.getValue().longValue());
		assertEquals(TimeUnit.DAYS, capturedUnit.getValue());

		verifyZeroInteractions(this.logger);
	}

	@Test
	public void scheduleWithFixedDelay_exception() {
		ScheduledFuture future = mock(ScheduledFuture.class);
		RuntimeException exception = mock(RuntimeException.class);
		Runnable run = () -> {throw exception;};
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return future;
		}).when(this.scheduledExecutor).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any());
		
		ScheduledFuture<?> rfuture = this.service.scheduleWithFixedDelay(this.logger, 12, 34, TimeUnit.DAYS, run);
		
		assertSame(future, rfuture);

		ArgumentCaptor<Runnable> capturedRunnable = ArgumentCaptor.forClass(Runnable.class);
		ArgumentCaptor<Long> capturedDelay = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<Long> capturedPeriod = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<TimeUnit> capturedUnit = ArgumentCaptor.forClass(TimeUnit.class);
		verify(this.scheduledExecutor).scheduleWithFixedDelay(capturedRunnable.capture(), capturedDelay.capture(), 
				capturedPeriod.capture(), capturedUnit.capture());
		assertInstanceOf(SreRunnable.class, capturedRunnable.getValue());
		SreRunnable jr = (SreRunnable) capturedRunnable.getValue();
		assertSame(logger, jr.getLogger());
		assertSame(run, jr.getWrappedRunnable());
		Assert.assertEquals(12, capturedDelay.getValue().longValue());
		Assert.assertEquals(34, capturedPeriod.getValue().longValue());
		assertEquals(TimeUnit.DAYS, capturedUnit.getValue());
		
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(1)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(exception, capturedException.getValue());
	}

}
