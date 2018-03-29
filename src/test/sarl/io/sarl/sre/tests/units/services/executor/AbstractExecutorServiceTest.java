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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.sarl.sre.services.executor.AbstractExecutorService;
import io.sarl.sre.services.executor.EarlyExitException;
import io.sarl.sre.tests.testutils.AbstractJanusTest;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public abstract class AbstractExecutorServiceTest<T extends AbstractExecutorService> extends AbstractJanusTest {

	@Nullable
	protected ExecutorService executor;

	@Nullable
	protected Logger logger;

	@Nullable
	protected T service;
	
	@Before
	public void setUp() {
		this.logger = mock(Logger.class);
		this.executor = mock(ExecutorService.class);
		this.service = newService();
		this.service.setExecutorService(this.executor);
	}

	protected abstract T newService();

	protected void startService() {
		startServiceManually(this.service);
	}
	
	@Test(expected = EarlyExitException.class)
	public void neverReturn_noPostCommand() {
		io.sarl.sre.services.executor.ExecutorService.neverReturn();
	}

	@Test(expected = EarlyExitException.class)
	public void neverReturn_postCommand() {
		io.sarl.sre.services.executor.ExecutorService.neverReturn(() -> {});
	}

	@Test
	public void applyBlockingConsumer() {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < 100; ++i) {
			list.add(i);
		}
		Set<Integer> set = new TreeSet<>();
		this.service.applyBlockingConsumer(this.logger, list, (it) -> {
			synchronized (set) {
				set.add(it * 10);
			}
		});
		assertEquals(100, set.size());
		for (int i = 0; i < 1000; i += 10) {
			assertTrue(set.contains(i));
		}
		verifyZeroInteractions(this.logger);
	}

	@Test
	public abstract void getServiceDependencies();

	@Test
	public void getServiceType() {
		assertEquals(io.sarl.sre.services.executor.ExecutorService.class, this.service.getServiceType());
	}

	@Test
	public void applyBlockingConsumer_exception_01() {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < 100; ++i) {
			list.add(i);
		}

		try {
			this.service.applyBlockingConsumer(this.logger, list, (it) -> {
				throw new RuntimeException();
			});
			fail("Expecting exception " + RuntimeException.class.getName());
		} catch (RuntimeException ex) {
		}

		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, atLeastOnce()).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertInstanceOf(RuntimeException.class, capturedException.getValue());
	}

	@Test
	public void applyBlockingConsumer_exception_02() {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < 100; ++i) {
			list.add(i);
		}

		this.service.applyBlockingConsumer(this.logger, list, (it) -> {
			io.sarl.sre.services.executor.ExecutorService.neverReturn();
		});
	}

	@Test
	public void applyBlockingConsumer_exception_03() {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < 1; ++i) {
			list.add(i);
		}

		try {
			this.service.applyBlockingConsumer(this.logger, list, (it) -> {
				throw new RuntimeException(new IllegalStateException());
			});
			fail("Expecting exception " + IllegalStateException.class.getName());
		} catch (IllegalStateException ex) {
		}

		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, atLeastOnce()).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertInstanceOf(IllegalStateException.class, capturedException.getValue());
	}

	@Test
	public void executeBlockingTasks_throws_noException() {
		Runnable run1 = mock(Runnable.class);
		Runnable run2 = mock(Runnable.class);
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return null;
		}).when(this.executor).execute(any());
		
		this.service.executeBlockingTasks(this.logger, true, Arrays.asList(run1, run2));

		verify(this.executor, times(2)).execute(any());
		
		verify(run1, only()).run();
		verify(run2, only()).run();
		
		verifyZeroInteractions(this.logger);
	}

	@Test
	public void executeBlockingTasks_throws_exception() {
		final RuntimeException exception = new RuntimeException();
		Runnable run1 = () -> {throw exception;};
		Runnable run2 = mock(Runnable.class);
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return null;
		}).when(this.executor).execute(any());

		try {
			this.service.executeBlockingTasks(this.logger, true, Arrays.asList(run1, run2));
			fail("Expecting exception: " + exception);
		} catch (Throwable ex) {
			assertSame(exception, ex);
		}

		verify(this.executor, times(2)).execute(any());
		
		verify(run2, only()).run();

		verifyZeroInteractions(this.logger);
	}

	@Test
	public void executeBlockingTasks_notThrows_noException() {
		Runnable run1 = mock(Runnable.class);
		Runnable run2 = mock(Runnable.class);
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return null;
		}).when(this.executor).execute(any());
		
		this.service.executeBlockingTasks(this.logger, false, Arrays.asList(run1, run2));

		verify(this.executor, times(2)).execute(any());
		
		verify(run1, only()).run();
		verify(run2, only()).run();
		
		verifyZeroInteractions(this.logger);
	}

	@Test
	public void executeBlockingTasks_notThrows_exception() {
		final RuntimeException exception = new RuntimeException();
		Runnable run1 = () -> {throw exception;};
		Runnable run2 = mock(Runnable.class);
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return null;
		}).when(this.executor).execute(any());

		this.service.executeBlockingTasks(this.logger, false, Arrays.asList(run1, run2));

		verify(this.executor, times(2)).execute(any());
		
		verify(run2, only()).run();

		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(exception, capturedException.getValue());
	}


	@Test
	public void executeBlockingTask_noException_1task() {
		Runnable run = mock(Runnable.class);
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return null;
		}).when(this.executor).execute(any());
		
		assertEquals(1, this.service.executeBlockingTask(this.logger, 1, 100, run));

		verify(this.executor, never()).execute(any());
		
		verify(run, times(1)).run();
		
		verifyZeroInteractions(this.logger);
	}

	@Test
	public void executeBlockingTask_noException_4tasks_1member() {
		Runnable run = mock(Runnable.class);
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return null;
		}).when(this.executor).execute(any());
		
		assertEquals(4, this.service.executeBlockingTask(this.logger, 4, 1, run));

		verify(this.executor, times(4)).execute(any());
		
		verify(run, times(4)).run();
		
		verifyZeroInteractions(this.logger);
	}

	@Test
	public void executeBlockingTask_noException_4tasks_2members() {
		Runnable run = mock(Runnable.class);
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return null;
		}).when(this.executor).execute(any());
		
		assertEquals(4, this.service.executeBlockingTask(this.logger, 4, 2, run));

		verify(this.executor, times(2)).execute(any());
		
		verify(run, times(4)).run();
		
		verifyZeroInteractions(this.logger);
	}

	@Test
	public void executeBlockingTask_noException_4tasks_3members() {
		Runnable run = mock(Runnable.class);
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return null;
		}).when(this.executor).execute(any());
		
		assertEquals(4, this.service.executeBlockingTask(this.logger, 4, 3, run));

		verify(this.executor, times(2)).execute(any());
		
		verify(run, times(4)).run();
		
		verifyZeroInteractions(this.logger);
	}

	@Test
	public void executeBlockingTask_noException_4tasks_4members() {
		Runnable run = mock(Runnable.class);
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return null;
		}).when(this.executor).execute(any());
		
		assertEquals(4, this.service.executeBlockingTask(this.logger, 4, 4, run));

		verify(this.executor, times(1)).execute(any());
		
		verify(run, times(4)).run();
		
		verifyZeroInteractions(this.logger);
	}

	@Test
	public void executeBlockingTask_noException_4tasks_5members() {
		Runnable run = mock(Runnable.class);
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return null;
		}).when(this.executor).execute(any());
		
		assertEquals(4, this.service.executeBlockingTask(this.logger, 4, 5, run));

		verify(this.executor, times(1)).execute(any());
		
		verify(run, times(4)).run();
		
		verifyZeroInteractions(this.logger);
	}

	@Test
	public void executeBlockingTask_exception_1task() {
		RuntimeException exception = mock(RuntimeException.class);
		Runnable run = () -> {throw exception;};
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return null;
		}).when(this.executor).execute(any());
		
		assertEquals(0, this.service.executeBlockingTask(this.logger, 1, 100, run));

		verify(this.executor, never()).execute(any());
		
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(1)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(exception, capturedException.getValue());
	}

	@Test
	public void executeBlockingTask_exception_4tasks_1member() {
		RuntimeException exception = mock(RuntimeException.class);
		Runnable run = () -> {throw exception;};
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return null;
		}).when(this.executor).execute(any());
		
		assertEquals(0, this.service.executeBlockingTask(this.logger, 4, 1, run));

		verify(this.executor, times(4)).execute(any());
	
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(4)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(exception, capturedException.getValue());
	}

	@Test
	public void executeBlockingTask_exception_4tasks_2members() {
		RuntimeException exception = mock(RuntimeException.class);
		Runnable run = () -> {throw exception;};
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return null;
		}).when(this.executor).execute(any());
		
		assertEquals(0, this.service.executeBlockingTask(this.logger, 4, 2, run));

		verify(this.executor, times(2)).execute(any());
		
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(4)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(exception, capturedException.getValue());
	}

	@Test
	public void executeBlockingTask_exception_4tasks_3members() {
		RuntimeException exception = mock(RuntimeException.class);
		Runnable run = () -> {throw exception;};
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return null;
		}).when(this.executor).execute(any());
		
		assertEquals(0, this.service.executeBlockingTask(this.logger, 4, 3, run));

		verify(this.executor, times(2)).execute(any());
		
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(4)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(exception, capturedException.getValue());
	}

	@Test
	public void executeBlockingTask_exception_4tasks_4members() {
		RuntimeException exception = mock(RuntimeException.class);
		Runnable run = () -> {throw exception;};
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return null;
		}).when(this.executor).execute(any());
		
		assertEquals(0, this.service.executeBlockingTask(this.logger, 4, 4, run));

		verify(this.executor, times(1)).execute(any());
		
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(4)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(exception, capturedException.getValue());
	}

	@Test
	public void executeBlockingTask_exception_4tasks_5members() {
		RuntimeException exception = mock(RuntimeException.class);
		Runnable run = () -> {throw exception;};
		doAnswer((it) -> {
			((Runnable) it.getArgument(0)).run();
			return null;
		}).when(this.executor).execute(any());
		
		assertEquals(0, this.service.executeBlockingTask(this.logger, 4, 5, run));

		verify(this.executor, times(1)).execute(any());
		
		ArgumentCaptor<Level> capturedLevel = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Throwable> capturedException = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger, times(4)).log(capturedLevel.capture(), any(), capturedException.capture());
		assertSame(Level.SEVERE, capturedLevel.getValue());
		assertSame(exception, capturedException.getValue());
	}

}
