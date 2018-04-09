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

package io.sarl.sre.tests.units.skills;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.sarl.lang.core.Event;
import io.sarl.revision.BehaviorGuardEvaluator;
import io.sarl.revision.BehaviorGuardEvaluatorRegistry;
import io.sarl.sre.services.executor.ExecutorService;
import io.sarl.sre.skills.EventBus;
import io.sarl.sre.tests.testutils.AbstractSreTest;
import io.sarl.tests.api.Nullable;


/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class EventBusTest extends AbstractSreTest {

	@Nullable
	private BehaviorGuardEvaluatorRegistry registry;

	@Nullable
	private EventBus eventBus;

	@Nullable
	private ExecutorService executor;

	@Before
	public void setUp() {
		this.registry = mock(BehaviorGuardEvaluatorRegistry.class);
		this.executor = mock(ExecutorService.class);
		this.eventBus = new EventBus(this.executor, this.registry);
	}

	@Test
	public void hasRegisteredEventListener() {
		when(this.registry.hasRegisteredEventListener(any())).thenReturn(true);
		assertTrue(this.eventBus.hasRegisteredEventListener(Event.class));
	}

	@Test
	public void getRegisteredEventListeners() {
		when(this.registry.hasRegisteredEventListener(any())).thenReturn(true);
		assertTrue(this.eventBus.hasRegisteredEventListener(Event.class));
	}

	@Test
	public void register() {
		Object listener = mock(Object.class);
		Function1<? super Event, ? extends Boolean> filter = mock(Function1.class);
		Procedure1<? super Object> callback = mock(Procedure1.class);
		this.eventBus.register(listener, filter, callback);
		ArgumentCaptor<Object> capturedListener = ArgumentCaptor.forClass(Object.class);
		ArgumentCaptor<Function1<? super Event, ? extends Boolean>> capturedFilter = ArgumentCaptor.forClass(Function1.class);
		ArgumentCaptor<Procedure1<? super Object>> capturedCallback = ArgumentCaptor.forClass(Procedure1.class);
		verify(this.registry).register(capturedListener.capture(), capturedFilter.capture(), capturedCallback.capture());
		assertSame(listener, capturedListener.getValue());
		assertSame(filter, capturedFilter.getValue());
		assertSame(callback, capturedCallback.getValue());
	}

	@Test
	public void unregister() {
		Object listener = mock(Object.class);
		Procedure1<? super Object> callback = mock(Procedure1.class);
		this.eventBus.unregister(listener, callback);
		ArgumentCaptor<Object> capturedListener = ArgumentCaptor.forClass(Object.class);
		ArgumentCaptor<Procedure1<? super Object>> capturedCallback = ArgumentCaptor.forClass(Procedure1.class);
		verify(this.registry).unregister(capturedListener.capture(), capturedCallback.capture());
		assertSame(listener, capturedListener.getValue());
		assertSame(callback, capturedCallback.getValue());
	}

	@Test
	public void unregisterAll() {
		Procedure1<? super Object> callback = mock(Procedure1.class);
		this.eventBus.unregisterAll(callback);
		ArgumentCaptor<Procedure1<? super Object>> capturedCallback = ArgumentCaptor.forClass(Procedure1.class);
		verify(this.registry).unregisterAll(capturedCallback.capture());
		assertSame(callback, capturedCallback.getValue());
	}

	@Test
	public void asyncDispatch() {
		Event event = mock(Event.class);
		Logger logger = mock(Logger.class);
		Runnable runnable = mock(Runnable.class);
		BehaviorGuardEvaluator evaluator = mock(BehaviorGuardEvaluator.class);
		doAnswer((it) -> {
			((Collection<Runnable>) it.getArgument(1)).add(runnable);
			return null;
		}).when(evaluator).evaluateGuard(any(), any());
		when(this.registry.getBehaviorGuardEvaluators(any())).thenReturn(Collections.singleton(evaluator));
		doAnswer((it) -> {
			((Consumer) it.getArgument(2)).accept(evaluator);
			return null;
		}).when(this.executor).applyBlockingConsumer(any(), any(), any());
	
		this.eventBus.asyncDispatch(event, logger);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		verify(this.registry).getBehaviorGuardEvaluators(capturedEvent.capture());
		assertSame(event, capturedEvent.getValue());

		ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
		ArgumentCaptor<Iterable<BehaviorGuardEvaluator>> capturedIterable = ArgumentCaptor.forClass(Iterable.class);
		verify(this.executor).applyBlockingConsumer(capturedLogger.capture(), capturedIterable.capture(), any(Consumer.class));
		assertSame(logger, capturedLogger.getValue());
		assertContains(capturedIterable.getValue(), evaluator);

		capturedLogger = ArgumentCaptor.forClass(Logger.class);
		ArgumentCaptor<Runnable> capturedRunnable = ArgumentCaptor.forClass(Runnable.class);
		verify(this.executor).executeAsap(capturedLogger.capture(), capturedRunnable.capture());
		assertSame(logger, capturedLogger.getValue());
		assertSame(runnable, capturedRunnable.getValue());
	}

	@Test
	public void immediateDispatch() {
		Event event = mock(Event.class);
		Logger logger = mock(Logger.class);
		Runnable runnable = mock(Runnable.class);
		BehaviorGuardEvaluator evaluator = mock(BehaviorGuardEvaluator.class);
		doAnswer((it) -> {
			((Collection<Runnable>) it.getArgument(1)).add(runnable);
			return null;
		}).when(evaluator).evaluateGuard(any(), any());
		when(this.registry.getBehaviorGuardEvaluators(any())).thenReturn(Collections.singleton(evaluator));
		doAnswer((it) -> {
			((Consumer) it.getArgument(2)).accept(evaluator);
			return null;
		}).when(this.executor).applyBlockingConsumer(any(), any(), any());
	
		this.eventBus.immediateDispatch(event, true, logger);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		verify(this.registry).getBehaviorGuardEvaluators(capturedEvent.capture());
		assertSame(event, capturedEvent.getValue());

		ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
		ArgumentCaptor<Iterable<BehaviorGuardEvaluator>> capturedIterable = ArgumentCaptor.forClass(Iterable.class);
		verify(this.executor).applyBlockingConsumer(capturedLogger.capture(), capturedIterable.capture(), any(Consumer.class));
		assertSame(logger, capturedLogger.getValue());
		assertContains(capturedIterable.getValue(), evaluator);

		capturedLogger = ArgumentCaptor.forClass(Logger.class);
		ArgumentCaptor<Boolean> capturedThrow = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Collection<Runnable>> capturedCollection = ArgumentCaptor.forClass(Collection.class);
		verify(this.executor).executeBlockingTasks(capturedLogger.capture(), capturedThrow.capture(), capturedCollection.capture());
		assertSame(logger, capturedLogger.getValue());
		assertTrue(capturedThrow.getValue());
		Collection<Runnable> col = capturedCollection.getValue();
		assertEquals(1, col.size());
		assertSame(runnable, col.iterator().next());
	}

	@Test
	public void immediateDispatchTo() {
		Event event = mock(Event.class);
		Logger logger = mock(Logger.class);
		Runnable runnable = mock(Runnable.class);
		BehaviorGuardEvaluator evaluator = mock(BehaviorGuardEvaluator.class);
		doAnswer((it) -> {
			((Collection<Runnable>) it.getArgument(1)).add(runnable);
			return null;
		}).when(evaluator).evaluateGuard(any(), any());
		when(this.registry.getBehaviorGuardEvaluatorsFor(any(), any())).thenReturn(Collections.singleton(evaluator));
		doAnswer((it) -> {
			((Consumer) it.getArgument(2)).accept(evaluator);
			return null;
		}).when(this.executor).applyBlockingConsumer(any(), any(), any());
		Object listener = mock(Object.class);

		this.eventBus.immediateDispatchTo(listener, event, true, logger);
		
		ArgumentCaptor<Event> capturedEvent = ArgumentCaptor.forClass(Event.class);
		ArgumentCaptor<Object> capturedListener = ArgumentCaptor.forClass(Object.class);
		verify(this.registry).getBehaviorGuardEvaluatorsFor(capturedEvent.capture(), capturedListener.capture());
		assertSame(event, capturedEvent.getValue());

		ArgumentCaptor<Logger> capturedLogger = ArgumentCaptor.forClass(Logger.class);
		ArgumentCaptor<Iterable<BehaviorGuardEvaluator>> capturedIterable = ArgumentCaptor.forClass(Iterable.class);
		verify(this.executor).applyBlockingConsumer(capturedLogger.capture(), capturedIterable.capture(), any(Consumer.class));
		assertSame(logger, capturedLogger.getValue());
		assertContains(capturedIterable.getValue(), evaluator);

		capturedLogger = ArgumentCaptor.forClass(Logger.class);
		ArgumentCaptor<Boolean> capturedThrow = ArgumentCaptor.forClass(Boolean.class);
		ArgumentCaptor<Collection<Runnable>> capturedCollection = ArgumentCaptor.forClass(Collection.class);
		verify(this.executor).executeBlockingTasks(capturedLogger.capture(), capturedThrow.capture(), capturedCollection.capture());
		assertSame(logger, capturedLogger.getValue());
		assertTrue(capturedThrow.getValue());
		Collection<Runnable> col = capturedCollection.getValue();
		assertEquals(1, col.size());
		assertSame(runnable, col.iterator().next());
	}

}
