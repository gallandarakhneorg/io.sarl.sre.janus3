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
package io.janusproject.sre.tests.units.services.executor;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import io.janusproject.sre.services.executor.ExecutorService;
import io.janusproject.sre.services.executor.VerboseThreadExecutorPolicy;
import io.janusproject.sre.services.logging.LoggingService;
import io.janusproject.sre.tests.testutils.AbstractJanusTest;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@RunWith(Suite.class)
@SuiteClasses({
	VerboseThreadExecutorPolicyTest.NotShutDownTest.class,
	VerboseThreadExecutorPolicyTest.ShutDownTest.class
})
@SuppressWarnings("all")
public class VerboseThreadExecutorPolicyTest {

	public static class NotShutDownTest extends AbstractJanusTest {
	
		@Nullable
		private LoggingService logService;

		@Nullable
		private Logger logger;

		@Nullable
		private ThreadPoolExecutor executor;

		@Nullable
		private VerboseThreadExecutorPolicy handler;

		@Before
		public void setUp() {
			this.logger = mock(Logger.class);
			when(this.logger.isLoggable(ArgumentMatchers.any(Level.class))).thenReturn(true);
			this.logService = mock(LoggingService.class);
			when(this.logService.getKernelLogger()).thenReturn(this.logger);

			this.executor = mock(ThreadPoolExecutor.class);
			when(this.executor.isShutdown()).thenReturn(false);

			this.handler = new VerboseThreadExecutorPolicy(this.logService);
		}

		@Test
		public void rejectedExecution() {
			Runnable runnable = mock(Runnable.class);
			this.handler.rejectedExecution(runnable, this.executor);

			verify(runnable, only()).run();

			ArgumentCaptor<Runnable> argument0 = ArgumentCaptor.forClass(Runnable.class);
			verify(this.executor, never()).submit(argument0.capture());
			
			ArgumentCaptor<LogRecord> argument1 = ArgumentCaptor.forClass(LogRecord.class);
			verify(this.logger, never()).log(argument1.capture());
		}

		@Test
		public void uncaughtException_Exception() {
			Exception e = new Exception();
			e.fillInStackTrace();
			this.handler.uncaughtException(Thread.currentThread(), e);

			ArgumentCaptor<LogRecord> argument = ArgumentCaptor.forClass(LogRecord.class);
			verify(this.logger).log(argument.capture());
			assertSame(Level.SEVERE, argument.getValue().getLevel());
			assertSame(e, argument.getValue().getThrown());
			assertEquals(getClass().getName(), argument.getValue().getSourceClassName());
			assertEquals("uncaughtException_Exception", argument.getValue().getSourceMethodName()); //$NON-NLS-1$
		}

		@Test
		public void uncaughtException_EarlyExit() {
			try {
				ExecutorService.neverReturn();
				fail("Early exit exception is expected");
			} catch (Exception e) {
				this.handler.uncaughtException(Thread.currentThread(), e);
				verifyZeroInteractions(this.logService);
			}
		}

		@Test
		public void uncaughtException_Cancellation() {
			Exception e = new CancellationException();
			e.fillInStackTrace();
			this.handler.uncaughtException(Thread.currentThread(), e);

			ArgumentCaptor<LogRecord> argument = ArgumentCaptor.forClass(LogRecord.class);
			verify(this.logger).log(argument.capture());
			assertSame(Level.FINEST, argument.getValue().getLevel());
			assertSame(e, argument.getValue().getThrown());
			assertEquals(getClass().getName(), argument.getValue().getSourceClassName());
			assertEquals("uncaughtException_Cancellation", argument.getValue().getSourceMethodName()); //$NON-NLS-1$
		}

		@Test
		public void uncaughtException_Interrupt() {
			Exception e = new InterruptedException();
			e.fillInStackTrace();
			this.handler.uncaughtException(Thread.currentThread(), e);

			ArgumentCaptor<LogRecord> argument = ArgumentCaptor.forClass(LogRecord.class);
			verify(this.logger).log(argument.capture());
			assertSame(Level.FINEST, argument.getValue().getLevel());
			assertSame(e, argument.getValue().getThrown());
			assertEquals(getClass().getName(), argument.getValue().getSourceClassName());
			assertEquals("uncaughtException_Interrupt", argument.getValue().getSourceMethodName()); //$NON-NLS-1$
		}

	}

	public static class ShutDownTest extends AbstractJanusTest {
		
		@Nullable
		private LoggingService logService;

		@Nullable
		private Logger logger;

		@Nullable
		private ThreadPoolExecutor executor;

		@Nullable
		private VerboseThreadExecutorPolicy handler;

		@Before
		public void setUp() {
			this.logger = mock(Logger.class);
			when(this.logger.isLoggable(ArgumentMatchers.any(Level.class))).thenReturn(true);
			this.logService = mock(LoggingService.class);
			when(this.logService.getKernelLogger()).thenReturn(this.logger);

			this.executor = mock(ThreadPoolExecutor.class);
			when(this.executor.isShutdown()).thenReturn(true);

			this.handler = new VerboseThreadExecutorPolicy(this.logService);
		}

		@Test
		public void rejectedExecution() {
			Runnable runnable = mock(Runnable.class);
			this.handler.rejectedExecution(runnable, this.executor);

			verify(runnable, never()).run();

			ArgumentCaptor<Runnable> argument0 = ArgumentCaptor.forClass(Runnable.class);
			verify(this.executor, never()).submit(argument0.capture());
			
			ArgumentCaptor<LogRecord> argument1 = ArgumentCaptor.forClass(LogRecord.class);
			verify(this.logger).log(argument1.capture());
			assertSame(Level.FINE, argument1.getValue().getLevel());
		}

		@Test
		public void uncaughtException_Exception() {
			Exception e = new Exception();
			e.fillInStackTrace();
			this.handler.uncaughtException(Thread.currentThread(), e);

			ArgumentCaptor<LogRecord> argument = ArgumentCaptor.forClass(LogRecord.class);
			verify(this.logger).log(argument.capture());
			assertSame(Level.SEVERE, argument.getValue().getLevel());
			assertSame(e, argument.getValue().getThrown());
			assertEquals(getClass().getName(), argument.getValue().getSourceClassName());
			assertEquals("uncaughtException_Exception", argument.getValue().getSourceMethodName()); //$NON-NLS-1$
		}

		@Test
		public void uncaughtException_EarlyExit() {
			try {
				ExecutorService.neverReturn();
				fail("Early exit exception is expected");
			} catch (Exception e) {
				this.handler.uncaughtException(Thread.currentThread(), e);
				verifyZeroInteractions(this.logService);
			}
		}

		@Test
		public void uncaughtException_Cancellation() {
			Exception e = new CancellationException();
			e.fillInStackTrace();
			this.handler.uncaughtException(Thread.currentThread(), e);

			ArgumentCaptor<LogRecord> argument = ArgumentCaptor.forClass(LogRecord.class);
			verify(this.logger).log(argument.capture());
			assertSame(Level.FINEST, argument.getValue().getLevel());
			assertSame(e, argument.getValue().getThrown());
			assertEquals(getClass().getName(), argument.getValue().getSourceClassName());
			assertEquals("uncaughtException_Cancellation", argument.getValue().getSourceMethodName()); //$NON-NLS-1$
		}

		@Test
		public void uncaughtException_Interrupt() {
			Exception e = new InterruptedException();
			e.fillInStackTrace();
			this.handler.uncaughtException(Thread.currentThread(), e);

			ArgumentCaptor<LogRecord> argument = ArgumentCaptor.forClass(LogRecord.class);
			verify(this.logger).log(argument.capture());
			assertSame(Level.FINEST, argument.getValue().getLevel());
			assertSame(e, argument.getValue().getThrown());
			assertEquals(getClass().getName(), argument.getValue().getSourceClassName());
			assertEquals("uncaughtException_Interrupt", argument.getValue().getSourceMethodName()); //$NON-NLS-1$
		}

	}

}
