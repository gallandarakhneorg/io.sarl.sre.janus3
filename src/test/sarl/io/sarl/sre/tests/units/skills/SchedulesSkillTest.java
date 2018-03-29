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
package io.sarl.sre.tests.units.skills;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.sarl.core.AgentTask;
import io.sarl.core.Logging;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Behavior;
import io.sarl.lang.core.Skill;
import io.sarl.lang.util.SynchronizedSet;
import io.sarl.sre.services.executor.ExecutorService;
import io.sarl.sre.services.lifecycle.AgentLife;
import io.sarl.sre.services.lifecycle.AgentState;
import io.sarl.sre.skills.SchedulesSkill;
import io.sarl.sre.tests.testutils.AbstractJanusTest;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class SchedulesSkillTest extends AbstractJanusTest {

	@Nullable
	private UUID contextId;
	
	@Nullable
	private UUID agentId;

	@Nullable
	private ExecutorService executor;

	@Nullable
	private Agent agent;

	@Nullable
	private SchedulesSkill skill;

	@Nullable
	private Logger rawLogger;

	@Nullable
	private MyLoggingSkill logger;

	@Before
	public void setUp() {
		this.contextId = UUID.randomUUID();
		this.agentId = UUID.randomUUID();
		this.executor = mock(ExecutorService.class);
		this.rawLogger = mock(Logger.class);
		this.logger = spy(new MyLoggingSkill(this.rawLogger));
		this.agent = spy(new MyAgent(contextId, this.agentId, this.logger));
		this.skill = new SchedulesSkill(this.agent);
		this.skill.setExecutorService(this.executor);
	}

	private void forceAlive() {
		// Force the agent being alive
		AgentLife.getLife(this.agent).setState(AgentState.ALIVE);
	}

	@Test
	public void task_nullName_notAlive() {
		AgentTask task = this.skill.task(null);
		assertNull(task);
	}

	@Test
	public void task_nullName_alive() {
		forceAlive();
		AgentTask task = this.skill.task(null);
		assertNotNull(task);
		assertSame(this.agent, task.getInitiator());
		assertTrue(task.getName().startsWith("task-"));
		assertNull(task.getProcedure());
	}

	@Test
	public void task_withName_notAlive() {
		String name = UUID.randomUUID().toString();
		AgentTask task = this.skill.task(name);
		assertNull(task);
	}

	@Test
	public void task_withName_alive() {
		forceAlive();
		String name = UUID.randomUUID().toString();
		AgentTask task = this.skill.task(name);
		assertNotNull(task);
		assertSame(this.agent, task.getInitiator());
		assertEquals(name, task.getName());
		assertNull(task.getProcedure());
	}

	@Test
	public void setName_nullName_notAlive() {
		AgentTask task = new AgentTask();
		this.skill.setName(task, null);
		assertNull(task.getName());
	}

	@Test
	public void setName_nullName_alive() {
		forceAlive();
		AgentTask task = new AgentTask();
		this.skill.setName(task, null);
		assertNotNullOrEmpty(task.getName());
	}

	@Test
	public void setName_withName_notAlive() {
		String name = UUID.randomUUID().toString();
		AgentTask task = new AgentTask();
		this.skill.setName(task, name);
		assertNull(task.getName());
	}

	@Test
	public void setName_withName_alive() {
		forceAlive();
		String name = UUID.randomUUID().toString();
		AgentTask task = new AgentTask();
		this.skill.setName(task, name);
		assertEquals(name, task.getName());
	}

	private <T extends Future> void testNoTask(boolean alive, boolean allowFunctionRunWhenNotAlive,
			Function2<? super AgentTask, ? super Procedure1<? super Agent>, ? extends AgentTask> call,
			Procedure1<T> executorConfig, Class<T> type,
			Procedure1<? super Boolean> specialTest) {
		if (alive) {
			forceAlive();
		}

		T future = mock(type);
		executorConfig.apply(future);
		Procedure1<? super Agent> procedure = (it) -> {};

		AgentTask task = call.apply(null, procedure);

		if (alive || allowFunctionRunWhenNotAlive) {
			assertNotNull(task);
			assertSame(AgentTask.TRUE_GUARD, task.getGuard());
			assertFalse(Strings.isEmpty(task.getName()));
			assertSame(this.agent, task.getInitiator());
			assertSame(procedure, task.getProcedure());
			assertEquals(1, this.skill.getActiveTasks().size());
		} else {
			assertNull(task);
			assertEquals(0, this.skill.getActiveTasks().size());
		}
		
		specialTest.apply(alive);
	}

	private <T extends Future> void testNotStartedTask(boolean alive, boolean allowFunctionRunWhenNotAlive, Function2<? super AgentTask, ? super Procedure1<? super Agent>, ? extends AgentTask> call,
			Procedure1<T> executorConfig, Class<T> type,
			Procedure1<? super Boolean> specialTest) {
		if (alive) {
			forceAlive();
		}

		T future = mock(type);
		executorConfig.apply(future);
		Procedure1<? super Agent> procedure = (it) -> {};

		AgentTask otask = new AgentTask(this);
		String taskName = UUID.randomUUID().toString();
		otask.setTaskName(taskName);
		otask = spy(otask);
		
		AgentTask task = call.apply(otask, procedure);

		assertSame(otask, task);
		
		if (alive || allowFunctionRunWhenNotAlive) {
			ArgumentCaptor<Procedure1<? super Agent>> capturedProcedure = ArgumentCaptor.forClass(Procedure1.class);
			verify(task).setProcedure(capturedProcedure.capture());
			assertSame(procedure, capturedProcedure.getValue());

			assertEquals(1, this.skill.getActiveTasks().size());
			assertContains(this.skill.getActiveTasks(), taskName);
		} else {
			verifyNoMoreInteractions(task);

			assertEquals(0, this.skill.getActiveTasks().size());
		}
		
		
		specialTest.apply(alive);
	}

	private <T extends Future> void testNotFinishedTask(boolean alive, Function2<? super AgentTask, ? super Procedure1<? super Agent>, ? extends AgentTask> call,
			Procedure1<T> executorConfig, Class<T> type) {
		if (alive) {
			forceAlive();
		}

		T future = mock(type);
		executorConfig.apply(future);
		Procedure1<? super Agent> procedure = (it) -> {};

		AgentTask otask = new AgentTask(this);
		String taskName = UUID.randomUUID().toString();
		otask.setTaskName(taskName);
		otask = spy(otask);
		
		AgentTask task = call.apply(otask, procedure);
		assertSame(otask, task);

		AgentTask task2 = call.apply(otask, procedure);
		assertSame(otask, task2);
	}

	private <T extends Future> void testFinishedTask(boolean alive, boolean allowFunctionRunWhenNotAlive, Function2<? super AgentTask, ? super Procedure1<? super Agent>, ? extends AgentTask> call,
			Procedure1<T> executorConfig, Class<T> type,
			Procedure1<? super Boolean> specialTest) {
		if (alive) {
			forceAlive();
		}
		
		T future = mock(type);
		executorConfig.apply(future);
		Procedure1<? super Agent> procedure = (it) -> {};

		AgentTask otask = new AgentTask(this);
		String taskName = UUID.randomUUID().toString();
		otask.setTaskName(taskName);
		otask = spy(otask);

		this.skill.execute(otask, (it) -> {});
		reset(otask, this.executor);
	
		when(future.isDone()).thenReturn(true);

		AgentTask task = call.apply(otask, procedure);

		assertSame(otask, task);
		
		if (alive || allowFunctionRunWhenNotAlive) {
			ArgumentCaptor<Procedure1<? super Agent>> capturedProcedure = ArgumentCaptor.forClass(Procedure1.class);
			verify(task).setProcedure(capturedProcedure.capture());
			assertSame(procedure, capturedProcedure.getValue());
		} else {
			verifyZeroInteractions(task);
		}
		
		assertEquals(1, this.skill.getActiveTasks().size());
		assertContains(this.skill.getActiveTasks(), taskName);
		
		specialTest.apply(alive);
	}

	private <T extends Future> void testCancelledTask(boolean alive, boolean allowFunctionRunWhenNotAlive, Function2<? super AgentTask, ? super Procedure1<? super Agent>, ? extends AgentTask> call,
			Procedure1<? super T> executorConfig, Class<T> type,
			Procedure1<? super Boolean> specialTest) {
		if (alive) {
			forceAlive();
		}
		
		T future = mock(type);
		executorConfig.apply(future);
		Procedure1<? super Agent> procedure = (it) -> {};

		AgentTask otask = new AgentTask(this);
		String taskName = UUID.randomUUID().toString();
		otask.setTaskName(taskName);
		otask = spy(otask);

		this.skill.execute(otask, (it) -> {});
		reset(otask, this.executor);
	
		when(future.isCancelled()).thenReturn(true);

		AgentTask task = call.apply(otask, procedure);

		assertSame(otask, task);
		
		if (alive || allowFunctionRunWhenNotAlive) {
			ArgumentCaptor<Procedure1<? super Agent>> capturedProcedure = ArgumentCaptor.forClass(Procedure1.class);
			verify(task).setProcedure(capturedProcedure.capture());
			assertSame(procedure, capturedProcedure.getValue());
		} else {
			verifyZeroInteractions(task);
		}
		
		assertEquals(1, this.skill.getActiveTasks().size());
		assertContains(this.skill.getActiveTasks(), taskName);
		
		specialTest.apply(alive);
	}
	
	private Procedure1<? super Boolean> getExecuteTester() {
		return (alive) -> {
			ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
			ArgumentCaptor<Runnable> capturedProcedure2 = ArgumentCaptor.forClass(Runnable.class);
			verify(this.executor, only()).executeAsap(capturedLogger.capture(), capturedProcedure2.capture());
			assertSame(this.rawLogger, capturedLogger.getValue());
			assertNotNull(capturedProcedure2.getValue());
		};
	}

	private Procedure1<Future> getExecuteExecutorConfig() {
		return (future) -> when(this.executor.executeAsap(any(), any(Runnable.class))).thenReturn(future);
	}

	@Test
	public void execute_noTask_notAlive() {
		testNoTask(false, true, (a, b) -> this.skill.execute(b), getExecuteExecutorConfig(), Future.class, getExecuteTester());
	}

	@Test
	public void execute_noTask_alive() {
		testNoTask(true, true, (a, b) -> this.skill.execute(b), getExecuteExecutorConfig(), Future.class, getExecuteTester());
	}

	@Test
	public void execute_task_notStarted_notAlive() {
		testNotStartedTask(false, true, (a, b) -> this.skill.execute(a, b), getExecuteExecutorConfig(), Future.class, getExecuteTester());
	}

	@Test
	public void execute_task_notStarted_alive() {
		testNotStartedTask(true, true, (a, b) -> this.skill.execute(a, b), getExecuteExecutorConfig(), Future.class, getExecuteTester());
	}

	@Test(expected = IllegalStateException.class)
	public void execute_task_notFinished_notAlive() {
		testNotFinishedTask(false, (a, b) -> this.skill.execute(a, b), getExecuteExecutorConfig(), Future.class);
	}

	@Test(expected = IllegalStateException.class)
	public void execute_task_notFinished_alive() {
		testNotFinishedTask(true, (a, b) -> this.skill.execute(a, b), getExecuteExecutorConfig(), Future.class);
	}

	@Test
	public void execute_task_finished_notAlive() {
		testFinishedTask(false, true, (a, b) -> this.skill.execute(a, b), getExecuteExecutorConfig(), Future.class, getExecuteTester());
	}

	@Test
	public void execute_task_finished_alive() {
		testFinishedTask(true, true, (a, b) -> this.skill.execute(a, b), getExecuteExecutorConfig(), Future.class, getExecuteTester());
	}

	@Test
	public void execute_task_cancelled_notAlive() {
		testCancelledTask(false, true, (a, b) -> this.skill.execute(a, b), getExecuteExecutorConfig(), Future.class, getExecuteTester());
	}

	@Test
	public void execute_task_cancelled_alive() {
		testCancelledTask(true, true, (a, b) -> this.skill.execute(a, b), getExecuteExecutorConfig(), Future.class, getExecuteTester());
	}

	private Procedure1<? super Boolean> getInTester() {
		return (alive) -> {
			if (alive) {
				ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
				ArgumentCaptor<Long> capturedDelay = ArgumentCaptor.forClass(Long.class);
				ArgumentCaptor<TimeUnit> capturedUnit = ArgumentCaptor.forClass(TimeUnit.class);
				ArgumentCaptor<Runnable> capturedProcedure = ArgumentCaptor.forClass(Runnable.class);
				verify(this.executor, only()).schedule(capturedLogger.capture(),
						capturedDelay.capture(),
						capturedUnit.capture(),
						capturedProcedure.capture());
				assertSame(this.rawLogger, capturedLogger.getValue());
				Assert.assertEquals(34, capturedDelay.getValue().intValue());
				assertEquals(TimeUnit.MILLISECONDS, capturedUnit.getValue());
				assertNotNull(capturedProcedure.getValue());
			} else {
				verifyZeroInteractions(this.executor);
			}
		};
	}

	private Procedure1<ScheduledFuture> getInExecutorConfig() {
		return (future) -> when(this.executor.schedule(any(), any(long.class),
				any(), any(Runnable.class))).thenReturn(future);
	}

	@Test
	public void in_noTask_notAlive() {
		testNoTask(false, false, (a, b) -> this.skill.in(34, b), getInExecutorConfig(), ScheduledFuture.class, getInTester());
	}

	@Test
	public void in_noTask_alive() {
		testNoTask(true, false, (a, b) -> this.skill.in(34, b), getInExecutorConfig(), ScheduledFuture.class, getInTester());
	}

	@Test
	public void in_task_notStarted_notAlive() {
		testNotStartedTask(false, false, (a, b) -> this.skill.in(a, 34, b), getInExecutorConfig(), ScheduledFuture.class, getInTester());
	}

	@Test
	public void in_task_notStarted_alive() {
		testNotStartedTask(true, false, (a, b) -> this.skill.in(a, 34, b), getInExecutorConfig(), ScheduledFuture.class, getInTester());
	}

	public void in_task_notFinished_notAlive() {
		testNotFinishedTask(false, (a, b) -> this.skill.in(a, 34, b), getInExecutorConfig(), ScheduledFuture.class);
	}

	@Test(expected = IllegalStateException.class)
	public void in_task_notFinished_alive() {
		testNotFinishedTask(true, (a, b) -> this.skill.in(a, 34, b), getInExecutorConfig(), ScheduledFuture.class);
	}

	@Test
	public void in_task_finished_notAlive() {
		testFinishedTask(false, false, (a, b) -> this.skill.in(a, 34, b), getInExecutorConfig(), ScheduledFuture.class, getInTester());
	}

	@Test
	public void in_task_finished_alive() {
		testFinishedTask(true, false, (a, b) -> this.skill.in(a, 34, b), getInExecutorConfig(), ScheduledFuture.class, getInTester());
	}

	@Test
	public void in_task_cancelled_notAlive() {
		testCancelledTask(false, false, (a, b) -> this.skill.in(a, 34, b), getInExecutorConfig(), ScheduledFuture.class, getInTester());
	}

	@Test
	public void in_task_cancelled_alive() {
		testCancelledTask(true, false, (a, b) -> this.skill.in(a, 34, b), getInExecutorConfig(), ScheduledFuture.class, getInTester());
	}

	private Procedure1<? super Boolean> getEveryTester() {
		return (alive) -> {
			if (alive) {
				ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
				ArgumentCaptor<Long> capturedDelay = ArgumentCaptor.forClass(Long.class);
				ArgumentCaptor<Long> capturedPeriod = ArgumentCaptor.forClass(Long.class);
				ArgumentCaptor<TimeUnit> capturedUnit = ArgumentCaptor.forClass(TimeUnit.class);
				ArgumentCaptor<Runnable> capturedProcedure = ArgumentCaptor.forClass(Runnable.class);
				verify(this.executor, only()).scheduleAtFixedRate(capturedLogger.capture(),
						capturedDelay.capture(),
						capturedPeriod.capture(),
						capturedUnit.capture(),
						capturedProcedure.capture());
				assertSame(this.rawLogger, capturedLogger.getValue());
				Assert.assertEquals(0, capturedDelay.getValue().intValue());
				Assert.assertEquals(34, capturedPeriod.getValue().intValue());
				assertEquals(TimeUnit.MILLISECONDS, capturedUnit.getValue());
				assertNotNull(capturedProcedure.getValue());
			} else {
				verifyZeroInteractions(this.executor);
			}
		};
	}

	private Procedure1<ScheduledFuture> getEveryExecutorConfig() {
		return (future) -> {
			when(this.executor.scheduleAtFixedRate(any(), any(long.class),
					any(long.class), any(), any(Runnable.class))).thenReturn(future);
		};
	}

	@Test
	public void every_noTask_notAlive() {
		testNoTask(false, false, (a, b) -> this.skill.every(34, b), getEveryExecutorConfig(), ScheduledFuture.class, getEveryTester());
	}

	@Test
	public void every_noTask_alive() {
		testNoTask(true, false, (a, b) -> this.skill.every(34, b), getEveryExecutorConfig(), ScheduledFuture.class, getEveryTester());
	}

	@Test
	public void every_task_notStarted_notAlive() {
		testNotStartedTask(false, false, (a, b) -> this.skill.every(a, 34, b), getEveryExecutorConfig(), ScheduledFuture.class, getEveryTester());
	}

	@Test
	public void every_task_notStarted_alive() {
		testNotStartedTask(true, false, (a, b) -> this.skill.every(a, 34, b), getEveryExecutorConfig(), ScheduledFuture.class, getEveryTester());
	}

	@Test
	public void every_task_notFinished_notAlive() {
		testNotFinishedTask(false, (a, b) -> this.skill.every(a, 34, b), getEveryExecutorConfig(), ScheduledFuture.class);
	}

	@Test(expected = IllegalStateException.class)
	public void every_task_notFinished_alive() {
		testNotFinishedTask(true, (a, b) -> this.skill.every(a, 34, b), getEveryExecutorConfig(), ScheduledFuture.class);
	}

	@Test
	public void every_task_finished_notAlive() {
		testFinishedTask(false, false, (a, b) -> this.skill.every(a, 34, b), getEveryExecutorConfig(), ScheduledFuture.class, getEveryTester());
	}

	@Test
	public void every_task_finished_alive() {
		testFinishedTask(true, false, (a, b) -> this.skill.every(a, 34, b), getEveryExecutorConfig(), ScheduledFuture.class, getEveryTester());
	}

	@Test
	public void every_task_cancelled_notAlive() {
		testCancelledTask(false, false, (a, b) -> this.skill.every(a, 34, b), getEveryExecutorConfig(), ScheduledFuture.class, getEveryTester());
	}

	@Test
	public void every_task_cancelled_alive() {
		testCancelledTask(true, false, (a, b) -> this.skill.every(a, 34, b), getEveryExecutorConfig(), ScheduledFuture.class, getEveryTester());
	}

	private Procedure1<? super Boolean> getAtFixedDelayTester() {
		return (alive) -> {
			if (alive) {
				ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
				ArgumentCaptor<Long> capturedDelay = ArgumentCaptor.forClass(Long.class);
				ArgumentCaptor<Long> capturedPeriod = ArgumentCaptor.forClass(Long.class);
				ArgumentCaptor<TimeUnit> capturedUnit = ArgumentCaptor.forClass(TimeUnit.class);
				ArgumentCaptor<Runnable> capturedProcedure = ArgumentCaptor.forClass(Runnable.class);
				verify(this.executor, only()).scheduleWithFixedDelay(capturedLogger.capture(),
						capturedDelay.capture(),
						capturedPeriod.capture(),
						capturedUnit.capture(),
						capturedProcedure.capture());
				assertSame(this.rawLogger, capturedLogger.getValue());
				Assert.assertEquals(0, capturedDelay.getValue().intValue());
				Assert.assertEquals(34, capturedPeriod.getValue().intValue());
				assertEquals(TimeUnit.MILLISECONDS, capturedUnit.getValue());
				assertNotNull(capturedProcedure.getValue());
			} else {
				verifyZeroInteractions(this.executor);
			}
		};
	}

	private Procedure1<ScheduledFuture> getAtFixedDelayExecutorConfig() {
		return (future) -> when(this.executor.scheduleWithFixedDelay(any(), any(long.class),
				any(long.class), any(), any(Runnable.class))).thenReturn(future);
	}

	@Test
	public void atFixedDelay_noTask_notAlive() {
		testNoTask(false, false, (a, b) -> this.skill.atFixedDelay(34, b), getAtFixedDelayExecutorConfig(), ScheduledFuture.class, getAtFixedDelayTester());
	}

	@Test
	public void atFixedDelay_noTask_alive() {
		testNoTask(true, false, (a, b) -> this.skill.atFixedDelay(34, b), getAtFixedDelayExecutorConfig(), ScheduledFuture.class, getAtFixedDelayTester());
	}

	@Test
	public void atFixedDelay_task_notStarted_notAlive() {
		testNotStartedTask(false, false, (a, b) -> this.skill.atFixedDelay(a, 34, b), getAtFixedDelayExecutorConfig(), ScheduledFuture.class, getAtFixedDelayTester());
	}

	@Test
	public void atFixedDelay_task_notStarted_alive() {
		testNotStartedTask(true, false, (a, b) -> this.skill.atFixedDelay(a, 34, b), getAtFixedDelayExecutorConfig(), ScheduledFuture.class, getAtFixedDelayTester());
	}

	@Test
	public void atFixedDelay_task_notFinished_notAlive() {
		testNotFinishedTask(false, (a, b) -> this.skill.atFixedDelay(a, 34, b), getAtFixedDelayExecutorConfig(), ScheduledFuture.class);
	}

	@Test(expected = IllegalStateException.class)
	public void atFixedDelay_task_notFinished_alive() {
		testNotFinishedTask(true, (a, b) -> this.skill.atFixedDelay(a, 34, b), getAtFixedDelayExecutorConfig(), ScheduledFuture.class);
	}

	@Test
	public void atFixedDelay_task_finished_notAlive() {
		testFinishedTask(false, false, (a, b) -> this.skill.atFixedDelay(a, 34, b), getAtFixedDelayExecutorConfig(), ScheduledFuture.class, getAtFixedDelayTester());
	}

	@Test
	public void atFixedDelay_task_finished_alive() {
		testFinishedTask(true, false, (a, b) -> this.skill.atFixedDelay(a, 34, b), getAtFixedDelayExecutorConfig(), ScheduledFuture.class, getAtFixedDelayTester());
	}

	@Test
	public void atFixedDelay_task_cancelled_notAlive() {
		testCancelledTask(false, false, (a, b) -> this.skill.atFixedDelay(a, 34, b), getAtFixedDelayExecutorConfig(), ScheduledFuture.class, getAtFixedDelayTester());
	}

	@Test
	public void atFixedDelay_task_cancelled_alive() {
		testCancelledTask(true, false, (a, b) -> this.skill.atFixedDelay(a, 34, b), getAtFixedDelayExecutorConfig(), ScheduledFuture.class, getAtFixedDelayTester());
	}

	@Test
	public void isCanceled_nullTask() {
		assertFalse(this.skill.isCanceled(null));
	}

	@Test
	public void isCanceled_notSubmitted() {
		AgentTask task = new AgentTask(this);
		String taskName = UUID.randomUUID().toString();
		task.setTaskName(taskName);
		task = spy(task);

		assertFalse(this.skill.isCanceled(task));
	}

	@Test
	public void isCanceled_running() {
		Future future = mock(Future.class);
		when(this.executor.executeAsap(any(), any(Runnable.class))).thenReturn(future);
		AgentTask task = this.skill.execute((it) -> {});
		assertFalse(this.skill.isCanceled(task));
	}

	@Test
	public void isCanceled_finished() {
		Future future = mock(Future.class);
		when(this.executor.executeAsap(any(), any(Runnable.class))).thenReturn(future);
		when(future.isDone()).thenReturn(true);

		AgentTask task = this.skill.execute((it) -> {});
		assertFalse(this.skill.isCanceled(task));
	}

	@Test
	public void isCanceled_cancelled() {
		Future future = mock(Future.class);
		when(this.executor.executeAsap(any(), any(Runnable.class))).thenReturn(future);
		when(future.isCancelled()).thenReturn(true);

		AgentTask task = this.skill.execute((it) -> {});
		assertTrue(this.skill.isCanceled(task));
	}

	@Test
	public void cancel_notSubmitted() {
		Future future = mock(Future.class);
		when(this.executor.executeAsap(any(), any(Runnable.class))).thenReturn(future);
		when(future.cancel(any(boolean.class))).thenReturn(true);

		AgentTask task = new AgentTask(this);
		String taskName = UUID.randomUUID().toString();
		task.setTaskName(taskName);
		task = spy(task);

		assertFalse(this.skill.cancel(task));
		assertTrue(this.skill.getActiveTasks().isEmpty());
		
		ArgumentCaptor<Boolean> capturedInterrupt = ArgumentCaptor.forClass(Boolean.class);
		verify(future, times(0)).cancel(capturedInterrupt.capture());
	}

	@Test
	public void cancel_running_notAlive() {
		ScheduledFuture future = mock(ScheduledFuture.class);
		getInExecutorConfig().apply(future);
		when(future.cancel(any(boolean.class))).thenReturn(true);
		
		AgentTask task = this.skill.in(10000, (it) -> {});
		assertFalse(this.skill.cancel(task));
		assertTrue(this.skill.getActiveTasks().isEmpty());
		verifyZeroInteractions(future);
	}

	@Test
	public void cancel_running_alive() {
		ScheduledFuture future = mock(ScheduledFuture.class);
		getInExecutorConfig().apply(future);
		when(future.cancel(any(boolean.class))).thenReturn(true);
		
		forceAlive();
		
		AgentTask task = this.skill.in(10000, (it) -> {});
		assertTrue(this.skill.cancel(task));
		assertTrue(this.skill.getActiveTasks().isEmpty());
		
		ArgumentCaptor<Boolean> capturedInterrupt = ArgumentCaptor.forClass(Boolean.class);
		verify(future, atLeastOnce()).cancel(capturedInterrupt.capture());
		assertTrue(capturedInterrupt.getValue());
	}

	@Test
	public void cancel_finished_notAlive() {
		ScheduledFuture future = mock(ScheduledFuture.class);
		getInExecutorConfig().apply(future);
		when(future.isDone()).thenReturn(true);
		when(future.cancel(any(boolean.class))).thenReturn(true);
		
		AgentTask task = this.skill.in(10000, (it) -> {});
		assertFalse(this.skill.cancel(task));
		assertContains(this.skill.getActiveTasks());
		
		verifyZeroInteractions(future);
	}

	@Test
	public void cancel_finished_alive() {
		ScheduledFuture future = mock(ScheduledFuture.class);
		getInExecutorConfig().apply(future);
		when(future.isDone()).thenReturn(true);
		when(future.cancel(any(boolean.class))).thenReturn(true);
		
		forceAlive();
		
		AgentTask task = this.skill.in(10000, (it) -> {});
		assertFalse(this.skill.cancel(task));
		// Caution, the task was artificially finished by forcing the value replied y isDone
		assertContains(this.skill.getActiveTasks(), task.getName());
		
		ArgumentCaptor<Boolean> capturedInterrupt = ArgumentCaptor.forClass(Boolean.class);
		verify(future, times(0)).cancel(capturedInterrupt.capture());
	}

	@Test
	public void cancel_cancelled_notAlive() {
		ScheduledFuture future = mock(ScheduledFuture.class);
		getInExecutorConfig().apply(future);
		when(future.isCancelled()).thenReturn(true);
		when(future.cancel(any(boolean.class))).thenReturn(true);
		
		AgentTask task = this.skill.in(10000, (it) -> {});
		assertFalse(this.skill.cancel(task));
		assertContains(this.skill.getActiveTasks());
		verifyZeroInteractions(future);
	}

	@Test
	public void cancel_cancelled_alive() {
		ScheduledFuture future = mock(ScheduledFuture.class);
		getInExecutorConfig().apply(future);
		when(future.isCancelled()).thenReturn(true);
		when(future.cancel(any(boolean.class))).thenReturn(true);
		
		forceAlive();
		
		AgentTask task = this.skill.in(10000, (it) -> {});
		assertFalse(this.skill.cancel(task));
		// Caution, the task was artificially finished by forcing the value replied y isDone
		assertContains(this.skill.getActiveTasks(), task.getName());
		
		ArgumentCaptor<Boolean> capturedInterrupt = ArgumentCaptor.forClass(Boolean.class);
		verify(future, times(0)).cancel(capturedInterrupt.capture());
	}

	@Test
	public void getActiveTasks_nothing() {
		SynchronizedSet<String> tasks = this.skill.getActiveTasks();
		assertTrue(tasks.isEmpty());
	}

	@Test
	public void getActiveTasks_oneTask() {
		AgentTask task = this.skill.execute((it) -> {});
		SynchronizedSet<String> tasks = this.skill.getActiveTasks();
		assertContains(tasks, task.getName());
	}

	@Test
	public void getActiveTasks_twoTasks() {
		AgentTask task1 = this.skill.execute((it) -> {});
		AgentTask task2 = this.skill.execute((it) -> {});
		SynchronizedSet<String> tasks = this.skill.getActiveTasks();
		assertContains(tasks, task1.getName(), task2.getName());
	}

	@Test
	public void getActiveTasks_twoTasks_firstCancelled() {
		when(this.executor.executeAsap(any(), any(Runnable.class))).thenAnswer((it) -> {
			Future future = mock(Future.class);
			when(future.cancel(any(boolean.class))).thenReturn(true);
			return future;
		});

		AgentTask task1 = this.skill.execute((it) -> {});
		AgentTask task2 = this.skill.execute((it) -> {});
		assertTrue(this.skill.cancel(task1));
		SynchronizedSet<String> tasks = this.skill.getActiveTasks();
		assertContains(tasks, task2.getName());
	}

	@Test
	public void getActiveTasks_twoTasks_secondCancelled() {
		when(this.executor.executeAsap(any(), any(Runnable.class))).thenAnswer((it) -> {
			Future future = mock(Future.class);
			when(future.cancel(any(boolean.class))).thenReturn(true);
			return future;
		});

		AgentTask task1 = this.skill.execute((it) -> {});
		AgentTask task2 = this.skill.execute((it) -> {});
		assertTrue(this.skill.cancel(task2));
		SynchronizedSet<String> tasks = this.skill.getActiveTasks();
		assertContains(tasks, task1.getName());
	}

	@Test
	public void unregisterTasksForBehavior_notAlive() {
		MyBehavior beh = new MyBehavior(this.agent);
		
		ScheduledFuture future = mock(ScheduledFuture.class);
		getInExecutorConfig().apply(future);

		AgentTask task1 = new AgentTask(beh);
		String taskName = UUID.randomUUID().toString();
		task1.setTaskName(taskName);
		task1 = spy(task1);
		this.skill.in(task1, 10000, (it) -> {});

		AgentTask task2 = new AgentTask(this.agent);
		taskName = UUID.randomUUID().toString();
		task2.setTaskName(taskName);
		task2 = spy(task2);
		this.skill.in(task2, 10000, (it) -> {});

		AgentTask task3 = new AgentTask(beh);
		taskName = UUID.randomUUID().toString();
		task3.setTaskName(taskName);
		task3 = spy(task3);
		this.skill.in(task3, 10000, (it) -> {});
		
		this.skill.unregisterTasksForBehavior(beh);
		
		assertContains(this.skill.getActiveTasks());
	}

	@Test
	public void unregisterTasksForBehavior_alive() {
		MyBehavior beh = new MyBehavior(this.agent);
		
		ScheduledFuture future = mock(ScheduledFuture.class);
		getInExecutorConfig().apply(future);

		AgentTask task1 = new AgentTask(beh);
		String taskName = UUID.randomUUID().toString();
		task1.setTaskName(taskName);
		task1 = spy(task1);
		
		forceAlive();
		
		this.skill.in(task1, 10000, (it) -> {});

		AgentTask task2 = new AgentTask(this.agent);
		taskName = UUID.randomUUID().toString();
		task2.setTaskName(taskName);
		task2 = spy(task2);
		this.skill.in(task2, 10000, (it) -> {});

		AgentTask task3 = new AgentTask(beh);
		taskName = UUID.randomUUID().toString();
		task3.setTaskName(taskName);
		task3 = spy(task3);
		this.skill.in(task3, 10000, (it) -> {});
		
		this.skill.unregisterTasksForBehavior(beh);
		
		assertContains(this.skill.getActiveTasks(), task2.getName());
	}

	private static class MyBehavior extends Behavior {

		public MyBehavior(Agent agent) {
			super(agent);
		}
		
	}

	private static class MyAgent extends Agent {

		public MyAgent(UUID parentID, UUID agentID, MyLoggingSkill skill) {
			super(parentID, agentID);
			setSkill(skill);
		}
		
	}

	private static class MyLoggingSkill extends Skill implements Logging {
		
		private final Logger logger;

		public MyLoggingSkill(Logger logger) {
			this.logger = logger;
		}
		
		@Override
		public void setLoggingName(String name) {
		}

		@Override
		public void println(Object message) {
		}

		@Override
		public void error(Object message, Throwable exception, Object... parameters) {
		}

		@Override
		public void warning(Object message, Throwable exception, Object... parameters) {
		}

		@Override
		public void info(Object message, Object... parameters) {
		}

		@Override
		public void debug(Object message, Object... parameters) {
		}

		@Override
		public boolean isErrorLogEnabled() {
			return false;
		}

		@Override
		public boolean isWarningLogEnabled() {
			return false;
		}

		@Override
		public boolean isInfoLogEnabled() {
			return false;
		}

		@Override
		public boolean isDebugLogEnabled() {
			return false;
		}

		@Override
		public int getLogLevel() {
			return 0;
		}

		@Override
		public void setLogLevel(int level) {
		}

		@Override
		public Logger getLogger() {
			return this.logger;
		}
		
		@Override
		public void error(Supplier<String> messageProvider) {
		}

		@Override
		public void warning(Supplier<String> messageProvider) {
		}

		@Override
		public void info(Supplier<String> messageProvider) {
		}

		@Override
		public void debug(Supplier<String> messageProvider) {
		}

	}

}
	