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

package io.sarl.sre.tests.units.skills.bic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.Skill;
import io.sarl.lang.util.ClearableReference;
import io.sarl.sre.services.logging.LoggerCreator;
import io.sarl.sre.services.logging.LoggingService;
import io.sarl.sre.skills.bic.LoggingSkill;
import io.sarl.sre.tests.testutils.AbstractSreTest;
import io.sarl.tests.api.ManualMocking;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@ManualMocking
public class LoggingSkillTest extends AbstractSreTest {

	@Nullable
	protected Handler handler;

	@Nullable
	protected Agent owner;

	@Nullable
	protected LoggingService logService;

	@Nullable
	protected LoggingSkill skill;

	@Nullable
	protected Logger logger;

	@Nullable
	protected Logger parentLogger;

	@Before
	public void setUp() throws Exception {
		this.handler = mock(Handler.class);
		//
		this.parentLogger = spy(Logger.getLogger("ROOT"));
		this.parentLogger.setUseParentHandlers(false);
		reset(this.parentLogger);
		when(this.parentLogger.getHandlers()).thenReturn(new Handler[] { this.handler });
		this.logger = Logger.getLogger("CHILD");
		this.logger.setParent(this.parentLogger);
		this.logger = spy(this.logger);
		//
		this.logService = mock(LoggingService.class);
		when(this.logService.getKernelLogger()).thenReturn(this.parentLogger);
		when(this.logService.createAgentLogger(any())).thenReturn(this.logger);
		when(this.logService.createAgentLogger(any(), any())).thenReturn(this.logger);
		//
		UUID agentId = UUID.randomUUID();
		this.owner = new TestAgent(agentId, this);
		this.owner = spy(this.owner);
		this.skill = this.reflect.newInstance(LoggingSkill.class, this.owner);
		this.skill.setLoggingService(this.logService);
	}

	@Test
	public void errorObject_off() {
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		//
		this.logger.setLevel(Level.OFF);
		this.skill.error(message);
		verify(this.logger, times(1)).isLoggable(argument1.capture());
		assertSame(Level.SEVERE, argument1.getValue());
		verifyNoMoreInteractions(this.parentLogger);
	}

	@Test
	public void errorObject_on() {
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Level> argument2 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<String> argument3 = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<LogRecord> argument4 = ArgumentCaptor.forClass(LogRecord.class);
		ArgumentCaptor<Object[]> argument5 = ArgumentCaptor.forClass(Object[].class);
		//
		this.logger.setLevel(Level.ALL);
		this.skill.error(message);
		//
		verify(this.logger, times(3)).isLoggable(argument1.capture());
		assertSame(Level.SEVERE, argument1.getValue());
		//
		verify(this.logger, times(1)).log(argument2.capture(), argument3.capture(), argument5.capture());
		assertSame(Level.SEVERE, argument2.getValue());
		assertEquals(message, argument3.getValue());
		//
		verify(this.handler).publish(argument4.capture());
		assertNotNull(argument4.getValue());
		assertSame(Level.SEVERE, argument4.getValue().getLevel());
		assertEquals(message, argument4.getValue().getMessage());
		assertNull(argument4.getValue().getThrown());
	}

	@Test
	public void errorObjectThrowable_off() {
		Throwable ex = new Exception();
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		//
		this.logger.setLevel(Level.OFF);
		this.skill.error(message, ex);
		verify(this.logger, times(1)).isLoggable(argument1.capture());
		assertSame(Level.SEVERE, argument1.getValue());
		verifyNoMoreInteractions(this.parentLogger);
	}

	@Test
	public void errorObjectThrowable_on() {
		Throwable ex = new Exception();
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<LogRecord> argument4 = ArgumentCaptor.forClass(LogRecord.class);
		//
		this.logger.setLevel(Level.ALL);
		this.skill.error(message, ex);
		//
		verify(this.logger, times(2)).isLoggable(argument1.capture());
		assertSame(Level.SEVERE, argument1.getValue());
		//
		verify(this.logger, times(1)).log(argument4.capture());
		assertSame(Level.SEVERE, argument4.getValue().getLevel());
		assertEquals(message, argument4.getValue().getMessage());
		assertSame(ex, argument4.getValue().getThrown());
		//
		verify(this.handler).publish(argument4.capture());
		assertNotNull(argument4.getValue());
		assertSame(Level.SEVERE, argument4.getValue().getLevel());
		assertEquals(message, argument4.getValue().getMessage());
		assertSame(ex, argument4.getValue().getThrown());
	}

	@Test
	public void warningObject_off() {
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		//
		this.logger.setLevel(Level.OFF);
		this.skill.warning(message);
		verify(this.logger, times(1)).isLoggable(argument1.capture());
		assertSame(Level.WARNING, argument1.getValue());
		verifyNoMoreInteractions(this.parentLogger);
	}

	@Test
	public void warningObject_on() {
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Level> argument2 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<String> argument3 = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<LogRecord> argument4 = ArgumentCaptor.forClass(LogRecord.class);
		ArgumentCaptor<Object[]> argument5 = ArgumentCaptor.forClass(Object[].class);
		//
		this.logger.setLevel(Level.ALL);
		this.skill.warning(message);
		//
		verify(this.logger, atLeast(1)).isLoggable(argument1.capture());
		assertSame(Level.WARNING, argument1.getValue());
		//
		verify(this.logger, times(1)).log(argument2.capture(), argument3.capture(), argument5.capture());
		assertSame(Level.WARNING, argument2.getValue());
		assertEquals(message, argument3.getValue());
		//
		verify(this.handler).publish(argument4.capture());
		assertNotNull(argument4.getValue());
		assertSame(Level.WARNING, argument4.getValue().getLevel());
		assertEquals(message, argument4.getValue().getMessage());
		assertNull(argument4.getValue().getThrown());
	}

	@Test
	public void warningObjectThrowable_off() {
		Throwable ex = new Exception();
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		//
		this.logger.setLevel(Level.OFF);
		this.skill.warning(message, ex);
		verify(this.logger, times(1)).isLoggable(argument1.capture());
		assertSame(Level.WARNING, argument1.getValue());
		verifyNoMoreInteractions(this.parentLogger);
	}

	@Test
	public void warningObjectThrowable_on() {
		Throwable ex = new Exception();
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<LogRecord> argument4 = ArgumentCaptor.forClass(LogRecord.class);
		//
		this.logger.setLevel(Level.ALL);
		this.skill.warning(message, ex);
		//
		verify(this.logger, times(2)).isLoggable(argument1.capture());
		assertSame(Level.WARNING, argument1.getValue());
		//
		verify(this.logger, times(1)).log(argument4.capture());
		assertSame(Level.WARNING, argument4.getValue().getLevel());
		assertEquals(message, argument4.getValue().getMessage());
		assertSame(ex, argument4.getValue().getThrown());
		//
		verify(this.handler).publish(argument4.capture());
		assertNotNull(argument4.getValue());
		assertSame(Level.WARNING, argument4.getValue().getLevel());
		assertEquals(message, argument4.getValue().getMessage());
		assertSame(ex, argument4.getValue().getThrown());
	}

	@Test
	public void info_off() {
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		//
		this.logger.setLevel(Level.OFF);
		this.skill.info(message);
		verify(this.logger, times(1)).isLoggable(argument1.capture());
		assertSame(Level.INFO, argument1.getValue());
		verifyNoMoreInteractions(this.parentLogger);
	}

	@Test
	public void info_on() {
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<LogRecord> argument4 = ArgumentCaptor.forClass(LogRecord.class);
		//
		this.logger.setLevel(Level.ALL);
		this.skill.info(message);
		//
		verify(this.logger, times(3)).isLoggable(argument1.capture());
		assertSame(Level.INFO, argument1.getValue());
		//
		verify(this.logger, times(1)).log(argument4.capture());
		assertSame(Level.INFO, argument4.getValue().getLevel());
		assertEquals(message, argument4.getValue().getMessage());
		//
		verify(this.handler).publish(argument4.capture());
		assertNotNull(argument4.getValue());
		assertSame(Level.INFO, argument4.getValue().getLevel());
		assertEquals(message, argument4.getValue().getMessage());
		assertNull(argument4.getValue().getThrown());
	}

	@Test
	public void debug_off() {
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		//
		this.logger.setLevel(Level.OFF);
		this.skill.debug(message);
		verify(this.logger, times(1)).isLoggable(argument1.capture());
		assertSame(Level.CONFIG, argument1.getValue());
		verifyNoMoreInteractions(this.parentLogger);
	}

	@Test
	public void debug_on() {
		String message = UUID.randomUUID().toString();
		ArgumentCaptor<Level> argument1 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<Level> argument2 = ArgumentCaptor.forClass(Level.class);
		ArgumentCaptor<String> argument3 = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<LogRecord> argument4 = ArgumentCaptor.forClass(LogRecord.class);
		ArgumentCaptor<Object[]> argument5 = ArgumentCaptor.forClass(Object[].class);
		//
		this.logger.setLevel(Level.ALL);
		this.skill.debug(message);
		//
		verify(this.logger, times(3)).isLoggable(argument1.capture());
		assertSame(Level.CONFIG, argument1.getValue());
		//
		verify(this.logger, times(1)).log(argument2.capture(), argument3.capture(), argument5.capture());
		assertSame(Level.CONFIG, argument2.getValue());
		assertEquals(message, argument3.getValue());
		//
		verify(this.handler).publish(argument4.capture());
		assertNotNull(argument4.getValue());
		assertSame(Level.CONFIG, argument4.getValue().getLevel());
		assertEquals(message, argument4.getValue().getMessage());
		assertNull(argument4.getValue().getThrown());
	}

	@Test
	public void getLogLevel() {
		int expected = LoggerCreator.toInt(this.logger.getLevel());
		assertEquals(expected, this.skill.getLogLevel());
	}

	@Test
	public void setLogLevel() {
		for (int i = 0; i < 10; i++) {
			this.skill.setLogLevel(i);
			assertEquals(Math.max(0, Math.min(7, i)), this.skill.getLogLevel());
		}
	}

	@Test
	public void isErrorLogEnabled() {
		this.skill.setLogLevel(0);
		assertFalse(this.skill.isErrorLogEnabled());
		this.skill.setLogLevel(1);
		assertTrue(this.skill.isErrorLogEnabled());
		this.skill.setLogLevel(2);
		assertTrue(this.skill.isErrorLogEnabled());
		this.skill.setLogLevel(3);
		assertTrue(this.skill.isErrorLogEnabled());
		this.skill.setLogLevel(4);
		assertTrue(this.skill.isErrorLogEnabled());
		this.skill.setLogLevel(5);
		assertTrue(this.skill.isErrorLogEnabled());
		this.skill.setLogLevel(6);
		assertTrue(this.skill.isErrorLogEnabled());
		this.skill.setLogLevel(7);
		assertTrue(this.skill.isErrorLogEnabled());
	}

	@Test
	public void isWarningLogEnabled() {
		this.skill.setLogLevel(0);
		assertFalse(this.skill.isWarningLogEnabled());
		this.skill.setLogLevel(1);
		assertFalse(this.skill.isWarningLogEnabled());
		this.skill.setLogLevel(2);
		assertTrue(this.skill.isWarningLogEnabled());
		this.skill.setLogLevel(3);
		assertTrue(this.skill.isWarningLogEnabled());
		this.skill.setLogLevel(4);
		assertTrue(this.skill.isWarningLogEnabled());
		this.skill.setLogLevel(5);
		assertTrue(this.skill.isWarningLogEnabled());
		this.skill.setLogLevel(6);
		assertTrue(this.skill.isWarningLogEnabled());
		this.skill.setLogLevel(7);
		assertTrue(this.skill.isWarningLogEnabled());
	}

	@Test
	public void isInfoLogEnabled() {
		this.skill.setLogLevel(0);
		assertFalse(this.skill.isInfoLogEnabled());
		this.skill.setLogLevel(1);
		assertFalse(this.skill.isInfoLogEnabled());
		this.skill.setLogLevel(2);
		assertFalse(this.skill.isInfoLogEnabled());
		this.skill.setLogLevel(3);
		assertTrue(this.skill.isInfoLogEnabled());
		this.skill.setLogLevel(4);
		assertTrue(this.skill.isInfoLogEnabled());
		this.skill.setLogLevel(5);
		assertTrue(this.skill.isInfoLogEnabled());
		this.skill.setLogLevel(6);
		assertTrue(this.skill.isInfoLogEnabled());
		this.skill.setLogLevel(7);
		assertTrue(this.skill.isInfoLogEnabled());
	}

	@Test
	public void isDebugLogEnabled() {
		this.skill.setLogLevel(0);
		assertFalse(this.skill.isDebugLogEnabled());
		this.skill.setLogLevel(1);
		assertFalse(this.skill.isDebugLogEnabled());
		this.skill.setLogLevel(2);
		assertFalse(this.skill.isDebugLogEnabled());
		this.skill.setLogLevel(3);
		assertFalse(this.skill.isDebugLogEnabled());
		this.skill.setLogLevel(4);
		assertTrue(this.skill.isDebugLogEnabled());
		this.skill.setLogLevel(5);
		assertTrue(this.skill.isDebugLogEnabled());
		this.skill.setLogLevel(6);
		assertTrue(this.skill.isDebugLogEnabled());
		this.skill.setLogLevel(7);
		assertTrue(this.skill.isDebugLogEnabled());
	}

	public static class TestAgent extends Agent {
		private final LoggingSkillTest test;

		public TestAgent(UUID agentId, LoggingSkillTest test) {
			super(agentId, null);
			this.test = test;
		}

		@Override
		protected ClearableReference<Skill> $getSkill(Class<? extends Capacity> capacity) {
			return new ClearableReference<>(this.test.skill);
		}

	}

}
