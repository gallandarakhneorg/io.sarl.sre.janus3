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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import io.sarl.sre.services.executor.ExecutorService;
import io.sarl.sre.services.executor.JanusRunnable;
import io.sarl.sre.tests.testutils.AbstractJanusTest;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class JanusRunnableTest extends AbstractJanusTest {

	@Mock
	private Logger logger;
	
	@Test
	public void run_logger_successTask() throws Exception {
		AtomicBoolean run = new AtomicBoolean();
		JanusRunnable runnable = new JanusRunnable(() -> {run.set(true);}, this.logger);
		runnable.run();
		assertTrue(runnable.isSuccess());
		assertTrue(run.get());
		verifyNoMoreInteractions(this.logger);
	}

	@Test
	public void run_logger_earlyExit1() throws Exception {
		AtomicBoolean run = new AtomicBoolean();
		JanusRunnable runnable = new JanusRunnable(() -> {run.set(true); ExecutorService.neverReturn();}, this.logger);
		runnable.run();
		assertTrue(runnable.isSuccess());
		assertTrue(run.get());
		verifyNoMoreInteractions(this.logger);
	}

	@Test
	public void run_logger_earlyExit2() throws Exception {
		AtomicBoolean run = new AtomicBoolean();
		JanusRunnable runnable = new JanusRunnable(() -> {ExecutorService.neverReturn(); run.set(true);}, this.logger);
		runnable.run();
		assertTrue(runnable.isSuccess());
		assertFalse(run.get());
		verifyNoMoreInteractions(this.logger);
	}

	@Test
	public void run_logger_exception() throws Exception {
		AtomicBoolean run = new AtomicBoolean();
		JanusRunnable runnable = new JanusRunnable(() -> {run.set(true); throw new IllegalStateException();}, this.logger);
		runnable.run();
		assertFalse(runnable.isSuccess());
		assertTrue(run.get());
		
		ArgumentCaptor<Level> level = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<String> msg = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Throwable> ex = ArgumentCaptor.forClass(Throwable.class);
		verify(this.logger).log(level.capture(), msg.capture(), ex.capture());
		assertSame(Level.SEVERE, level.getValue());
		assertEquals(IllegalStateException.class.getSimpleName(), msg.getValue());
		assertInstanceOf(IllegalStateException.class, ex.getValue());
	}

	@Test
	public void run_noLogger_successTask() throws Exception {
		AtomicBoolean run = new AtomicBoolean();
		JanusRunnable runnable = new JanusRunnable(() -> {run.set(true);}, null);
		runnable.run();
		assertTrue(runnable.isSuccess());
		assertTrue(run.get());
		verifyNoMoreInteractions(this.logger);
	}

	@Test
	public void run_noLogger_earlyExit1() throws Exception {
		AtomicBoolean run = new AtomicBoolean();
		JanusRunnable runnable = new JanusRunnable(() -> {run.set(true); ExecutorService.neverReturn();}, null);
		runnable.run();
		assertTrue(runnable.isSuccess());
		assertTrue(run.get());
		verifyNoMoreInteractions(this.logger);
	}

	@Test
	public void run_noLogger_earlyExit2() throws Exception {
		AtomicBoolean run = new AtomicBoolean();
		JanusRunnable runnable = new JanusRunnable(() -> {ExecutorService.neverReturn(); run.set(true);}, null);
		runnable.run();
		assertTrue(runnable.isSuccess());
		assertFalse(run.get());
		verifyNoMoreInteractions(this.logger);
	}

	@Test
	public void run_noLogger_exception() throws Exception {
		AtomicBoolean run = new AtomicBoolean();
		JanusRunnable runnable = new JanusRunnable(() -> {run.set(true); throw new IllegalStateException();}, null);
		try {
			runnable.run();
			fail("Expecting exception " + IllegalStateException.class.getName());
		} catch (IllegalStateException ex) {
		}
		assertFalse(runnable.isSuccess());
	}

}
