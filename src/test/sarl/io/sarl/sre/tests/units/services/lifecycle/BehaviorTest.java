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
package io.sarl.sre.tests.units.services.lifecycle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import io.sarl.sre.services.lifecycle.BehaviorLife;
import io.sarl.core.AgentTask;
import io.sarl.lang.core.Behavior;
import io.sarl.sre.tests.testutils.AbstractJanusTest;
import io.sarl.tests.api.Nullable;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class BehaviorTest extends AbstractJanusTest {

	@Nullable
	private Behavior behavior;

	@Nullable
	private BehaviorLife life;

	@Before
	public void setUp() {
		this.behavior = mock(Behavior.class);
		this.life = BehaviorLife.getLife(this.behavior);
	}

	@Test
	public void addTaskReference() {
		AgentTask t1 = mock(AgentTask.class);
		AgentTask t2 = mock(AgentTask.class);
		this.life.addTaskReference(t1);
		this.life.addTaskReference(t2);
		
		Set<WeakReference<AgentTask>> tasks = this.life.removeAllTaskReferences();
		assertNotNull(tasks);
		Iterator<WeakReference<AgentTask>> iterator = tasks.iterator();
		assertTrue(iterator.hasNext());
		if (System.identityHashCode(t1) <= System.identityHashCode(t2)) {
			assertSame(t1, iterator.next().get());
			assertTrue(iterator.hasNext());
			assertSame(t2, iterator.next().get());
		} else {
			assertSame(t2, iterator.next().get());
			assertTrue(iterator.hasNext());
			assertSame(t1, iterator.next().get());
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void removeTaskReference() {
		AgentTask t1 = mock(AgentTask.class);
		AgentTask t2 = mock(AgentTask.class);
		this.life.addTaskReference(t1);
		this.life.addTaskReference(t2);

		this.life.removeTaskReference(t1);
		
		Set<WeakReference<AgentTask>> tasks = this.life.removeAllTaskReferences();
		assertNotNull(tasks);
		Iterator<WeakReference<AgentTask>> iterator = tasks.iterator();
		assertTrue(iterator.hasNext());
		assertSame(t2, iterator.next().get());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void removeAllTaskReferences() {
		assertNull(this.life.removeAllTaskReferences());

		AgentTask t1 = mock(AgentTask.class);
		AgentTask t2 = mock(AgentTask.class);
		this.life.addTaskReference(t1);
		this.life.addTaskReference(t2);

		this.life.removeTaskReference(t1);
		
		Set<WeakReference<AgentTask>> tasks = this.life.removeAllTaskReferences();
		assertNotNull(tasks);
		Iterator<WeakReference<AgentTask>> iterator = tasks.iterator();
		assertTrue(iterator.hasNext());
		assertSame(t2, iterator.next().get());
		assertFalse(iterator.hasNext());

		assertNull(this.life.removeAllTaskReferences());
	}

}
